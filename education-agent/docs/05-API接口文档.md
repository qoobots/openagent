# API 接口文档

> **OpenAPI 3.0** | JSON: `/v3/api-docs` | Swagger UI: `/swagger-ui.html`

---

## 一、通用规范

### 1.1 基础信息

| 项 | 值 |
|----|-----|
| Base URL | `http://localhost:8202/education-agent` |
| 协议 | HTTP / HTTPS |
| 数据格式 | `application/json` |
| 字符编码 | UTF-8 |
| API 版本 | 1.0.0 |

### 1.2 认证方式

除健康检查和 Actuator 外，所有接口需要 Bearer Token 认证：

```
Authorization: Bearer <access_token>
```

**安全方案** (OpenAPI SecurityScheme):

```yaml
components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT Token，通过 /api/auth/login 获取
```

**免认证接口**: `GET /system/health`, `/actuator/**`

### 1.3 统一响应格式

所有接口返回 `Result<T>` 泛型结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-07-02T10:00:00Z"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | Integer | Y | 状态码，200=成功，其他=失败 |
| message | String | Y | 响应消息 |
| data | T | N | 响应数据，失败时可能为 null |
| timestamp | String | Y | 服务器时间戳 (ISO 8601) |

### 1.4 分页请求/响应

**分页请求参数** (Query):

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | Integer | N | 1 | 当前页码 |
| size | Integer | N | 20 | 每页条数，最大 100 |
| sortBy | String | N | create_time | 排序字段 |
| sortOrder | String | N | desc | 排序方向 (asc/desc) |

**分页响应结构** `Result<Page<T>>`:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| records | Array\<T\> | 当前页数据列表 |
| total | Long | 总记录数 |
| size | Integer | 每页条数 |
| current | Integer | 当前页码 |
| pages | Integer | 总页数 |

### 1.5 错误码一览

| 错误码 | HTTP Status | 说明 |
|--------|-------------|------|
| 200 | 200 | 操作成功 |
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未授权访问 (Token 无效/过期) |
| 403 | 403 | 权限不足 (角色/学段不匹配) |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务器内部错误 |
| 503 | 503 | 服务不可用 |
| 1001 | 200 | 智能体不存在 |
| 1002 | 200 | 智能体已禁用 |
| 1003 | 200 | 模型调用失败 |
| 1004 | 200 | 工具执行失败 |
| 2001 | 200 | 用户不存在 |
| 2002 | 200 | 手机号已注册 |
| 2003 | 200 | 验证码错误/过期 |
| 2004 | 200 | 学段切换受限 |
| 4001 | 200 | 对话会话不存在 |
| 4002 | 200 | 上下文超出长度限制 |
| 6001 | 200 | 试题生成失败 |
| 8001 | 200 | 作业提交不存在 |
| 11001 | 200 | 内容安全审核未通过 |

### 1.6 公共请求头

| Header | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | Y* | Bearer Token (*免认证接口除外) |
| Content-Type | String | Y | `application/json` |
| X-Education-Stage | String | N | 请求学段上下文 (elementary/middle/high/university/vocational/adult) |
| X-Request-Id | String | N | 请求追踪 ID (客户端生成) |

### 1.7 学段枚举值

```
educationStage: elementary | middle | high | university | vocational | adult
```

| 值 | 说明 |
|----|------|
| elementary | 小学 (6-12岁) |
| middle | 初中 (12-15岁) |
| high | 高中 (15-18岁) |
| university | 大学 (18-22岁) |
| vocational | 职业培训 |
| adult | 成人终身学习 |

---

## 二、已实现接口

### 2.1 健康检查

```
GET /system/health
```

| 项 | 说明 |
|----|------|
| Tag | 系统管理 |
| 认证 | 不需要 |
| 描述 | 获取系统健康状态及智能体基础信息 |

**响应** `Result<HealthVO>`:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "agent": "教育培训智能体",
    "description": "覆盖小学到成人终身学习的全学段AI智能体助手",
    "timestamp": "2026-07-02T10:00:00"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| data.status | String | 服务状态 (UP / DOWN) |
| data.agent | String | 智能体名称 |
| data.description | String | 智能体描述 |
| data.timestamp | String | 当前时间戳 |

### 2.2 Actuator 监控端点

