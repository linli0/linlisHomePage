---
name: xiaomi-cli
description: >-
  Integrate Xiaomi/XiaoAI speakers: web bind, LAN miIO + cloud TTS, multi-turn
  dialogue (wake 小爱小爱), DeepSeek/Ollama, Panel/Codex/Debug routes, completion
  announce. Use for 小米音箱, 小爱, MIOT, miIO, micli, passToken, dialogue, TTS.
---

# xiaomi-cli

## Capabilities (feature/miAi)

1. **Bind** admin web flow → SQLite `xiaomi_binding` (encrypted tokens)
2. **Control** miIO probe + cloud MIoT TTS (`siid=5,aiid=1`)
3. **Dialogue** `POST /api/xiaomi/dialogue/utterance` + WS `/api/xiaomi/dialogue/ws`
4. **Wake** local mic (SpeechRecognition) for「小爱小爱」; web typing always works
5. **Routes** Panel keywords / Codex CLI / Debug / free-form LLM（默认 Ollama，DeepSeek 备用）
6. **Announce** Cursor / Codex / 子 agent 可分别开关；力度 brief|detailed（方案 A）；固定话术不经 LLM

## Keywords

| 动作 | 词 |
|------|-----|
| 多轮 | 小爱小爱 / 小爱同学 |
| Codex | 进入codex模式、codex模式 / 退出codex模式 |
| Panel | 进入panel / 退出panel |
| Debug | 进入调试模式 / 退出调试模式 |

## Env

`AI_DEFAULT_PROVIDER=ollama`（推荐）, `DEEPSEEK_*` 可选回退, `XIAOMI_*` — see `api/.env.example`

## Half-duplex

While TTS/`speaking=true`, voice worker ignores mic input to avoid speaker→mic loop.

## Refs

- [reference.md](reference.md)
- `docs/superpowers/specs/2026-07-19-miai-dialogue-design.md`
