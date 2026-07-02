package com.qoobot.agent.education_agent.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * <p>错误码分段：
 * <ul>
 *   <li>1xxx - 智能体 / 模型 / 工具相关</li>
 *   <li>2xxx - 用户 / 认证相关</li>
 *   <li>4xxx - 对话 / 上下文相关</li>
 *   <li>6xxx - 内容生成 / 试题相关</li>
 *   <li>8xxx - 作业 / 考试相关</li>
 *   <li>9xxx - 系统管理 / 公告相关</li>
 *   <li>11xxx - 内容安全 / 合规相关</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // ============ 1xxx - 智能体 / 模型 / 工具 ============
    AGENT_NOT_FOUND(1001, "智能体不存在"),
    AGENT_DISABLED(1002, "智能体已禁用"),
    MODEL_CALL_FAILED(1003, "模型调用失败"),
    TOOL_EXECUTE_FAILED(1004, "工具执行失败"),

    // ============ 2xxx - 用户 / 认证 ============
    USER_NOT_FOUND(2001, "用户不存在"),
    PHONE_ALREADY_REGISTERED(2002, "手机号已注册"),
    SMS_CODE_INVALID(2003, "验证码错误或已过期"),
    STAGE_SWITCH_FORBIDDEN(2004, "学段切换受限"),
    USERNAME_ALREADY_EXISTS(2005, "用户名已被占用"),
    PASSWORD_INCORRECT(2006, "用户名或密码错误"),
    TOKEN_INVALID(2007, "Token 无效或已过期"),
    TOKEN_EXPIRED(2008, "Token 已过期"),
    PERMISSION_DENIED(2009, "无操作权限"),

    // ============ 4xxx - 对话 / 上下文 ============
    CHAT_SESSION_NOT_FOUND(4001, "对话会话不存在"),
    CONTEXT_OVER_LIMIT(4002, "上下文超出长度限制"),

    // ============ 6xxx - 内容生成 / 试题 ============
    QUESTION_GENERATE_FAILED(6001, "试题生成失败"),

    // ============ 8xxx - 作业 / 考试 ============
    HOMEWORK_NOT_FOUND(8001, "作业提交不存在"),

    // ============ 9xxx - 系统管理 ============
    ANNOUNCEMENT_NOT_FOUND(9001, "公告不存在"),

    // ============ 11xxx - 内容安全 / 合规 ============
    CONTENT_SAFETY_REJECTED(11001, "内容安全审核未通过"),
    SENSITIVE_WORD_DETECTED(11002, "检测到敏感词");

    private final Integer code;
    private final String message;
}
