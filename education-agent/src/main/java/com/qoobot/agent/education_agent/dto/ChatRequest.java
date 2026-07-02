package com.qoobot.agent.education_agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 智能对话请求
 */
@Data
@Schema(description = "智能对话请求")
public class ChatRequest {

    @Schema(description = "会话 ID（首次为空时自动生成）", example = "a1b2c3d4e5f6")
    private String sessionId;

    @Schema(description = "用户消息", example = "你能帮我解释一下勾股定理吗？")
    @NotBlank(message = "消息不能为空")
    @Size(max = 2000, message = "消息长度不能超过 2000")
    private String message;
}
