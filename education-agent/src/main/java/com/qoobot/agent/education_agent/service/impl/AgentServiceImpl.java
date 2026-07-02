package com.qoobot.agent.education_agent.service.impl;

import com.qoobot.agent.education_agent.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service 实现示例
 */
@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    @Override
    public String getAgentStatus() {
        return "RUNNING";
    }
}
