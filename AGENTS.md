# CoffeeCookie'sHomePage

> 全栈个人主页：金价追踪 + 技术文章 + 实用工具 + AI 对话

**Generated:** 2026-04-15 | **Commit:** 5e7f0f7 | **Branch:** feature/frontend-redesign

---

## OVERVIEW

全栈个人主页应用。**Spring Boot 后端** + Vue 3 前端架构。

**核心栈**: Vue 3 + TypeScript + Vite (前端) | Spring Boot 3.2 + Java 17 (后端)

---

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 修改 API 端点 | `backend/src/.../controller/` | Spring Boot 控制器 |
| 前端页面 | `frontend/src/views/` | 11 个 View 组件 |
| 前端组件 | `frontend/src/components/` | 8 个可复用组件 |
| API 封装 | `frontend/src/api/*.ts` | 按模块分文件 |
| 状态管理 | `frontend/src/stores/auth.ts` | Pinia + Composition API |
| 路由配置 | `frontend/src/router/index.ts` | 导航守卫 + 懒加载 |
| HTTP 拦截器 | `frontend/src/utils/request.ts` | JWT 注入 + 401 处理 |
| Composables | `frontend/src/composables/*.ts` | Vue 3 组合式函数 |
| Spring Boot 后端 | `backend/AGENTS.md` | **后端详细文档** |
| Vue 详细规范 | `frontend/AGENTS.md` | **前端详细文档** |

---

## STRUCTURE

```
coffeeCookie'sHomePage/
├── backend/                # 【主后端】Spring Boot，端口 8080 → 见 backend/AGENTS.md
│   ├── src/main/java/      # Java 源代码（89 文件）
│   ├── src/test/java/      # 测试代码（JUnit 5 + Testcontainers）
│   └── pom.xml            # Maven 配置
├── frontend/               # Vue 3 前端 → 见 frontend/AGENTS.md
│   ├── src/               # 源代码（12 views, 8 components, 8 API）
│   ├── tests/             # 测试代码（39 文件，Vitest + Playwright）
│   └── package.json       # npm 配置
├── doc/                    # 集中式文档目录
│   ├── architecture/      # 系统架构
│   ├── backend/           # 后端实现细节
│   ├── frontend/          # 前端实现细节
│   ├── testing/           # 测试策略
│   └── deployment/        # 部署指南
├── .config/                # Cloudflare Tunnel 配置
├── start.bat               # Windows 启动脚本
├── docker-compose.yml      # Docker 编排
└── source-projects/        # 原始项目备份（仅参考）
```

---

## TECH STACK

### 后端 (Spring Boot)
| 层级 | 技术 | 说明 |
|------|------|------|
| 框架 | Spring Boot 3.2.0 + Java 17 | Maven 项目，`pom.xml` 管理依赖 |
| 数据库 | H2（开发）/ MySQL 8（生产）| JPA + Hibernate，`ddl-auto: update` |
| 安全 | Spring Security + JJWT 0.12.3 | JWT Bearer Token，有效期 24 小时 |
| 构建 | Maven 3.8+ | `mvn clean package -DskipTests` |
| 实时通信 | WebSocket + WebFlux | 实时数据推送和响应式编程 |

### 前端 (Vue 3)
| 层级 | 技术 | 说明 |
|------|------|------|
| 框架 | Vue 3.4.15 + TypeScript 5.3.3 | Composition API 风格 |
| 构建 | Vite 5.0.11 | 开发端口 3000，构建输出到 `frontend/dist/` |
| 状态 | Pinia 2.x | 使用 Composition API 风格的 Store |
| 路由 | Vue Router 4.x | 历史模式路由 |
| 样式 | Tailwind CSS 3.x | 自定义 `primary` 和 `gold` 色板，支持 `darkMode: 'class'` |
| 图表 | Chart.js 4.x + vue-chartjs | 金价走势图 |
| HTTP | Axios 1.x | 拦截器自动注入 JWT |
| AI | Ollama | 本地 LLM 代理，默认地址 `http://localhost:11434` |

---

## CONVENTIONS

### 前端导入顺序
```typescript
// 1. Vue/框架内置
import { ref, computed, onMounted } from 'vue'
// 2. 第三方库
import { useAuthStore } from '@/stores/auth'
// 3. 本地组件/工具
import PriceChart from '@/components/PriceChart.vue'
// 4. 类型
import type { GoldPrice } from '@/api/goldPrice'
```

### 命名约定
- 组件文件：`PascalCase`（如 `AIChat.vue`）
- 变量/函数：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- CSS 类：`kebab-case`（如 `.btn-primary`）

### 回复规范
- **所有答复使用中文** — AI 助手与用户交流时统一使用中文

---

## ANTI-PATTERNS (THIS PROJECT)

**CRITICAL - 违反会导致功能异常：**

1. **Spring Boot 是当前运行后端** — 端口 8080，使用 H2/MySQL 数据库
2. **前端构建产物在 `frontend/dist/`** — Spring Boot 静态资源目录
3. **金价数据从外部 API 获取** — 定时任务每分钟更新
4. **密码-only 登录** — 默认密码 admin123 (admin 用户) 或 user123 (user 用户)
5. **注册功能禁用** — `/api/auth/register` 返回 403 禁止访问

**数据一致性：**

6. **Spring Boot 使用数据库** — H2（开发）或 MySQL（生产），数据持久化
7. **Docker Compose 指向 Spring Boot** — 与实际运行的后端一致

**安全注意：**

8. **JWT 密钥通过环境变量配置** — 生产环境必须设置 `JWT_SECRET`
9. **CORS 已配置** — 允许前端跨域访问

