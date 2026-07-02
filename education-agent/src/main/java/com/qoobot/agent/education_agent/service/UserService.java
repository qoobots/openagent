package com.qoobot.agent.education_agent.service;

import com.qoobot.agent.education_agent.dto.LoginRequest;
import com.qoobot.agent.education_agent.dto.RegisterRequest;
import com.qoobot.agent.education_agent.dto.StageSwitchRequest;
import com.qoobot.agent.education_agent.vo.LoginVO;
import com.qoobot.agent.education_agent.vo.UserProfileVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @return 登录响应（含 Token）
     */
    LoginVO register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginVO login(LoginRequest request);

    /**
     * 刷新 Token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 获取用户档案
     */
    UserProfileVO getProfile(Long userId);

    /**
     * 切换学段
     */
    void switchStage(Long userId, StageSwitchRequest request);

    /**
     * 发送短信验证码
     *
     * @return 是否发送成功
     */
    boolean sendSmsCode(String phone, String bizType);

    /**
     * 校验短信验证码
     */
    void verifySmsCode(String phone, String smsCode, String bizType);

    /**
     * 登出（销毁 Token）
     */
    void logout(Long userId);
}
