# CoffeeCookie'sHomePage - AI Agent 项目指南

> 本文件面向 AI 编程助手。如果你正在阅读此文件，说明你需要修改或维护本项目。请务必仔细阅读，避免对项目架构产生错误假设。

---

## 项目概述

CoffeeCookie'sHomePage 是一个全栈个人主页应用，整合了国际金价追踪、技术文章管理、实用工具箱和 AI 对话等功能。

**重要事实：** 本项目存在两套后端实现，但当前实际运行的是 **Node.js + Express** 版本（根目录 `server.js`），而非 `backend/` 目录下的 Spring Boot 应用。`backend/` 目录中的 Spring Boot 代码是并行存在的替代实现，Docker Compose 配置指向的是该 Spring Boot 后端。

项目由三个原始项目合并而来，原始代码备份存放在 `source-projects/` 目录下：
- `cooffeeCookiesHomePage` — 金价查询网页
- `miAi` — AI 相关功能
- `personal-homepage` — 个人主页前后端

---

## 技术栈

### 当前活跃架构（默认运行方式）
| 层级 | 技术 | 说明 |
|------|------|------|
| 后端 | Node.js 18+ + Express 4.x | 入口文件：`server.js`（端口 8080） |
| 认证 | jsonwebtoken + bcryptjs | JWT Bearer Token，有效期 24 小时 |
| 前端 | Vue 3.4+ + TypeScript | Composition API 风格 |
| 构建 | Vite 5.x | 开发端口 3000，构建输出到 `frontend/dist/` |
| 状态 | Pinia 2.x | 使用 Composition API 风格的 Store |
| 路由 | Vue Router 4.x | 历史模式路由 |
| 样式 | Tailwind CSS 3.x | 自定义 `primary` 和 `gold` 色板，支持 `darkMode: 'class'` |
| 图表 | Chart.js 4.x + vue-chartjs | 金价走势图 |
| HTTP | Axios 1.x | 拦截器自动注入 JWT |
| AI | Ollama | 本地 LLM 代理，默认地址 `http://localhost:11434` |

### 备用架构（`backend/` 目录）
| 层级 | 技术 | 说明 |
|------|------|------|
| 后端 | Spring Boot 3.2 + Java 17 | Maven 项目，`pom.xml` 管理依赖 |
| 数据库 | H2（开发）/ MySQL 8（生产）| JPA + Hibernate，`ddl-auto: update` |
| 安全 | Spring Security + JJWT 0.12.3 | JWT 过滤器链 |
| 构建 | Maven 3.8+ | `mvn clean package -DskipTests` |

---

## 项目结构

```
coffeeCookie'sHomePage/
├── server.js                    # 【当前活跃后端】Express 主文件
├── package.json                 # 后端（Express）依赖
├── package-lock.json
├── node_modules/                # Express 后端依赖
├── start.bat                    # Windows 启动脚本（Node.js 直接运行）
├── start.sh                     # Linux 启动脚本（Docker Compose）
├── docker-compose.yml           # Docker 编排（指向 Spring Boot + Nginx + MySQL）
├── .env.example                 # 环境变量模板
│
├── frontend/                    # Vue 3 前端源码
│   ├── package.json
│   ├── vite.config.ts           # Vite 配置，开发代理 /api -> localhost:8080
│   ├── tsconfig.json
│   ├── tailwind.config.js       # Tailwind + 自定义颜色
│   ├── postcss.config.js
│   ├── nginx.conf               # Docker 部署用的 Nginx 配置
│   ├── Dockerfile               # 前端 Docker 镜像（Nginx）
│   ├── index.html
│   └── src/
│       ├── main.ts              # 入口：createApp + Pinia + Router
│       ├── App.vue
│       ├── assets/main.css
│       ├── api/                 # API 接口封装（按模块分文件）
│       │   ├── auth.ts
│       │   ├── goldPrice.ts
│       │   ├── tools.ts
│       │   ├── article.ts
│       │   └── ai.ts
│       ├── components/          # 可复用组件
│       │   ├── NavigationBar.vue
│       │   ├── FooterBar.vue
│       │   ├── PriceChart.vue
│       │   └── AIChat.vue
│       ├── router/index.ts      # 路由表 + 导航守卫
│       ├── stores/auth.ts       # Pinia：认证状态（token 存 localStorage）
│       ├── utils/request.ts     # Axios 实例 + 拦截器
│       └── views/               # 页面级组件
│           ├── HomeView.vue
│           ├── GoldPriceView.vue
│           ├── ArticlesView.vue
│           ├── ArticleDetailView.vue
│           ├── ToolsView.vue
│           ├── LoginView.vue
│           ├── RegisterView.vue
│           └── ProfileView.vue
│
├── frontend-dist/               # 【生产部署目录】Express 静态文件服务指向此处
│   └── (构建后的 HTML/JS/CSS)
│
├── backend/                     # 【备用后端】Spring Boot 完整工程
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/coffeecookies/homepage/
│       │   ├── Application.java
│       │   ├── config/          # SecurityConfig, DataInitializer
│       │   ├── controller/      # REST API 控制器
│       │   ├── service/         # 业务逻辑
│       │   ├── repository/      # JPA Repository
│       │   ├── entity/          # JPA 实体
│       │   ├── dto/             # 数据传输对象
│       │   └── security/        # JWT 工具、过滤器、UserDetails
│       ├── main/resources/
│       │   ├── application.yml      # H2 开发配置
│       │   └── application-prod.yml # 生产配置
│       └── test/java/           # 测试目录（当前为空）
│
├── source-projects/             # 原始项目备份
│   ├── cooffeeCookiesHomePage/
│   ├── miAi/
│   └── personal-homepage/
│
├── README.md                    # 面向人类用户的项目说明
├── PROJECT_DOCUMENTATION.md     # 详细技术文档
├── DEPLOY_GUIDE.md              # 公网部署指南
├── HISTORY.md                   # 开发历史记录
└── TASK_STATUS.md               # 任务进度记录
```

