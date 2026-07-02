package com.qoobot.agent.education_agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应（含 Token）
 */
@Data
@Builder
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "访问 Token（JWT）")
    private String accessToken;

    @Schema(description = "刷新 Token")
    private String refreshToken;

    @Schema(description = "访问 Token 有效期（秒）")
    private Long expiresIn;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "当前学段")
    private String educationStage;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像 URL")
    private String avatarUrl;
}
