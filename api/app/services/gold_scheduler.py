"""Background daily refresh for AU9999 gold price (Asia/Shanghai 09:30)."""

from __future__ import annotations

import logging
import threading
from datetime import datetime, timedelta, timezone

from app.core.database import SessionLocal
from app.services import gold_price as gold_service

logger = logging.getLogger(__name__)

try:
    from zoneinfo import ZoneInfo

    _TZ = ZoneInfo("Asia/Shanghai")
except Exception:  # pragma: no cover
    _TZ = timezone(timedelta(hours=8))

_stop = threading.Event()
_thread: threading.Thread | None = None


def _seconds_until_next_0930() -> float:
    now = datetime.now(_TZ)
    target = now.replace(hour=9, minute=30, second=0, microsecond=0)
    if now >= target:
        target += timedelta(days=1)
    return max(1.0, (target - now).total_seconds())


def _loop() -> None:
    while not _stop.is_set():
        wait = _seconds_until_next_0930()
        logger.info("Next gold refresh in %.0f seconds", wait)
        if _stop.wait(wait):
            break
        db = SessionLocal()
        try:
            gold_service.refresh_from_sina(db, force=True)
        except Exception:
            logger.exception("Scheduled gold refresh failed")
        finally:
            db.close()


def start_gold_scheduler() -> None:
    global _thread
    if _thread and _thread.is_alive():
        return
    _stop.clear()
    _thread = threading.Thread(target=_loop, name="gold-daily-refresh", daemon=True)
    _thread.start()
    logger.info("Gold daily refresh scheduler started")


def stop_gold_scheduler() -> None:
    _stop.set()
