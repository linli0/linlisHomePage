"""Text utterance orchestrator for Xiaomi dialogue."""

from __future__ import annotations

import asyncio
import logging
from typing import Any

from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.database import SessionLocal
from app.models import XiaomiChatMessage
from app.plugins.xiaomi import miot
from app.plugins.xiaomi.dialogue import hub, keywords, panel, settings_store
from app.plugins.xiaomi.dialogue.codex import run_codex_prompt
from app.plugins.xiaomi.dialogue.session import Route, get_state
from app.services import llm as llm_service

logger = logging.getLogger(__name__)

SYSTEM_PROMPT = (
    "你是家居助手，通过小爱音箱语音回复。回答简短口语化，尽量不超过80字。"
)


async def handle_utterance(text: str, *, source: str = "web") -> dict[str, Any]:
    text = (text or "").strip()
    if not text:
        raise RuntimeError("空输入")

    db = SessionLocal()
    try:
        return await _handle(db, text, source=source)
    finally:
        db.close()


async def _handle(db: Session, text: str, *, source: str) -> dict[str, Any]:
    st = get_state()
    cfg = settings_store.as_dict(db)
    idle_sec = int(settings.xiaomi_dialogue_idle_sec)

    idle_notify = False
    immediate: dict[str, Any] | None = None
    speak: dict[str, Any] | None = None
    codex_text: str | None = None
    panel_action: dict[str, Any] | None = None
    llm_action: dict[str, Any] | None = None
    user_route = Route.IDLE.value

    with st.lock:
        if st.idle_expired(idle_sec) and st.route != Route.IDLE:
            st.route = Route.IDLE
            st.history.clear()
            idle_notify = True

        if st.speaking and source != "web":
            immediate = {"ignored": True, "reason": "speaking"}
        else:
            st.touch()
            user_route = st.route.value

            if keywords.match_exit_multi(text):
                st.route = Route.IDLE
                st.history.clear()
                speak = {
                    "reply": "已退出多轮对话",
                    "route": "idle",
                    "provider": "system",
                }
            elif st.route == Route.IDLE:
                if keywords.match_wake(text):
                    st.route = Route.MULTI
                    speak = {
                        "reply": "已进入多轮对话，可以说进入panel、codex模式或直接提问",
                        "route": "multi",
                        "provider": "system",
                    }
                elif source == "web":
                    # Web typing: auto-enter multi and continue routing this utterance
                    st.route = Route.MULTI
                else:
                    immediate = {
                        "reply": "请先说小爱小爱进入多轮对话",
                        "route": "idle",
                        "tts": False,
                    }

            if immediate is None and speak is None:
                route = st.route

                if route in (Route.MULTI, Route.DEBUG):
                    if keywords.match_enter_debug(text) and route != Route.DEBUG:
                        st.route = Route.DEBUG
                        speak = {
                            "reply": "已进入调试模式，Codex 与 Panel 关键词同时生效",
                            "route": "debug",
                            "provider": "system",
                        }
                    elif keywords.match_exit_debug(text) and route == Route.DEBUG:
                        st.route = Route.MULTI
                        speak = {
                            "reply": "已退出调试模式",
                            "route": "multi",
                            "provider": "system",
                        }
                    elif keywords.match_enter_codex(text):
                        st.route = Route.CODEX
                        speak = {
                            "reply": "已进入 Codex 模式，后续内容将发给 Codex",
                            "route": "codex",
                            "provider": "system",
                        }
                    elif keywords.match_enter_panel(text):
                        st.route = Route.PANEL
                        speak = {
                            "reply": "已进入 Panel 模式，请说关键词，例如今日金价",
                            "route": "panel",
                            "provider": "system",
                        }

                if speak is None and st.route == Route.CODEX:
                    if keywords.match_exit_codex(text):
                        st.route = Route.MULTI
                        speak = {
                            "reply": "已退出 Codex 模式",
                            "route": "multi",
                            "provider": "system",
                        }
                    else:
                        codex_text = text

                if speak is None and codex_text is None and st.route == Route.PANEL:
                    if keywords.match_exit_panel(text):
                        st.route = Route.MULTI
                        speak = {
                            "reply": "已退出 Panel 模式",
                            "route": "multi",
                            "provider": "system",
                        }
                    else:
                        matched = panel.match_and_run(db, text)
                        if matched is None:
                            speak = {
                                "reply": "未匹配关键词",
                                "route": "panel",
                                "provider": "panel",
                            }
                        else:
                            panel_action = {
                                "reply": matched["reply"],
                                "route": "panel",
                                "provider": "panel",
                                "do_tts": bool(matched.get("tts", True)),
                            }

                if (
                    speak is None
                    and codex_text is None
                    and panel_action is None
                    and st.route == Route.DEBUG
                ):
                    if keywords.match_exit_codex(text):
                        speak = {
                            "reply": "调试模式下无需退出 Codex 子路由",
                            "route": "debug",
                            "provider": "system",
                            "do_tts": False,
                        }
                    else:
                        matched = panel.match_and_run(db, text)
                        if matched is not None:
                            panel_action = {
                                "reply": matched["reply"],
                                "route": "debug",
                                "provider": "panel",
                                "do_tts": bool(matched.get("tts", True)),
                            }

                if (
                    speak is None
                    and codex_text is None
                    and panel_action is None
                    and llm_action is None
                ):
                    provider = cfg.get("provider") or "deepseek"
                    llm_action = {
                        "text": text,
                        "hist": list(st.history[-12:]),
                        "provider": provider,
                        "route": st.route.value,
                    }

    if immediate:
        if not immediate.get("ignored"):
            await _persist_and_broadcast(db, "user", text, route=user_route)
        return immediate

    await _persist_and_broadcast(db, "user", text, route=user_route)

    if idle_notify:
        await hub.publish(
            {"type": "status", "route": "idle", "message": "超时退出多轮对话"}
        )

    if speak:
        await _speak_and_reply(
            db,
            st,
            speak["reply"],
            route=speak["route"],
            provider=speak["provider"],
            do_tts=speak.get("do_tts", True),
        )
        out = {"reply": speak["reply"], "route": speak["route"]}
        if speak.get("provider"):
            out["provider"] = speak["provider"]
        return out

    if codex_text is not None:
        try:
            out = await run_codex_prompt(codex_text)
        except Exception as exc:
            out = f"Codex 调用失败：{exc}"
        await _speak_and_reply(db, st, out, route="codex", provider="codex")
        return {"reply": out, "route": "codex", "provider": "codex"}

    if panel_action:
        await _speak_and_reply(
            db,
            st,
            panel_action["reply"],
            route=panel_action["route"],
            provider=panel_action["provider"],
            do_tts=panel_action.get("do_tts", True),
        )
        return {
            "reply": panel_action["reply"],
            "route": panel_action["route"],
            "provider": panel_action["provider"],
        }

    if llm_action:
        provider = llm_action["provider"]
        try:
            result = await llm_service.chat_complete(
                llm_action["text"],
                system=SYSTEM_PROMPT,
                history=llm_action["hist"],
                provider=provider,
                max_tokens=512,
            )
            reply = result["text"]
            used = result.get("provider")
        except Exception as exc:
            reply = f"回答失败：{exc}"
            used = "error"
        with st.lock:
            st.history.append({"role": "user", "content": text})
            st.history.append({"role": "assistant", "content": reply})
            route = st.route.value
        await _speak_and_reply(
            db, st, reply, route=route, provider=used or provider
        )
        return {"reply": reply, "route": route, "provider": used}

    return {"reply": "", "route": get_state().route.value}


