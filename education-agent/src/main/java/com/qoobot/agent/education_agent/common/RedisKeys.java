package com.qoobot.agent.education_agent.common;

/**
 * Redis Key 前缀常量
 *
 * <p>统一管理 Redis 缓存 Key，便于维护和扫描
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /** 用户 Token 缓存前缀：auth:token:{userId} */
    public static final String USER_TOKEN = "auth:token:";

    /** Refresh Token 缓存前缀：auth:refresh:{userId} */
    public static final String REFRESH_TOKEN = "auth:refresh:";

    /** 短信验证码前缀：sms:code:{phone} */
    public static final String SMS_CODE = "sms:code:";

    /** 登录失败计数：auth:login:fail:{username} */
    public static final String LOGIN_FAIL = "auth:login:fail:";

    /** 对话上下文：chat:ctx:{sessionId} */
    public static final String CHAT_CONTEXT = "chat:ctx:";

    /** 用户画像：user:profile:{userId} */
    public static final String USER_PROFILE = "user:profile:";

    /** 用户会话：auth:session:{userId} */
    public static final String USER_SESSION = "auth:session:";
}
