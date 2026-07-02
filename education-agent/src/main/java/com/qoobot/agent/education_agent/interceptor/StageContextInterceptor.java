package com.qoobot.agent.education_agent.interceptor;

import com.qoobot.agent.education_agent.common.Constants;
import com.qoobot.agent.education_agent.security.SecurityContextHolder;
import com.qoobot.agent.education_agent.util.StageUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 学段上下文拦截器
 *
 * <p>从请求头 X-Education-Stage 或 SecurityContextHolder 提取当前学段，
 * 用于业务层做学段路由、内容差异化处理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StageContextInterceptor implements HandlerInterceptor {

    private final StageUtil stageUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 优先级：请求头 > 安全上下文
        String headerStage = request.getHeader("X-Education-Stage");
        if (headerStage != null && stageUtil.isValid(headerStage)) {
            request.setAttribute(Constants.CTX_USER_STAGE, headerStage);
            return true;
        }
        if (SecurityContextHolder.get() != null && SecurityContextHolder.get().getEducationStage() != null) {
            request.setAttribute(Constants.CTX_USER_STAGE, SecurityContextHolder.get().getEducationStage());
        }
        return true;
    }
}
