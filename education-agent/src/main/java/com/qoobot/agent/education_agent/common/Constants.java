package com.qoobot.agent.education_agent.common;

/**
 * 业务常量
 */
public final class Constants {

    private Constants() {
    }

    /** 系统默认用户头像 */
    public static final String DEFAULT_AVATAR = "https://cdn.openagent.example.com/avatar/default.png";

    /** Token 续期阈值（秒），剩余有效期小于此值时刷新 */
    public static final long TOKEN_RENEW_THRESHOLD = 300L;

    /** Access Token 有效期（秒）：2 小时 */
    public static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60L;

    /** Refresh Token 有效期（秒）：7 天 */
    public static final long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60L;

    /** 密码最小长度 */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /** 密码最大长度 */
    public static final int PASSWORD_MAX_LENGTH = 32;

    /** 学段枚举 */
    public static final String STAGE_ELEMENTARY = "elementary";
    public static final String STAGE_MIDDLE = "middle";
    public static final String STAGE_HIGH = "high";
    public static final String STAGE_UNIVERSITY = "university";
    public static final String STAGE_VOCATIONAL = "vocational";
    public static final String STAGE_ADULT = "adult";

    /** 角色枚举 */
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_PARENT = "parent";
    public static final String ROLE_ADMIN = "admin";

    /** 用户状态 */
    public static final int USER_STATUS_DISABLED = 0;
    public static final int USER_STATUS_ACTIVE = 1;

    /** 登录方式 */
    public static final String LOGIN_TYPE_PASSWORD = "password";
    public static final String LOGIN_TYPE_SMS = "sms";
    public static final String LOGIN_TYPE_THIRD_PARTY = "third_party";

    /** JWT Header */
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_PREFIX = "Bearer ";

    /** ThreadLocal 用户上下文 Key */
    public static final String CTX_USER_ID = "currentUserId";
    public static final String CTX_USER_ROLE = "currentUserRole";
    public static final String CTX_USER_STAGE = "currentUserStage";
}