---

## COMMANDS

```bash
# 构建与运行（推荐）
cd frontend && npm install && npm run build && cd ..  # 安装+构建前端
cd backend && mvn spring-boot:run                     # 启动 Spring Boot（端口 8080）

# 或使用一键脚本
start.bat                             # Windows（自动构建前端并启动后端）

# 开发模式（热更新）
cd backend && mvn spring-boot:run     # 终端1：Spring Boot 后端
cd frontend && npm run dev            # 终端2：Vite 开发服务器（端口 3000）

# 生产部署
cd backend && mvn clean package -DskipTests && java -jar target/*.jar
```

---

## QUICK DEPLOY 🚀

### 一键部署（推荐）

使用 AI 助手执行：

```bash
# 直接告诉 AI 助手
"load-service"
```

AI 助手将自动：
1. ✅ 检查环境（Java、Maven、Node.js）
2. ✅ 构建前端
3. ✅ 启动 Spring Boot 后端
4. ✅ 创建 Cloudflare Tunnel 穿透
5. ✅ 输出公网访问地址

### 手动部署

```bash
# 1. 构建前端
cd frontend
npm install
npm run build
cd ..

# 2. 启动后端
cd backend
nohup mvn spring-boot:run > ../backend.log 2>&1 &
cd ..

# 3. 启动 Tunnel（Windows）
CLOUDFLARED="/c/Users/Windows11/AppData/Local/Microsoft/WinGet/Packages/Cloudflare.cloudflared_Microsoft.Winget.Source_8wekyb3d8bbwe/cloudflared.exe"
nohup $CLOUDFLARED tunnel --url http://localhost:8080 > tunnel.log 2>&1 &

# 4. 查看公网 URL
cat tunnel.log | grep "trycloudflare.com"
```

### 访问信息

**本地访问**：
- 前端：http://localhost:8080
- 后端 API：http://localhost:8080/api

**公网访问**：
- 自动生成的 URL（如：https://xxx-trycloudflare.com）

**默认账号**：
- 用户名：`admin`
- 密码：`admin123`

### 管理命令

```bash
# 查看日志
tail -f backend.log        # 后端日志
tail -f tunnel.log         # Tunnel 日志

# 停止服务
pkill -f "mvn spring-boot:run"
pkill -f "cloudflared"

# 重新启动
cd backend && nohup mvn spring-boot:run > ../backend.log 2>&1 &
nohup $CLOUDFLARED tunnel --url http://localhost:8080 > tunnel.log 2>&1 &
```

---

## TESTING FRAMEWORK

### Backend Testing (Spring Boot)
- **Framework**: JUnit 5 + Spring Boot Test + Testcontainers 1.19.7
- **Dependencies**: Added Testcontainers, MySQL container, JUnit Jupiter
- **Test Structure**: 
  - `src/test/java/com/coffeecookies/homepage/` - Base test classes
  - `controller/` - Controller integration tests with MockMvc
  - `service/` - Service unit tests with Mockito  
  - `repository/` - Repository tests with Testcontainers (real MySQL)
  - `security/` - JWT and security component tests
  - `integration/` - Full-stack integration tests
- **Configuration**: `application-test.yml` for test environment
- **Commands**: `mvn test`, `mvn test jacoco:report`

### Frontend Testing (Vue 3)
- **Framework**: Vitest + Vue Test Utils + MSW + Playwright 1.43.0
- **Dependencies**: Added Vitest, Vue Test Utils, MSW, Playwright, happy-dom
- **Test Structure**:
  - `tests/setup.ts` - Global test setup with MSW
  - `tests/unit/` - Utility and composable unit tests
  - `tests/components/` - Component interaction tests
  - `tests/stores/` - Pinia store tests
  - `tests/api/` - API module tests with MSW mocking
  - `tests/e2e/` - End-to-end tests with Playwright
- **Configuration**: `vitest.config.ts`, `playwright.config.ts`
- **Commands**: `npm test`, `npm run test:coverage`, `npm run test:e2e`

### Testing Strategy
- **Coverage Target**: >80% line coverage for business logic
- **Testing Pyramid**: 70% Unit, 20% Integration, 10% E2E
- **CI/CD**: All tests must pass before merge/deployment

## DOCUMENTATION ARCHITECTURE

### Centralized Documentation
- **Location**: `doc/` directory (separate from code/config files)
- **Structure**:
  - `doc/architecture/` - System architecture and design decisions
  - `doc/backend/` - Spring Boot backend implementation details
  - `doc/frontend/` - Vue 3 frontend implementation details
  - `doc/design/` - UI/UX design system and guidelines
  - `doc/testing/` - Test frameworks, strategies, and results
  - `doc/deployment/` - Deployment guides and production setup
- **Migration**: All existing documentation files moved to appropriate locations in `doc/`
- **Navigation**: Main `doc/index.md` serves as documentation hub

## NOTES

- **项目来源**：三个原始项目合并，备份存放在 `source-projects/`
- **测试状态**：完整的前后端测试框架已实现，包含单元测试、集成测试和E2E测试
- **TypeScript 严格模式**：`strict: true`，避免 `any`
- **Tailwind 自定义色板**：`primary`（蓝）、`gold`（金）、`accent`（紫）、`surface`（灰）
- **Vue 组件风格**：全部使用 `<script setup lang="ts">`，无 Options API
- **数据库**：开发使用 H2 内存数据库，生产使用 MySQL
- **功能改动归档**：所有功能改动必须基于需求设计、技术设计、代码开发、白盒测试以及自动化测试进行完整归档