# linlisHomePage (CoffeeCookies Homepage)

个人主页全栈应用：**Vue 3 + Python FastAPI** 轻量化架构。

> Java/Spring Boot 后端已归档至 `legacy/spring-boot/`，详见 [ROADMAP.md](ROADMAP.md) 与 [FastAPI 架构说明](doc/architecture/fastapi-overview.md)。

## 快速开始

```bash
# Windows 一键本地开发
start.bat

# Unix / macOS 本地开发（FastAPI :8000 + Vite :3000）
./start.sh dev

# Docker（api + nginx frontend，SQLite）
docker compose up -d
# 或: ./start.sh

# API 测试
cd api && .venv/Scripts/python.exe -m pytest   # Windows venv
# 或: pip install -r requirements.txt && PYTHONPATH=. pytest
```

公网 Tunnel（可选）：复制 `.skills/load-service/service-config.example.yml` → `.config/service-config.yml`，再运行 `.\.skills\load-service\load-service.ps1`。

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 (dev) / http://localhost (docker) |
| API | http://localhost:8000/api |
| OpenAPI | http://localhost:8000/docs |

默认账号：`admin` / `admin123`（密码-only 登录；**不开放注册**）

## Agent skills (Cursor)

This repo is configured with [agent-skills](https://github.com/addyosmani/agent-skills) using **Option 1: Rules Directory**.

- **Always loaded:** `.cursor/rules/test-driven-development.md`, `code-review-and-quality.md`, `incremental-implementation.md`
- **On demand:** `./scripts/agent-skills.sh add <skill>` / `remove <skill>`
- **Docs:** `.cursor/AGENT-SKILLS.md` and `.cursor/rules/README.md`
- **Code review agent:** `.cursor/agents/code-reviewer.md`

## 📚 完整文档

完整的项目文档现在位于 [`doc/`](doc/) 目录中，包含：

- **[架构概述](doc/architecture/overview.md)** - 系统架构和设计决策
- **[FastAPI 架构](doc/architecture/fastapi-overview.md)** - 当前后端架构
- **[前端文档](doc/frontend/)** - Vue 3 前端实现细节  
- **[路线图](ROADMAP.md)** - 迁移进度与下一步
- 根目录 [AGENTS.md](AGENTS.md) - Agent 工作约定（以 FastAPI 为准）
- **[设计系统](doc/design/)** - UI/UX 设计指南
- **[测试框架](doc/testing/)** - 测试策略和结果
- **[部署指南](doc/deployment/)** - 部署配置和生产设置

## 功能特性

### 国内金价
- 黄金9999 参考价（元/克，新浪行情）
- 手动刷新 + 服务端定时同步；失败时保留上次成功价并提示
- 多货币换算与走势图（7 / 30 / 90 天）

### Wiki / 文章
- 公开已发布文章列表与详情
- Markdown、分类/标签、阅读统计
- 管理端需登录（管理员）

### 工具箱
- JSON、Base64、URL、哈希、时间戳、二维码

### AI 对话
- 本地 Ollama 流式对话（默认；DeepSeek 仅作回退/显式选择）

### 用户系统
- JWT + 密码-only 登录（私人站点，关闭注册）

### 扩展（可选）
- 小米音箱 / 量化插件：环境变量启停（见 `api/.env.example`）

## 🛠️ 技术栈

### 后端
- **框架**: FastAPI + Uvicorn
- **语言**: Python 3.12
- **数据库**: SQLite（默认）
- **ORM**: SQLAlchemy 2.0
- **安全**: JWT + bcrypt
- **测试**: pytest（`api/tests`）

### 前端
- **框架**: Vue 3 + Composition API
- **语言**: TypeScript
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI 框架**: Tailwind CSS
- **图表**: Chart.js

### 部署
- **容器化**: Docker + Docker Compose
- **Web 服务器**: Nginx

## 📁 项目结构

```
linlisHomePage/
├── api/                    # FastAPI 后端
│   ├── app/                # 应用代码
│   ├── tests/              # pytest TDD 测试
│   └── Dockerfile
├── frontend/               # Vue 3 前端
├── legacy/spring-boot/     # 已归档 Java 后端
├── docker-compose.yml
└── README.md
```

## 🚀 快速开始

### 环境要求
- Python 3.12+
- Node.js 20+
- Docker & Docker Compose (可选)

### 本地开发

```bash
# Windows
start.bat

# Unix
./start.sh dev
```

- API: http://localhost:8000
- 前端: http://localhost:3000（`/api` 代理到 8000）
- API 文档: http://localhost:8000/docs

### 运行测试

```bash
cd api && pip install -r requirements.txt && PYTHONPATH=. pytest
```

### Docker 部署

#### 1. 构建并启动所有服务
```bash
docker-compose up -d
```

#### 2. 查看服务状态
```bash
docker-compose ps
```

#### 3. 停止服务
```bash
docker-compose down
```

部署后访问：
- 前端: http://localhost
- 后端 API: http://localhost:8000/api

## 🔑 默认账号

- 管理员: `admin` / `admin123`
- 普通用户: `user` / `user123`

## 📚 API 文档

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息

### 金价相关
- `GET /api/gold-price/current?currency=CNY` - 获取当前金价（默认 CNY / AU9999）
- `GET /api/gold-price/history?currency=USD&days=30` - 获取历史价格
- `GET /api/gold-price/currencies` - 获取支持的货币列表

### 文章相关
- `GET /api/articles/public/list` - 获取文章列表
- `GET /api/articles/public/{id}` - 获取文章详情
- `GET /api/articles/public/recent` - 获取最新文章
- `GET /api/articles/public/popular` - 获取热门文章

### 工具相关
- `POST /api/tools/json/format` - JSON 格式化
- `POST /api/tools/base64/encode` - Base64 编码
- `POST /api/tools/hash/md5` - MD5 哈希
- `POST /api/tools/qrcode/generate` - 生成二维码

## 🔧 配置说明

### 后端配置
编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/homepage
    username: your_username
    password: your_password

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours
```

### 前端配置
编辑 `frontend/vite.config.ts`：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
      changeOrigin: true,
    },
  },
}
```

## 📝 开发计划

- [x] 基础项目架构
- [x] 用户认证系统
- [x] 金价追踪模块
- [x] 文章管理系统
- [x] 实用工具箱
- [x] Docker 部署
- [ ] 文件上传功能
- [ ] 评论系统
- [ ] 站内搜索
- [ ] 主题切换

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Chart.js](https://www.chartjs.org/)

---

Made with ☕ by CoffeeCookies
