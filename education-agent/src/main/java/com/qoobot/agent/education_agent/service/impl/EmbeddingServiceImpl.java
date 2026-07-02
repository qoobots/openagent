package com.qoobot.agent.education_agent.service.impl;

import com.qoobot.agent.education_agent.common.BusinessException;
import com.qoobot.agent.education_agent.common.ErrorCode;
import com.qoobot.agent.education_agent.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 向量嵌入服务实现
 *
 * <p>基于 Spring AI EmbeddingModel（text-embedding-3-small）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    public List<Double> embed(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        try {
            EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
            EmbeddingResponse response = embeddingModel.call(request);
            if (response.getResults().isEmpty()) {
                throw new BusinessException(ErrorCode.MODEL_CALL_FAILED.getCode(), "嵌入模型返回空结果");
            }
            float[] floats = response.getResults().get(0).getOutput();
            List<Double> result = new ArrayList<>(floats.length);
            for (float f : floats) {
                result.add((double) f);
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("向量嵌入失败: text={}, error={}",
                    text.substring(0, Math.min(50, text.length())), e.getMessage());
            throw new BusinessException(ErrorCode.MODEL_CALL_FAILED.getCode(),
                    "嵌入模型调用失败: " + e.getMessage());
        }
    }

    @Override
    public List<List<Double>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<Double>> results = new ArrayList<>(texts.size());
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }

    @Override
    public int getDimension() {
        return 1536; // text-embedding-3-small
    }
}
