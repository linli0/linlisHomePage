"""Parse Cursor / Codex transcript chunks into announce payloads (no LLM)."""

from __future__ import annotations

import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Any


TTS_MAX_CHARS = 280

_BRIEF = {
    "cursor": "Cursor 任务完成",
    "codex": "Codex 任务完成",
    "subagent": "子任务完成",
}


@dataclass
class FileRollup:
    """Rolling context for a Cursor jsonl file."""

    last_text: str = ""
    last_summary: str = ""
    last_subtitle: str = ""


@dataclass
class AnnounceEvent:
    kind: str  # cursor | codex | subagent
    brief: str
    detailed: str


def is_subagent_path(path: str | Path) -> bool:
    parts = Path(path).parts
    return "subagents" in parts


def truncate_tts(text: str, limit: int = TTS_MAX_CHARS) -> str:
    t = re.sub(r"\s+", " ", (text or "").strip())
    # strip light markdown for speech
    t = re.sub(r"[#*`>|]+", "", t)
    if len(t) <= limit:
        return t
    return t[: max(0, limit - 1)].rstrip() + "…"


def format_announce(event: AnnounceEvent, *, detail: str) -> str:
    mode = (detail or "brief").lower()
    if mode == "detailed" and event.detailed.strip():
        return truncate_tts(event.detailed)
    return event.brief


def _assistant_text_blocks(obj: dict[str, Any]) -> None | tuple[str, str, str]:
    """Return (text, summary, subtitle) updates from one jsonl object."""
    if obj.get("role") != "assistant":
        return None
    msg = obj.get("message") or {}
    content = msg.get("content")
    if not isinstance(content, list):
        return None
    text_parts: list[str] = []
    summary = ""
    subtitle = ""
    for block in content:
        if not isinstance(block, dict):
            continue
        if block.get("type") == "text" and block.get("text"):
            text_parts.append(str(block["text"]))
        if block.get("type") == "tool_use" and block.get("name") == "UpdateCurrentStep":
            inp = block.get("input") or {}
            if isinstance(inp, dict):
                if inp.get("final_summary"):
                    summary = str(inp["final_summary"])
                if inp.get("completed_subtitle"):
                    subtitle = str(inp["completed_subtitle"])
    text = "\n".join(text_parts).strip()
    return text, summary, subtitle


def feed_cursor_line(state: FileRollup, line: str, *, subagent: bool) -> AnnounceEvent | None:
    line = (line or "").strip()
    if not line:
        return None
    try:
        obj = json.loads(line)
    except json.JSONDecodeError:
        return None
    if not isinstance(obj, dict):
        return None

    if obj.get("type") == "turn_ended":
        kind = "subagent" if subagent else "cursor"
        if subagent:
            detailed = state.last_summary or state.last_subtitle or state.last_text
        else:
            detailed = state.last_text or state.last_summary or state.last_subtitle
        return AnnounceEvent(
            kind=kind,
            brief=_BRIEF[kind],
            detailed=detailed or _BRIEF[kind],
        )

    upd = _assistant_text_blocks(obj)
    if upd is None:
        return None
    text, summary, subtitle = upd
    if text:
        state.last_text = text
    if summary:
        state.last_summary = summary
    if subtitle:
        state.last_subtitle = subtitle
    return None


def feed_cursor_chunk(
    state: FileRollup, chunk: str, *, subagent: bool
) -> list[AnnounceEvent]:
    out: list[AnnounceEvent] = []
    for line in chunk.splitlines():
        ev = feed_cursor_line(state, line, subagent=subagent)
        if ev is not None:
            out.append(ev)
    return out


def feed_codex_chunk(chunk: str) -> list[AnnounceEvent]:
    out: list[AnnounceEvent] = []
    for line in chunk.splitlines():
        line = (line or "").strip()
        if not line:
            continue
        try:
            obj = json.loads(line)
        except json.JSONDecodeError:
            continue
        if not isinstance(obj, dict):
            continue
        payload = obj.get("payload")
        if obj.get("type") == "event_msg" and isinstance(payload, dict):
            if payload.get("type") == "task_complete":
                msg = str(payload.get("last_agent_message") or "").strip()
                out.append(
                    AnnounceEvent(
                        kind="codex",
                        brief=_BRIEF["codex"],
                        detailed=msg or _BRIEF["codex"],
                    )
                )
        elif obj.get("type") == "task_complete":
            # tolerate flatter shapes
            msg = str(obj.get("last_agent_message") or "").strip()
            out.append(
                AnnounceEvent(
                    kind="codex",
                    brief=_BRIEF["codex"],
                    detailed=msg or _BRIEF["codex"],
                )
            )
    return out
