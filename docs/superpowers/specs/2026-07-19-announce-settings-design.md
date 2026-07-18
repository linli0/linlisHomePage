# Announce settings (Cursor / Codex / Subagent)

**Branch:** feature/miAi  
**Status:** Approved 2026-07-19 (option A)

## Settings

| Key | Default | Meaning |
|-----|---------|---------|
| `announceEnabled` | true | Master switch |
| `announceCursor` | true | Parent Cursor `turn_ended` |
| `announceCodex` | true | Codex `task_complete` |
| `announceSubagent` | false | Cursor path contains `/subagents/` |
| `announceDetail` | `brief` | `brief` \| `detailed` |

## Detail mode A

- **brief:** fixed short Chinese phrase
- **detailed:**
  - Subagent: `UpdateCurrentStep.final_summary` → `completed_subtitle` → last assistant text
  - Parent Cursor: last assistant `text` before `turn_ended`
  - Codex: `payload.last_agent_message`
- Truncate for TTS (~280 chars). No LLM.

## Parse

- Cursor: JSON `type == "turn_ended"` only (not substring)
- Parent vs subagent: path contains `subagents`
- Codex: `event_msg` + `payload.type == "task_complete"`
- Maintain per-file rolling summary/text state across reads