---

## 构建与运行命令

### 方式一：Node.js 直接运行（当前实际使用）

```bash
# 1. 安装后端依赖
cd "D:\AI\coffeeCookie'sHomePage"
npm install

# 2. 安装前端依赖并构建
cd frontend
npm install
npm run build
cd ..

# 3. 将构建产物复制到部署目录
# Windows
xcopy /E /I /Y frontend\dist frontend-dist
# PowerShell
Copy-Item -Path "frontend\dist\*" -Destination "frontend-dist" -Recurse -Force

# 4. 启动服务
node server.js
```

或使用一键脚本（Windows）：
```bash
start.bat
```

服务启动后：
- 本地访问：http://localhost:8080
- API 地址：http://localhost:8080/api
- 开发前端（如需独立运行）：http://localhost:3000

### 方式二：开发模式（热更新）

需要同时启动两个进程：

```bash
# 终端 1：Express 后端
cd "D:\AI\coffeeCookie'sHomePage"
npm run dev        # 使用 nodemon 热重载

# 终端 2：Vue 前端
cd "D:\AI\coffeeCookie'sHomePage\frontend"
npm run dev        # Vite 开发服务器，端口 3000，代理 /api 到 8080
```

### 方式三：Docker Compose（指向 Spring Boot 后端）

```bash
# Linux / macOS / WSL
./start.sh

# 或手动
docker-compose up -d
```

此方式会启动三个容器：
- `coffee-homepage-backend`：Spring Boot（端口 8080）
- `coffee-homepage-frontend`：Nginx（端口 80）
- `coffee-homepage-mysql`：MySQL 8（端口 3306）

---

## 代码组织规范

### 前端（Vue 3）
- **API 层**：所有 HTTP 请求封装在 `frontend/src/api/` 下，按业务模块分文件。使用 `frontend/src/utils/request.ts` 中的 Axios 实例。
- **状态管理**：使用 Pinia + Composition API（`stores/auth.ts` 为示例）。Token 持久化到 `localStorage`，键名为 `token`。
- **路由守卫**：`router/index.ts` 中实现了 `requiresAuth` 和 `guest` 元字段的导航守卫。
- **路径别名**：`@/` 映射到 `src/`（在 `vite.config.ts` 和 `tsconfig.json` 中同时配置）。
- **样式**：Tailwind CSS 为主，自定义了 `gold-*` 和 `primary-*` 颜色。支持通过 `dark` class 切换暗色模式。

### 后端（Express - 当前活跃）
- `server.js` 是单体文件，所有路由、中间件、模拟数据都在其中。
- 金价数据为内存模拟，每分钟通过 `setInterval` 自动更新。
- AI 功能通过 `fetch` 代理到本地 Ollama 服务（`/api/ai/*`）。
- 静态文件服务指向 `frontend-dist/` 目录，所有未知路由回退到 `index.html`（SPA 支持）。

### 后端（Spring Boot - 备用）
- 标准分层架构：Controller → Service → Repository → Entity。
- 使用 Lombok（`@RequiredArgsConstructor`、`@Slf4j` 等）。
- 统一返回格式：`Result<T>` 包装成功/失败响应。
- JWT 过滤器链：`AuthTokenFilter` → `UsernamePasswordAuthenticationFilter`。

---

## API 端点说明

### 认证相关
| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册（Spring Boot 版本）|
| GET | `/api/auth/me` | 获取当前用户信息 |