async def _speak_and_reply(
    db: Session,
    st,
    reply: str,
    *,
    route: str,
    provider: str | None,
    do_tts: bool = True,
) -> None:
    await _persist_and_broadcast(db, "assistant", reply, route=route, provider=provider)
    if not do_tts:
        await hub.publish({"type": "status", **st.snapshot()})
        return

    st.speaking = True
    st.return_route = st.route if st.route != Route.SPEAKING else st.return_route
    await hub.publish({"type": "status", **st.snapshot(), "speaking": True})
    try:
        await miot.tts(reply[:500])
    except Exception as exc:
        logger.warning("TTS failed: %s", exc)
        await hub.publish({"type": "error", "message": f"TTS 失败: {exc}"})
    finally:
        cooldown = max(0, settings.xiaomi_tts_cooldown_ms) / 1000.0
        if cooldown:
            await asyncio.sleep(cooldown)
        st.speaking = False
        await hub.publish({"type": "status", **st.snapshot(), "speaking": False})


async def _persist_and_broadcast(
    db: Session,
    role: str,
    content: str,
    *,
    route: str | None = None,
    provider: str | None = None,
) -> None:
    row = XiaomiChatMessage(role=role, content=content, route=route, provider=provider)
    db.add(row)
    db.commit()
    db.refresh(row)
    await hub.publish(
        {
            "type": "message",
            "id": row.id,
            "role": role,
            "content": content,
            "route": route,
            "provider": provider,
            "createdAt": row.created_at.isoformat() if row.created_at else None,
        }
    )


def recent_messages(db: Session, limit: int = 50) -> list[dict]:
    rows = (
        db.query(XiaomiChatMessage)
        .order_by(XiaomiChatMessage.id.desc())
        .limit(limit)
        .all()
    )
    rows = list(reversed(rows))
    return [
        {
            "id": r.id,
            "role": r.role,
            "content": r.content,
            "route": r.route,
            "provider": r.provider,
            "createdAt": r.created_at.isoformat() if r.created_at else None,
        }
        for r in rows
    ]


async def set_mode(mode: str, *, speak: bool = False) -> dict[str, Any]:
    """Switch dialogue route without requiring keyword utterance."""
    allowed = {
        "idle": Route.IDLE,
        "multi": Route.MULTI,
        "codex": Route.CODEX,
        "panel": Route.PANEL,
        "debug": Route.DEBUG,
    }
    key = (mode or "").strip().lower()
    if key not in allowed:
        raise RuntimeError("无效模式，可选：idle / multi / panel / codex / debug")

    st = get_state()
    db = SessionLocal()
    try:
        with st.lock:
            st.route = allowed[key]
            st.touch()
            if key == "idle":
                st.history.clear()
        labels = {
            "idle": "已回到空闲",
            "multi": "已切换到多轮对话",
            "codex": "已切换到 Codex 模式",
            "panel": "已切换到 Panel 模式",
            "debug": "已切换到调试模式",
        }
        reply = labels[key]
        await hub.publish({"type": "status", **st.snapshot()})
        if speak:
            await _speak_and_reply(db, st, reply, route=key, provider="ui", do_tts=True)
        else:
            await _persist_and_broadcast(db, "system", reply, route=key, provider="ui")
        return {"route": key, "reply": reply, "speaking": speak}
    finally:
        db.close()


async def announce(text: str, *, kind: str = "announce") -> None:
    """Completion announce with half-duplex TTS (respects settings)."""
    db = SessionLocal()
    try:
        cfg = settings_store.as_dict(db)
        if not cfg.get("announceEnabled", True):
            await hub.publish({"type": "announce_skipped", "message": text, "kind": kind})
            return
        st = get_state()
        await _speak_and_reply(
            db, st, text, route=st.route.value, provider="announce", do_tts=True
        )
        await hub.publish({"type": "announce", "kind": kind, "message": text})
    finally:
        db.close()
