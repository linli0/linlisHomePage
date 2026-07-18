# linlisHomePage Roadmap

> **架构方向（2026-07）**：已迁移为 **Vue 3 + Python FastAPI** 轻量化全栈架构。  
> **迁移状态（2026-07-09）**：MVP 已完成 — `api/` 后端、27 项 pytest、Java 归档至 `legacy/spring-boot/`。

## 1. 现状与决策

### 1.1 当前基线（`master`）

| 层级 | 现状 | 问题 |
|------|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Tailwind | 可保留，仅需适配 API |
| 后端 | Spring Boot 3.2 + Java 17 + Maven | 启动慢、镜像大、依赖重 |
| 数据库 | H2（开发）/ MySQL 8（生产） | 个人站点运维成本高 |
| 部署 | Docker Compose（backend + frontend + mysql） | 3 容器 + JVM 内存占用高 |
| 扩展模块 | AI、社交推文、量化交易、小米音箱 | 与主站耦合，增加复杂度 |

### 1.2 架构决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 后端语言 | **Python 3.12+** | 生态轻量、AI/工具类库丰富、开发效率高 |
| Web 框架 | **FastAPI** | 原生 OpenAPI、异步友好、性能足够 |
| ORM | **SQLAlchemy 2.0 + Alembic** | 成熟、迁移脚本可版本化 |
| 默认数据库 | **SQLite**（开发 & 小规模生产） | 零运维、单文件、适合个人主页 |
| 可选数据库 | PostgreSQL / MySQL | 仅在多实例或高并发时启用 |
| 认证 | JWT + bcrypt（`python-jose` / `passlib`） | 对齐现有前端 Bearer Token 流程 |
| 任务调度 | **APScheduler** 或 FastAPI `BackgroundTasks` | 替代 Spring `@Scheduled` |
| 实时能力 | **SSE**（AI 流式）+ 轮询（推文） | 替代 STOMP WebSocket，降低复杂度 |
| Java 后端 | **废弃** | `backend/` 进入只读归档期后删除 |

### 1.3 轻量化目标

| 指标 | Java 现状（估） | FastAPI 目标 |
|------|----------------|--------------|
| 后端镜像 | ~300–500 MB（JRE + fat jar） | **< 150 MB**（slim Python + wheels） |
| 冷启动 | 15–30 s | **< 3 s** |
| 本地内存 | 512 MB–1 GB+ | **< 256 MB** |
| 容器数（默认） | 3（backend + mysql + frontend） | **2**（api + frontend）；SQLite 内嵌 |
| 构建工具 | Maven + JDK 17 | **uv / pip** + Python 3.12 |
| CI 耗时 | 后端测试 3–8 min | **< 2 min** |

---

## 2. 目标架构

```
linlisHomePage/
├── api/                          # FastAPI 后端（新）
│   ├── app/
│   │   ├── main.py               # 应用入口
│   │   ├── core/                 # 配置、安全、依赖注入
│   │   ├── models/               # SQLAlchemy 模型
│   │   ├── schemas/              # Pydantic DTO（对齐现有 API 响应）
│   │   ├── routers/              # 路由（按域拆分）
│   │   ├── services/             # 业务逻辑
│   │   └── tasks/                # 定时任务（金价刷新等）
│   ├── alembic/                  # 数据库迁移
│   ├── tests/                    # pytest
│   ├── pyproject.toml            # 依赖（推荐 uv）
│   └── Dockerfile
├── frontend/                     # Vue 3 前端（保留，渐进适配）
├── docker-compose.yml            # api + frontend（默认无 MySQL）
├── doc/
│   └── architecture/
│       └── fastapi-overview.md   # 新架构说明（阶段 1 产出）
├── legacy/
│   └── spring-boot/              # 旧 Java 代码归档（过渡期）
└── ROADMAP.md                    # 本文件
```

### 2.1 技术栈对照

| 能力 | Spring Boot（废弃） | FastAPI（目标） |
|------|---------------------|-----------------|
| REST API | `@RestController` | `APIRouter` |
| DTO 校验 | Bean Validation | Pydantic v2 |
| 持久化 | Spring Data JPA | SQLAlchemy 2.0 |
| 迁移 | Flyway / 手动 | Alembic |
| 安全 | Spring Security | 自定义 `Depends(get_current_user)` |
| HTTP 客户端 | WebClient | `httpx`（异步） |
| 定时任务 | `@Scheduled` | APScheduler |
| AI 流式 | SSE（已有） | `StreamingResponse` + SSE |
| 二维码 | ZXing | `qrcode` / `segno` |
| 文档 | 手写 README | **自动生成** `/docs` `/redoc` |

