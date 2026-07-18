"""In-memory pub/sub for dialogue events (WS + recent history)."""

from __future__ import annotations

import asyncio
import json
import logging
from collections import deque
from typing import Any

logger = logging.getLogger(__name__)

_loop: asyncio.AbstractEventLoop | None = None
_subscribers: set[asyncio.Queue] = set()
_recent: deque[dict[str, Any]] = deque(maxlen=200)


def recent_events(limit: int = 50) -> list[dict[str, Any]]:
    items = list(_recent)
    return items[-limit:]


def bind_loop(loop: asyncio.AbstractEventLoop) -> None:
    global _loop
    _loop = loop


def notify_status_sync() -> None:
    """Broadcast dialogue status from a background thread (voice worker)."""
    from app.plugins.xiaomi.dialogue.session import get_state

    loop = _loop
    if loop is None or not loop.is_running():
        return
    event = {"type": "status", **get_state().snapshot()}
    asyncio.run_coroutine_threadsafe(publish(event), loop)


async def publish(event: dict[str, Any]) -> None:
    _recent.append(event)
    dead: list[asyncio.Queue] = []
    for q in list(_subscribers):
        try:
            q.put_nowait(event)
        except asyncio.QueueFull:
            dead.append(q)
        except Exception:
            dead.append(q)
    for q in dead:
        _subscribers.discard(q)


def subscribe() -> asyncio.Queue:
    q: asyncio.Queue = asyncio.Queue(maxsize=100)
    _subscribers.add(q)
    return q


def unsubscribe(q: asyncio.Queue) -> None:
    _subscribers.discard(q)


def dumps(event: dict[str, Any]) -> str:
    return json.dumps(event, ensure_ascii=False)
