package com.qoobot.agent.education_agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 知识检索请求 DTO
 */
@Data
@Schema(description = "知识检索请求")
public class KnowledgeSearchRequest implements Serializable {

    @NotBlank(message = "查询文本不能为空")
    @Schema(description = "查询文本（自然语言）", example = "如何解一元二次方程？")
    private String query;

    @NotBlank(message = "学段不能为空")
    @Schema(description = "学段过滤", example = "middle")
    private String educationStage;

    @Schema(description = "学科过滤（可选）", example = "math")
    private String subject;

    @Schema(description = "返回 topK 数量", example = "5")
    private Integer topK;

    @Schema(description = "相似度阈值（0~1），低于此分数的返回结果会被过滤", example = "0.6")
    private Double threshold;
}
