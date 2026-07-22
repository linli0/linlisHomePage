---
date: 2026-07-23
topic: master-merge-handoff
branch: master
status: active
---

# Epitaph: master 合入交接（#7 + #8）

本会话把非小爱可用性/运维文档改动全部合入 `master`。运行时仍以 FastAPI `api/:8000` + Vite `frontend/:3000` 为准。

## Built this session

- 确认 PR #7 已合入（FastAPI 文档、`load-service` v4、金价/AI/Wiki 等可用性）
- 漏推的中文 API 错误处理：新分支 `fix/api-error-messages` → PR #8 → **已 merge**
- 本地 `master` 同步至 `2224916`
- 归档 `2026-07-19-non-miai-usability`（`status: absorbed`）

## Key paths

- `frontend/src/utils/apiError.ts` / `request.ts` / `stores/auth.ts`
- `.skills/load-service/load-service.ps1` + `service-config.example.yml`
- `AGENTS.md` / `README.md` / `ROADMAP.md` / `docker-compose.yml`
- `docs/epitaph/archive/2026-07-19-non-miai-usability.md`（详细已做清单与坑）

## Locked product decisions

- 默认栈：FastAPI + Vite/Docker；Spring 仅 `legacy/spring-boot/`
- LLM：Ollama 优先；不默认 DeepSeek
- 金价：国内 AU9999 元/克；刷新失败保留上次成功价
- 私人站点：密码-only，不开放注册
- 小爱：本批未改；深度能力另开会话

## Known pitfalls

- 路径含 `'`（`coffeeCookie'sHomePage`）时 PowerShell/`working_directory` 易炸 → `git -C "D:/AI/coffeeCookie'sHomePage"` 或 `subst`
- `load-service` 需本机 `.config/service-config.yml`（gitignore）
- Vitest 断言可全绿但进程仍可能 exit 1（MSW/happy-dom）
- GitHub push 偶发 443 reset；可重试 `-c http.version=HTTP/1.1`
- Docker CLI 未必安装；compose 未在本机实测 `up`

## How to run / verify

```text
git checkout master
git pull origin master

cd api && .venv\Scripts\python.exe -m pytest tests -q
start.bat
# 或: .\.skills\load-service\load-service.ps1 -SkipTunnel
```

Login: `admin` / `admin123`  
PR: https://github.com/linli0/linlisHomePage/pull/7 · https://github.com/linli0/linlisHomePage/pull/8

## Do not regress

- 不要把默认 LLM / load-service 改回 DeepSeek / Spring `:8080`
- 不要对 GET 强加 `Content-Type: application/json`
- 不要 commit `api/.env`、`.config/` 凭证、`.cursor/settings.json`

## Open follow-ups

- **Wiki 写作 UI**（管理员 Markdown 编辑；API 已有 CRUD）
- **金价假历史**：停 `random.uniform` 造点或标「示意」
- 设置页推特关键词与后端采集打通，或删掉死配置
- 清理 `doc/deployment`、`项目架构.md` 残留 Spring 叙述
- Vitest exit 1 根治；裸域 → www；生产冷启动/内存实测
- 小爱 Panel/Codex/Debug 深度打磨（另开对话）
