package com.qoobot.agent.education_agent.security;

import com.qoobot.agent.education_agent.common.BusinessException;
import com.qoobot.agent.education_agent.common.ErrorCode;

/**
 * 安全上下文（基于 ThreadLocal）
 *
 * <p>存放当前请求的登录用户信息，由 {@link AuthFilter} 在请求开始时设置，
 * 在请求结束时清理（{@link org.springframework.web.filter.OncePerRequestFilter#afterCompletion}）。
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    private SecurityContextHolder() {
    }

    public static void set(LoginUser user) {
        CONTEXT.set(user);
    }

    public static LoginUser get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Long requireUserId() {
        LoginUser user = get();
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user.getUserId();
    }

    public static String requireRole() {
        LoginUser user = get();
        if (user == null || user.getRole() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user.getRole();
    }
}
