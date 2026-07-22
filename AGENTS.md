# CoffeeCookie'sHomePage

> 全栈个人主页：金价追踪 + 技术文章 + 实用工具 + AI 对话 + 小米音箱

**架构（2026-07）**：Vue 3 + **FastAPI**（轻量栈）。Spring Boot 仅归档于 `legacy/spring-boot/`。

**Entry Points:**
- Backend: `api/app/main.py`（uvicorn，端口 **8000**）
- Frontend: `frontend/src/main.ts`，开发端口 **3000**（代理 `/api` → `http://localhost:8000`）
- 一键本地：`start.bat` / `./start.sh dev` → `scripts/dev_launcher.py`
- 公网穿透：`load-service` → `.skills/load-service/load-service.ps1`

---

## SESSION HANDOFF (epitaph / 墓志铭)

**开场必做：** 新开对话接手本仓库时，主动检查并阅读 `docs/epitaph/`（见 `docs/epitaph/README.md`），优先最新日期、未归档条目。不必等用户说「墓志铭」。读完后按全局 `epitaph` skill 自行判断删除或移入 `docs/epitaph/archive/`。

活跃后端为 FastAPI `api/`（端口 8000）；小米/对话见 `.cursor/skills/xiaomi-cli/` 与 `docs/superpowers/specs/2026-07-19-miai-dialogue-design.md`。路线图见根目录 `ROADMAP.md`。

---

## OVERVIEW

全栈个人主页。**FastAPI** 后端 + Vue 3 前端；默认 SQLite；量化 / 小米为可开关插件。

**核心栈**: Vue 3 + TypeScript + Vite | FastAPI + SQLAlchemy + SQLite | JWT

---

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 修改 API 端点 | `api/app/routers/`、`api/app/plugins/` | FastAPI 路由 |
| 业务逻辑 | `api/app/services/` | LLM、金价、认证等 |
| 前端页面 | `frontend/src/views/` | Vue SFC |
| 前端组件 | `frontend/src/components/` | 可复用组件 |
| API 封装 | `frontend/src/api/*.ts` | 按模块分文件 |
| 状态管理 | `frontend/src/stores/` | Pinia |
| 路由 | `frontend/src/router/index.ts` | 导航守卫 + 懒加载 |
| HTTP 客户端 | `frontend/src/utils/request.ts` | JWT 注入 + 401 |
| 架构说明 | `doc/architecture/fastapi-overview.md` | FastAPI 概览 |
| Vue 规范 | `frontend/AGENTS.md` | 前端约定 |
| 旧 Java 后端 | `legacy/spring-boot/` | 只读参考，勿当运行时 |

---

## STRUCTURE

```
coffeeCookie'sHomePage/
├── api/                    # 【主后端】FastAPI，端口 8000
│   ├── app/                # main、routers、services、plugins
│   ├── tests/              # pytest
│   ├── requirements.txt
│   └── Dockerfile
├── frontend/               # Vue 3 → frontend/AGENTS.md
│   ├── src/
│   └── tests/              # Vitest + Playwright（部分可能与改版 UI 暂不对齐）
├── legacy/spring-boot/     # 已归档 Java 后端
├── docs/                   # epitaph、superpowers specs
├── doc/                    # 历史文档（部分仍写 Spring，以本文件与 ROADMAP 为准）
├── .skills/load-service/   # 一键部署 + Tunnel
├── start.bat / start.sh    # 本地 / Docker 启动
└── docker-compose.yml      # 默认 api + frontend（SQLite）
```

---

## TECH STACK

### 后端 (FastAPI)
| 层级 | 技术 | 说明 |
|------|------|------|
| 框架 | FastAPI + uvicorn | OpenAPI `/docs` |
| ORM | SQLAlchemy 2.x | 默认 SQLite `api/data` 或 `./data` |
| 认证 | JWT + bcrypt | Bearer Token |
| HTTP | httpx | 外部 API / Ollama |
| 插件 | `api/app/plugins/` | quant、xiaomi（环境变量启停） |

### 前端 (Vue 3)
| 层级 | 技术 | 说明 |
|------|------|------|
| 框架 | Vue 3 + TypeScript | `<script setup>` |
| 构建 | Vite | 开发 3000，构建 `frontend/dist/` |
| 状态 | Pinia | Composition API |
| 样式 | Tailwind CSS | 暖色「咖啡台」UI |
| HTTP | Axios | `/api` 经 Vite 或 nginx 反代 |

---

## CONVENTIONS

