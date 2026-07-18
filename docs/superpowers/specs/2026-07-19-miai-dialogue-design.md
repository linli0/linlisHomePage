# miAi 小爱对话与完成播报

**Branch:** `feature/miAi`  
**Status:** Implemented per roadmap M0–M5

## Decisions

- Input: local mic wake「小爱小爱」+ web typing; no Xiaomi cloud history polling
- LLM: DeepSeek primary (`DEEPSEEK_*`), Ollama fallback
- Half-duplex: pause mic/ASR while TTS
- Announce: Cursor transcripts / Codex sessions / site AI done; toggleable
- Routes: multi → codex / panel / debug; exit keywords + 2 min idle

## Modules

- `api/app/services/llm.py`
- `api/app/plugins/xiaomi/dialogue/*`
- `api/app/plugins/xiaomi/watchers/manager.py`
- `api/app/plugins/xiaomi/voice/worker.py`
- `frontend/src/views/XiaomiView.vue`