### 2.2 API 兼容策略

**原则：前端尽量不改，后端保持 REST 契约。**

- 保持现有路径前缀：`/api/auth`、`/api/articles`、`/api/gold-price`、`/api/tools` 等
- 保持响应 envelope 结构（如 `{ code, data, message }`），与 `frontend/src/utils/request.ts` 解包逻辑一致
- 用 OpenAPI spec 做契约测试，对比 Java 版与 FastAPI 版响应差异
- 不兼容处在前端 `api/` 层做薄适配，避免散落修改 views

---

## 3. 功能迁移矩阵

### 3.1 核心模块（MVP，必须迁移）

| 模块 | Java 端点 | 优先级 | FastAPI 路由 | 备注 |
|------|-----------|--------|--------------|------|
| 认证 | `/api/auth/*` | P0 | `routers/auth.py` | JWT + bcrypt，废除弱默认密钥 |
| 文章 | `/api/articles/*` | P0 | `routers/articles.py` | 含公开列表、详情、CRUD |
| 分类/标签 | `/api/categories`, `/api/tags` | P0 | `routers/taxonomy.py` | 可合并为一个 router |
| 金价 | `/api/gold-price/*` | P0 | `routers/gold_price.py` | httpx 拉外部 API + 本地缓存 |
| 工具箱 | `/api/tools/*` | P0 | `routers/tools.py` | 纯计算，无 DB 依赖 |

### 3.2 扩展模块（按轻量化原则分级）

| 模块 | 优先级 | 策略 |
|------|--------|------|
| AI 对话 | P1 | 保留；`httpx` 代理 Ollama/OpenAI 兼容接口，SSE 流式 |
| 社交推文 | P2 | **简化**：去掉 STOMP，改轮询 + 缓存表 |
| 量化交易 | P3 | **插件化**：`api/app/plugins/quant/`，默认关闭 |
| 小米音箱 | P3 | **插件化**：独立 router，需硬件时启用 |
| WebSocket | — | **不迁移**；用 SSE / 轮询替代 |

### 3.3 明确放弃（Java 特有冗余）

- Spring Security 过滤器链 → FastAPI 中间件 + Depends
- JPA/Hibernate 实体图与懒加载复杂度 → 显式 SQLAlchemy 关系
- `.m2/` Maven 缓存、多模块 pom 配置
- `source-projects/` 历史备份（迁入 `legacy/` 说明文档后删除）
- MySQL 作为默认依赖（改为可选 profile）

---

## 4. 分阶段实施计划

### 阶段 0 — 冻结与准备（1 周）

**目标**：停止 Java 新功能开发，建立迁移基线。

- [ ] 在 `README.md` 和 `AGENTS.md` 标注「Java 后端进入维护冻结，新开发走 FastAPI」
- [ ] 导出 Java 版 OpenAPI / 接口清单（可用 SpringDoc 或手工整理现有 Controller）
- [ ] 将 `backend/` 标记为 `legacy/spring-boot/`（或添加 `LEGACY.md` 说明）
- [ ] 清理仓库：删除 `.m2/`、`.m2repo/`、`target/` 等构建产物
- [ ] 确定 Python 工具链：`uv` + `pyproject.toml` + `ruff` + `pytest`

**验收**：团队对迁移范围、API 契约、模块优先级达成一致。

---

### 阶段 1 — FastAPI 脚手架（1–2 周）

**目标**：可启动的空壳 API + 健康检查 + 数据库连通。

工作项：

```
api/
├── app/main.py          # FastAPI(), CORS, lifespan
├── app/core/config.py   # pydantic-settings，读取 .env
├── app/core/database.py # SQLAlchemy engine + session
├── app/core/security.py # JWT 工具函数
└── tests/test_health.py
```

- [ ] 初始化 `api/` 项目，`GET /api/health` 与 `GET /api/tools/health` 返回 200
- [ ] SQLite 默认：`DATABASE_URL=sqlite:///./data/homepage.db`
- [ ] Alembic 初始化，创建 `users` 表迁移
- [ ] Docker：`api` 服务替换 `backend` 服务
- [ ] CI：新增 `api` job（`ruff check` + `pytest`）

**验收**：`docker compose up` 启动 api + frontend；`/docs` 可访问；CI 绿灯。

---

### 阶段 2 — 核心 API 迁移（2–3 周）

**目标**：MVP 五件套（认证、文章、分类标签、金价、工具）在 FastAPI 跑通。

#### 2a. 认证（P0）

