# 教育培训智能体 (Education Agent)

## 概述

Education Agent 是 OpenAgent 平台下的教育培训 AI 智能体子系统，覆盖**小学 → 初中 → 高中 → 大学 → 职业培训 → 成人终身学习**全学段，服务学生、教师、成人学习者、家长等多类用户角色。系统基于 Spring Boot 3.x 构建，集成 Spring AI、MyBatis-Plus、PostgreSQL + pgvector、Redis、Kafka 等核心技术栈，提供智能辅导、学习路径规划、内容生成、教师助手、作业考试、游戏化激励等全方位智能教育服务。

## 功能模块

| 模块 | 功能数 | 说明 |
|------|--------|------|
| 系统管理 | 4 | 健康检查、应用监控、Agent 生命周期、系统公告 |
| 用户体系 | 5 | 注册登录、学段角色管理、学习者画像、成长档案 |
| 智能辅导 | 8 | 对话辅导、知识讲解、苏格拉底引导、错题分析、个性化推荐 |
| 学习路径规划 | 6 | 学习计划、职业路径、碎片化学习、进度跟踪、能力评估 |
| 内容生成 | 6 | 试题生成、课标对齐出题、知识图谱、资料推荐、笔记整理 |
| 教师助手 | 6 | 智能助教、课件生成、教学质量分析、班级对比、试卷预测 |
| 作业与考试 | 6 | 作业批改、错题归因、中高考模拟、证书刷题、考前押题 |
| 家长端 | 4 | 学情报告、亲子学习、时长管控、效果可视化 |
| 管理后台 | 5 | 学情大屏、学生预警、课程评估、区域分析、教师看板 |
| 游戏化激励 | 4 | 成就徽章、积分体系、闯关模式、排行榜 |
| 多模态交互 | 4 | 语音交互、拍照识题、手写识别、流式输出 |
| AI 能力引擎 | 6 | LLM 对话、向量嵌入、RAG 检索、多模型路由、工具调用 |
| 数据与缓存 | 5 | PostgreSQL、Redis、Kafka、事件存储、上下文持久化 |
| 安全与合规 | 5 | 未成年人保护、接口鉴权、隐私保护、Prompt 防护、审计日志 |
| **合计** | **73** | P0: 20 / P1: 30 / P2: 20 |

## 技术栈

| 技术 | 版本/说明 |
|------|-----------|
| Java | 17+ |
| Spring Boot | 3.5.10 |
| Spring AI | 2.0 |
| MyBatis-Plus | 最新稳定版 |
| PostgreSQL | 15+ (含 pgvector 扩展) |
| Redis | 7+ |
| Kafka | 3+ |
| SpringDoc OpenAPI | 最新稳定版 |
| Lombok | 最新稳定版 |

## 项目结构

```
education-agent/
├── src/main/java/com/qoobot/agent/education_agent/
│   ├── common/              # 通用模块 (Result, ErrorCode, BusinessException)
│   ├── config/              # 配置类 (MyBatis, Redis, WebMvc)
│   ├── controller/          # 控制器层
│   ├── dto/                 # 数据传输对象 (请求参数)
│   ├── entity/              # 实体类
│   ├── exception/           # 全局异常处理
│   ├── mapper/              # 数据访问层
│   ├── service/             # 业务逻辑层
│   │   └── impl/            # 业务实现
│   ├── util/                # 工具类
│   ├── vo/                  # 视图对象 (响应数据)
│   └── Application.java     # 启动入口
├── src/main/resources/
│   ├── mapper/              # MyBatis XML 映射文件
│   └── application.yml      # 应用配置
└── pom.xml                  # Maven 构建配置
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- PostgreSQL 15+ (需安装 pgvector 扩展)
- Redis 7+
- Kafka 3+

### 1. 启动基础设施

```bash
# 使用 Docker 启动依赖服务
docker-compose up -d postgres redis kafka
```

### 2. 初始化数据库

```bash
psql -U postgres -h localhost -f 02-scripts/db-init-education-agent.sql
```

### 3. 配置环境变量

```bash
export OPENAI_API_KEY=your-api-key
```

### 4. 修改配置 (可选)

编辑 `src/main/resources/application.yml`，根据实际环境修改数据库、Redis、Kafka 连接信息。

### 5. 启动应用

```bash
mvn spring-boot:run
```

应用启动后访问: http://localhost:8202/education-agent/system/health

## 端口与路径

| 项目 | 值 |
|------|-----|
| 应用端口 | `8202` |
| 上下文路径 | `/education-agent` |
| Swagger UI | `http://localhost:8202/education-agent/swagger-ui.html` |
| API 文档 | `http://localhost:8202/education-agent/v3/api-docs` |
| 健康检查 | `http://localhost:8202/education-agent/system/health` |
| Actuator | `http://localhost:8202/education-agent/actuator` |

## 配置说明

### Agent 配置

```yaml
agent:
  id: education-agent              # 智能体唯一标识
  name: 教育培训智能体               # 智能体名称
  model:
    provider: spring-ai            # AI 模型提供者
    chat-model: gpt-4o-mini        # 对话模型
    embedding-model: text-embedding-3-small  # 向量模型
  vector:
    enabled: true                  # 是否启用向量检索
    dimension: 1536                # 向量维度
    index-type: ivfflat            # 索引类型
    metric-type: cosine            # 距离度量方式
```

### 数据源配置

- 数据库: PostgreSQL
- 连接池: HikariCP (最大 20 连接, 最小 5 空闲)
- 数据库名: `openagent_education_agent`

### 缓存配置

- Redis database: `14`
- 序列化: Key 使用 StringSerializer, Value 使用 JSON Serializer

### 消息队列配置

- Kafka consumer group: `education-agent-group`
- offset 重置策略: `earliest`

## 相关文档

### 产品与设计

- [00-头脑风暴](docs/00-头脑风暴.md) - 产品创意、技术探索、竞品分析、待讨论议题
- [01-功能清单](docs/01-功能清单.md) - 全量功能列表与优先级
- [02-产品设计文档](docs/02-产品设计文档.md) - 全学段产品定位、用户角色、功能设计、迭代规划
- [03-应用设计文档](docs/03-应用设计文档.md) - 应用架构、接口路由、缓存/消息队列、安全设计
- [04-技术设计文档](docs/04-技术设计文档.md) - 技术选型、系统架构、AI 集成、多模型路由、多模态

### 开发与运维

- [05-API接口文档](docs/05-API接口文档.md) - 接口规范、全量接口规划、响应格式、错误码
- [06-数据库设计文档](docs/06-数据库设计文档.md) - 14 张表结构、向量检索、连接池
- [07-部署运维文档](docs/07-部署运维文档.md) - 环境部署、监控运维、故障排查
- [08-开发规范文档](docs/08-开发规范文档.md) - 分层架构、命名规范、多学段接口规范、模块包结构

### 前端设计

- [10-前端页面交互设计文档](docs/10-前端页面交互设计文档.md) - 设计系统、学段自适应主题、全页面交互规范、组件规范、路由规划

### 进度跟踪

- [09-功能开发进度](docs/09-功能开发进度.md) - 73 功能点进度跟踪、迭代里程碑

## License

MIT License
