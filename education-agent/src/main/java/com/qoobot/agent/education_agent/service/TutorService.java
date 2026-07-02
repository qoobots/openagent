package com.qoobot.agent.education_agent.service;

import reactor.core.publisher.Flux;

/**
 * 智能辅导服务
 */
public interface TutorService {

    /**
     * 多轮对话（同步）
     *
     * @param userId    用户 ID
     * @param stage     当前学段
     * @param sessionId 会话 ID（首次为空时由内部生成）
     * @param message   用户消息
     * @return AI 回复
     */
    String chat(Long userId, String stage, String sessionId, String message);

    /**
     * 流式对话（SSE）
     *
     * @return Flux 流式输出字符串
     */
    Flux<String> chatStream(Long userId, String stage, String sessionId, String message);
}