- [ ] `User` 模型：id, username, password_hash, role, created_at
- [ ] `POST /api/auth/login` — bcrypt 验证，签发 JWT
- [ ] `POST /api/auth/register`、`GET /api/auth/me`
- [ ] `PUT /api/auth/profile`、`PUT /api/auth/password`
- [ ] 移除弱默认密钥；`JWT_SECRET` 必须通过环境变量注入
- [ ] pytest：登录成功/失败、token 过期、角色权限

#### 2b. 文章系统（P0）

- [ ] 模型：`Article`, `Category`, `Tag` + 多对多关联
- [ ] 公开接口：`/public/list`, `/public/{id}`, `/public/recent`, `/public/popular`
- [ ] 管理接口：CRUD（需 ADMIN）
- [ ] 数据迁移脚本：从现有 MySQL/H2 导出 → 导入 SQLite（一次性）

#### 2c. 金价（P0）

- [ ] `GoldPrice`, `ExchangeRate` 模型
- [ ] `GET /current`, `/history`, `/currencies`
- [ ] APScheduler：每分钟刷新金价，每小时刷新汇率
- [ ] 外部 API 失败时的缓存兜底

#### 2d. 工具箱（P0）

- [ ] 端口所有 `/api/tools/*` 端点
- [ ] 使用 Python 标准库 + `hashlib` + `qrcode`
- [ ] 输入校验与错误响应格式对齐 Java 版

**验收**：

- 前端在不改 views 的情况下可完成：登录 → 首页 → 文章列表 → 金价页 → 工具箱
- 核心路径 pytest 覆盖率 ≥ 80%
- 契约测试：关键接口响应 schema 与 Java 版 diff 为空

---

### 阶段 3 — 前端适配与 E2E（1–2 周）

**目标**：前端完全指向 FastAPI，移除对 Java 后端的依赖。

- [ ] `frontend/vite.config.ts` proxy 目标改为 `http://localhost:8000`
- [ ] 检查 `request.ts` 响应解包是否与 FastAPI 一致
- [ ] 更新 `frontend/src/api/*.ts` 中个别不兼容字段
- [ ] Playwright E2E：登录、文章、金价、工具箱主流程
- [ ] 更新 `start.sh` / `start.bat`：启动 `uvicorn` 而非 `mvnw`

**验收**：`npm run test:e2e` 通过；README 启动说明有效。

---

### 阶段 4 — 扩展模块（按需，2–4 周）

**目标**：以插件方式迁移非核心能力，默认关闭。

#### 4a. AI 对话（P1）

- [ ] `routers/ai.py`：`/models`, `/chat`（SSE）, `/status`
- [ ] `httpx` 异步代理 Ollama
- [ ] 前端 `AIChat.vue` 验证流式输出

#### 4b. 社交推文（P2，简化版）

- [ ] 去掉 WebSocket/STOMP
- [ ] `GET /api/tweets/latest` + 后台轮询抓取
- [ ] 可选：管理员手动触发 `POST /api/tweets/search`

#### 4c. 量化 / 小米（P3，插件）

- [ ] `api/app/plugins/quant/` — `ENABLE_QUANT=true` 时挂载
- [ ] `api/app/plugins/xiaomi/` — `ENABLE_XIAOMI=true` 时挂载
- [ ] 文档说明：默认不启用，不影响主站启动

**验收**：`ENABLE_QUANT=false` 时主站功能完整；启用插件后对应页面可用。

---

### 阶段 5 — 部署轻量化（1 周）

**目标**：生产配置极简、可维护。

- [x] `docker-compose.yml` 默认栈：`api` + `frontend`（SQLite volume）
- [ ] `docker-compose.prod.yml`（可选）：加 PostgreSQL
- [x] 后端 Dockerfile：`python:3.12-slim` + uvicorn
- [ ] GitHub Actions：api 测试 + 前端构建 + Docker build
- [x] 健康检查：`/api/health` 纳入 compose healthcheck
- [ ] 删除 Java 相关 CI job（若仍有）

**验收**：

- 单节点 `docker compose up -d` 后 30 秒内可访问
- 镜像总体积较 Java 版减少 50%+

---

### 阶段 6 — 下线 Java（1 周）

**目标**：彻底移除 Spring Boot 代码与依赖。

- [ ] 确认所有核心 + 已启用扩展模块在 FastAPI 运行 ≥ 2 周无阻塞 bug
- [x] 将 `backend/` 移至 `legacy/spring-boot/`（源码仍保留作参考）
- [ ] 可选：打 tag `v1-java-final` 后从默认树删除 legacy
- [x] 更新根 `AGENTS.md`、`load-service` 为 FastAPI（2026-07-19）
- [ ] 更新剩余文档：`doc/deployment/`、`项目架构.md`、`README.md` 中过时 Spring 叙述

