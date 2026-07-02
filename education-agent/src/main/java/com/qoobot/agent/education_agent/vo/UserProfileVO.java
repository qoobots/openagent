package com.qoobot.agent.education_agent.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户档案视图
 */
@Data
@Schema(description = "用户档案")
public class UserProfileVO {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像 URL")
    private String avatarUrl;

    @Schema(description = "手机号（脱敏）")
    private String phoneMasked;

    @Schema(description = "邮箱（脱敏）")
    private String emailMasked;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "当前学段")
    private String educationStage;

    @Schema(description = "学习风格")
    private String learningStyle;

    @Schema(description = "账号状态")
    private Integer status;

    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @Schema(description = "注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
