---
date: 2026-07-19
topic: miai-dialogue
branch: feature/miAi
status: archived
---

# Epitaph: miAi 对话与 Xiaomi UI 收尾

> 已归档。增量运维见 active：`docs/epitaph/2026-07-19-miai-ops-handoff.md`（Ollama 优先等已覆盖本文「DeepSeek primary」）。

## Built this session

- 小米对话总线：本地唤醒「小爱小爱」+ Web 输入；routes：`idle` / `multi` / `panel` / `codex` / `debug`
- LLM：DeepSeek 主路径（`api/app/services/llm.py`），Ollama 回退；完成播报 watchers
- 模式切换 API：`POST /api/xiaomi/dialogue/mode` → `orchestrator.set_mode`（修「卡在 multi」；锁只包状态变更，不包 LLM/TTS）
- 前端 `XiaomiView`：TTS vs listening 波浪动画；设置抽屉（音量/对话选项/Panel 关键词/绑定）；模式按钮
- 音量：优先云端 MIoT `set_volume`（本地 miIO 易超时）；TTS 前勿阻塞等本地设音量
- 网页绑定小米账号（passToken 加密落库）+ 测试
- 国内金价（新浪 AU9999）与前端咖啡台 UI 重写（同日相关 specs，见 `docs/superpowers/`）

## Key paths

- `api/app/plugins/xiaomi/dialogue/` — orchestrator, routes, session, hub, keywords, panel, codex, settings_store
- `api/app/plugins/xiaomi/miot.py`, `router.py`, `binding.py`, `voice/worker.py`, `watchers/manager.py`
- `api/app/services/llm.py`
- `frontend/src/views/XiaomiView.vue`, `frontend/src/api/xiaomi.ts`
- `api/tests/test_xiaomi_dialogue_mode.py`, `test_xiaomi_binding.py`, `test_xiaomi_volume.py`, `test_xiaomi_dialogue_*.py`
- Specs: `docs/superpowers/specs/2026-07-19-miai-dialogue-design.md`, `...-xiaomi-web-bind-design.md`
- Project skill: `.cursor/skills/xiaomi-cli/SKILL.md`

## Locked product decisions

- 输入：本地 mic 唤醒 + 网页打字；**不做**小米云历史轮询
- 半双工：TTS 时暂停 mic/ASR（`speaking` 时忽略非 web 输入）
- DeepSeek primary；routes 空闲/多轮/panel/codex/debug；退出关键词 + 约 2 分钟 idle
- 绑定真相源：DB 优先于 `api/.env`；单台音箱

## Known pitfalls

- **Lock scope**：`st.lock` 不可在持锁时 await LLM/TTS，否则 UI `set_mode` 会卡住
- **speaking 禁用按钮**：播报中前端会反映 `speaking`；模式切换应用 `speak=false` 做静默切换
- **mode 404 until restart**：新路由需重启 FastAPI `:8000` 才生效
- **本地 miIO volume 超时**：音量走云端 MIoT；TTS 前不要 await 本地 set_volume
- **TTS 可能改音量副作用**：留意 LX06 云 TTS 行为
- **DeepSeek `reasoning_content`**：解析回复时勿把 reasoning 当最终答案
- **LX06 cloud TTS**：依赖云会话；绑定失效则播报失败

## How to run / verify

```text
# API
cd api && .venv\Scripts\python.exe -m uvicorn app.main:app --reload --port 8000

# Frontend
cd frontend && npm run dev   # :3000，代理 /api

# Login
admin / admin123

# Cheap tests
cd api && .venv\Scripts\python.exe -m pytest tests/test_xiaomi_dialogue_mode.py tests/test_xiaomi_binding.py tests/test_xiaomi_volume.py -q
```

## Do not regress

- 不要把 LLM/TTS 放进 `st.lock` 临界区
- 不要恢复「仅本地 miIO 设音量」为主路径
- 不要改回云端对话历史轮询
- 不要在未重启 API 时假定新 endpoint 已加载
- 不要把密钥写进 epitaph / commit `.env`

## Open follow-ups

- 前端 Vitest 与重写后的 `frontend/src` 可能仍不完全对齐（构建不阻塞）
- AGENTS.md 仍有大量 Spring Boot 时代描述；运行时以 FastAPI `api/` 为准
- Panel/Codex/Debug 深度能力与完成播报路径可持续打磨
- 本文件读完吸收后，接手 agent 可删或移入 `docs/epitaph/archive/`
