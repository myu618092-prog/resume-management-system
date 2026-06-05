# AI 实习申请管理系统

一个面向大学生实习求职场景的全栈项目，用来管理简历、岗位、投递进度、面试日程，并通过 AI 生成岗位匹配分析和面试准备建议。

项目重点不是训练 AI 模型，而是展示完整的后端开发、数据库设计、前端管理台、权限控制、第三方 API 接入和 Docker 部署能力。

## 项目功能

- 用户注册、登录和 JWT 鉴权
- 个人资料管理，支持学校、专业、技能栈等信息
- 简历上传和结构化信息保存
- 岗位新增、岗位列表和投递状态看板
- 投递状态流转：待投递、已投递、笔试、面试、Offer、拒绝
- 从公开岗位 API 导入真实岗位数据，并做中文化展示
- 面试日程创建和日期范围查询
- 投递统计：总投递数、各状态数量、面试转化率、公司分布
- AI 岗位匹配分析
- AI 面试题和准备清单生成
- Docker Compose 一键启动前端、后端、PostgreSQL、Redis

## 技术栈

### 后端

- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- Flyway
- PostgreSQL
- Redis
- OpenAI API / OpenAI 兼容 API

### 前端

- Vue 3
- Vite
- lucide-vue-next
- 原生 Fetch API

### 部署

- Docker
- Docker Compose
- Nginx 前端静态资源服务

## 系统模块

登录后系统分为 5 个主要页面：

- 总览：展示投递统计、最近岗位和最近面试
- 岗位管理：新增岗位、导入示例岗位、切换投递状态
- 简历管理：上传简历，维护技能、教育经历和项目经历
- 面试日程：创建和查看笔试、面试安排
- AI 分析：根据岗位 JD 和候选人背景生成分析结果

## AI 能力说明

系统支持两种 AI 模式：

- `mock`：未配置 API Key 时自动使用，方便本地演示
- `openai`：配置 `OPENAI_API_KEY` 后调用真实 OpenAI 接口

AI 分析结果使用结构化 JSON 返回，前端会分区展示：

- 岗位匹配分
- 匹配优势
- 短板风险
- 简历优化建议
- 7 天行动计划
- 技术面试题
- 行为面试题
- 项目深挖问题
- 面试准备清单

默认推荐模型：

```text
gpt-5.5
```

## 数据库设计亮点

项目使用 PostgreSQL 作为主数据库，核心表包括：

- `users`
- `resumes`
- `jobs`
- `applications`
- `interview_schedules`
- `ai_analysis_records`

数据库相关设计：

- 使用 Flyway 管理数据库迁移
- 使用 `jsonb` 保存结构化简历内容和 AI 分析结果
- 岗位和投递数据按 `user_id` 隔离
- 对岗位、状态、创建时间等字段建立索引
- 使用 Redis 缓存统计数据，并在岗位或投递状态变化后主动失效

## 快速启动

确保本机已经安装 Docker Desktop，然后在项目根目录执行：

```powershell
docker compose up -d --build
```

启动完成后访问：

```text
前端页面：http://localhost:5173
后端接口：http://localhost:8080/api
PostgreSQL：localhost:5432
Redis：localhost:6379
```

默认演示账号：

```text
用户名：demo
密码：password123
```

## 启用真实 AI

不要把真实 API Key 写进代码。

PowerShell 设置方式：

```powershell
$env:OPENAI_API_KEY="你的 OpenAI API Key"
$env:OPENAI_BASE_URL="https://api.openai.com/v1"
$env:OPENAI_MODEL="gpt-4.1-mini"
docker compose up -d --build backend
```

也可以复制 `.env.example` 为 `.env`，然后填写：

```env
OPENAI_API_KEY=
OPENAI_BASE_URL=https://api.openai.com/v1
OPENAI_MODEL=gpt-5.5
```

配置成功后，AI 分析页会显示：

```text
当前 AI 来源：openai
```

如果没有配置 Key，会显示：

```text
当前 AI 来源：mock
```

## 常用命令

启动项目：

```powershell
docker compose up -d --build
```

查看容器状态：

```powershell
docker compose ps
```

停止项目：

```powershell
docker compose down
```

只重启后端：

```powershell
docker compose up -d --build backend
```

查看后端日志：

```powershell
docker compose logs -f backend
```

## 核心接口

### 认证

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/profile
```

### 简历

```text
POST /api/resumes
GET  /api/resumes
```

### 岗位和投递

```text
POST  /api/jobs
GET   /api/jobs
POST  /api/jobs/import
PATCH /api/applications/{id}/status
```

### AI

```text
POST /api/ai/analyze-job-match
POST /api/ai/interview-questions
```

### 面试和统计

```text
POST /api/interviews
GET  /api/interviews
GET  /api/stats
```

## 项目验收流程

1. 使用 `demo / password123` 登录系统。
2. 进入“岗位管理”，点击“导入示例岗位”导入真实岗位数据。
3. 在岗位看板中切换投递状态。
4. 进入“简历管理”，上传简历并填写技能、教育和项目经历。
5. 进入“AI 分析”，选择岗位后生成岗位匹配分析。
6. 点击“生成面试题”，查看技术题、行为题、项目深挖和准备清单。
7. 进入“面试日程”，添加面试安排。
8. 回到“总览”，查看投递统计和最近记录。

## 适合写进简历的亮点

- 使用 Spring Security + JWT 实现登录鉴权，并通过用户维度的数据查询保证数据隔离。
- 使用 PostgreSQL 设计用户、岗位、投递、简历、面试和 AI 分析表，结合 Flyway 管理数据库迁移。
- 使用 PostgreSQL `jsonb` 存储结构化简历和 AI 分析结果，兼顾灵活性和可查询性。
- 使用 Redis 缓存投递统计数据，并在业务数据变化时主动刷新缓存。
- 接入 OpenAI 兼容接口，实现岗位匹配分析和面试题生成，同时保留 mock 模式方便演示。
- 使用 Docker Compose 编排前端、后端、数据库和缓存，实现一键启动和部署。

## 目录结构

```text
.
├── backend                 # Spring Boot 后端
│   ├── src/main/java       # 后端业务代码
│   ├── src/main/resources  # 配置和 Flyway 迁移
│   └── src/test/java       # 后端测试
├── frontend                # Vue 3 前端
│   ├── src                 # 前端源码
│   └── nginx.conf          # 前端容器 Nginx 配置
├── docker-compose.yml      # 一键启动配置
├── .env.example            # 环境变量示例
└── README.md
```

## 注意事项

- 不要提交 `.env` 或真实 API Key。
- 第一次启动需要拉取 Docker 镜像，可能会比较慢。
- 如果岗位导入失败，通常是网络无法访问公开岗位 API，可以继续使用手动新增岗位。
- 如果 AI 来源显示为 `mock`，说明当前没有配置 `OPENAI_API_KEY`。
