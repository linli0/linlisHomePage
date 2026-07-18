"""HTTP + WebSocket routes for Xiaomi dialogue."""

from __future__ import annotations

import asyncio
import logging
from typing import Annotated, Any

from fastapi import APIRouter, Depends, HTTPException, WebSocket, WebSocketDisconnect
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.core.database import SessionLocal, get_db
from app.core.deps import get_current_user, require_admin
from app.core.security import validate_token
from app.models import User
from app.plugins.xiaomi.dialogue import hub, orchestrator, panel, settings_store
from app.plugins.xiaomi.dialogue.session import get_state
from app.schemas.common import Result
from app.services import llm as llm_service

logger = logging.getLogger(__name__)

router = APIRouter(tags=["xiaomi-dialogue"])


class UtteranceRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=2000)
    source: str = "web"


class ModeRequest(BaseModel):
    mode: str = Field(..., min_length=3, max_length=20)
    speak: bool = False


class SettingsUpdate(BaseModel):
    announceEnabled: bool | None = None
    voiceInputEnabled: bool | None = None
    provider: str | None = None
    announceCursor: bool | None = None
    announceCodex: bool | None = None
    announceSubagent: bool | None = None
    announceDetail: str | None = None


class PanelKeywordBody(BaseModel):
    keyword: str
    actionType: str
    payload: dict[str, Any] | None = None
    enabled: bool = True
    sortOrder: int = 0


@router.get("/dialogue/status")
def dialogue_status(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    st = get_state().snapshot()
    return Result.success(
        {
            **st,
            "settings": settings_store.as_dict(db),
            "llm": llm_service.provider_status(),
        }
    )


@router.get("/dialogue/messages")
def dialogue_messages(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(get_current_user)],
    limit: int = 50,
) -> Result[list]:
    return Result.success(orchestrator.recent_messages(db, limit=min(limit, 200)))


@router.post("/dialogue/utterance")
async def dialogue_utterance(
    req: UtteranceRequest,
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    try:
        data = await orchestrator.handle_utterance(req.text, source=req.source or "web")
        return Result.success(data)
    except Exception as exc:
        logger.exception("utterance failed")
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/dialogue/mode")
async def dialogue_mode(
    req: ModeRequest,
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    try:
        data = await orchestrator.set_mode(req.mode, speak=req.speak)
        return Result.success(data)
    except Exception as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.get("/dialogue/settings")
def get_settings(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    return Result.success(settings_store.as_dict(db))


@router.put("/dialogue/settings")
def put_settings(
    body: SettingsUpdate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    try:
        data = settings_store.update(
            db,
            announce_enabled=body.announceEnabled,
            voice_input_enabled=body.voiceInputEnabled,
            provider=body.provider,
            announce_cursor=body.announceCursor,
            announce_codex=body.announceCodex,
            announce_subagent=body.announceSubagent,
            announce_detail=body.announceDetail,
        )
        return Result.success(data)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.get("/panel/keywords")
def panel_list(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(get_current_user)],
) -> Result[list]:
    panel.seed_presets(db)
    return Result.success(panel.list_keywords(db))


@router.post("/panel/keywords")
def panel_create(
    body: PanelKeywordBody,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    try:
        row = panel.upsert_keyword(
            db,
            keyword=body.keyword,
            action_type=body.actionType,
            payload=body.payload,
            enabled=body.enabled,
            sort_order=body.sortOrder,
        )
        return Result.success(row)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.put("/panel/keywords/{keyword_id}")
def panel_update(
    keyword_id: int,
    body: PanelKeywordBody,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    try:
        row = panel.upsert_keyword(
            db,
            keyword=body.keyword,
            action_type=body.actionType,
            payload=body.payload,
            enabled=body.enabled,
            sort_order=body.sortOrder,
            keyword_id=keyword_id,
        )
        return Result.success(row)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.delete("/panel/keywords/{keyword_id}")
def panel_delete(
    keyword_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    ok = panel.delete_keyword(db, keyword_id)
    if not ok:
        raise HTTPException(status_code=404, detail="not found")
    return Result.success({"deleted": True})


@router.websocket("/dialogue/ws")
async def dialogue_ws(websocket: WebSocket) -> None:
    token = websocket.query_params.get("token")
    if not token or validate_token(token) is None:
        await websocket.close(code=4401)
        return
    await websocket.accept()
    q = hub.subscribe()
    try:
        # send snapshot + recent
        db = SessionLocal()
        try:
            msgs = orchestrator.recent_messages(db, 40)
            await websocket.send_json(
                {"type": "hello", "status": get_state().snapshot(), "messages": msgs}
            )
        finally:
            db.close()
        while True:
            try:
                event = await asyncio.wait_for(q.get(), timeout=25.0)
                await websocket.send_json(event)
            except asyncio.TimeoutError:
                await websocket.send_json({"type": "ping"})
    except WebSocketDisconnect:
        pass
    except Exception:
        logger.exception("dialogue ws error")
    finally:
        hub.unsubscribe(q)
