package com.qoobot.agent.education_agent.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.agent.education_agent.common.Constants;
import com.qoobot.agent.education_agent.common.ErrorCode;
import com.qoobot.agent.education_agent.common.RedisKeys;
import com.qoobot.agent.education_agent.common.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 鉴权过滤器
 *
 * <p>白名单放行，其余接口必须携带有效 Access Token。
 * Token 解析后将 {@link LoginUser} 放入 {@link SecurityContextHolder}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    /** 不需要鉴权的路径 */
    private static final List<String> WHITE_LIST = List.of(
            "/system/health",
            "/system/announcements",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/sms-code",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/**",
            "/favicon.ico",
            "/error"
    );

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        // 去掉 context-path
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        // 白名单放行
        if (isWhitelist(uri)) {
            chain.doFilter(request, response);
            return;
        }

        // 提取 Token
        String authHeader = request.getHeader(Constants.AUTH_HEADER);
        String token = jwtUtil.extractBearer(authHeader);
        if (token == null) {
            writeUnauthorized(response, ErrorCode.UNAUTHORIZED.getCode(), "未携带 Token");
            return;
        }

        // 解析 Token
        LoginUser principal = jwtUtil.parseAccessToken(token);
        if (principal == null) {
            writeUnauthorized(response, ErrorCode.TOKEN_EXPIRED.getCode(), "Token 无效或已过期");
            return;
        }

        // 校验 Redis 中 Token 是否一致（用于主动失效 / 单点登录）
        Object stored = redisTemplate.opsForValue().get(RedisKeys.USER_TOKEN + principal.getUserId());
        if (stored == null || !stored.toString().equals(token)) {
            writeUnauthorized(response, ErrorCode.TOKEN_INVALID.getCode(), "Token 已被吊销");
            return;
        }

        try {
            SecurityContextHolder.set(principal);
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clear();
        }
    }

    private boolean isWhitelist(String uri) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    private void writeUnauthorized(HttpServletResponse response, Integer code, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> result = Result.fail(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
