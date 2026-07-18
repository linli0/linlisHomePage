---
date: 2026-07-19
topic: miai-ops-handoff
branch: feature/miAi
status: active
---

# Epitaph: miAi 运维与播报配置收尾

承接已归档 `archive/2026-07-19-miai-dialogue.md`（对话总线本体）。本会话增量：测试合入、Ollama 优先、公网域名、完成播报可配置。

## Built this session

- 过时 pytest 对齐（quant/xiaomi 默认启用、AI offline mock、plugin 鉴权形状）；全量曾 **66 passed** 后开 PR #5 → master
- LLM：**Ollama 优先**，DeepSeek 回退；本地 `AI_DEFAULT_PROVIDER=ollama`（勿提交 `.env`）；启动时可将 DB 中 stale `deepseek` 迁到 ollama
- 固定话术（唤醒/退出/panel/announce）不经 LLM；多轮自由问答才 `chat_complete`
- 公网：重建 `.config/tunnel/opencode-config.yml` + `service-config.yml`（gitignore）；`my-tunnel` → `https://www.coffeecookie.online` → Vite `:3000` → API `:8000`
- 完成播报设置（方案 A）：Cursor / Codex / 子 agent 分开关；`brief` | `detailed`；解析见 `watchers/parse.py`
- Spec：`docs/superpowers/specs/2026-07-19-announce-settings-design.md`

## Key paths

- `api/app/services/llm.py` — Ollama primary
- `api/app/plugins/xiaomi/dialogue/settings_store.py` — settings + SQLite ALTER 新列
- `api/app/plugins/xiaomi/watchers/parse.py` / `manager.py` — 播报解析与开关
- `frontend/src/views/XiaomiView.vue` — 设置抽屉
- `.config/tunnel/opencode-config.yml` — 本地 tunnel（不入库）
- `.skills/load-service/SKILL.md` — OpenCode 域名方案说明
- PR：https://github.com/linli0/linlisHomePage/pull/5

## Locked product decisions

- 默认 LLM：`ollama`；DeepSeek 仅回退或 UI 显式选择
- 播报 detailed：子 agent 用 `final_summary`；主会话/Codex 用末段助手话，TTS 截断 ~280 字；不经 LLM
- 子 agent 播报默认 **关**；主 Cursor / Codex 默认开
- 公网固定域名用 existing-domain tunnel，不是 quick tunnel
- 鉴权策略本会话明确「先不修」——测试已对齐现状，勿为「更安全」擅自改产品行为 unless asked

## Known pitfalls

- **DeepSeek 用量**：曾因默认 deepseek + 多轮 ASR 自由问答暴涨；确认 `.env` 与设置抽屉均为 ollama，并重启 API
- **mode / 新路由**：改 FastAPI 后需重启 `:8000`；新 DB 列靠 `ensure_announce_columns` ALTER
- **Lock scope**：`st.lock` 内勿 await LLM/TTS（旧坑仍有效）
- **音量**：优先云端 MIoT；TTS 前勿阻塞本地 miIO set_volume
- **turn_ended**：必须 JSON `type === "turn_ended"`，禁止子串匹配（会误伤 Grep input）
- **主会话无官方摘要字段**：detailed 只能读末段 text，不是 Cursor UI 完成卡片
- **域名**：`www.coffeecookie.online` 已绑 tunnel；裸域 `coffeecookie.online` 有 A/AAAA 冲突，不能简单再挂 CNAME
- **Vite Host**：`allowedHosts` 含 `www.coffeecookie.online`；tunnel 指向 `127.0.0.1:3000`
- **`.config/` gitignored**：换机需重建；凭证在 `~/.cloudflared/d2f7ff06-….json`，tunnel 名 `my-tunnel`
- 勿把 API key / passToken / tunnel secret 写入 epitaph 或 commit

## How to run / verify

```text
# API
cd api && .venv\Scripts\python.exe -m uvicorn app.main:app --reload --port 8000

# Frontend
cd frontend && npm run dev   # :3000

# Tunnel（需本地 3000+8000 已起）
cloudflared tunnel --config .config/tunnel/opencode-config.yml run
# 公网: https://www.coffeecookie.online

# Tests
cd api && .venv\Scripts\python.exe -m pytest tests/test_announce_parse.py tests/test_llm_provider.py tests/test_dialogue_no_llm_for_fixed.py -q
```

Login: admin / admin123

## Do not regress

- 不要把默认 provider 改回 deepseek
- 不要对 transcript 用 `"turn_ended" in chunk` 子串判断
- 不要把 LLM 放进固定话术 / announce 路径
- 不要 commit `api/.env` 或 `.config/` 凭证
- 不要在未重启 API 时假定新 settings 列 / 路由已加载

## Open follow-ups

- 前端 Vitest 与改版 UI 可能仍不对齐（构建不阻塞）
- AGENTS.md 仍偏 Spring Boot；运行时以 FastAPI `api/` 为准
- 裸域 → www 跳转（Cloudflare Page Rule / 改 DNS）未做
- load-service.ps1 仍偏 Spring Boot `:8080`，与当前 Vite+FastAPI 栈不一致
- PR #5 合并状态与后续 commit（Ollama/announce/tunnel）是否需再推一层
- Panel/Codex/Debug 深度能力可继续打磨
