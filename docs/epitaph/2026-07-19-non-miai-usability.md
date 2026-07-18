---
date: 2026-07-19
topic: non-miai-usability
branch: chore/fastapi-docs-and-compose
status: active
---

# Epitaph: 非小爱主站可用性通宵切片

用户睡眠中授权自主推进；**未改小爱/对话/播报**。PR：https://github.com/linli0/linlisHomePage/pull/7

## Built this session

- 文档/运维：`AGENTS.md`、`ROADMAP`、`load-service` v4（FastAPI `:8000` + Vite `:3000`）、compose env、`service-config.example.yml`
- 可用性：金价加载/刷新错误态（保留上次成功价）、首页板价失败提示、AI Ollama 状态条、Wiki/登录/推文空态与重试、设置页 AI 说明、README 对齐国内金价 + `start.bat`
- 前端测试：Vitest 排除 e2e/已删组件；Login/AIChat/auth/PriceChart/Profile 对齐 → **80 passed / 0 failed**（`npm run test:run` 仍可能 exit 1：MSW/happy-dom 噪音）
- Spec：`docs/superpowers/specs/2026-07-19-non-miai-usability-design.md`
- API pytest：**74 passed**（未回归）

## Key paths

- `.skills/load-service/load-service.ps1`
- `frontend/src/views/{GoldPrice,Home,AI,Articles,Login,Tweets,Settings}View.vue`
- `frontend/src/components/AIChat.vue` / `frontend/src/api/ai.ts`
- `frontend/vitest.config.ts` + `frontend/tests/**`
- `AGENTS.md` / `README.md` / `ROADMAP.md`

## Locked product decisions

- 日常叙事只宣传 FastAPI + Vite/Docker；Spring 仅 `legacy/`
- 金价 = 国内 AU9999 元/克；失败保留上次价
- AI 默认 Ollama；UI 明确不默认 DeepSeek
- 私人站点：密码-only，不开放注册
- 小爱相关本切片冻结

## Known pitfalls

- `load-service` 依赖本机 `.config/service-config.yml`（gitignore）；示例在 `.skills/load-service/service-config.example.yml`
- 路径含撇号时 PowerShell/`working_directory` 易炸；用 `git -C "D:/AI/coffeeCookie'sHomePage"` 或 `subst`
- Vitest：断言可全绿但进程 exit 1（MSW + happy-dom）；看用例计数而非只看 exit code
- Docker CLI 本机可能未装，compose 未实测 `up`
- 裸域 → www DNS/跳转仍未做（需 Cloudflare 手工）

## How to run / verify

```text
git checkout chore/fastapi-docs-and-compose
# 或看 PR #7

cd api && .venv\Scripts\python.exe -m pytest tests -q
cd frontend && npm run test:run   # 期望 80 passed；忽略 exit 1 噪音若断言全绿

start.bat
# 或 load-service: .\.skills\load-service\load-service.ps1 -SkipTunnel
```

Login: admin / admin123

## Do not regress

- 不要把默认 LLM 改回 deepseek
- 不要把 load-service 改回 Spring `:8080`
- 不要动小爱 dialogue/announce/watchers（除非用户点名）
- 不要 commit `api/.env` / `.config/` 凭证

## Open follow-ups

- 合并 PR #7
- **Wiki 写作 UI**：公开列表只读；API 已有 create/update，缺管理员 Markdown 编辑页（Home「去写第一篇」无入口）
- **金价历史可信度**：`gold_price.py` 历史不足时 `random.uniform` 造点 + 写死汇率；应停造假或标「示意」，主路径锁定 AU9999 元/克
- 设置页推特关键词仍是 localStorage 死配置，与 `TweetsView`/后端采集未打通（要么接线要么删设置项）
- 清理 `doc/deployment`、`项目架构.md` 残留 Spring 叙述
- Vitest 进程 exit 1（MSW/happy-dom）根治
- 裸域跳转；生产镜像冷启动/内存实测
- Panel/Codex/小爱深度能力另开会话

## Absorbed from explore backlog (已做)

- README / start.bat / FastAPI 叙事
- 金价/首页/AI/Wiki/登录错误与空态
- Vitest 排除 e2e + 对齐 Login/AIChat 等
- `request` 不再对 GET 强加 JSON Content-Type；`auth` 优先展示后端中文 `message`
