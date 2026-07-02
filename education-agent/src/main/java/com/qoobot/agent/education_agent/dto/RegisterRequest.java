package com.qoobot.agent.education_agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度需在 4-50 字符之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "用户名必须以字母开头，只能包含字母数字下划线")
    private String username;

    @Schema(description = "密码", example = "P@ssw0rd123")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在 8-32 字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "密码必须包含大小写字母和数字")
    private String password;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "角色", example = "student", allowableValues = {"student", "teacher", "parent", "admin"})
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(student|teacher|parent|admin)$", message = "角色取值不合法")
    private String role;

    @Schema(description = "学段", example = "high",
            allowableValues = {"elementary", "middle", "high", "university", "vocational", "adult"})
    @NotBlank(message = "学段不能为空")
    @Pattern(regexp = "^(elementary|middle|high|university|vocational|adult)$", message = "学段取值不合法")
    private String educationStage;

    @Schema(description = "短信验证码（手机号注册时必填）", example = "123456")
    @Pattern(regexp = "^$|^\\d{6}$", message = "验证码必须为 6 位数字")
    private String smsCode;

    @Schema(description = "昵称（可选）", example = "张三")
    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;
}
