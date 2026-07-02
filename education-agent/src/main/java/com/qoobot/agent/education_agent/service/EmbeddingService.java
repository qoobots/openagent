package com.qoobot.agent.education_agent.service;

import java.util.List;

/**
 * 向量嵌入服务
 *
 * <p>Phase 1 提供基础嵌入生成能力；Phase 2 对接 pgvector 做语义检索。
 */
public interface EmbeddingService {

    /**
     * 将文本转换为向量
     *
     * @param text 待嵌入文本
     * @return 向量 (1536 维)
     */
    List<Double> embed(String text);

    /**
     * 批量嵌入
     *
     * @param texts 文本列表
     * @return 向量列表
     */
    List<List<Double>> embedBatch(List<String> texts);

    /**
     * 获取向量维度
     */
    int getDimension();
}
