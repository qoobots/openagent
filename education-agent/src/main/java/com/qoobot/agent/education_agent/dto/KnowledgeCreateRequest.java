package com.qoobot.agent.education_agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 知识入库请求 DTO
 *
 * <p>支持手工录入和 AI 抽取后入库两种场景。
 */
@Data
@Schema(description = "知识入库请求")
public class KnowledgeCreateRequest implements Serializable {

    @NotBlank(message = "学科不能为空")
    @Schema(description = "学科", example = "math")
    private String subject;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "知识点标题", example = "一元二次方程求根公式")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "知识点内容")
    private String content;

    @NotBlank(message = "学段不能为空")
    @Schema(description = "学段", example = "middle")
    private String educationStage;

    @Schema(description = "课程ID（可选）")
    private Long courseId;

    @Schema(description = "难度（1-5）", example = "3")
    private Integer difficulty;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "来源：manual/ai/imported", example = "manual")
    private String source;
}
