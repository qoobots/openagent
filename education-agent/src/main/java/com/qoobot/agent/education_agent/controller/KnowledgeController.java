package com.qoobot.agent.education_agent.controller;

import com.qoobot.agent.education_agent.common.Result;
import com.qoobot.agent.education_agent.dto.KnowledgeCreateRequest;
import com.qoobot.agent.education_agent.dto.KnowledgeSearchRequest;
import com.qoobot.agent.education_agent.service.RAGService;
import com.qoobot.agent.education_agent.vo.KnowledgeMatchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识点 / RAG 管理 Controller
 *
 * <p>提供知识入库、相似度检索能力。
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识库管理", description = "RAG 检索增强 - 知识入库与相似度查询")
public class KnowledgeController {

    private final RAGService ragService;

    @PostMapping("/create")
    @Operation(summary = "入库知识（自动生成向量）")
    public Result<Long> create(@Valid @RequestBody KnowledgeCreateRequest request) {
        Long id = ragService.createKnowledge(request);
        return Result.ok(id);
    }

    @PostMapping("/search")
    @Operation(summary = "基于向量相似度检索")
    public Result<List<KnowledgeMatchVO>> search(@Valid @RequestBody KnowledgeSearchRequest request) {
        List<KnowledgeMatchVO> matches = ragService.search(request);
        return Result.ok(matches);
    }

    @PostMapping("/explain")
    @Operation(summary = "RAG 讲解：检索相关知识并拼装为 Prompt 上下文")
    public Result<Map<String, Object>> explain(@Valid @RequestBody KnowledgeSearchRequest request) {
        List<KnowledgeMatchVO> matches = ragService.search(request);
        String context = ragService.buildContext(matches);
        return Result.ok(Map.of(
                "matchCount", matches.size(),
                "context", context,
                "matches", matches
        ));
    }
}
