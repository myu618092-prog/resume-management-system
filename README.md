# AI 实习申请管理系统

一个面向学生的实习申请管理平台，覆盖注册登录、简历上传、岗位管理、投递状态看板、AI 岗位匹配分析、面试日程和统计数据。

## 技术栈

- 后端：Spring Boot 3、Spring Security、JWT、Spring Data JPA、Flyway
- 数据库：PostgreSQL，使用 `jsonb` 保存结构化简历和 AI 分析结果
- 缓存：Redis，用于用户统计数据缓存
- 前端：Vue 3、Vite、lucide-vue-next
- 部署：Docker Compose

## 快速启动

```bash
docker compose up --build
```

启动后访问：

- 前端：http://localhost:5173
- 后端 API：http://localhost:8080/api
- PostgreSQL：localhost:5432，数据库 `internship_tracker`
- Redis：localhost:6379

如果要接入真实 OpenAI 或兼容接口：

```bash
$env:OPENAI_API_KEY="你的 API Key"
$env:OPENAI_BASE_URL="https://api.openai.com/v1"
$env:OPENAI_MODEL="gpt-4.1-mini"
docker compose up --build
```

没有 `OPENAI_API_KEY` 时，系统会自动使用 mock AI 结果，保证演示流程可跑通。

## 核心接口

- `POST /api/auth/register`：注册
- `POST /api/auth/login`：登录
- `GET /api/profile`：当前用户资料
- `POST /api/resumes`：上传简历
- `GET /api/resumes`：简历列表
- `POST /api/jobs`：新增岗位，并自动创建投递记录
- `GET /api/jobs`：岗位看板数据
- `PATCH /api/applications/{id}/status`：更新投递状态
- `POST /api/ai/analyze-job-match`：岗位匹配分析
- `POST /api/ai/interview-questions`：面试题生成
- `POST /api/interviews`：新增面试日程
- `GET /api/interviews`：查询面试日程
- `GET /api/stats`：投递统计

## 简历亮点写法

- 使用 Spring Security + JWT 实现无状态认证，并通过 `user_id` 查询条件保证用户数据隔离。
- 使用 PostgreSQL `jsonb` 存储结构化简历和 AI 分析结果，Flyway 管理数据库迁移。
- 使用 Redis 缓存用户投递统计，并在岗位/状态变化后主动失效。
- AI 服务支持真实 OpenAI 兼容接口和 mock 模式，保证本地演示稳定。
- 使用 Docker Compose 编排前端、后端、PostgreSQL、Redis，实现一键启动。

## 验收流程

1. 注册或使用默认表单登录账号。
2. 新增岗位，填写公司、岗位、城市和 JD。
3. 在看板中切换投递状态。
4. 上传一份 PDF/Word 简历并填写技能、教育和项目经历。
5. 点击岗位卡片上的 AI 分析，查看匹配分数、优势、不足和行动建议。
6. 添加面试日程，查看统计数据和转化率变化。
