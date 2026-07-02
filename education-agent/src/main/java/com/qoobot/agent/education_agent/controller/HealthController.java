package com.qoobot.agent.education_agent.controller;

import com.qoobot.agent.education_agent.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Tag(name = "系统管理", description = "系统健康检查与基础信息")
@RestController
@RequestMapping("/system")
public class HealthController {

    @Value("${agent.name:education-agent}")
    private String agentName;

    @Value("${agent.description:}")
    private String agentDescription;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("agent", agentName);
        info.put("description", agentDescription);
        info.put("timestamp", LocalDateTime.now());
        return Result.ok(info);
    }
}
