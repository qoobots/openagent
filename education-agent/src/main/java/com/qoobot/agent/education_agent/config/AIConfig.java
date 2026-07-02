package com.qoobot.agent.education_agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 能力引擎配置
 *
 * <p>管理 ChatClient（对话）、EmbeddingModel（向量嵌入）等核心 AI Bean。
 */
@Configuration
public class AIConfig {

    /**
     * 对话客户端（普通模式）
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * ChatClient Builder（注入 ChatModel 后可自定义默认参数）
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
