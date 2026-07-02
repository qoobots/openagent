package com.qoobot.agent.education_agent.service.impl;

import com.qoobot.agent.education_agent.common.BusinessException;
import com.qoobot.agent.education_agent.common.ErrorCode;
import com.qoobot.agent.education_agent.common.RedisKeys;
import com.qoobot.agent.education_agent.dto.KnowledgeSearchRequest;
import com.qoobot.agent.education_agent.service.ContentSafetyService;
import com.qoobot.agent.education_agent.service.RAGService;
import com.qoobot.agent.education_agent.service.TutorService;
import com.qoobot.agent.education_agent.util.RedisUtil;
import com.qoobot.agent.education_agent.util.StageUtil;
import com.qoobot.agent.education_agent.vo.KnowledgeMatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 智能辅导服务实现（LLM 版本）
 *
 * <p>Phase 1：接入 Spring AI OpenAI ChatClient，支持多轮对话 + SSE 流式输出。
 * 通过 Prompt Engineering 实现学段自适应的教育辅导。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    /** 单个会话最大消息数（用于截断历史） */
    private static final int MAX_HISTORY_MESSAGES = 10;

    /** 上下文有效期：6 小时 */
    private static final long CONTEXT_EXPIRE_HOURS = 6L;

    private final ChatClient chatClient;
    private final ContentSafetyService contentSafetyService;
    private final StageUtil stageUtil;
    private final RedisUtil redisUtil;
    private final RAGService ragService;

    @Override
    public String chat(Long userId, String stage, String sessionId, String message) {
        // 1. 参数校验
        if (message == null || message.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "消息不能为空");
        }
        // 2. 入参内容安全
        String violation = contentSafetyService.checkAndExplain(message);
        if (violation != null) {
            log.warn("用户 {} 输入触发内容安全: {}", userId, violation);
            throw new BusinessException(ErrorCode.CONTENT_SAFETY_REJECTED.getCode(), violation);
        }
        // 3. 学段合法性
        stageUtil.validate(stage);
        // 4. 会话 ID
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }
        // 5. 构建 Prompt
        String systemPrompt = buildSystemPrompt(stage);
        String ragContext = buildRagContext(stage, message);
        if (ragContext != null && !ragContext.isEmpty()) {
            systemPrompt = systemPrompt + "\n\n" + ragContext;
        }
        List<Message> historyMessages = loadHistory(sessionId);
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage(systemPrompt));
        promptMessages.addAll(historyMessages);
        promptMessages.add(new UserMessage(message));

        // 6. 调用 LLM
        String reply;
        try {
            reply = chatClient.prompt(new Prompt(promptMessages))
                    .call()
                    .content();
            if (reply == null || reply.isBlank()) {
                reply = "抱歉，我暂时无法回答这个问题，请稍后再试。";
            }
        } catch (Exception e) {
            log.error("LLM 调用失败: userId={}, error={}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.MODEL_CALL_FAILED.getCode(), "AI 模型调用失败，请稍后重试");
        }

        // 7. 出参内容安全
        String replyViolation = contentSafetyService.checkAndExplain(reply);
        if (replyViolation != null) {
            log.error("AI 回复触发内容安全: {}", replyViolation);
            reply = "抱歉，我暂时无法回答这个问题，请换个话题试试。";
        }

        // 8. 持久化上下文
        saveHistory(sessionId, message, reply);
        return reply;
    }

    /**
     * 流式对话（SSE）
     *
     * @return Flux 流式输出字符串
     */
    public Flux<String> chatStream(Long userId, String stage, String sessionId, String message) {
        if (message == null || message.isBlank()) {
            return Flux.error(new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "消息不能为空"));
        }
        String violation = contentSafetyService.checkAndExplain(message);
        if (violation != null) {
            return Flux.error(new BusinessException(ErrorCode.CONTENT_SAFETY_REJECTED.getCode(), violation));
        }
        stageUtil.validate(stage);
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }
        final String sid = sessionId;

        String systemPrompt = buildSystemPrompt(stage);
        String ragContext = buildRagContext(stage, message);
        if (ragContext != null && !ragContext.isEmpty()) {
            systemPrompt = systemPrompt + "\n\n" + ragContext;
        }
        List<Message> historyMessages = loadHistory(sessionId);
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage(systemPrompt));
        promptMessages.addAll(historyMessages);
        promptMessages.add(new UserMessage(message));

        return chatClient.prompt(new Prompt(promptMessages))
                .stream()
                .content()
                .doOnNext(chunk -> {
                    // 每个 chunk 也可以做内容安全检查（开销较大，建议按需开启）
                })
                .doOnComplete(() -> {
                    // 流式结束后收集完整结果写入上下文
                    // 注：Flux 完成后无法直接获取全部结果，此处依赖前端拼接后保存
                    // 简化处理：仅保存用户消息
                    saveHistory(sid, message, "[流式回复]");
                })
                .onErrorResume(e -> {
                    log.error("流式对话异常: userId={}, error={}", userId, e.getMessage());
                    return Flux.just("[AI 服务暂时不可用，请稍后重试]");
                });
    }

    /**
     * 根据学段和角色构建系统 Prompt
     */
    private String buildSystemPrompt(String stage) {
        String stageName = stageUtil.displayName(stage);
        return String.format("""
                你是一位专业、耐心的%s教育辅导老师。请遵循以下规则：
                
                1. 根据学生当前学段（%s）调整回答的深度和语言风格：
                   - 小学：用简单生动的语言，多举例子，保持耐心和鼓励
                   - 初中：逻辑清晰，适当引入公式和推导，注重基础知识巩固
                   - 高中：严谨推导，强调解题思路和考试技巧，帮助冲刺提分
                   - 大学：专业深入，关注理论与实践结合
                   - 职业培训：实用导向，聚焦岗位技能和证书考点
                   - 成人：高效浓缩，注重即学即用
                   
                2. 回答规范：
                   - 先确认理解学生的问题
                   - 用苏格拉底式提问引导思考，而不是直接给答案
                   - 解题时展示完整的推理过程
                   - 最后给出总结和延伸学习建议
                   - 长度控制在 300-800 字
                   
                3. 安全要求：
                   - 不回答与学习无关的敏感话题
                   - 如学生出现心理危机倾向，建议联系家长/老师/心理热线
                   - 鼓励积极健康的学习态度
                """, stageName, stageName);
    }

    /**
     * 从 Redis 加载历史消息（最近 10 条）
     */
    @SuppressWarnings("unchecked")
    private List<Message> loadHistory(String sessionId) {
        String ctxKey = RedisKeys.CHAT_CONTEXT + sessionId + ":msg";
        Object stored = redisUtil.get(ctxKey);
        if (stored instanceof List<?> list) {
            List<Message> messages = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s) {
                    if (s.startsWith("U:")) {
                        messages.add(new UserMessage(s.substring(2)));
                    } else if (s.startsWith("A:")) {
                        messages.add(new org.springframework.ai.chat.messages.AssistantMessage(s.substring(2)));
                    }
                }
            }
            return messages;
        }
        return List.of();
    }

    /**
     * 保存对话消息到 Redis
     */
    private void saveHistory(String sessionId, String userMessage, String aiReply) {
        String ctxKey = RedisKeys.CHAT_CONTEXT + sessionId + ":msg";
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) redisUtil.get(ctxKey);
        if (history == null) {
            history = new ArrayList<>();
        }
        history.add("U:" + truncate(userMessage, 2000));
        history.add("A:" + truncate(aiReply, 2000));
        // 只保留最近 N*2 条消息
        while (history.size() > MAX_HISTORY_MESSAGES * 2) {
            history.remove(0);
            history.remove(1);
        }
        redisUtil.set(ctxKey, new ArrayList<>(history), CONTEXT_EXPIRE_HOURS, java.util.concurrent.TimeUnit.HOURS);
    }

    private String truncate(String s, int maxLen) {
        return s == null ? "" : s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    /**
     * 调用 RAG 服务检索相关知识并拼装为 Prompt 片段。
     * <p>检索失败/无结果时静默降级，不影响主链路。
     */
    private String buildRagContext(String stage, String query) {
        try {
            KnowledgeSearchRequest request = new KnowledgeSearchRequest();
            request.setQuery(query);
            request.setEducationStage(stage);
            request.setTopK(3);
            request.setThreshold(0.65);
            List<KnowledgeMatchVO> matches = ragService.search(request);
            if (matches.isEmpty()) {
                return null;
            }
            return ragService.buildContext(matches);
        } catch (Exception e) {
            log.warn("RAG 检索失败，已降级: {}", e.getMessage());
            return null;
        }
    }
}
