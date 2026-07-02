package com.qoobot.agent.education_agent.controller;

import com.qoobot.agent.education_agent.common.Result;
import com.qoobot.agent.education_agent.dto.ChatRequest;
import com.qoobot.agent.education_agent.security.SecurityContextHolder;
import com.qoobot.agent.education_agent.service.TutorService;
import com.qoobot.agent.education_agent.vo.ChatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 智能辅导控制器
 */
@Tag(name = "智能辅导", description = "智能对话辅导（LLM 驱动，支持流式）")
@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    @Operation(summary = "普通对话")
    @PostMapping("/chat")
    public Result<ChatVO> chat(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityContextHolder.requireUserId();
        String stage = SecurityContextHolder.get().getEducationStage();
        String reply = tutorService.chat(userId, stage, request.getSessionId(), request.getMessage());
        return Result.ok(new ChatVO(request.getSessionId(), reply, stage));
    }

    @Operation(summary = "流式对话（SSE）")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityContextHolder.requireUserId();
        String stage = SecurityContextHolder.get().getEducationStage();
        return tutorService.chatStream(userId, stage, request.getSessionId(), request.getMessage());
    }
}
