package com.qoobot.agent.education_agent.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * <p>对应表 edu_user，覆盖学生/教师/家长/管理员 4 类角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_user")
public class User extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @TableField("username")
    private String username;

    /** 手机号（加密存储） */
    @TableField("phone")
    private String phone;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 密码哈希（BCrypt） */
    @TableField("password_hash")
    private String passwordHash;

    /** 角色：student/teacher/parent/admin */
    @TableField("role")
    private String role;

    /** 当前学段：elementary/middle/high/university/vocational/adult */
    @TableField("education_stage")
    private String educationStage;

    /** 昵称 */
    @TableField("nickname")
    private String nickname;

    /** 头像 URL */
    @TableField("avatar_url")
    private String avatarUrl;

    /** 学习风格：visual/auditory/kinesthetic */
    @TableField("learning_style")
    private String learningStyle;

    /** 状态：0=禁用, 1=正常 */
    @TableField("status")
    private Integer status;

    /** 最后登录时间 */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
}
