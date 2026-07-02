package com.qoobot.agent.education_agent.service.impl;

import com.qoobot.agent.education_agent.common.BusinessException;
import com.qoobot.agent.education_agent.common.ErrorCode;
import com.qoobot.agent.education_agent.dto.KnowledgeCreateRequest;
import com.qoobot.agent.education_agent.dto.KnowledgeSearchRequest;
import com.qoobot.agent.education_agent.entity.Knowledge;
import com.qoobot.agent.education_agent.mapper.KnowledgeMapper;
import com.qoobot.agent.education_agent.service.EmbeddingService;
import com.qoobot.agent.education_agent.service.RAGService;
import com.qoobot.agent.education_agent.util.StageUtil;
import com.qoobot.agent.education_agent.vo.KnowledgeMatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 检索增强服务实现
 *
 * <p>核心流程：
 * <ol>
 *   <li>写入：调 EmbeddingModel 生成 1536 维向量，拼成 PG vector 字面量，原生 SQL 写入</li>
 *   <li>检索：把用户 query 嵌入为向量，拼成 PG vector 字面量，pgvector &lt;=&gt; 余弦距离排序</li>
 *   <li>拼装：把检索结果格式化为 Prompt 上下文片段</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RAGServiceImpl implements RAGService {

    private static final int DEFAULT_TOP_K = 5;
    private static final double DEFAULT_THRESHOLD = 0.6;

    private final KnowledgeMapper knowledgeMapper;
    private final EmbeddingService embeddingService;
    private final StageUtil stageUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createKnowledge(KnowledgeCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "请求不能为空");
        }
        stageUtil.validate(request.getEducationStage());

        // 1. 调 EmbeddingModel 生成向量
        String text = request.getTitle() + "\n" + request.getContent();
        List<Double> vector = embeddingService.embed(text);
        if (vector.isEmpty()) {
            throw new BusinessException(ErrorCode.MODEL_CALL_FAILED.getCode(), "嵌入结果为空");
        }
        float[] embedding = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            embedding[i] = vector.get(i).floatValue();
        }

        // 2. 保存实体（使用 INSERT 走原生 SQL，把 vector 写入）
        Knowledge knowledge = new Knowledge();
        knowledge.setCourseId(request.getCourseId());
        knowledge.setSubject(request.getSubject());
        knowledge.setTitle(request.getTitle());
        knowledge.setContent(request.getContent());
        knowledge.setEducationStage(request.getEducationStage());
        knowledge.setDifficulty(request.getDifficulty() == null ? 3 : request.getDifficulty());
        knowledge.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        knowledge.setStatus(1);
        knowledge.setSource(request.getSource() == null ? "manual" : request.getSource());
        knowledge.setEmbedding(embedding);

        knowledgeMapper.insert(knowledge);

        // 3. 单独更新 vector 字段
        if (knowledge.getId() != null) {
            String vectorLiteral = toPgVectorLiteral(embedding);
            knowledgeMapper.updateVector(knowledge.getId(), vectorLiteral);
        }
        log.info("知识入库成功: id={}, title={}", knowledge.getId(), knowledge.getTitle());
        return knowledge.getId();
    }

    @Override
    public List<KnowledgeMatchVO> search(KnowledgeSearchRequest request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "查询文本不能为空");
        }
        stageUtil.validate(request.getEducationStage());

        // 1. 嵌入查询文本
        List<Double> queryVec = embeddingService.embed(request.getQuery());
        if (queryVec.isEmpty()) {
            return List.of();
        }
        float[] floats = new float[queryVec.size()];
        for (int i = 0; i < queryVec.size(); i++) {
            floats[i] = queryVec.get(i).floatValue();
        }
        String vectorLiteral = toPgVectorLiteral(floats);

        // 2. pgvector 检索
        int topK = request.getTopK() == null || request.getTopK() <= 0 ? DEFAULT_TOP_K : Math.min(request.getTopK(), 20);
        List<KnowledgeMatchVO> matches = knowledgeMapper.searchByVector(
                vectorLiteral,
                request.getEducationStage(),
                request.getSubject(),
                topK);

        // 3. 阈值过滤
        double threshold = request.getThreshold() == null ? DEFAULT_THRESHOLD : request.getThreshold();
        return matches.stream()
                .filter(m -> m.getScore() != null && m.getScore() >= threshold)
                .collect(Collectors.toList());
    }

    @Override
    public String buildContext(List<KnowledgeMatchVO> matches) {
        if (matches == null || matches.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("【参考资料】\n");
        for (int i = 0; i < matches.size(); i++) {
            KnowledgeMatchVO m = matches.get(i);
            sb.append("[").append(i + 1).append("] ");
            if (m.getTitle() != null) {
                sb.append(m.getTitle()).append("\n");
            }
            if (m.getContent() != null) {
                String content = m.getContent();
                if (content.length() > 500) {
                    content = content.substring(0, 500) + "...";
                }
                sb.append(content).append("\n");
            }
            sb.append("\n");
        }
        sb.append("【参考结束】\n请基于以上参考资料，并结合学生问题，给出准确、适合学段的教学回答。");
        return sb.toString();
    }

    /**
     * 把 float[] 转成 PostgreSQL vector 字面量，例如 {@code [0.1,0.2,...]}
     */
    private String toPgVectorLiteral(float[] floats) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < floats.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(floats[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
