# xiaomi-cli reference

## Dialogue API

| Method | Path | Auth |
|--------|------|------|
| GET | `/api/xiaomi/dialogue/status` | login |
| GET | `/api/xiaomi/dialogue/messages` | login |
| POST | `/api/xiaomi/dialogue/utterance` | login |
| GET/PUT | `/api/xiaomi/dialogue/settings` | login / admin |
| WS | `/api/xiaomi/dialogue/ws?token=` | JWT query |
| CRUD | `/api/xiaomi/panel/keywords` | admin write |
| GET | `/api/xiaomi/voice/status` | login |

## Tables

`xiaomi_dialogue_settings`, `xiaomi_chat_messages`, `xiaomi_panel_keywords`, `xiaomi_binding`

## Watchers

- Cursor: `~/.cursor/projects/*/agent-transcripts/**/*.jsonl` → `turn_ended`
- Codex: `~/.codex/sessions/**/*.jsonl` → `task_complete`
- Debounce: `XIAOMI_ANNOUNCE_DEBOUNCE_SEC` (default 30)

## Voice

Optional `SpeechRecognition` + `PyAudio`. If missing, `voice/status.degraded=true`; web input still works.