| 端点 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/actuator/health` | GET | 不需要 | 应用健康详情 (DB/Redis/Kafka 状态) |
| `/actuator/info` | GET | 不需要 | 应用构建信息 |
| `/actuator/metrics` | GET | 不需要 | 应用性能指标 (JVM/HTTP/自定义) |

---

## 三、认证授权模块 `auth`

### 3.1 用户注册 `P0`

```
POST /api/auth/register
```

| 项 | 说明 |
|----|------|
| Tag | 认证授权 |
| 认证 | 不需要 |

**请求体** `RegisterRequest`:

```json
{
  "username": "zhangsan",
  "password": "P@ssw0rd123",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "role": "student",
  "educationStage": "high",
  "smsCode": "123456"
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| username | String | Y | 4-50字符, 字母开头 | 用户名 |
| password | String | Y | 8-32字符, 含大小写+数字 | 密码 |
| phone | String | N | 11位手机号 | 手机号 |
| email | String | N | 邮箱格式 | 邮箱 |
| role | String | Y | student/teacher/parent | 用户角色 |
| educationStage | String | Y | 见学段枚举 | 当前学段 |
| smsCode | String | N | 6位数字 | 短信验证码 (手机号注册时必填) |

**响应** `Result<LoginVO>`:

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1812345678901234567,
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200,
    "role": "student",
    "educationStage": "high"
  }
}
```

### 3.2 用户登录 `P0`

```
POST /api/auth/login
```

| 项 | 说明 |
|----|------|
| Tag | 认证授权 |
| 认证 | 不需要 |

**请求体** `LoginRequest`:

```json
{
  "loginType": "password",
  "username": "zhangsan",
  "password": "P@ssw0rd123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| loginType | String | Y | 登录方式: password / sms / third_party |
| username | String | 条件 | 用户名 (password 方式必填) |
| password | String | 条件 | 密码 (password 方式必填) |
| phone | String | 条件 | 手机号 (sms 方式必填) |
| smsCode | String | 条件 | 验证码 (sms 方式必填) |
| thirdPartyType | String | 条件 | 第三方类型: wechat / alipay (third_party 方式必填) |
| thirdPartyToken | String | 条件 | 第三方 Token |

**响应** `Result<LoginVO>`:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1812345678901234567,
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200,
    "role": "student",
    "educationStage": "high",
    "nickname": "张三",
    "avatarUrl": "https://cdn.example.com/avatar/xxx.jpg"
  }
}
```

### 3.3 刷新 Token `P0`

```
POST /api/auth/token/refresh
```

**请求体**: `{ "refreshToken": "eyJhbGci..." }`

**响应**: 同 LoginVO

### 3.4 退出登录 `P0`

```
POST /api/auth/logout
```

| 项 | 说明 |
|----|------|
| 认证 | 需要 Bearer Token |

**请求头**: `Authorization: Bearer <token>`

**响应**: `Result<Void>` — `{ "code": 200, "message": "退出成功", "data": null }`

### 3.5 第三方登录 `P1`

```
POST /api/auth/login/third-party
```

**请求体**: `{ "type": "wechat", "code": "oauth_code_xxx" }`

**响应**: 同 LoginVO

---

## 四、用户体系模块 `user`

### 4.1 获取用户信息 `P0`

```
GET /api/user/profile
```

**响应** `Result<UserProfileVO>`:

```json
{
  "code": 200,
  "data": {
    "userId": 1812345678901234567,
    "username": "zhangsan",
    "nickname": "张三",
    "avatarUrl": "https://cdn.example.com/avatar/xxx.jpg",
    "role": "student",
    "educationStage": "high",
    "phone": "138****8000",
    "email": "zhangsan@example.com",
    "learningStyle": "visual",
    "registerTime": "2026-07-01T08:00:00Z",
    "lastLoginTime": "2026-07-02T10:00:00Z"
  }
}
```

> 注: 手机号脱敏显示 (中间4位替换为*)

### 4.2 更新用户信息 `P0`

```
PUT /api/user/profile
```

**请求体** `UpdateProfileRequest`:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | String | N | 昵称 (2-50字符) |
| avatarUrl | String | N | 头像 URL |
| email | String | N | 邮箱 |
| learningStyle | String | N | 学习风格: visual/auditory/kinesthetic |

### 4.3 切换学段 `P0`

```
PUT /api/user/profile/stage
```

**请求体**: `{ "educationStage": "university" }`

**响应**: `Result<Void>`

### 4.4 获取学习者画像 `P1`

```
GET /api/user/profile/learning
```

**响应** `Result<LearningProfileVO>`:

```json
{
  "code": 200,
  "data": {
    "learningStyle": "visual",
    "knowledgeLevel": "intermediate",
    "strongSubjects": ["数学", "物理"],
    "weakSubjects": ["英语"],
    "totalStudyHours": 120.5,
    "averageDailyMinutes": 45,
    "preferredStudyTime": "evening",
    "completionRate": 78.5
  }
}
```

### 4.5 获取学习成长档案 `P1`

```
GET /api/user/archive
```

**查询参数**: `?educationStage=high&year=2026`

**响应** `Result<LearningArchiveVO>`: 含学习历程时间线、各学段成绩趋势、能力变化

### 4.6 多孩子管理 `P2`

| 接口 | 方法 | 请求体 | 响应 |
|------|------|--------|------|
| `/api/family/children` | GET | - | `Result<List<ChildVO>>` — 孩子列表 |
| `/api/family/children` | POST | `{ "childUserId": 123, "relation": "father" }` | `Result<Void>` |

---

## 五、智能辅导模块 `tutor`

### 5.1 智能对话辅导 `P0`

```
POST /api/tutor/chat
```

| 项 | 说明 |
|----|------|
| Tag | 智能辅导 |
| 认证 | Bearer Token |

**请求体** `ChatRequest`:

```json
{
  "sessionId": "session_abc123",
  "educationStage": "high",
  "message": "请帮我讲解一下二次函数的顶点式",
  "context": {
    "courseId": 100,
    "knowledgeId": 200
  }
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | String | N | 会话 ID，首次对话不传，后续传入保持上下文 |
| educationStage | String | Y | 学段，决定回答风格和内容深度 |
| message | String | Y | 用户问题 (最大 2000 字符) |
| context.courseId | Long | N | 关联课程 ID |
| context.knowledgeId | Long | N | 关联知识点 ID |

**响应** `Result<ChatResponseVO>`:

```json
{
  "code": 200,
  "data": {
    "sessionId": "session_abc123",
    "reply": "二次函数的顶点式为 y = a(x-h)² + k，其中 (h,k) 是顶点坐标...",
    "references": [
      {
        "knowledgeId": 200,
        "title": "二次函数的顶点式",
        "courseName": "高中数学必修一",
        "relevance": 0.95
      }
    ],
    "suggestedQuestions": [
      "顶点式如何转化为一般式？",
      "如何由顶点式判断开口方向？"
    ],
    "modelUsed": "gpt-4o-mini",
    "tokensUsed": 350
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| sessionId | String | 会话 ID，后续请求需传入 |
| reply | String | AI 回复内容 |
| references | Array | 引用的知识点列表 |
| references[].knowledgeId | Long | 知识点 ID |
| references[].title | String | 知识点标题 |
| references[].courseName | String | 所属课程 |
| references[].relevance | Double | 相关度 (0-1) |
| suggestedQuestions | Array\<String\> | AI 推荐的追问 |
| modelUsed | String | 使用的模型名称 |
| tokensUsed | Integer | 本次消耗 Token 数 |

### 5.2 知识点讲解 `P0`

```
POST /api/tutor/explain
```

**请求体** `ExplainRequest`:

```json
{
  "educationStage": "middle",
  "knowledgeId": 150,
  "keyword": "勾股定理",
  "explainDepth": "standard",
  "includeExamples": true
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| educationStage | String | Y | 学段 |
| knowledgeId | Long | N | 知识点 ID (与 keyword 二选一) |
| keyword | String | N | 知识点关键词 (与 knowledgeId 二选一) |
| explainDepth | String | N | 讲解深度: basic/standard/advanced (默认 standard) |
| includeExamples | Boolean | N | 是否包含例题 (默认 true) |

**响应** `Result<ExplainVO>`:

```json
{
  "code": 200,
  "data": {
    "title": "勾股定理",
    "definition": "在直角三角形中，两条直角边的平方和等于斜边的平方...",
    "formula": "a² + b² = c²",
    "explanation": "勾股定理是几何学中最基本的定理之一...",
    "examples": [
      {
        "question": "已知直角三角形两直角边为3和4，求斜边",
        "solution": "c = √(3² + 4²) = √(9+16) = √25 = 5"
      }
    ],
    "relatedKnowledge": ["三角函数", "余弦定理"],
    "commonMistakes": ["混淆直角边和斜边", "忘记开方"]
  }
}
```

### 5.3 苏格拉底式引导 `P1`

```
POST /api/tutor/guide
```

**请求体**: `{ "sessionId": "xxx", "educationStage": "high", "question": "为什么sin²θ+cos²θ=1?" }`

**响应**: 同 ChatResponseVO，但 reply 为追问式引导而非直接答案

### 5.4 错题本 `P1`

| 接口 | 方法 | 参数/请求体 | 响应 |
|------|------|------------|------|
| `/api/tutor/wrong-questions` | GET | `?educationStage=high&knowledgeId=200&current=1&size=20` | `Result<Page<WrongQuestionVO>>` |
| `/api/tutor/wrong-questions/analyze` | POST | `{ "educationStage": "high", "knowledgeId": 200 }` | `Result<ErrorAnalysisVO>` |

**ErrorAnalysisVO** 结构:

```json
{
  "totalWrongCount": 15,
  "errorTypeDistribution": {
    "conceptual": 8,
    "calculation": 4,
    "careless": 3
  },
  "weakKnowledgePoints": [
    { "knowledgeId": 200, "title": "二次函数顶点式", "wrongRate": 0.6 }
  ],
  "suggestedPractice": [1001, 1002, 1003]
}
```

### 5.5 个性化学习推荐 `P1`

```
POST /api/tutor/recommend
```

**请求体**: `{ "educationStage": "high", "limit": 10 }`

**响应** `Result<List<RecommendVO>>`: 含推荐内容类型(content/exercise/knowledge)、标题、理由、相关度

### 5.6 其他辅导接口 `P2`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/tutor/bilingual` | POST | 多语言讲解，请求体含 `{ "keyword": "xxx", "languages": ["zh","en"] }` |
| `/api/tutor/sessions` | GET | 会话列表，`?current=1&size=20` → `Result<Page<SessionVO>>` |
| `/api/tutor/sessions/{id}/messages` | GET | 会话消息，`?current=1&size=50` → `Result<Page<ChatMessageVO>>` |
| `/api/tutor/study-partner` | POST | 学习伙伴匹配，请求体 `{ "educationStage": "high", "subjects": ["数学"] }` |

---

## 六、学习路径规划模块 `plan`

### 6.1 创建学习计划 `P0`

```
POST /api/plan/create
```

**请求体** `CreatePlanRequest`:

```json
{
  "educationStage": "high",
  "planName": "高考数学冲刺计划",
  "courseId": 100,
  "targetScore": 140,
  "currentLevel": "intermediate",
  "startDate": "2026-07-15",
  "endDate": "2026-12-05",
  "dailyStudyMinutes": 90,
  "weakKnowledgeIds": [200, 201, 205]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| educationStage | String | Y | 学段 |
| planName | String | Y | 计划名称 |
| courseId | Long | N | 关联课程 ID |
| targetScore | Integer | N | 目标分数 |
| currentLevel | String | N | 当前水平: beginner/intermediate/advanced |
| startDate | Date | Y | 开始日期 |
| endDate | Date | Y | 结束日期 |
| dailyStudyMinutes | Integer | N | 每日学习时长(分钟) |
| weakKnowledgeIds | Array\<Long\> | N | 薄弱知识点 ID 列表 |

**响应** `Result<StudyPlanVO>`:

```json
{
  "code": 200,
  "data": {
    "planId": 1812345678901234567,
    "planName": "高考数学冲刺计划",
    "totalTasks": 120,
    "totalDays": 143,
    "phases": [
      {
        "phaseName": "基础巩固",
        "startDate": "2026-07-15",
        "endDate": "2026-09-01",
        "tasks": 40
      }
    ],
    "dailySchedule": {
      "studyMinutes": 90,
      "knowledgePerDay": 2,
      "practicePerDay": 5
    }
  }
}
```

### 6.2 职业培训学习路径 `P0`

```
POST /api/plan/career-path
```

**请求体**: `{ "targetOccupation": "Java后端工程师", "currentLevel": "beginner", "availableMonths": 6 }`

**响应** `Result<CareerPathVO>`: 含技能树节点、学习顺序、预计时长、推荐资源

### 6.3 其他规划接口

| 接口 | 方法 | 优先级 | 请求/响应说明 |
|------|------|--------|--------------|
| `/api/plan/daily-learn` | GET | P1 | `?educationStage=adult` → `Result<DailyLearnVO>` 含今日内容+打卡状态 |
| `/api/plan/daily-learn/checkin` | POST | P1 | `{ "educationStage": "adult" }` → `Result<CheckinVO>` |
| `/api/plan/progress` | GET | P1 | `?planId=123` → `Result<ProgressVO>` 含完成百分比、已学知识点、待学知识点 |
| `/api/plan/evaluate` | POST | P1 | `{ "planId": 123, "testScore": 85 }` → `Result<EvaluationVO>` 含能力雷达图数据 |
| `/api/plan/goal` | POST | P2 | `{ "goalName": "xxx", "deadline": "2026-12-31", "educationStage": "high" }` → `Result<GoalVO>` |
| `/api/plan/goal/{id}/tasks` | GET | P2 | → `Result<List<TaskVO>>` 目标拆解任务列表 |

---

## 七、内容生成模块 `content`

### 7.1 试题自动生成 `P0`

```
POST /api/content/generate-questions
```

**请求体** `GenerateQuestionRequest`:

```json
{
  "educationStage": "high",
  "courseId": 100,
  "knowledgeIds": [200, 201],
  "questionTypes": ["single_choice", "multi_choice", "essay"],
  "difficulty": 3,
  "count": 10,
  "options": {
    "includeAnswer": true,
    "includeAnalysis": true,
    "adaptiveDifficulty": false
  }
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| educationStage | String | Y | 学段 |
| courseId | Long | Y | 课程 ID |
| knowledgeIds | Array\<Long\> | N | 知识点 ID 列表 (不传则按课程全量) |
| questionTypes | Array\<String\> | Y | 题型: single_choice/multi_choice/true_false/fill_blank/essay |
| difficulty | Integer | Y | 难度 1-5 |
| count | Integer | Y | 生成数量 (1-50) |
| options.includeAnswer | Boolean | N | 是否含答案 (默认 true) |
| options.includeAnalysis | Boolean | N | 是否含解析 (默认 true) |
| options.adaptiveDifficulty | Boolean | N | 是否难度自适应 (默认 false) |

**响应** `Result<GeneratedQuestionsVO>`:

```json
{
  "code": 200,
  "data": {
    "questions": [
      {
        "questionId": 1001,
        "type": "single_choice",
        "content": "函数 y = 2(x-1)² + 3 的顶点坐标为：",
        "options": ["A. (1,3)", "B. (-1,3)", "C. (1,-3)", "D. (2,3)"],
        "answer": "A",
        "analysis": "由顶点式 y=a(x-h)²+k 可知顶点为 (h,k)=(1,3)",
        "knowledgeId": 200,
        "knowledgeTitle": "二次函数顶点式",
        "difficulty": 3
      }
    ],
    "totalCount": 10,
    "estimatedCompletionMinutes": 30
  }
}
```

### 7.2 其他内容接口

| 接口 | 方法 | 优先级 | 请求/响应说明 |
|------|------|--------|--------------|
| `/api/content/generate-questions/standard` | POST | P1 | 增加 `textbookVersion` (人教版/北师大版) + `grade` 字段 |
| `/api/content/knowledge-graph` | GET | P1 | `?courseId=100` → `Result<KnowledgeGraphVO>` 含 nodes + edges |
| `/api/content/knowledge-graph/build` | POST | P1 | `{ "courseId": 100, "sourceType": "textbook" }` → `Result<BuildStatusVO>` |
| `/api/content/recommend` | GET | P1 | `?educationStage=high&knowledgeId=200&limit=10` → `Result<List<MaterialVO>>` |
| `/api/content/notes/organize` | POST | P2 | multipart/form-data 上传笔记图片/文字 → `Result<OrganizedNotesVO>` |
| `/api/content/generate-lesson-plan` | POST | P2 | `{ "educationStage": "middle", "topic": "xxx", "duration": 45 }` → `Result<LessonPlanVO>` |

---

## 八、教师助手模块 `teacher`

| 接口 | 方法 | 优先级 | 请求体 | 响应 |
|------|------|--------|--------|------|
| `/api/teacher/assistant` | POST | P0 | `{ "educationStage": "middle", "courseId": 100, "question": "学生常问的问题有哪些" }` | `Result<AssistantReplyVO>` |
| `/api/teacher/courseware` | POST | P0 | `{ "educationStage": "elementary", "topic": "分数的认识", "duration": 40, "style": "interactive" }` | `Result<CoursewareVO>` 含大纲/讲义/案例 |
| `/api/teacher/quality-analysis` | GET | P1 | `?courseId=100&dateRange=2026-06` | `Result<QualityAnalysisVO>` 含薄弱知识点排行 |
| `/api/teacher/class-comparison` | GET | P1 | `?courseId=100&classIds=1,2,3` | `Result<ClassComparisonVO>` 含各班级对比数据 |
| `/api/teacher/exam-predict` | POST | P1 | `{ "questionIds": [1001,1002,...], "targetClass": "高三(1)班" }` | `Result<ExamPredictVO>` 含预测难度/通过率 |
| `/api/teacher/materials` | GET | P2 | `?educationStage=high&subject=数学&keyword=函数` | `Result<List<MaterialVO>>` |

---

## 九、作业与考试模块 `homework` / `exam`

| 接口 | 方法 | 优先级 | 请求体 | 响应 |
|------|------|--------|--------|------|
| `/api/homework/correct` | POST | P0 | `{ "homeworkId": 100, "answers": [{"questionId":1001,"answer":"A"}], "educationStage": "high" }` | `Result<CorrectionVO>` 含逐题得分+总评+改进建议 |
| `/api/homework/error-analysis` | POST | P1 | `{ "homeworkId": 100 }` | `Result<ErrorAnalysisVO>` 含归因分析+薄弱知识点 |
| `/api/exam/mock` | POST | P1 | `{ "examType": "gaokao_mock", "educationStage": "high", "subject": "数学", "year": "2025", "region": "全国甲卷" }` | `Result<ExamVO>` 含试卷内容+计时 |
| `/api/exam/cert-practice` | POST | P1 | `{ "certType": "PMP", "mode": "practice", "count": 10 }` | `Result<PracticeVO>` |
| `/api/exam/grad-exam` | POST | P2 | `{ "examType": "kaoyan", "targetSchool": "清华大学", "subject": "数学一" }` | `Result<ExamPlanVO>` |
| `/api/exam/predict-questions` | POST | P2 | `{ "educationStage": "high", "courseId": 100, "count": 20 }` | `Result<List<QuestionVO>>` AI 预测题 |

---

## 十、家长端模块 `parent`

| 接口 | 方法 | 优先级 | 参数 | 响应 |
|------|------|--------|------|------|
| `/api/parent/report/daily` | GET | P1 | `?childId=123&date=2026-07-02` | `Result<DailyReportVO>` 含学习时长/完成作业/薄弱点 |
| `/api/parent/report/weekly` | GET | P1 | `?childId=123&weekStart=2026-06-26` | `Result<WeeklyReportVO>` 含周趋势图数据 |
| `/api/parent/interactive` | POST | P1 | `{ "childId": 123, "activityType": "math_game" }` | `Result<InteractiveTaskVO>` |
| `/api/parent/time-limit` | PUT | P2 | `{ "childId": 123, "dailyLimitMinutes": 60, "lockEnabled": true }` | `Result<Void>` |
| `/api/parent/progress-chart` | GET | P2 | `?childId=123&period=3m` | `Result<ProgressChartVO>` 含进步趋势数据 |

---

## 十一、管理后台模块 `admin`

| 接口 | 方法 | 优先级 | 参数 | 响应 |
|------|------|--------|------|------|
| `/api/admin/dashboard` | GET | P1 | `?scope=school&scopeId=1` | `Result<DashboardVO>` 含热力图+统计卡片数据 |
| `/api/admin/warning` | GET | P1 | `?scope=class&scopeId=1&level=high` | `Result<Page<WarningVO>>` 含预警学生列表+原因 |
| `/api/admin/course-evaluation` | GET | P2 | `?courseId=100` | `Result<EvaluationVO>` 含评分+反馈+建议 |
| `/api/admin/region-stats` | GET | P2 | `?regionCode=310000` | `Result<RegionStatsVO>` 含区域学校对比 |
| `/api/admin/teacher-dashboard` | GET | P2 | `?teacherId=456` | `Result<TeacherDashboardVO>` 含使用数据+效果 |

---

## 十二、游戏化激励模块 `gamification`

| 接口 | 方法 | 优先级 | 参数/请求 | 响应 |
|------|------|--------|----------|------|
| `/api/gamification/achievements` | GET | P1 | `?userId=123&type=badge` | `Result<List<AchievementVO>>` |
| `/api/gamification/points` | GET | P1 | - | `Result<PointsVO>` 含当前积分+今日获取 |
| `/api/gamification/points/exchange` | POST | P1 | `{ "itemId": "premium_7d", "quantity": 1 }` | `Result<ExchangeVO>` |
| `/api/gamification/levels` | GET | P2 | `?educationStage=elementary&courseId=100` | `Result<List<LevelVO>>` 含关卡解锁状态 |
| `/api/gamification/ranking` | GET | P2 | `?scope=class&scopeId=1&period=weekly` | `Result<RankingVO>` 含排名列表 |

---

## 十三、多模态交互模块 `multimodal`

| 接口 | 方法 | 优先级 | 请求 | 响应 |
|------|------|--------|------|------|
| `/api/multimodal/voice` | POST | P0 | multipart: audio/webm 文件 + `educationStage` | `Result<VoiceResponseVO>` 含识别文本+AI回复+音频URL |
| `/api/multimodal/photo-solve` | POST | P1 | multipart: image/jpeg 文件 + `educationStage` | `Result<PhotoSolveVO>` 含识别题目+解答过程 |
| `/api/multimodal/handwriting` | POST | P1 | multipart: image/jpeg 文件 | `Result<HandwritingVO>` 含识别文本+置信度 |
| `/api/multimodal/stream-chat` | POST | P2 | 同 ChatRequest，Accept: `text/event-stream` | SSE 流式文本响应 |

---

## 十四、其他模块

### AI 管理 `ai`

| 接口 | 方法 | 优先级 | 说明 |
|------|------|--------|------|
| `/api/ai/prompts` | GET | P2 | Prompt 模板列表，`?educationStage=high&module=tutor` |
| `/api/ai/prompts` | POST | P2 | 创建/更新模板，请求体 `{ "name": "xxx", "template": "...", "variables": ["subject","grade"] }` |

### 事件追踪 `event`

| 接口 | 方法 | 优先级 | 说明 |
|------|------|--------|------|
| `/api/event/track` | POST | P1 | 上报行为，`{ "eventType": "study_complete", "educationStage": "high", "courseId": 100, "duration": 1800, "payload": {} }` |
| `/api/event/query` | GET | P1 | 查询行为，`?userId=123&eventType=study_complete&startDate=2026-07-01` → `Result<Page<EventVO>>` |

### Agent 管理 `agent`

| 接口 | 方法 | 优先级 | 说明 |
|------|------|--------|------|
| `/api/agent/start` | POST | P1 | 启动智能体 |
| `/api/agent/stop` | POST | P1 | 停止智能体 |
| `/api/agent/status` | GET | P1 | 查询状态 → `Result<AgentStatusVO>` |

### 审计与系统 `audit` / `system`

| 接口 | 方法 | 优先级 | 说明 |
|------|------|--------|------|
| `/api/audit/logs` | GET | P2 | `?userId=123&action=login&startDate=2026-07-01` → `Result<Page<AuditLogVO>>` |
| `/api/system/announcements` | GET | P2 | `?active=true` → `Result<List<AnnouncementVO>>` |

---

## 接口统计

| 优先级 | 接口数 | 说明 |
|--------|--------|------|
| ✅ 已实现 | 3 | 健康检查 + Actuator |
| 📋 P0 | 10 | MVP 必须 (详细规格已定义) |
| 📋 P1 | 33 | 第二迭代 (核心字段已定义) |
| 📋 P2 | 22 | 后续规划 (接口已规划) |
| **合计** | **68** | |
