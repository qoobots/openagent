package com.qoobot.agent.education_agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户登录请求
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {

    @Schema(description = "登录方式", example = "password",
            allowableValues = {"password", "sms", "third_party"})
    @NotBlank(message = "登录方式不能为空")
    @Pattern(regexp = "^(password|sms|third_party)$", message = "登录方式不合法")
    private String loginType;

    @Schema(description = "用户名（password 方式必填）", example = "zhangsan")
    private String username;

    @Schema(description = "密码（password 方式必填）", example = "P@ssw0rd123")
    private String password;

    @Schema(description = "手机号（sms 方式必填）", example = "13800138000")
    private String phone;

    @Schema(description = "短信验证码（sms 方式必填）", example = "123456")
    private String smsCode;

    @Schema(description = "第三方类型（third_party 方式必填）",
            allowableValues = {"wechat", "alipay"})
    private String thirdPartyType;

    @Schema(description = "第三方授权 Token")
    private String thirdPartyToken;
}
