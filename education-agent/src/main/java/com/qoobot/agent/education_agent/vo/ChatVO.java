package com.qoobot.agent.education_agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 智能对话响应
 */
@Data
@AllArgsConstructor
@Schema(description = "智能对话响应")
public class ChatVO {

    @Schema(description = "会话 ID")
    private String sessionId;

    @Schema(description = "AI 回复内容")
    private String reply;

    @Schema(description = "当前学段")
    private String educationStage;
}
