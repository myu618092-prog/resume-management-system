# AI 实习申请管理系统

一个面向学生的实习申请管理平台，覆盖注册登录、简历上传、岗位管理、投递状态看板、真实岗位数据导入、AI 岗位匹配分析、面试题生成、面试日程和统计数据。

## 技术栈

- 后端：Spring Boot 3、Spring Security、JWT、Spring Data JPA、Flyway
- 数据库：PostgreSQL，使用 `jsonb` 保存结构化简历和 AI 分析结果
- 缓存：Redis，用于用户统计数据缓存
- 前端：Vue 3、Vite、lucide-vue-next
- 部署：Docker Compose
- AI：OpenAI API，未配置 Key 时自动使用 mock 模式

## 快速启动

```bash
docker compose up --build
```

启动后访问：

- 前端：http://localhost:5173
- 后端 API：http://localhost:8080/api
- PostgreSQL：localhost:5432，数据库 `internship_tracker`
- Redis：localhost:6379

默认演示账号：

```text
用户名：demo
密码：password123
```

## 接入真实 AI

不要把真实 API Key 写进代码或提交到 Git。推荐在 PowerShell 里设置环境变量：

```powershell
$env:OPENAI_API_KEY="你的 OpenAI API Key"
$env:OPENAI_BASE_URL="https://api.openai.com/v1"
$env:OPENAI_MODEL="gpt-4.1-mini"
docker compose up -d --build backend
```

配置后，AI 分析页会显示 provider 为 `openai`。如果没有配置 `OPENAI_API_KEY`，系统会显示 provider 为 `mock`，仍然可以演示完整流程。

也可以复制 `.env.example` 为 `.env` 后填写变量，再运行：

```bash
docker compose up -d --build
```

## 核心接口

- `POST /api/auth/register`：注册
- `POST /api/auth/login`：登录
- `GET /api/profile`：当前用户资料
- `POST /api/resumes`：上传简历
- `GET /api/resumes`：简历列表
- `POST /api/jobs`：新增岗位，并自动创建投递记录
- `GET /api/jobs`：岗位看板数据
- `POST /api/jobs/import`：从 Remotive 公开 API 导入真实远程岗位，并做中文化展示
- `PATCH /api/applications/{id}/status`：更新投递状态
- `POST /api/ai/analyze-job-match`：岗位匹配分析
- `POST /api/ai/interview-questions`：面试题生成
- `POST /api/interviews`：新增面试日程
- `GET /api/interviews`：查询面试日程
- `GET /api/stats`：投递统计

## 简历亮点写法

- 使用 Spring Security + JWT 实现无状态认证，并通过 `user_id` 查询条件保证用户数据隔离。
- 使用 PostgreSQL `jsonb` 存储结构化简历和 AI 分析结果，Flyway 管理数据库迁移。
- 使用 Redis 缓存用户投递统计，并在岗位或状态变化后主动失效。
- AI 服务支持 OpenAI 真实调用和 mock 模式，使用结构化 JSON 输出岗位匹配和面试准备结果。
- 使用 Docker Compose 编排前端、后端、PostgreSQL、Redis，实现一键启动。

## 验收流程

1. 使用 `demo / password123` 登录。
2. 进入“岗位管理”，查看已导入的中文化真实岗位数据，或点击“导入示例岗位”继续导入。
3. 在看板中切换投递状态。
4. 进入“简历管理”，上传一份 PDF/Word 简历并填写技能、教育和项目经历。
5. 进入“AI 分析”，选择岗位后生成匹配分析或面试题。
6. 进入“面试日程”，添加面试安排并查看统计数据和转化率变化。
