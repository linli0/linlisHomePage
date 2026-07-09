# linlisHomePage (CoffeeCookies Homepage)

个人主页全栈应用：**Vue 3 + Python FastAPI** 轻量化架构。

> Java/Spring Boot 后端已归档至 `legacy/spring-boot/`，详见 [ROADMAP.md](ROADMAP.md) 与 [FastAPI 架构说明](doc/architecture/fastapi-overview.md)。

## 快速开始

```bash
# Docker（推荐）
./start.sh

# 本地开发（FastAPI :8000 + Vite :3000）
./start.sh dev

# API 测试（TDD）
cd api && pip install -r requirements.txt && PYTHONPATH=. pytest
```

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 (dev) / http://localhost (docker) |
| API | http://localhost:8000/api |
| OpenAPI | http://localhost:8000/docs |

默认账号：`admin` / `admin123`，`user` / `user123`

## Agent skills (Cursor)

This repo is configured with [agent-skills](https://github.com/addyosmani/agent-skills) using **Option 1: Rules Directory**.

- **Always loaded:** `.cursor/rules/test-driven-development.md`, `code-review-and-quality.md`, `incremental-implementation.md`
- **On demand:** `./scripts/agent-skills.sh add <skill>` / `remove <skill>`
- **Docs:** `.cursor/AGENT-SKILLS.md` and `.cursor/rules/README.md`
- **Code review agent:** `.cursor/agents/code-reviewer.md`

## 📚 完整文档

完整的项目文档现在位于 [`doc/`](doc/) 目录中，包含：

- **[架构概述](doc/architecture/overview.md)** - 系统架构和设计决策
- **[后端文档](doc/backend/)** - Spring Boot 后端实现细节
- **[前端文档](doc/frontend/)** - Vue 3 前端实现细节  
- **[设计系统](doc/design/)** - UI/UX 设计指南
- **[测试框架](doc/testing/)** - 测试策略和结果
- **[部署指南](doc/deployment/)** - 部署配置和生产设置

## 🚀 功能特性

### 💰 金价追踪模块
- 实时国际金价查询
- 支持多货币显示（USD、CNY、EUR、GBP）
- 价格走势图表（7天/30天/90天）
- 价格统计（最高/最低/平均/波动率）
- 自动刷新（每分钟）

### 📝 文章管理
- 文章发布与分类管理
- Markdown 内容支持
- 标签系统
- 文章浏览统计
- 文章搜索

### 🛠️ 实用工具箱
- JSON 格式化/压缩
- Base64 编码/解码
- URL 编码/解码
- 哈希计算（MD5、SHA1、SHA256、SHA512）
- 时间戳转换
- 二维码生成

### 🔐 用户系统
- JWT 认证
- 用户注册/登录
- 个人中心
- 角色管理（用户/管理员）

## 🛠️ 技术栈

### 后端
- **框架**: FastAPI + Uvicorn
- **语言**: Python 3.12
- **数据库**: SQLite（默认）
- **ORM**: SQLAlchemy 2.0
- **安全**: JWT + bcrypt
- **测试**: pytest（TDD，27 项用例）

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
./start.sh dev
```

- API: http://localhost:8000
- 前端: http://localhost:3000
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
- 后端 API: http://localhost:8080
- MySQL: localhost:3306

## 🔑 默认账号

- 管理员: `admin` / `admin123`
- 普通用户: `user` / `user123`

## 📚 API 文档

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息

### 金价相关
- `GET /api/gold-price/current?currency=USD` - 获取当前金价
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
