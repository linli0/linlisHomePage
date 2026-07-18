"""Panel keyword actions."""

from __future__ import annotations

import json
import logging
from typing import Any

from sqlalchemy.orm import Session

from app.models import XiaomiPanelKeyword
from app.plugins.xiaomi import binding
from app.services import gold_price as gold_service

logger = logging.getLogger(__name__)

PRESETS: list[dict[str, Any]] = [
    {
        "keyword": "今日金价",
        "action_type": "gold_price",
        "payload": {},
        "sort_order": 10,
    },
    {
        "keyword": "刷新金价",
        "action_type": "gold_refresh",
        "payload": {},
        "sort_order": 20,
    },
    {
        "keyword": "音箱状态",
        "action_type": "speaker_status",
        "payload": {},
        "sort_order": 30,
    },
    {
        "keyword": "帮助",
        "action_type": "help",
        "payload": {"screenOnly": True},
        "sort_order": 40,
    },
]


def seed_presets(db: Session) -> None:
    for p in PRESETS:
        exists = (
            db.query(XiaomiPanelKeyword)
            .filter(XiaomiPanelKeyword.keyword == p["keyword"])
            .first()
        )
        if exists:
            continue
        db.add(
            XiaomiPanelKeyword(
                keyword=p["keyword"],
                action_type=p["action_type"],
                payload=json.dumps(p.get("payload") or {}, ensure_ascii=False),
                enabled=True,
                sort_order=int(p.get("sort_order") or 0),
            )
        )
    db.commit()


def list_keywords(db: Session) -> list[dict]:
    rows = (
        db.query(XiaomiPanelKeyword)
        .order_by(XiaomiPanelKeyword.sort_order.asc(), XiaomiPanelKeyword.id.asc())
        .all()
    )
    return [_row_dict(r) for r in rows]


def _row_dict(r: XiaomiPanelKeyword) -> dict:
    try:
        payload = json.loads(r.payload) if r.payload else {}
    except Exception:
        payload = {}
    return {
        "id": r.id,
        "keyword": r.keyword,
        "actionType": r.action_type,
        "payload": payload,
        "enabled": r.enabled,
        "sortOrder": r.sort_order,
    }


def upsert_keyword(
    db: Session,
    *,
    keyword: str,
    action_type: str,
    payload: dict | None = None,
    enabled: bool = True,
    sort_order: int = 0,
    keyword_id: int | None = None,
) -> dict:
    keyword = keyword.strip()
    if not keyword:
        raise ValueError("keyword 不能为空")
    row = None
    if keyword_id is not None:
        row = db.query(XiaomiPanelKeyword).filter(XiaomiPanelKeyword.id == keyword_id).first()
    if row is None:
        row = (
            db.query(XiaomiPanelKeyword)
            .filter(XiaomiPanelKeyword.keyword == keyword)
            .first()
        )
    if row is None:
        row = XiaomiPanelKeyword(keyword=keyword)
        db.add(row)
    row.keyword = keyword
    row.action_type = action_type
    row.payload = json.dumps(payload or {}, ensure_ascii=False)
    row.enabled = enabled
    row.sort_order = sort_order
    db.commit()
    db.refresh(row)
    return _row_dict(row)


def delete_keyword(db: Session, keyword_id: int) -> bool:
    row = db.query(XiaomiPanelKeyword).filter(XiaomiPanelKeyword.id == keyword_id).first()
    if row is None:
        return False
    db.delete(row)
    db.commit()
    return True


def match_and_run(db: Session, text: str) -> dict[str, Any] | None:
    """Return { reply, tts: bool, actionType } or None if no match."""
    seed_presets(db)
    rows = (
        db.query(XiaomiPanelKeyword)
        .filter(XiaomiPanelKeyword.enabled.is_(True))
        .order_by(XiaomiPanelKeyword.sort_order.asc())
        .all()
    )
    t = text.strip()
    for r in rows:
        if r.keyword and r.keyword in t:
            return _execute(db, r)
    return None


def _execute(db: Session, r: XiaomiPanelKeyword) -> dict[str, Any]:
    try:
        payload = json.loads(r.payload) if r.payload else {}
    except Exception:
        payload = {}
    at = r.action_type
    screen_only = bool(payload.get("screenOnly"))

    if at == "gold_price":
        dto = gold_service.get_current_price(db, "CNY")
        reply = f"今日国内金价约 {dto.price:.2f} 元每克" if dto and dto.price else "暂无金价数据"
        return {"reply": reply, "tts": True, "actionType": at}

    if at == "gold_refresh":
        gold_service.refresh_from_sina(db)
        dto = gold_service.get_current_price(db, "CNY")
        reply = f"已刷新，金价约 {dto.price:.2f} 元每克" if dto and dto.price else "刷新完成，暂无数据"
        return {"reply": reply, "tts": True, "actionType": at}

    if at == "speaker_status":
        rt = binding.get_runtime(db)
        if rt is None:
            reply = "尚未绑定小爱音箱"
        else:
            reply = f"已绑定 {rt.name}，地址 {rt.ip}，型号 {rt.model or '未知'}"
        return {"reply": reply, "tts": True, "actionType": at}

    if at == "say":
        reply = str(payload.get("text") or "好的")
        return {"reply": reply, "tts": not screen_only, "actionType": at}

    if at == "help":
        reply = (
            "Panel 模式：可以说今日金价、刷新金价、音箱状态、帮助。"
            "说退出panel 回到多轮对话。"
        )
        return {"reply": reply, "tts": not screen_only, "actionType": at}

    reply = f"已触发动作 {at}"
    return {"reply": reply, "tts": True, "actionType": at}
