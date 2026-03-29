# CoffeeCookie'sHomePage - 系统设计文档

> 本文档描述 CoffeeCookie'sHomePage 项目的整体架构、模块设计、接口规范及部署方案。

---

## 1. 项目概述

### 1.1 背景

CoffeeCookie'sHomePage 是一个全栈个人主页应用，由三个原始项目整合而来：

| 原始项目 | 功能 | 备份位置 |
|---------|------|---------|
| `cooffeeCookiesHomePage` | 国际金价查询网页 | `source-projects/cooffeeCookiesHomePage/` |
| `miAi` | AI 对话相关功能 | `source-projects/miAi/` |
| `personal-homepage` | 个人主页前后端 | `source-projects/personal-homepage/` |

### 1.2 设计目标

- **功能整合**：将金价查询、工具箱、文章管理、AI 对话整合到统一平台
- **双后端兼容**：同时维护 Express (Node.js) 和 Spring Boot (Java) 两套后端，以 Express 为当前活跃版本
- **单页应用 (SPA)**：Vue 3 前端，支持历史模式路由
- **快速部署**：支持 Node.js 直接运行和 Docker Compose 两种部署方式

### 1.3 功能模块

```
┌─────────────────────────────────────────────────────────────┐
│                    CoffeeCookie'sHomePage                    │
├─────────────┬─────────────┬─────────────┬───────────────────┤
│  金价追踪    │  实用工具箱  │  文章管理    │    AI 对话        │
├─────────────┼─────────────┼─────────────┼───────────────────┤
│ • 实时金价   │ • JSON 格式化│ • 文章列表   │ • 模型列表        │
│ • 多货币    │ • Base64    │ • 文章详情   │ • 流式对话        │
│ • 走势图    │ • URL 编解码 │ • 分类/标签  │ • Ollama 代理     │
│ • 统计指标   │ • 哈希计算   │ • 浏览统计   │                   │
└─────────────┴─────────────┴─────────────┴───────────────────┘
```

---

## 2. 架构设计

### 2.1 整体架构

项目采用 **前后端分离** 架构，存在 **两套后端实现**：

#### 架构 A：Express + Vue（当前活跃部署方式）

```
┌─────────────┐      ┌─────────────────┐      ┌──────────────┐
│   浏览器     │ ──▶  │  Express 8080   │ ──▶  │  Ollama      │
│  (Vue SPA)  │ ◀──  │  (server.js)    │ ◀──  │  :11434      │
└─────────────┘      └─────────────────┘      └──────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │ frontend-dist│
                     │ (静态文件)    │
                     └──────────────┘
```

#### 架构 B：Spring Boot + Nginx + MySQL（Docker Compose）

```
┌─────────────┐      ┌─────────────┐      ┌─────────────────┐
│   浏览器     │ ──▶  │   Nginx 80  │ ──▶  │ Spring Boot     │
│  (Vue SPA)  │ ◀──  │ (frontend)  │ ◀──  │ 8080 (backend)  │
└─────────────┘      └─────────────┘      └─────────────────┘
                                                 │
                                                 ▼
                                          ┌──────────────┐
                                          │  MySQL 3306  │
                                          └──────────────┘
```

### 2.2 架构决策说明

| 决策 | 说明 |
|-----|------|
| 双后端并存 | Spring Boot 后端已完成完整业务逻辑（文章、用户、分类等），但当前实际运行的是 Express 后端（快速迭代、简化部署） |
| 静态文件分离 | Express 服务 `frontend-dist/`，而非直接读取 `frontend/dist/`，避免构建时污染源码目录 |
| Ollama 本地代理 | AI 功能通过 Express 反向代理到本地 Ollama，避免前端直接暴露 Ollama 端口 |
| 内存模拟金价 | 当前金价为随机模拟数据，待接入真实 API（如 GoldAPI） |

---

## 3. 技术栈

