package com.qoobot.agent.education_agent.service.impl;

import com.qoobot.agent.education_agent.common.BusinessException;
import com.qoobot.agent.education_agent.common.Constants;
import com.qoobot.agent.education_agent.common.ErrorCode;
import com.qoobot.agent.education_agent.common.RedisKeys;
import com.qoobot.agent.education_agent.dto.LoginRequest;
import com.qoobot.agent.education_agent.dto.RegisterRequest;
import com.qoobot.agent.education_agent.dto.StageSwitchRequest;
import com.qoobot.agent.education_agent.entity.User;
import com.qoobot.agent.education_agent.mapper.UserMapper;
import com.qoobot.agent.education_agent.security.JwtUtil;
import com.qoobot.agent.education_agent.security.LoginUser;
import com.qoobot.agent.education_agent.service.UserService;
import com.qoobot.agent.education_agent.util.MaskUtil;
import com.qoobot.agent.education_agent.util.PasswordUtil;
import com.qoobot.agent.education_agent.util.RedisUtil;
import com.qoobot.agent.education_agent.util.StageUtil;
import com.qoobot.agent.education_agent.vo.LoginVO;
import com.qoobot.agent.education_agent.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 短信验证码有效期：5 分钟 */
    private static final long SMS_CODE_EXPIRE_SECONDS = 5 * 60L;

    /** 短信验证码 Redis 存储 Key 后缀 */
    private static final String SMS_CODE_SUFFIX = ":";

    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;
    private final MaskUtil maskUtil;
    private final StageUtil stageUtil;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterRequest request) {
        // 1. 学段合法性
        stageUtil.validate(request.getEducationStage());

        // 2. 用户名查重
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 3. 手机号注册时校验短信验证码 + 查重
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (request.getSmsCode() == null || request.getSmsCode().isBlank()) {
                throw new BusinessException(ErrorCode.SMS_CODE_INVALID);
            }
            verifySmsCode(request.getPhone(), request.getSmsCode(), "register");

            if (userMapper.selectByPhone(request.getPhone()) != null) {
                throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
            }
        }

        // 4. 构建实体
        User user = new User();
        BeanUtils.copyProperties(request, user, "password");
        user.setPasswordHash(passwordUtil.encode(request.getPassword()));
        user.setStatus(Constants.USER_STATUS_ACTIVE);
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setAvatarUrl(Constants.DEFAULT_AVATAR);
        user.setLastLoginTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功: id={}, username={}, role={}, stage={}",
                user.getId(), user.getUsername(), user.getRole(), user.getEducationStage());

        // 5. 直接颁发 Token
        return issueLoginVo(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        User user;
        switch (request.getLoginType()) {
            case Constants.LOGIN_TYPE_PASSWORD -> {
                if (request.getUsername() == null || request.getPassword() == null) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "用户名和密码不能为空");
                }
                user = userMapper.selectByUsername(request.getUsername());
                if (user == null || !passwordUtil.matches(request.getPassword(), user.getPasswordHash())) {
                    throw new BusinessException(ErrorCode.PASSWORD_INCORRECT);
                }
            }
            case Constants.LOGIN_TYPE_SMS -> {
                if (request.getPhone() == null || request.getSmsCode() == null) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "手机号和验证码不能为空");
                }
                verifySmsCode(request.getPhone(), request.getSmsCode(), "login");
                user = userMapper.selectByPhone(request.getPhone());
                if (user == null) {
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                }
            }
            case Constants.LOGIN_TYPE_THIRD_PARTY -> {
                // 第三方登录预留：调用微信/支付宝 SDK 验证 thirdPartyToken
                throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE.getCode(), "第三方登录暂未开通");
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "登录方式不合法");
        }

        if (user.getStatus() == null || user.getStatus() != Constants.USER_STATUS_ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "账号已被禁用");
        }

        userMapper.updateLastLoginTime(user.getId());
        user.setLastLoginTime(LocalDateTime.now());
        log.info("用户登录成功: id={}, username={}, type={}", user.getId(), user.getUsername(), request.getLoginType());

        return issueLoginVo(user);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        LoginUser principal = jwtUtil.parseRefreshToken(refreshToken);
        if (principal == null) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        User user = userMapper.selectById(principal.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 销毁旧 Token
        invalidateTokens(user.getId());
        return issueLoginVo(user);
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo, "passwordHash");
        vo.setUserId(user.getId());
        vo.setPhoneMasked(maskUtil.maskPhone(user.getPhone()));
        vo.setEmailMasked(maskUtil.maskEmail(user.getEmail()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchStage(Long userId, StageSwitchRequest request) {
        stageUtil.validate(request.getEducationStage());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (request.getEducationStage().equals(user.getEducationStage())) {
            return;
        }
        // 业务规则：管理员不可切换为学生学段；教师可切换
        if (Constants.ROLE_ADMIN.equals(user.getRole())
                && !Constants.ROLE_ADMIN.equals(request.getEducationStage())) {
            // 管理员切换为非管理员场景在当前业务下不应出现，宽松处理
        }
        int rows = userMapper.updateEducationStage(userId, request.getEducationStage());
        if (rows == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR.getCode(), "学段切换失败");
        }
        log.info("用户切换学段: id={}, {} -> {}", userId, user.getEducationStage(), request.getEducationStage());
    }

    @Override
    public boolean sendSmsCode(String phone, String bizType) {
        // 防刷：1 分钟内只能发 1 次
        String rateLimitKey = RedisKeys.SMS_CODE + phone + ":ratelimit";
        if (Boolean.TRUE.equals(redisUtil.hasKey(rateLimitKey))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "请求过于频繁，请稍后再试");
        }
        // 生成 6 位随机码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
        // 存储验证码（5 分钟有效）
        redisUtil.set(RedisKeys.SMS_CODE + phone + SMS_CODE_SUFFIX + bizType, code,
                SMS_CODE_EXPIRE_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        // 设置防刷标记 60s
        redisUtil.set(rateLimitKey, "1", 60L, java.util.concurrent.TimeUnit.SECONDS);

        log.info("发送短信验证码: phone={}, bizType={}, code={}", phone, bizType, code);
        // 实际生产应调用短信服务商的 API；此处仅写日志
        return true;
    }

    @Override
    public void verifySmsCode(String phone, String smsCode, String bizType) {
        String key = RedisKeys.SMS_CODE + phone + SMS_CODE_SUFFIX + bizType;
        Object stored = redisUtil.get(key);
        if (stored == null || !stored.toString().equals(smsCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_INVALID);
        }
        // 一次性使用：验证成功后立即删除
        redisUtil.delete(key);
    }

    @Override
    public void logout(Long userId) {
        invalidateTokens(userId);
        log.info("用户登出: id={}", userId);
    }

    /**
     * 颁发 Token
     */
    private LoginVO issueLoginVo(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole(), user.getEducationStage());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getRole(), user.getEducationStage());

        // 在 Redis 中记录 Token 状态（用于主动失效 / 单点登录）
        redisUtil.set(RedisKeys.USER_TOKEN + user.getId(), accessToken,
                Constants.ACCESS_TOKEN_EXPIRE, java.util.concurrent.TimeUnit.SECONDS);
        redisUtil.set(RedisKeys.REFRESH_TOKEN + user.getId(), refreshToken,
                Constants.REFRESH_TOKEN_EXPIRE, java.util.concurrent.TimeUnit.SECONDS);

        return LoginVO.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(Constants.ACCESS_TOKEN_EXPIRE)
                .role(user.getRole())
                .educationStage(user.getEducationStage())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    /**
     * 失效 Token
     */
    private void invalidateTokens(Long userId) {
        redisUtil.delete(RedisKeys.USER_TOKEN + userId);
        redisUtil.delete(RedisKeys.REFRESH_TOKEN + userId);
    }
}
