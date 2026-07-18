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
from app.plugins.xiaomi.watchers.parse import (
    FileRollup,
    feed_codex_chunk,
    feed_cursor_chunk,
    format_announce,
    is_subagent_path,
)

logger = logging.getLogger(__name__)

_task: asyncio.Task | None = None
_last_announce: dict[str, float] = {}
_cursor_rollup: dict[str, FileRollup] = {}


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
    home = Path.home() / ".cursor" / "projects"
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
    await asyncio.to_thread(_seed_offsets, _cursor_dir(), cursor_offsets)
    await asyncio.to_thread(_seed_offsets, _codex_dir(), codex_offsets)

    while True:
        try:
            db = SessionLocal()
            try:
                cfg = settings_store.as_dict(db)
            finally:
                db.close()
            if cfg.get("announceEnabled", True):
                await _scan_cursor(cursor_offsets, cfg)
                await _scan_codex(codex_offsets, cfg)
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


def _read_new(path: Path, offsets: dict[str, int]) -> str | None:
    key = str(path)
    try:
        size = path.stat().st_size
    except OSError:
        return None
    pos = offsets.get(key, size)
    if size < pos:
        pos = 0
    if size == pos:
        offsets[key] = size
        return None
    try:
        with path.open("r", encoding="utf-8", errors="replace") as f:
            f.seek(pos)
            chunk = f.read()
            offsets[key] = f.tell()
    except OSError:
        return None
    return chunk or None


async def _scan_cursor(offsets: dict[str, int], cfg: dict) -> None:
    root = _cursor_dir()
    if not root.is_dir():
        return
    detail = cfg.get("announceDetail") or "brief"
    for p in await asyncio.to_thread(lambda: list(root.rglob("*.jsonl"))):
        chunk = await asyncio.to_thread(_read_new, p, offsets)
        if not chunk:
            continue
        key = str(p)
        sub = is_subagent_path(p)
        if sub and not cfg.get("announceSubagent", False):
            # still advance rollup so state stays warm if user enables later
            st = _cursor_rollup.setdefault(key, FileRollup())
            feed_cursor_chunk(st, chunk, subagent=True)
            continue
        if not sub and not cfg.get("announceCursor", True):
            st = _cursor_rollup.setdefault(key, FileRollup())
            feed_cursor_chunk(st, chunk, subagent=False)
            continue
        st = _cursor_rollup.setdefault(key, FileRollup())
        events = feed_cursor_chunk(st, chunk, subagent=sub)
        for ev in events:
            debounce_key = f"{ev.kind}:{key}"
            if not _debounce(debounce_key):
                continue
            text = format_announce(ev, detail=detail)
            await announce(text, kind=ev.kind)


async def _scan_codex(offsets: dict[str, int], cfg: dict) -> None:
    if not cfg.get("announceCodex", True):
        return
    root = _codex_dir()
    if not root.is_dir():
        return
    detail = cfg.get("announceDetail") or "brief"
    for p in await asyncio.to_thread(lambda: list(root.rglob("*.jsonl"))):
        chunk = await asyncio.to_thread(_read_new, p, offsets)
        if not chunk:
            continue
        key = str(p)
        for ev in feed_codex_chunk(chunk):
            if not _debounce(f"codex:{key}"):
                continue
            text = format_announce(ev, detail=detail)
            await announce(text, kind="codex")


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