### 3.1 活跃架构（Express + Vue）

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 运行时 | Node.js | 18+ | Express 后端运行时 |
| Web 框架 | Express | 4.x | REST API 服务 |
| 认证 | jsonwebtoken + bcryptjs | 9.x / 2.x | JWT 签发与密码哈希 |
| 前端框架 | Vue | 3.4+ | 组件化 UI |
| 语言 | TypeScript | 5.3+ | 类型安全 |
| 构建工具 | Vite | 5.x | 开发与生产构建 |
| 路由 | Vue Router | 4.x | SPA 历史模式路由 |
| 状态管理 | Pinia | 2.x | Composition API Store |
| 样式 | Tailwind CSS | 3.x | 原子化 CSS |
| 图表 | Chart.js + vue-chartjs | 4.x / 5.x | 金价走势图 |
| HTTP 客户端 | Axios | 1.x | API 请求封装 |
| AI 代理 | Ollama | - | 本地 LLM 推理 |

### 3.2 备用架构（Spring Boot）

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 框架 | Spring Boot | 3.2 | Java 后端框架 |
| 语言 | Java | 17 | 后端开发语言 |
| ORM | Spring Data JPA | - | 数据库访问 |
| 数据库 | H2 (开发) / MySQL 8 (生产) | - | 数据持久化 |
| 安全 | Spring Security + JJWT | 0.12.3 | JWT 认证链 |
| 构建 | Maven | 3.8+ | 依赖与打包 |

---

## 4. 项目结构

```
coffeeCookie'sHomePage/
├── server.js                    # 【活跃后端】Express 主入口
├── package.json                 # Express 依赖
├── package-lock.json
├── node_modules/
├── .env.example                 # 环境变量模板
├── start.bat                    # Windows 一键启动
├── start.sh                     # Linux Docker 启动
├── docker-compose.yml           # Docker 编排（指向 Spring Boot）
│
├── frontend/                    # Vue 3 前端源码
│   ├── package.json
│   ├── vite.config.ts           # Vite 配置（代理 /api -> 8080）
│   ├── tsconfig.json
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   ├── nginx.conf               # Docker 部署 Nginx 配置
│   ├── Dockerfile
│   ├── index.html
│   └── src/
│       ├── main.ts              # Vue 应用入口
│       ├── App.vue
│       ├── assets/main.css
│       ├── api/                 # 按模块封装的 API
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
│       ├── stores/auth.ts       # Pinia 认证状态
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
├── frontend-dist/               # 【生产部署目录】Express 静态服务
│   └── (构建产物)
│
├── backend/                     # 【备用后端】Spring Boot 工程
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/coffeecookies/homepage/
│       ├── Application.java
│       ├── config/              # SecurityConfig, DataInitializer
│       ├── controller/          # REST 控制器
│       ├── service/             # 业务逻辑
│       ├── repository/          # JPA Repository
│       ├── entity/              # JPA 实体
│       ├── dto/                 # 数据传输对象
│       └── security/            # JWT 工具、过滤器、UserDetails
│
└── source-projects/             # 原始项目备份
    ├── cooffeeCookiesHomePage/
    ├── miAi/
    └── personal-homepage/
```

---

## 5. 核心模块设计

### 5.1 认证模块

#### 5.1.1 设计要点

- **Token 机制**：JWT Bearer Token，有效期 24 小时
- **存储位置**：`localStorage`，键名 `token`
- **密码策略**：Express 版本当前为硬编码明文比对（`admin/admin123`、`user/user123`）；Spring Boot 版本使用 bcrypt 哈希
- **角色模型**：`ADMIN` / `USER`

#### 5.1.2 认证流程

```
┌─────────┐    POST /api/auth/login    ┌─────────┐
│  前端    │ ─────────────────────────▶ │  后端    │
│         │  { username, password }    │         │
│         │ ◀───────────────────────── │         │
│         │    { token, userInfo }     │         │
└─────────┘                            └─────────┘
     │
     ▼ 存储 token 到 localStorage
┌─────────┐
│ 后续请求 │ ──▶ Axios 拦截器自动注入 Authorization: Bearer <token>
└─────────┘
```

#### 5.1.3 路由守卫

```typescript
// router/index.ts
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.guest && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})
```

