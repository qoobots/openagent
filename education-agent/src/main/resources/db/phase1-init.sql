-- ================================================================
-- Education Agent - Phase 1 数据库初始化脚本
-- ================================================================
-- 适用版本：v0.1.0（Phase 1 - MVP 基础框架）
-- 数据库：openagent_education_agent
-- 创建日期：2026-07-02
-- ================================================================

-- 1. 创建数据库（如不存在）
-- CREATE DATABASE openagent_education_agent
--     WITH ENCODING = 'UTF8'
--     LC_COLLATE = 'en_US.UTF-8'
--     LC_CTYPE = 'en_US.UTF-8'
--     TEMPLATE = template0;

-- 2. 启用 pgvector 扩展（Phase 2 RAG 必用，Phase 1 预装）
CREATE EXTENSION IF NOT EXISTS vector;

-- 3. uuid 扩展（用于雪花 ID 兜底场景）
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================================
-- 4. 用户表 (edu_user)
-- ================================================================
DROP TABLE IF EXISTS edu_user CASCADE;

CREATE TABLE edu_user (
    id              BIGINT          PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL,
    phone           VARCHAR(20),
    email           VARCHAR(100),
    password_hash   VARCHAR(200)    NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    education_stage VARCHAR(20)     NOT NULL,
    nickname        VARCHAR(50),
    avatar_url      VARCHAR(500),
    learning_style  VARCHAR(20),
    status          INT             NOT NULL DEFAULT 1,
    last_login_time TIMESTAMP,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

-- 用户名唯一索引（逻辑删除下保证唯一）
CREATE UNIQUE INDEX uk_user_username
    ON edu_user (username)
    WHERE deleted = 0;

-- 手机号索引（用于登录查询）
CREATE INDEX idx_user_phone
    ON edu_user (phone)
    WHERE deleted = 0 AND phone IS NOT NULL;

-- 学段索引（按学段统计、推荐）
CREATE INDEX idx_user_stage
    ON edu_user (education_stage)
    WHERE deleted = 0;

-- 角色索引
CREATE INDEX idx_user_role
    ON edu_user (role)
    WHERE deleted = 0;

-- 邮箱索引
CREATE INDEX idx_user_email
    ON edu_user (email)
    WHERE deleted = 0 AND email IS NOT NULL;

-- 表注释
COMMENT ON TABLE  edu_user IS '用户表 - 覆盖学生/教师/家长/管理员四类角色';
COMMENT ON COLUMN edu_user.id              IS '主键，雪花算法生成';
COMMENT ON COLUMN edu_user.username        IS '用户名（全局唯一）';
COMMENT ON COLUMN edu_user.phone           IS '手机号（加密存储）';
COMMENT ON COLUMN edu_user.email           IS '邮箱';
COMMENT ON COLUMN edu_user.password_hash   IS '密码哈希（SHA-256 + Salt）';
COMMENT ON COLUMN edu_user.role            IS '角色：student/teacher/parent/admin';
COMMENT ON COLUMN edu_user.education_stage IS '当前学段：elementary/middle/high/university/vocational/adult';
COMMENT ON COLUMN edu_user.nickname        IS '昵称';
COMMENT ON COLUMN edu_user.avatar_url      IS '头像 URL';
COMMENT ON COLUMN edu_user.learning_style  IS '学习风格：visual/auditory/kinesthetic';
COMMENT ON COLUMN edu_user.status          IS '状态：0=禁用, 1=正常';
COMMENT ON COLUMN edu_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN edu_user.create_time     IS '创建时间';
COMMENT ON COLUMN edu_user.update_time     IS '更新时间';
COMMENT ON COLUMN edu_user.deleted         IS '逻辑删除标记：0=未删除, 1=已删除';

-- ================================================================
-- 5. 初始化数据（仅开发环境）
-- ================================================================
-- 默认管理员账号（密码：admin123，登录后请立即修改）
-- 密码哈希计算规则见 PasswordUtil.encode("admin123")
INSERT INTO edu_user (
    id, username, phone, email, password_hash, role, education_stage,
    nickname, avatar_url, status, last_login_time
) VALUES (
    1,
    'admin',
    '13800000000',
    'admin@openagent.example.com',
    '1024$8JZUEKE9eO3wEhAtmKtcNQ==$b3WOhTLip9jJZpcjCJV0PqIHfmGyZc1NL2NnQwxL6C0=',
    'admin',
    'adult',
    '系统管理员',
    'https://cdn.openagent.example.com/avatar/admin.png',
    1,
    NULL
) ON CONFLICT (id) DO NOTHING;

-- 测试学生账号（密码：Test@123）
INSERT INTO edu_user (
    id, username, phone, email, password_hash, role, education_stage,
    nickname, avatar_url, status, last_login_time
) VALUES (
    10001,
    'student_demo',
    '13800138001',
    'student_demo@example.com',
    '1024$placeholder==$placeholder',
    'student',
    'high',
    '示例学生',
    'https://cdn.openagent.example.com/avatar/student.png',
    1,
    NULL
) ON CONFLICT (id) DO NOTHING;

-- ================================================================
-- 6. 完成
-- ================================================================
-- 验证
SELECT 'edu_user created' AS status, COUNT(*) AS row_count FROM edu_user;