### 金价相关
| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/gold-price/current?currency=USD` | 获取当前金价 |
| GET | `/api/gold-price/history?currency=USD&days=7` | 获取历史价格 |
| GET | `/api/gold-price/currencies` | 获取支持的货币列表 |

### 工具相关
| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/tools/json/format` | JSON 格式化 |
| POST | `/api/tools/base64/encode` | Base64 编码 |
| POST | `/api/tools/base64/decode` | Base64 解码 |
| POST | `/api/tools/url/encode` | URL 编码 |
| POST | `/api/tools/url/decode` | URL 解码 |
| POST | `/api/tools/hash/md5` | MD5 哈希 |
| POST | `/api/tools/hash/sha256` | SHA256 哈希 |

### AI 相关（Ollama 代理）
| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/ai/models` | 获取可用模型列表 |
| GET | `/api/ai/status` | 检查 Ollama 服务状态 |
| POST | `/api/ai/chat` | 与 AI 对话（支持流式响应）|

### 文章相关（Spring Boot 版本）
| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/articles/public/list` | 获取文章列表 |
| GET | `/api/articles/public/{id}` | 获取文章详情 |
| GET | `/api/articles/public/recent` | 获取最新文章 |
| GET | `/api/articles/public/popular` | 获取热门文章 |

---

## 测试策略

**当前状态：项目中没有实际编写的测试用例。**

- `backend/src/test/java/` 目录结构存在，但无测试文件。
- 前端未配置 Vitest / Jest / Cypress 等测试框架。
- 若需添加测试，建议：
  - 后端：使用 `spring-boot-starter-test`（已引入依赖）编写单元测试和集成测试。
  - 前端：引入 Vitest + Vue Test Utils 进行组件和 Store 测试。

---

## 安全注意事项

1. **默认硬编码凭据**
   - 管理员：`admin` / `admin123`
   - 普通用户：`user` / `user123`
   - 生产环境必须修改或禁用默认账号。

2. **JWT 密钥**
   - Express 默认值：`coffee-cookies-secret-key-2024`
   - Spring Boot 默认值：`bXlTdXBlclNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbjEyMzQ1Njc4OQ==`
   - 生产环境务必通过环境变量 `JWT_SECRET` 覆盖。

3. **CORS 配置**
   - Express 中使用了 `app.use(cors())`（允许所有来源）。
   - Spring Boot 中 `@CrossOrigin(origins = "*")` 广泛存在。
   - 生产环境应限制为实际域名。

4. **H2 控制台**
   - Spring Boot 的 `application.yml` 中启用了 H2 Console（`/h2-console`）且 `web-allow-others: true`。
   - 生产环境（`application-prod.yml`）应禁用此功能。

5. **Ollama 代理**
   - `server.js` 将客户端请求直接代理到 `localhost:11434`。
   - 确保 Ollama 服务仅在本地监听，避免未授权访问。

---

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `PORT` | `8080` | Express 服务端口 |
| `JWT_SECRET` | 见上文 | JWT 签名密钥 |
| `OLLAMA_URL` | `http://localhost:11434` | Ollama 服务地址 |
| `NODE_ENV` | `development` | Node.js 运行环境 |

Docker Compose 额外使用：
- `MYSQL_ROOT_PASSWORD`、`MYSQL_USER`、`MYSQL_PASSWORD`、`MYSQL_DB`
- `SPRING_PROFILES_ACTIVE=prod`

---

## 开发工作流提示

1. **修改 Express 后端**：编辑 `server.js` 后必须重启服务（`node server.js` 或 `npm run dev`）。
2. **修改 Vue 前端**：
   - 开发时运行 `npm run dev`（热更新）。
   - 部署前必须执行 `npm run build` 并将 `frontend/dist/` 内容同步到 `frontend-dist/`。
3. **修改 Spring Boot 后端**：编辑 `backend/` 下文件后，需重新编译或重启 Spring Boot 应用。当前默认运行方式不会自动加载这些变更。
4. **公网访问**：当前使用内网穿透工具（Tunnelmole / LocalTunnel）。每次启动域名可能变化，相关信息记录在 `DEPLOY_GUIDE.md` 中。

---

## 常见陷阱

- **不要假设 Spring Boot 是当前运行后端**。默认启动 `server.js` 时，所有 `/api/*` 请求由 Express 处理。
- **前端构建产物必须同步到 `frontend-dist/`**。Express 不直接读取 `frontend/dist/`。
- **Docker Compose 与当前活跃架构不一致**。`docker-compose.yml` 构建的是 Spring Boot + Nginx 组合，与 `server.js` 无关。
- **没有真实金价 API**。金价数据是内存模拟的随机数，接入真实 API 在待办列表中。