**验收**：新开发者无需安装 JDK/Maven 即可跑通主栈；Java 仅可选归档。

---

## 5. 时间线总览

```
2026-Q3
├── W1–2   阶段 0 + 阶段 1（脚手架）
├── W3–5   阶段 2（核心 API）
├── W6–7   阶段 3（前端适配 + E2E）
├── W8–11  阶段 4（扩展模块，可并行）
├── W12    阶段 5（部署轻量化）
└── W13    阶段 6（下线 Java）
```

> 以上为单人/part-time 估算；全职可压缩至 4–6 周完成 MVP（阶段 0–3 + 5）。

---

## 6. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| API 契约不一致导致前端大面积改动 | 高 | 先写契约测试；保持 response envelope；逐模块切换 |
| 数据迁移丢失 | 高 | 迁移脚本 + 校验行数；保留 Java 版 DB 备份 |
| SQLite 并发写入限制 | 中 | 个人主页场景足够；生产可切 PostgreSQL |
| 扩展模块范围膨胀 | 中 | 插件化 + 环境变量开关；P3 模块可永久搁置 |
| 团队不熟悉 Python 异步 | 低 | 核心路由先用同步 SQLAlchemy；热点路径再 async |
| 测试覆盖不足导致回归 | 高 | 每个阶段强制 pytest + E2E 门禁 |

---

## 7. 最近下一步（本周）

> **2026-07-19 状态**：MVP（阶段 0–4 主体）与 miAi 对话/播报已合入 `master`。运行时以 FastAPI `api/:8000` + Vite `frontend/:3000` 为准；Spring Boot 仅 `legacy/spring-boot/`。

1. ~~创建 `api/` 目录~~ ✅
2. ~~编写 `doc/architecture/fastapi-overview.md`~~ ✅
3. ~~核心 API + JWT 登录~~ ✅
4. ~~`docker-compose.yml` 默认 api + frontend（SQLite）~~ ✅
5. **文档与运维对齐**：根 `AGENTS.md`、`load-service` 指向 FastAPI/Vite（进行中）
6. **阶段 6 收尾（可选）**：清理过时 `doc/` / `项目架构.md` 中的 Spring 叙述；前端 Vitest 与改版 UI 对齐
7. **公网**：裸域 → `www` 跳转（Cloudflare）；tunnel 配置仍在本机 `.config/`（gitignore）

---

## 8. 成功标准（Migration Done）

- [ ] 仓库无 Java 后端代码（已归档至 `legacy/spring-boot/`，未删除）
- [x] `docker compose up -d` 仅 api + frontend 即可完整运行 MVP
- [ ] 前端 E2E 测试通过（Vitest 与改版 UI 可能仍不对齐）
- [x] API 测试覆盖率门禁：`api/` pytest 全量通过（核心路径有覆盖）
- [ ] 冷启动 < 3 s，内存 < 256 MB（待生产镜像实测）
- [x] `/docs` 提供完整 OpenAPI 文档（FastAPI 自动生成）
- [x] 扩展模块可通过环境变量独立启停（quant / xiaomi 插件）

---

## 附录 A — 推荐 Python 依赖

```toml
# api/pyproject.toml（草案）
[project]
dependencies = [
  "fastapi>=0.115",
  "uvicorn[standard]>=0.32",
  "sqlalchemy>=2.0",
  "alembic>=1.14",
  "pydantic-settings>=2.6",
  "python-jose[cryptography]>=3.3",
  "passlib[bcrypt]>=1.7",
  "httpx>=0.28",
  "apscheduler>=3.10",
  "qrcode[pil]>=8.0",
]
```

## 附录 B — 目录迁移对照

| 旧路径（Java） | 新路径（FastAPI） |
|----------------|-------------------|
| `backend/.../controller/AuthController.java` | `api/app/routers/auth.py` |
| `backend/.../service/AuthService.java` | `api/app/services/auth.py` |
| `backend/.../entity/User.java` | `api/app/models/user.py` |
| `backend/.../dto/AuthResponse.java` | `api/app/schemas/auth.py` |
| `backend/.../repository/UserRepository.java` | `api/app/services/auth.py`（SQLAlchemy 查询） |
| `backend/src/main/resources/application.yml` | `api/app/core/config.py` + `.env` |

---

*最后更新：2026-07-19 · 维护者：linlisHomePage 团队*
