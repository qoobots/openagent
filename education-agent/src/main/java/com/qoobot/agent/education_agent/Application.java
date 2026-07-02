package com.qoobot.agent.education_agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 教育培训AI Agent应用入口
 *
 * <p>覆盖小学 → 初中 → 高中 → 大学 → 职业培训 → 成人终身学习 全学段
 * 基于 Spring Boot 3.x + Spring AI + MyBatis-Plus + PostgreSQL/pgvector + Redis + Kafka 构建
 */
@EnableScheduling
@MapperScan("com.qoobot.agent.education_agent.mapper")
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
