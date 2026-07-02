package com.qoobot.agent.education_agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送短信验证码请求
 */
@Data
@Schema(description = "发送短信验证码请求")
public class SmsCodeRequest {

    @Schema(description = "手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "业务类型", example = "register",
            allowableValues = {"register", "login", "reset_password"})
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
}
