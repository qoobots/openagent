package com.qoobot.agent.education_agent.service;

import com.qoobot.agent.education_agent.dto.KnowledgeCreateRequest;
import com.qoobot.agent.education_agent.dto.KnowledgeSearchRequest;
import com.qoobot.agent.education_agent.vo.KnowledgeMatchVO;

import java.util.List;

/**
 * RAG 检索增强服务接口
 *
 * <p>提供知识入库、相似度检索、上下文拼装等能力。
 */
public interface RAGService {

    /**
     * 写入知识（自动生成向量）
     *
     * @param request 入库请求
     * @return 新增知识 ID
     */
    Long createKnowledge(KnowledgeCreateRequest request);

    /**
     * 基于向量相似度检索
     *
     * @param request 检索请求
     * @return 检索结果（已按分数倒序）
     */
    List<KnowledgeMatchVO> search(KnowledgeSearchRequest request);

    /**
     * 拼装 RAG 上下文（用于对话）
     *
     * @param matches 检索结果
     * @return 拼装好的 Prompt 片段
     */
    String buildContext(List<KnowledgeMatchVO> matches);
}
