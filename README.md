# CoffeeCookies Homepage / linlisHomePage

基于 Spring Boot 3.2 + Vue 3 的全栈个人主页。当前主线采用 **Core + Plugins** 架构：默认只启用认证、金价、文章、工具箱；AI / 推文 / 量化 / 小米音箱按需开启。

## 文档入口

- [ROADMAP.md](ROADMAP.md) — 开发计划
- [ARCHITECTURE_OPTIMIZATION.md](ARCHITECTURE_OPTIMIZATION.md) — 架构优化方案与落地状态
- [`doc/`](doc/) — 架构、前后端、测试、部署说明

## 技术栈

- **后端**: Java 17, Spring Boot 3.2, JPA, Security/JWT, H2/MySQL
- **前端**: Vue 3, TypeScript, Vite, Pinia, Tailwind CSS
- **部署**: 推荐单容器 Spring Boot；可选 Docker Compose + MySQL

## 快速开始

### 环境要求

- JDK 17+
- Node.js 20+
- Maven 3.8+
- Docker（可选）

### 本地开发

```bash
# 后端
cd backend
mvn spring-boot:run

# 前端（另开终端）
cd frontend
npm install
npm run dev
```

- 前端: http://localhost:3000
- 后端: http://localhost:8080

### 推荐部署：单容器

```bash
cd frontend && npm ci && npm run build
mkdir -p ../backend/src/main/resources/static
cp -r dist/* ../backend/src/main/resources/static/
cd ../backend && mvn -DskipTests package
java -jar target/*.jar
```

或使用精简 Compose：

```bash
cp .env.example .env
docker compose -f docker-compose.yml up -d --build
```

访问 http://localhost:8080

### 扩展模块开关

默认全部关闭。在 `.env` 或环境变量中开启：

```bash
FEATURES_AI_ENABLED=true
FEATURES_TWEETS_ENABLED=true
FEATURES_QUANT_ENABLED=true
FEATURES_XIAOMI_ENABLED=true   # 依赖 AI 已启用
```

## 默认账号

- 管理员: `admin` / `admin123`
- 普通用户: `user` / `user123`

生产环境请立即修改密码，并通过 `JWT_SECRET` / 数据库凭据注入密钥。

## 核心功能

- 金价追踪、文章管理、实用工具箱、JWT 用户系统
- 可选：AI 对话、社交推文、量化交易、小米音箱

## 开发计划摘要

详见 [ROADMAP.md](ROADMAP.md)。近期重点：

1. 验证后端/前端测试与单容器部署
2. 稳定 Core MVP
3. 按需开启扩展模块并补齐搜索 / 主题等产品功能
