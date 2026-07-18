# Load Service Skill

> 自动部署本地 FastAPI + Vite，并启动 Cloudflare Tunnel

**Version:** 4.0.0  
**Category:** Deployment

---

## 功能描述

通过 `load-service.ps1` 一键拉起 CoffeeCookie's HomePage：

1. 读取 `.config/service-config.yml`（可从 `service-config.example.yml` 复制）
2. 检查 Python / Node / npm / cloudflared
3. 确保 `api/.venv` 与 `frontend/node_modules`
4. 启动 FastAPI `:8000` + Vite `:3000`（Vite 代理 `/api` → API）
5. 启动 Cloudflare Tunnel（默认已有域名 → Vite `:3000`）
6. 输出本地与公网访问地址

---

## 使用方法

```powershell
.\.skills\load-service\load-service.ps1

# 跳过依赖安装（venv / node_modules 已就绪）
.\.skills\load-service\load-service.ps1 -SkipBuild

# 仅本地，不启 Tunnel
.\.skills\load-service\load-service.ps1 -SkipTunnel
```

AI 助手：用户说 `load-service` 时执行上述脚本。

本地无 Tunnel 时也可：`start.bat` / `./start.sh dev`。

---

## 配置

`.config/` 已 gitignore。首次：

```powershell
mkdir .config -Force
copy .skills\load-service\service-config.example.yml .config\service-config.yml
# 另配 .config/tunnel/opencode-config.yml（ingress → http://127.0.0.1:3000）
```

| method | 说明 |
|--------|------|
| `existing-domain` | 固定域名（如 `https://www.coffeecookie.online`） |
| `quick-tunnel` | 临时 `*.trycloudflare.com`，指向 `service.port`（默认 3000） |

---

## 前置条件

- Python 3.12+
- Node.js 18+ / npm
- cloudflared（需要公网时）

不需要 Java / Maven。

---

## 故障排除

```powershell
Get-Content api-error.log -Tail 50
Get-Content frontend-error.log -Tail 50
Invoke-WebRequest http://localhost:8000/api/health
Invoke-WebRequest http://localhost:3000
Test-Path .config\tunnel\opencode-config.yml
```

停止：运行 `stop.bat`，或结束 uvicorn / node / cloudflared 进程。

---

## 更新日志

**v4.0.0** (2026-07-19)
- 切换为 FastAPI `:8000` + Vite `:3000`
- 环境检查改为 python/node/npm/cloudflared
- 增加 `service-config.example.yml`

**v3.0.0** (2026-04-09)
- 引入 `load-service.ps1`（当时为 Spring Boot `:8080`）
