package com.qoobot.agent.education_agent.controller;

import com.qoobot.agent.education_agent.common.Result;
import com.qoobot.agent.education_agent.security.SecurityContextHolder;
import com.qoobot.agent.education_agent.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 多模态交互控制器
 */
@Tag(name = "多模态交互", description = "语音识别/合成、拍照识题等")
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @Operation(summary = "语音转文字 (STT)")
    @PostMapping(value = "/asr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> speechToText(
            @Parameter(description = "音频文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "音频格式 (mp3/wav/webm/ogg)") @RequestParam(defaultValue = "mp3") String format) {
        SecurityContextHolder.requireUserId();
        try {
            byte[] bytes = file.getBytes();
            String ext = getFileExtension(file.getOriginalFilename(), format);
            String text = mediaService.speechToText(bytes, ext);
            return Result.ok(Map.of("text", text));
        } catch (IOException e) {
            return Result.fail(400, "音频文件读取失败");
        }
    }

    @Operation(summary = "文字转语音 (TTS)")
    @PostMapping("/tts")
    public ResponseEntity<byte[]> textToSpeech(
            @Parameter(description = "合成文本") @RequestParam String text,
            @Parameter(description = "音色 (alloy/echo/fable/onyx/nova/shimmer)")
            @RequestParam(defaultValue = "alloy") String voice) {
        SecurityContextHolder.requireUserId();
        byte[] audio = mediaService.textToSpeech(text, voice);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=speech.mp3")
                .body(audio);
    }

    @Operation(summary = "获取可用音色列表")
    @GetMapping("/voices")
    public Result<String[]> voices() {
        SecurityContextHolder.requireUserId();
        return Result.ok(mediaService.getAvailableVoices());
    }

    private String getFileExtension(String filename, String defaultFormat) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return defaultFormat;
    }
}
