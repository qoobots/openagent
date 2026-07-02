package com.qoobot.agent.education_agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 知识检索结果 VO
 */
@Data
@Schema(description = "知识检索结果")
public class KnowledgeMatchVO implements Serializable {

    @Schema(description = "知识点 ID")
    private Long id;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "学段")
    private String educationStage;

    @Schema(description = "难度")
    private Integer difficulty;

    @Schema(description = "相似度分数（0~1，越大越相关）")
    private Double score;
}
