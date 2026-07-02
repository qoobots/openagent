package com.qoobot.agent.education_agent.util;

import com.qoobot.agent.education_agent.common.BusinessException;
import com.qoobot.agent.education_agent.common.Constants;
import com.qoobot.agent.education_agent.common.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 学段相关工具
 */
@Component
public class StageUtil {

    private static final Set<String> VALID_STAGES = Set.of(
            Constants.STAGE_ELEMENTARY,
            Constants.STAGE_MIDDLE,
            Constants.STAGE_HIGH,
            Constants.STAGE_UNIVERSITY,
            Constants.STAGE_VOCATIONAL,
            Constants.STAGE_ADULT
    );

    /**
     * 校验学段合法性
     */
    public void validate(String stage) {
        if (stage == null || !VALID_STAGES.contains(stage)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                    "学段取值不合法: " + stage);
        }
    }

    /**
     * 是否合法学段
     */
    public boolean isValid(String stage) {
        return stage != null && VALID_STAGES.contains(stage);
    }

    /**
     * 学段中文名称
     */
    public String displayName(String stage) {
        if (stage == null) {
            return "未知";
        }
        return switch (stage) {
            case Constants.STAGE_ELEMENTARY -> "小学";
            case Constants.STAGE_MIDDLE -> "初中";
            case Constants.STAGE_HIGH -> "高中";
            case Constants.STAGE_UNIVERSITY -> "大学";
            case Constants.STAGE_VOCATIONAL -> "职业培训";
            case Constants.STAGE_ADULT -> "成人终身学习";
            default -> "未知";
        };
    }
}
