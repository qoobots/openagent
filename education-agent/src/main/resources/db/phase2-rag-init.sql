-- ================================================================
-- Education Agent - Phase 2 RAG 数据库初始化脚本
-- ================================================================
-- 适用版本：v0.4.0（Phase 2 - RAG 检索增强）
-- 数据库：openagent_education_agent
-- 前置依赖：phase1-init.sql 已执行（pgvector 扩展已启用）
-- 创建日期：2026-07-02
-- ================================================================

-- ================================================================
-- 1. 知识点表 (edu_knowledge)
-- ================================================================
DROP TABLE IF EXISTS edu_knowledge CASCADE;

CREATE TABLE edu_knowledge (
    id              BIGINT          PRIMARY KEY,
    course_id       BIGINT,
    subject         VARCHAR(20)     NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    content         TEXT            NOT NULL,
    embedding       VECTOR(1536),
    education_stage VARCHAR(20)     NOT NULL,
    difficulty      INT             NOT NULL DEFAULT 3,
    sort_order      INT             NOT NULL DEFAULT 0,
    status          INT             NOT NULL DEFAULT 1,
    source          VARCHAR(20)     NOT NULL DEFAULT 'manual',
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

-- ================================================================
-- 2. 索引
-- ================================================================

-- 学段 + 学科 联合索引（最常用过滤组合）
CREATE INDEX idx_knowledge_stage_subject
    ON edu_knowledge (education_stage, subject)
    WHERE deleted = 0;

-- 课程索引
CREATE INDEX idx_knowledge_course
    ON edu_knowledge (course_id)
    WHERE deleted = 0 AND course_id IS NOT NULL;

-- 状态索引
CREATE INDEX idx_knowledge_status
    ON edu_knowledge (status)
    WHERE deleted = 0;

-- pgvector 相似度索引（ivfflat 列表数 100 适合百万级以内）
CREATE INDEX idx_knowledge_embedding
    ON edu_knowledge
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- ================================================================
-- 3. 注释
-- ================================================================
COMMENT ON TABLE  edu_knowledge IS '知识点表 - 存储结构化知识条目与 1536 维向量（pgvector）';
COMMENT ON COLUMN edu_knowledge.id              IS '主键，雪花算法生成';
COMMENT ON COLUMN edu_knowledge.course_id       IS '所属课程 ID（可选）';
COMMENT ON COLUMN edu_knowledge.subject         IS '学科（math/chinese/english/physics/chemistry/biology/history/geography/politics）';
COMMENT ON COLUMN edu_knowledge.title           IS '知识点标题';
COMMENT ON COLUMN edu_knowledge.content         IS '知识点内容（可包含公式/段落）';
COMMENT ON COLUMN edu_knowledge.embedding       IS '内容向量 (text-embedding-3-small 1536 维)';
COMMENT ON COLUMN edu_knowledge.education_stage IS '所属学段（elementary/middle/high/university/vocational/adult）';
COMMENT ON COLUMN edu_knowledge.difficulty      IS '难度等级（1-5）';
COMMENT ON COLUMN edu_knowledge.sort_order      IS '排序序号';
COMMENT ON COLUMN edu_knowledge.status          IS '状态：0=下架, 1=上架';
COMMENT ON COLUMN edu_knowledge.source          IS '来源：manual/ai/imported';
COMMENT ON COLUMN edu_knowledge.create_time     IS '创建时间';
COMMENT ON COLUMN edu_knowledge.update_time     IS '更新时间';
COMMENT ON COLUMN edu_knowledge.deleted         IS '逻辑删除标记：0=未删除, 1=已删除';

-- ================================================================
-- 4. 样本数据（注意：embedding 字段为示例占位向量）
-- ================================================================
-- 生产环境向量必须由 EmbeddingModel 生成；
-- 此处示例用全零向量占位，调用 /api/knowledge/create 后自动重写为真实向量。
INSERT INTO edu_knowledge (
    id, subject, title, content, embedding, education_stage, difficulty, sort_order, status, source
) VALUES
    (1, 'math', '勾股定理',
     '勾股定理：在直角三角形中，两条直角边的平方和等于斜边的平方。即 a² + b² = c²，其中 c 为斜边。该定理是欧几里得几何的基本定理之一，距今已有 2000 多年历史，常用于求边长、判断三角形形状等场景。',
     array_fill(0, ARRAY[1536])::vector, 'middle', 2, 100, 1, 'manual'),
    (2, 'math', '一元二次方程求根公式',
     '对于方程 ax² + bx + c = 0 (a≠0)，其解为 x = (-b ± √(b² - 4ac)) / (2a)。判别式 Δ = b² - 4ac：Δ > 0 时有两个不同实根；Δ = 0 时有一个重根；Δ < 0 时无实数根。',
     array_fill(0, ARRAY[1536])::vector, 'middle', 3, 101, 1, 'manual'),
    (3, 'physics', '牛顿第二定律',
     '牛顿第二定律：物体加速度的大小跟作用力成正比，跟物体的质量成反比，加速度的方向跟作用力的方向相同。公式：F = ma。其中 F 是合外力，m 是质量，a 是加速度。',
     array_fill(0, ARRAY[1536])::vector, 'high', 2, 200, 1, 'manual'),
    (4, 'chinese', '古诗词鉴赏方法',
     '古诗词鉴赏五步法：1）看题目（理解题意和写作背景）；2）看作者（了解作者生平和风格）；3）看注释（解决生僻字词）；4）看意象（把握情感寄托）；5）看关键句（捕捉主旨和情感）。',
     array_fill(0, ARRAY[1536])::vector, 'high', 4, 300, 1, 'manual'),
    (5, 'english', '英语时态 - 现在完成时',
     '现在完成时（Present Perfect）表示过去发生的动作对现在造成的影响或结果。结构：have/has + 过去分词。常与 for, since, already, yet, just, ever, never 等时间状语连用。例：I have lived here for 5 years.',
     array_fill(0, ARRAY[1536])::vector, 'middle', 3, 400, 1, 'manual')
ON CONFLICT (id) DO NOTHING;

-- ================================================================
-- 5. 验证
-- ================================================================
SELECT 'edu_knowledge created' AS status, COUNT(*) AS row_count FROM edu_knowledge;
