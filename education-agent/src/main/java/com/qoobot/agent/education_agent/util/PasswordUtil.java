package com.qoobot.agent.education_agent.util;

import com.qoobot.agent.education_agent.common.BusinessException;
import com.qoobot.agent.education_agent.common.Constants;
import com.qoobot.agent.education_agent.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类
 *
 * <p>采用 SHA-256 + 随机盐 + 多次迭代的方案。
 * 注意：生产环境建议使用 BCrypt 或 Spring Security Crypto，
 * 当前为轻量级实现，避免引入过重依赖。
 */
@Slf4j
@Component
public class PasswordUtil {

    /** 盐长度（字节） */
    private static final int SALT_LENGTH = 16;

    /** 哈希迭代次数 */
    private static final int ITERATIONS = 1024;

    private final SecureRandom random = new SecureRandom();

    /**
     * 加密密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的字符串，格式：{iterations}${salt}${hash}
     */
    public String encode(String rawPassword) {
        validatePasswordStrength(rawPassword);
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        String hash = hash(rawPassword, salt, ITERATIONS);
        return ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + hash;
    }

    /**
     * 验证密码
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            String expected = parts[2];
            String actual = hash(rawPassword, salt, iterations);
            return constantTimeEquals(expected, actual);
        } catch (Exception e) {
            log.warn("密码校验异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 校验密码强度
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < Constants.PASSWORD_MIN_LENGTH
                || password.length() > Constants.PASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                    "密码长度需在 " + Constants.PASSWORD_MIN_LENGTH + "-" + Constants.PASSWORD_MAX_LENGTH + " 之间");
        }
        if (!password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "密码必须包含大小写字母和数字");
        }
    }

    private String hash(String password, byte[] salt, int iterations) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(salt);
            byte[] result = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < iterations - 1; i++) {
                digest.reset();
                result = digest.digest(result);
            }
            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    /**
     * 常量时间字符串比较，防止计时攻击
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
