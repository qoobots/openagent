package com.qoobot.agent.education_agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识点实体（pgvector）
 *
 * <p>用于 RAG 检索增强：存储结构化知识条目与 1536 维向量。
 *
 * @author OpenAgent Education
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_knowledge")
public class Knowledge extends BaseEntity {

    /** 所属课程 ID */
    @TableField("course_id")
    private Long courseId;

    /** 学科（math/chinese/english/...） */
    @TableField("subject")
    private String subject;

    /** 知识点标题 */
    @TableField("title")
    private String title;

    /** 知识点内容（可包含公式/段落） */
    @TableField("content")
    private String content;

    /**
     * 内容向量（pgvector VECTOR(1536)）。
     * <p>注：MyBatis-Plus 默认不识别 vector 类型，运行时由 {@code KnowledgeMapper} 单独处理。
     */
    @TableField(exist = false)
    private float[] embedding;

    /** 所属学段（elementary/middle/high/university/vocational/adult） */
    @TableField("education_stage")
    private String educationStage;

    /** 难度等级（1-5） */
    @TableField("difficulty")
    private Integer difficulty;

    /** 排序序号 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态：0=下架, 1=上架 */
    @TableField("status")
    private Integer status;

    /** 来源（manual/ai/imported） */
    @TableField("source")
    private String source;
}
