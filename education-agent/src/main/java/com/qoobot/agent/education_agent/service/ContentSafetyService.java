package com.qoobot.agent.education_agent.service;

/**
 * 内容安全审核服务
 *
 * <p>针对未成年人使用场景的输入输出内容安全检查。
 */
public interface ContentSafetyService {

    /**
     * 检查文本内容是否合规
     *
     * @param text 待检查文本
     * @return true=通过, false=不通过
     */
    boolean check(String text);

    /**
     * 检查并返回不通过原因
     *
     * @return 不通过时返回违规原因，通过时返回 null
     */
    String checkAndExplain(String text);

    /**
     * 过滤敏感词（替换为 *）
     */
    String mask(String text);
}
