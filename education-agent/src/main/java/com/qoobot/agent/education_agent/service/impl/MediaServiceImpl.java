package com.qoobot.agent.education_agent.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.agent.education_agent.common.BusinessException;
import com.qoobot.agent.education_agent.common.ErrorCode;
import com.qoobot.agent.education_agent.service.ContentSafetyService;
import com.qoobot.agent.education_agent.service.MediaService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * 多媒体服务实现
 *
 * <p>直接调用 OpenAI Audio API（Whisper-1 STT + TTS-1）。
 * Phase 2 后可切换为 Spring AI 原生 audio starter（当其 API 稳定后）。
 */
@Slf4j
@Service
public class MediaServiceImpl implements MediaService {

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ContentSafetyService contentSafetyService;

    public MediaServiceImpl(ContentSafetyService contentSafetyService) {
        this.contentSafetyService = contentSafetyService;
    }

    @Override
    public String speechToText(byte[] audioBytes, String format) {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "音频数据为空");
        }
        try {
            String url = baseUrl + "/v1/audio/transcriptions";

            ByteArrayResource resource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return "audio." + (format != null ? format : "mp3");
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("model", "whisper-1");
            body.add("response_format", "json");
            body.add("language", "zh");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(apiKey);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

            JsonNode json = objectMapper.readTree(responseEntity.getBody());
            String result = json.has("text") ? json.get("text").asText() : "";

            // 内容安全
            if (!result.isBlank()) {
                String violation = contentSafetyService.checkAndExplain(result);
                if (violation != null) {
                    log.warn("语音识别结果触发内容安全: {}", violation);
                    throw new BusinessException(ErrorCode.CONTENT_SAFETY_REJECTED.getCode(), violation);
                }
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("语音识别失败: format={}, error={}", format, e.getMessage());
            throw new BusinessException(ErrorCode.MODEL_CALL_FAILED.getCode(), "语音识别失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] textToSpeech(String text, String voice) {
        if (text == null || text.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "文本为空");
        }
        String violation = contentSafetyService.checkAndExplain(text);
        if (violation != null) {
            throw new BusinessException(ErrorCode.CONTENT_SAFETY_REJECTED.getCode(), violation);
        }
        try {
            String url = baseUrl + "/v1/audio/speech";
            String voiceName = (voice != null && !voice.isBlank()) ? voice : "alloy";

            Map<String, Object> requestBody = Map.of(
                    "model", "tts-1",
                    "input", text,
                    "voice", voiceName,
                    "response_format", "mp3"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(url, requestEntity, byte[].class);

            return responseEntity.getBody();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("语音合成失败: text={}, error={}", text.substring(0, Math.min(50, text.length())), e.getMessage());
            throw new BusinessException(ErrorCode.MODEL_CALL_FAILED.getCode(), "语音合成失败: " + e.getMessage());
        }
    }

    @Override
    public String[] getAvailableVoices() {
        return new String[]{"alloy", "echo", "fable", "onyx", "nova", "shimmer"};
    }
}