| 路由 | 元字段 | 说明 |
|------|--------|------|
| `/profile` | `requiresAuth: true` | 需登录 |
| `/login`, `/register` | `guest: true` | 已登录用户不可访问 |

---

### 5.2 金价模块

#### 5.2.1 数据模型

```typescript
interface GoldPrice {
  price: number           // 当前价格
  changeAmount: number    // 涨跌额
  changePercent: number   // 涨跌幅 (%)
  currency: string        // 货币代码
  symbol: string          // 货币符号
  timestamp: string       // ISO 时间戳
  high: number            // 30 日最高
  low: number             // 30 日最低
  average: number         // 30 日平均
  volatility: number      // 波动率
}
```

#### 5.2.2 支持货币

| 代码 | 名称 | 符号 | 汇率基准 |
|------|------|------|---------|
| USD | 美元 | $ | 1.0 |
| CNY | 人民币 | ¥ | 对 USD 汇率 |
| EUR | 欧元 | € | 对 USD 汇率 |
| GBP | 英镑 | £ | 对 USD 汇率 |

#### 5.2.3 数据更新机制

Express 后端通过 `setInterval` 每分钟随机波动金价：

```javascript
setInterval(() => {
  Object.keys(goldPrice).forEach(currency => {
    const variation = (Math.random() - 0.5) * 2;
    goldPrice[currency].price += variation;
    // ...
  });
}, 60000);
```

> ⚠️ 当前为模拟数据，真实接入方案待实现（GoldAPI / 雅虎财经等）。

---

### 5.3 工具箱模块

#### 5.3.1 工具分类

| 分类 | 端点 | 功能 |
|------|------|------|
| JSON | `POST /api/tools/json/format` | 格式化 JSON |
| Base64 | `POST /api/tools/base64/encode` | Base64 编码 |
| Base64 | `POST /api/tools/base64/decode` | Base64 解码 |
| URL | `POST /api/tools/url/encode` | URL 编码 |
| URL | `POST /api/tools/url/decode` | URL 解码 |
| Hash | `POST /api/tools/hash/md5` | MD5 哈希 |
| Hash | `POST /api/tools/hash/sha256` | SHA256 哈希 |

> 前端 `tools.ts` 中定义了更多接口（`sha1`、`sha512`、`timestamp`、`qrcode`），但 Express 后端当前仅实现了部分端点。

---

### 5.4 AI 对话模块

#### 5.4.1 架构设计

Express 作为 **Ollama 代理层**，前端不直接访问 `localhost:11434`：

```
前端 ──▶ Express /api/ai/* ──▶ Ollama localhost:11434
```

#### 5.4.2 核心端点

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/ai/models` | 获取本地模型列表 |
| GET | `/api/ai/status` | 检查 Ollama 连接状态 |
| POST | `/api/ai/chat` | 对话（支持流式/非流式）|

#### 5.4.3 流式响应处理

Express 将 Ollama 的流式响应直接透传给客户端：

```javascript
res.setHeader('Content-Type', 'text/plain; charset=utf-8');
res.setHeader('Transfer-Encoding', 'chunked');
const reader = response.body.getReader();
while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  res.write(value);
}
```

前端使用 `fetch` + `ReadableStream` 解析 NDJSON 流：

```typescript
const reader = response.body?.getReader();
const decoder = new TextDecoder();
// 逐行解析 JSON，提取 response 字段
```

---

### 5.5 文章模块

#### 5.5.1 当前状态

- **Spring Boot 后端**：已实现完整的 CRUD、分类、标签、浏览统计
- **Express 后端**：未实现文章相关 API
- **前端**：已封装 `articleApi`，页面组件 `ArticlesView.vue`、`ArticleDetailView.vue` 已存在，但依赖后端数据

#### 5.5.2 数据模型

```typescript
interface Article {
  id: number
  title: string
  content: string
  summary: string
  coverImage: string
  published: boolean
  viewCount: number
  createdAt: string
  updatedAt: string
  category?: Category
  tags?: Tag[]
  author?: User
}
```

#### 5.5.3 公开 API

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/articles/public/list?page=&size=` | 分页列表 |
| GET | `/api/articles/public/{id}` | 文章详情 |
| GET | `/api/articles/public/recent` | 最新文章 |
| GET | `/api/articles/public/popular` | 热门文章 |

