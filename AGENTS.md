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
├── project.properties           # 站点配置（如 site.password）
├── start.bat                    # Windows 启动脚本
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
│   ├── dist/                    # 【构建产物】由 server.js 直接服务
│   └── src/
│       ├── main.ts              # 入口：createApp + Pinia + Router
│       ├── App.vue
│       ├── assets/main.css
│       ├── api/                 # API 接口封装（按模块分文件）
│       ├── components/          # 可复用组件
│       ├── router/index.ts      # 路由表 + 导航守卫
│       ├── stores/              # Pinia Store
│       ├── utils/request.ts     # Axios 实例 + 拦截器
│       └── views/               # 页面级组件
│
├── backend/                     # 【备用后端】Spring Boot 完整工程
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/...
│
├── source-projects/             # 原始项目备份
├── design/                      # 需求文档
└── README.md
```

---

## 构建与运行命令

### Express 后端 + Vue 前端（推荐）

```bash
# 安装后端依赖
cd "D:\AI\coffeeCookie'sHomePage"
npm install

# 安装前端依赖
cd frontend
npm install

# 构建前端
npm run build
cd ..

# 启动服务
node server.js
```

或使用一键脚本：
```bash
start.bat
```

服务启动后：
- 本地访问：http://localhost:8080
- API 地址：http://localhost:8080/api

### 开发模式（热更新）

```bash
# 终端 1：Express 后端
cd "D:\AI\coffeeCookie'sHomePage"
npm run dev        # 使用 nodemon 热重载

# 终端 2：Vue 前端
cd "D:\AI\coffeeCookie'sHomePage\frontend"
npm run dev        # Vite 开发服务器，端口 3000，代理 /api 到 8080
```

### Spring Boot 后端（备用）

```bash
cd backend
mvn clean package -DskipTests
java -jar target/homepage-backend-1.0.0.jar
```

---

## 测试命令

### 当前状态
- **后端测试**：项目无单元测试，建议引入 Jest 或 Mocha
- **前端测试**：项目无测试框架，建议引入 Vitest
- **Spring Boot 测试**：依赖已配置（`spring-boot-starter-test`），但无测试文件

### 建议的测试命令（未来添加后）

```bash
# 后端
npm test                    # 运行所有测试
npm test -- --watch        # 监听模式
npx jest --testNamePattern="login"  # 运行单个测试

# 前端
cd frontend
npm run test               # 运行 Vitest
npm run test:ui           # UI 模式
npx vitest run login      # 运行单个测试文件

# Spring Boot
cd backend
mvn test                              # 运行所有测试
mvn test -Dtest=AuthControllerTest    # 运行单个测试类
mvn test -Dtest=AuthControllerTest#login  # 运行单个测试方法
```

---

## 代码风格指南

### 前端（Vue 3 + TypeScript）

#### 导入顺序
```typescript
// 1. Vue/框架内置
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. 第三方库
import { useAuthStore } from '@/stores/auth'
import { goldPriceApi } from '@/api/goldPrice'

// 3. 本地组件/工具
import PriceChart from '@/components/PriceChart.vue'
import { formatPrice } from '@/utils/format'

// 4. 类型
import type { GoldPrice } from '@/api/goldPrice'
```

#### 组件规范
- 使用 `<script setup lang="ts">` 语法
- Props 使用 `withDefaults` 定义默认值
- 事件使用 `emit` 显式声明
- 避免使用 `any`，使用具体类型或 `unknown`

#### 命名约定
- 组件文件：`PascalCase`（如 `AIChat.vue`）
- 组件变量：`camelCase`（如 `aiChatRef`）
- 常量：`UPPER_SNAKE_CASE`（如 `API_BASE_URL`）
- 接口/类型：`PascalCase`（如 `interface User`）
- CSS 类：`kebab-case`（如 `.btn-primary`）

#### 模板规范
- 使用 `v-if`/`v-else` 处理条件渲染，避免 `v-show`
- 使用 `v-for` 时必须添加 `:key`
- 事件处理使用箭头函数或方法引用，避免内联过多逻辑

### 后端（Express）

#### 代码组织
- `server.js` 是单体文件，包含所有路由、中间件、数据
- 新功能使用函数模块化，添加在文件适当位置
- 错误处理使用统一的 `error()` 响应格式

#### 命名约定
- 路由处理函数：`async function handleXxx(req, res)`
- 中间件：`function authMiddleware(req, res, next)`
- 变量：`camelCase`
- 常量：`UPPER_SNAKE_CASE`

#### 错误处理模式
```javascript
// 正确
app.get('/api/data', async (req, res) => {
  try {
    const data = await fetchData();
    res.json(success(data));
  } catch (err) {
    console.error('Error:', err.message);
    res.status(500).json(error('Failed to fetch data'));
  }
});
```

### 后端（Spring Boot - 备用）

#### 分层架构
- Controller → Service → Repository → Entity
- 使用 Lombok 减少样板代码
- 统一返回 `Result<T>` 格式

#### 命名约定
- 类：`PascalCase`（如 `UserController`）
- 方法：`camelCase`（如 `getUserById`）
- 包：`小写单数`（如 `com.coffeecookies.homepage.controller`）

---

## API 端点说明

### 认证相关
| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录（仅需密码）|
| GET | `/api/auth/me` | 获取当前用户信息 |

### 金价相关
| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/gold-price/current?currency=USD` | 获取当前金价 |
| GET | `/api/gold-price/history?currency=USD&days=7` | 获取历史价格 |
| GET | `/api/gold-price/currencies` | 获取支持的货币列表 |

### AI 相关（Ollama）
| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/ai/models` | 获取可用模型列表 |
| GET | `/api/ai/status` | 检查 Ollama 服务状态 |
| POST | `/api/ai/chat` | 与 AI 对话（流式响应）|

### 工具相关
| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/tools/json/format` | JSON 格式化 |
| POST | `/api/tools/base64/encode` | Base64 编码 |
| POST | `/api/tools/hash/md5` | MD5 哈希 |

---

## 安全注意事项

1. **登录凭据**：密码配置在 `project.properties` 的 `site.password` 字段，默认值 `admin`
2. **JWT 密钥**：Express 默认值 `coffee-cookies-secret-key-2024`，通过环境变量 `JWT_SECRET` 覆盖
3. **CORS**：Express 使用 `cors()` 允许所有来源，生产环境应限制域名
4. **Ollama 代理**：`server.js` 直接代理到 `localhost:11434`，确保仅本地可访问

---

## 开发工作流提示

1. **修改 Express 后端**：编辑 `server.js` 后必须重启服务
2. **修改 Vue 前端**：开发时运行 `npm run dev`，部署前执行 `npm run build`
3. **构建产物**：Express 直接服务 `frontend/dist/`，无需额外复制
4. **提交前检查**：确保前端构建成功，无 TypeScript 错误

---

## 常见陷阱

- **不要假设 Spring Boot 是当前运行后端**
- **前端构建产物在 `frontend/dist/`，Express 直接读取此目录**
- **没有真实金价 API**，数据为内存模拟
- **登录只需密码**，注册功能已禁用
