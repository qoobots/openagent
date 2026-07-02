package com.qoobot.agent.education_agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 学段切换请求
 */
@Data
@Schema(description = "学段切换请求")
public class StageSwitchRequest {

    @Schema(description = "目标学段",
            allowableValues = {"elementary", "middle", "high", "university", "vocational", "adult"})
    @NotBlank(message = "学段不能为空")
    @Pattern(regexp = "^(elementary|middle|high|university|vocational|adult)$", message = "学段取值不合法")
    private String educationStage;
}
