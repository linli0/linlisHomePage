"""
Background voice worker.

Uses SpeechRecognition when available. Wake phrase 「小爱小爱」 enters multi-turn;
subsequent phrases are forwarded as utterances. Respects half-duplex (speaking)
and voiceInputEnabled setting.
"""

from __future__ import annotations

import asyncio
import logging
import threading

from app.core.database import SessionLocal
from app.plugins.xiaomi.dialogue import settings_store
from app.plugins.xiaomi.dialogue import hub
from app.plugins.xiaomi.dialogue.keywords import match_wake
from app.plugins.xiaomi.dialogue.orchestrator import handle_utterance
from app.plugins.xiaomi.dialogue.session import get_state

logger = logging.getLogger(__name__)

_thread: threading.Thread | None = None
_stop = threading.Event()
_status = {"running": False, "engine": None, "error": None, "degraded": False}


def status() -> dict:
    return dict(_status)


def start_voice_worker() -> None:
    global _thread
    if _thread and _thread.is_alive():
        return
    _stop.clear()
    _thread = threading.Thread(target=_run, name="xiaomi-voice", daemon=True)
    _thread.start()


def stop_voice_worker() -> None:
    _stop.set()
    _set_listening(False)


def _set_listening(active: bool) -> None:
    st = get_state()
    with st.lock:
        if st.listening == active:
            return
        st.listening = active
    hub.notify_status_sync()


def _run() -> None:
    global _status
    try:
        import speech_recognition as sr  # type: ignore
    except Exception as exc:
        _status = {
            "running": False,
            "engine": None,
            "error": f"speech_recognition 未安装: {exc}",
            "degraded": True,
        }
        logger.warning("voice worker degraded: %s", exc)
        return

    recognizer = sr.Recognizer()
    try:
        mic = sr.Microphone()
    except Exception as exc:
        _status = {
            "running": False,
            "engine": "speech_recognition",
            "error": f"无法打开麦克风: {exc}",
            "degraded": True,
        }
        logger.warning("mic open failed: %s", exc)
        return

    _status = {"running": True, "engine": "speech_recognition", "error": None, "degraded": False}
    logger.info("voice worker started")

    with mic as source:
        try:
            recognizer.adjust_for_ambient_noise(source, duration=0.5)
        except Exception:
            pass

    while not _stop.is_set():
        db = SessionLocal()
        try:
            enabled = settings_store.as_dict(db).get("voiceInputEnabled", True)
        finally:
            db.close()
        if not enabled:
            _stop.wait(1.0)
            continue

        st = get_state()
        if st.speaking:
            _stop.wait(0.3)
            continue

        try:
            _set_listening(True)
            with mic as source:
                audio = recognizer.listen(source, timeout=2, phrase_time_limit=8)
        except Exception:
            continue
        finally:
            _set_listening(False)

        if st.speaking or _stop.is_set():
            continue

        text = ""
        try:
            # offline-ish: use recognize_google as default network ASR; fallback whisper if present
            text = recognizer.recognize_google(audio, language="zh-CN")
        except Exception:
            try:
                text = recognizer.recognize_sphinx(audio)  # type: ignore
            except Exception:
                continue

        text = (text or "").strip()
        if not text:
            continue

        st = get_state()
        if st.route.value == "idle" and not match_wake(text):
            # ignore non-wake in idle
            continue

        try:
            asyncio.run(handle_utterance(text, source="voice"))
        except Exception:
            logger.exception("voice utterance failed: %s", text)

    _status["running"] = False
    logger.info("voice worker stopped")