---

## 6. API 设计规范

### 6.1 基础信息

- **Base URL**：`http://localhost:8080/api`
- **Content-Type**：`application/json`
- **认证头**：`Authorization: Bearer <token>`

### 6.2 响应格式

#### Express 版本（当前）

直接返回数据对象或错误对象：

```json
// 成功
{ "price": 2045.50, "currency": "USD", ... }

// 失败
{ "error": "Invalid credentials" }
```

#### Spring Boot 版本

统一使用 `Result<T>` 包装：

```json
// 成功
{
  "code": 200,
  "message": "success",
  "data": { ... }
}

// 失败
{
  "code": 401,
  "message": "Unauthorized",
  "data": null
}
```

> 前端 `auth.ts` 中按 Spring Boot 格式解析（`response.data.data`），但 Express 当前返回的是扁平结构，存在兼容性差异。

### 6.3 端点汇总

#### 认证

| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 否 | 登录 |
| POST | `/api/auth/register` | 否 | 注册 |
| GET | `/api/auth/me` | 是 | 当前用户信息 |

#### 金价

| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/gold-price/current` | 否 | 当前金价 |
| GET | `/api/gold-price/history` | 否 | 历史价格 |
| GET | `/api/gold-price/currencies` | 否 | 货币列表 |

#### 工具

| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/tools/json/format` | 否 | JSON 格式化 |
| POST | `/api/tools/base64/encode` | 否 | Base64 编码 |
| POST | `/api/tools/base64/decode` | 否 | Base64 解码 |
| POST | `/api/tools/url/encode` | 否 | URL 编码 |
| POST | `/api/tools/url/decode` | 否 | URL 解码 |
| POST | `/api/tools/hash/md5` | 否 | MD5 |
| POST | `/api/tools/hash/sha256` | 否 | SHA256 |

#### AI

| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/ai/models` | 否 | 模型列表 |
| GET | `/api/ai/status` | 否 | 服务状态 |
| POST | `/api/ai/chat` | 否 | 对话 |

#### 文章（Spring Boot  only）

| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/articles/public/list` | 否 | 文章列表 |
| GET | `/api/articles/public/{id}` | 否 | 文章详情 |
| GET | `/api/articles/public/recent` | 否 | 最新文章 |
| GET | `/api/articles/public/popular` | 否 | 热门文章 |

---

## 7. 安全设计

### 7.1 认证安全

| 项目 | 当前配置 | 建议 |
|------|---------|------|
| JWT Secret | 硬编码默认值 | 生产环境通过 `JWT_SECRET` 环境变量覆盖 |
| Token 有效期 | 24 小时 | 可考虑 Refresh Token 机制 |
| 密码存储 | Express 明文比对 / Spring Boot bcrypt | Express 应接入 bcrypt |

### 7.2 网络安全

| 项目 | 当前配置 | 风险 |
|------|---------|------|
| CORS | `app.use(cors())`（允许所有） | 生产环境应限制为实际域名 |
| H2 Console | Spring Boot 开发环境启用 | 生产环境（`application-prod.yml`）应禁用 |
| Ollama 代理 | 仅本地访问 | 确保 Ollama 不监听公网接口 |

### 7.3 默认凭据

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |
| 普通用户 | `user` | `user123` |

> ⚠️ 生产环境必须修改或禁用默认账号。

---

## 8. 部署架构

### 8.1 方式一：Node.js 直接运行（当前活跃）

```bash
# 1. 安装依赖
npm install
cd frontend && npm install && npm run build && cd ..

# 2. 同步构建产物
xcopy /E /I /Y frontend\dist frontend-dist

# 3. 启动
node server.js
```

访问：
- 应用：`http://localhost:8080`
- API：`http://localhost:8080/api`