### 前端导入顺序
```typescript
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import PriceChart from '@/components/PriceChart.vue'
import type { GoldPrice } from '@/api/goldPrice'
```

### 命名约定
- 组件：`PascalCase`；变量/函数：`camelCase`；常量：`UPPER_SNAKE_CASE`

### 回复规范
- **所有答复使用中文**

---

## ANTI-PATTERNS (THIS PROJECT)

1. **不要把 Spring Boot / `:8080` 当成当前运行后端** — 运行时是 FastAPI `:8000`
2. **不要默认 DeepSeek** — `AI_DEFAULT_PROVIDER=ollama`；DeepSeek 仅回退或 UI 显式选择
3. **不要 commit** `api/.env`、`.config/` 凭证、passToken / API key
4. **密码-only 登录** — 默认 `admin` / `admin123`；注册关闭
5. **金价** — 国内 AU9999（新浪）；勿假设 Metalprice 必开
6. **Docker 默认栈** — `api` + `frontend` + SQLite，无 MySQL

---

## COMMANDS

```bash
# 本地开发（推荐）
start.bat                    # Windows：API 8000 + Vite 3000
./start.sh dev               # Unix 同栈

# 手动
cd api && .venv/Scripts/python.exe -m uvicorn app.main:app --reload --port 8000
cd frontend && npm run dev

# 测试
cd api && .venv/Scripts/python.exe -m pytest tests -q

# Docker（生产态）
docker compose up -d         # API :8000 + frontend :80（nginx 反代 /api）

# 公网 Tunnel（需本机 .config/）
.\.skills\load-service\load-service.ps1
```

---

## QUICK DEPLOY

### load-service（推荐，含 Tunnel）

```bash
"load-service"
```

助手执行 `.skills/load-service/load-service.ps1`：
1. 检查 Python / Node / cloudflared
2. 确保 venv + npm 依赖
3. 启动 FastAPI + Vite
4. 启动 Cloudflare Tunnel（固定域名见本机配置）
5. 输出公网地址（默认 `https://www.coffeecookie.online`）

示例配置：`.skills/load-service/service-config.example.yml` → 复制为 `.config/service-config.yml`。

### 访问

| 模式 | 前端 | API |
|------|------|-----|
| 本地开发 | http://localhost:3000 | http://localhost:8000/api |
| Docker | http://localhost:80 | 经 nginx `/api` |
| 公网 | https://www.coffeecookie.online | 同站 `/api` |

登录：`admin` / `admin123`

---

## TESTING

- **API**: `cd api && pytest`（pytest + TestClient）
- **前端**: Vitest / Playwright；改版 UI 后部分旧用例可能暂不对齐，**不以前端单测阻塞构建**
- 覆盖目标：核心业务逻辑优先；API 全量应保持绿灯

---

## DOCUMENTATION

| 文档 | 用途 |
|------|------|
| `ROADMAP.md` | 迁移路线图与当前下一步 |
| `doc/architecture/fastapi-overview.md` | FastAPI 架构 |
| `docs/superpowers/specs/` | 近期功能设计（金价、miAi、前端重写等） |
| `docs/epitaph/` | 会话交接 |
| `doc/` 其余 | 历史材料；与本文件冲突时以本文件 + ROADMAP 为准 |

---

## NOTES

- LLM：Ollama 优先，DeepSeek 回退；固定话术（唤醒/退出/announce）不经 LLM
- 小米完成播报：`turn_ended` 必须 JSON `type` 判断，禁止子串匹配
- Tailwind：改版后以 brand/ink/奶油底为主（见 frontend 设计 specs）
- 功能改动归档：需求 / 技术设计 / 实现 / 测试完整可追溯

---

## Cursor Cloud specific instructions

### Toolchain
- **Python 3.12+** + `api/.venv`；**Node 18+** 跑前端
- **不需要** JDK/Maven/MySQL 即可开发
- Docker 仅用于 compose 生产态栈

### Dev
- API: `uvicorn` → **8000**；SQLite 种子用户 password-only `admin123`
- Frontend: `npm run dev` → **3000**，proxy `/api` → `localhost:8000`
- 可选：Ollama、小米硬件、DeepSeek key — 默认不阻塞主站

### Known issues
- 前端 Vitest 可能仍 glob 到 Playwright e2e 或与改版 UI 不对齐；E2E 用 `npm run test:e2e` 单独跑
- 裸域 `coffeecookie.online` DNS 与 www 跳转可能未配齐；公网以 www + tunnel 为准
