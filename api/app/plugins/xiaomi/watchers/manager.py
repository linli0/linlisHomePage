"""Start/stop file watchers."""

from __future__ import annotations

import asyncio
import logging
import time
from pathlib import Path

from app.core.config import settings
from app.core.database import SessionLocal
from app.plugins.xiaomi.dialogue import settings_store
from app.plugins.xiaomi.dialogue.orchestrator import announce

logger = logging.getLogger(__name__)

_task: asyncio.Task | None = None
_last_announce: dict[str, float] = {}


def _debounce(key: str) -> bool:
    now = time.time()
    last = _last_announce.get(key, 0)
    if now - last < settings.xiaomi_announce_debounce_sec:
        return False
    _last_announce[key] = now
    return True


def _cursor_dir() -> Path:
    if settings.cursor_transcripts_dir:
        return Path(settings.cursor_transcripts_dir)
    # default: this project's agent-transcripts
    home = Path.home() / ".cursor" / "projects"
    # try coffeeCookie path variants
    candidates = list(home.glob("*coffeeCookie*")) + list(home.glob("*HomePage*"))
    for c in candidates:
        t = c / "agent-transcripts"
        if t.is_dir():
            return t
    return home


def _codex_dir() -> Path:
    if settings.codex_sessions_dir:
        return Path(settings.codex_sessions_dir)
    return Path.home() / ".codex" / "sessions"


async def _watch_loop() -> None:
    cursor_offsets: dict[str, int] = {}
    codex_offsets: dict[str, int] = {}
    # seed offsets at EOF
    await asyncio.to_thread(_seed_offsets, _cursor_dir(), cursor_offsets)
    await asyncio.to_thread(_seed_offsets, _codex_dir(), codex_offsets)

    while True:
        try:
            db = SessionLocal()
            try:
                enabled = settings_store.as_dict(db).get("announceEnabled", True)
            finally:
                db.close()
            if enabled:
                await _scan_cursor(cursor_offsets)
                await _scan_codex(codex_offsets)
        except asyncio.CancelledError:
            raise
        except Exception:
            logger.exception("watcher loop error")
        await asyncio.sleep(2.0)


def _seed_offsets(root: Path, offsets: dict[str, int]) -> None:
    if not root.is_dir():
        return
    for p in root.rglob("*.jsonl"):
        try:
            offsets[str(p)] = p.stat().st_size
        except OSError:
            pass


async def _scan_cursor(offsets: dict[str, int]) -> None:
    root = _cursor_dir()
    if not root.is_dir():
        return
    for p in await asyncio.to_thread(lambda: list(root.rglob("*.jsonl"))):
        key = str(p)
        try:
            size = p.stat().st_size
        except OSError:
            continue
        pos = offsets.get(key, size)
        if size < pos:
            pos = 0
        if size == pos:
            offsets[key] = size
            continue
        try:
            with p.open("r", encoding="utf-8", errors="replace") as f:
                f.seek(pos)
                chunk = f.read()
                offsets[key] = f.tell()
        except OSError:
            continue
        if '"turn_ended"' in chunk or '"type": "turn_ended"' in chunk:
            if _debounce(f"cursor:{key}"):
                await announce("Cursor 任务完成", kind="cursor")


async def _scan_codex(offsets: dict[str, int]) -> None:
    root = _codex_dir()
    if not root.is_dir():
        return
    for p in await asyncio.to_thread(lambda: list(root.rglob("*.jsonl"))):
        key = str(p)
        try:
            size = p.stat().st_size
        except OSError:
            continue
        pos = offsets.get(key, size)
        if size < pos:
            pos = 0
        if size == pos:
            offsets[key] = size
            continue
        try:
            with p.open("r", encoding="utf-8", errors="replace") as f:
                f.seek(pos)
                chunk = f.read()
                offsets[key] = f.tell()
        except OSError:
            continue
        if "task_complete" in chunk:
            if _debounce(f"codex:{key}"):
                await announce("Codex 任务完成", kind="codex")


def start_watchers() -> None:
    global _task
    if _task and not _task.done():
        return
    try:
        loop = asyncio.get_running_loop()
    except RuntimeError:
        return
    _task = loop.create_task(_watch_loop(), name="xiaomi-watchers")
    logger.info("xiaomi completion watchers started")


async def stop_watchers() -> None:
    global _task
    if _task:
        _task.cancel()
        try:
            await _task
        except asyncio.CancelledError:
            pass
        _task = None
