package com.qoobot.agent.education_agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.agent.education_agent.entity.Knowledge;
import com.qoobot.agent.education_agent.vo.KnowledgeMatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识点 Mapper（RAG 检索）
 *
 * <p>支持基于 pgvector 的余弦相似度检索 + MyBatis-Plus 通用 CRUD。
 */
@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {

    /**
     * 基于向量相似度的 TopK 检索（pgvector cosine）
     *
     * @param embeddingString 1536 维向量的 PostgreSQL 字面量，例如 {@code [0.1,0.2,...]}
     * @param stage           学段过滤
     * @param subject         学科过滤（可空）
     * @param topK            返回前 N 条
     * @return 匹配结果（包含余弦相似度）
     */
    List<KnowledgeMatchVO> searchByVector(@Param("embedding") String embeddingString,
                                          @Param("stage") String stage,
                                          @Param("subject") String subject,
                                          @Param("topK") int topK);

    /**
     * 写入知识向量（pgvector 原生 SQL）
     *
     * @param id              知识 ID
     * @param embeddingString PostgreSQL vector 字面量
     * @return 影响行数
     */
    int updateVector(@Param("id") Long id, @Param("embedding") String embeddingString);
}