### 8.2 方式二：开发模式（热更新）

```bash
# 终端 1：Express 后端
npm run dev

# 终端 2：Vue 前端
npm run dev   # 端口 3000，代理 /api 到 8080
```

### 8.3 方式三：Docker Compose（Spring Boot 架构）

```bash
docker-compose up -d
```

服务：
- `coffee-homepage-backend`：Spring Boot（8080）
- `coffee-homepage-frontend`：Nginx（80）
- `coffee-homepage-mysql`：MySQL 8（3306）

### 8.4 公网访问

当前使用内网穿透工具：

| 工具 | 命令 | 特点 |
|------|------|------|
| LocalTunnel | `npx -y localtunnel --port 8080` | 需公网 IP 密码验证 |
| Tunnelmole | `npx -y tunnelmole 8080` | 免密码，HTTPS |

---

## 9. 环境变量

### Express 环境

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `PORT` | `8080` | 服务端口 |
| `JWT_SECRET` | `coffee-cookies-secret-key-2024` | JWT 签名密钥 |
| `OLLAMA_URL` | `http://localhost:11434` | Ollama 服务地址 |
| `NODE_ENV` | `development` | 运行环境 |

### Docker Compose 环境

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | `root123` | MySQL root 密码 |
| `MYSQL_USER` | `homepage` | MySQL 用户名 |
| `MYSQL_PASSWORD` | `homepage123` | MySQL 密码 |
| `MYSQL_DB` | `homepage` | 数据库名 |
| `JWT_SECRET` | Spring Boot 默认值 | JWT 密钥 |
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring 激活的配置文件 |

---

## 10. 前端工程规范

### 10.1 目录约定

| 目录 | 用途 |
|------|------|
| `src/api/` | 按业务模块封装 HTTP 请求 |
| `src/components/` | 可复用 UI 组件 |
| `src/views/` | 页面级组件 |
| `src/stores/` | Pinia 状态管理 |
| `src/router/` | 路由配置与导航守卫 |
| `src/utils/` | 工具函数（如 Axios 实例）|

### 10.2 路径别名

```typescript
// vite.config.ts
alias: {
  '@': resolve(__dirname, 'src')
}
```

### 10.3 状态管理风格

使用 Pinia + Composition API：

```typescript
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  // ...
  return { token, /* ... */ }
})
```

### 10.4 样式规范

- 主框架：Tailwind CSS
- 自定义色板：`primary-*`、`gold-*`
- 暗色模式：`darkMode: 'class'`

---

## 11. 已知问题与发展路线

### 11.1 当前限制

1. **Express 后端缺少文章 API**：文章模块前端已就绪，但 Express 后端未实现对应端点
2. **前端与 Express 响应格式不兼容**：前端按 `response.data.data` 解析，Express 直接返回 `response.data`
3. **工具箱 API 不完整**：Express 仅实现了 JSON、Base64、URL、MD5、SHA256
4. **金价为模拟数据**：未接入真实金价数据源

### 11.2 发展路线

| 优先级 | 任务 |
|--------|------|
| 高 | 接入真实金价 API |
| 高 | 完善 Express 后端文章 API |
| 中 | 统一前后端响应格式 |
| 中 | 补全工具箱 API |
| 中 | 添加价格预警功能 |
| 低 | 多语言支持 (i18n) |
| 低 | 移动端 App |

---

## 12. 附录

### 12.1 相关文档

| 文档 | 说明 |
|------|------|
| `README.md` | 面向用户的项目说明 |
| `AGENTS.md` | 面向 AI 编程助手的开发指南 |
| `PROJECT_DOCUMENTATION.md` | 详细技术文档与 API 示例 |
| `DEPLOY_GUIDE.md` | 公网部署与故障排查指南 |
| `HISTORY.md` | 开发历史记录 |
| `TASK_STATUS.md` | 任务进度记录 |

### 12.2 文档版本

- **创建时间**：2026-03-29
- **对应代码版本**：v1.0.0
- **维护者**：CoffeeCookie'sHomePage Dev Team
