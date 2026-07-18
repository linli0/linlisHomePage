"""Dialogue settings persisted in SQLite."""

from __future__ import annotations

from sqlalchemy.orm import Session

from app.core.config import settings
from app.models import XiaomiDialogueSettings


def get_or_create(db: Session) -> XiaomiDialogueSettings:
    row = db.query(XiaomiDialogueSettings).order_by(XiaomiDialogueSettings.id.asc()).first()
    if row is None:
        row = XiaomiDialogueSettings(
            id=1,
            announce_enabled=settings.xiaomi_announce_enabled_default,
            voice_input_enabled=settings.xiaomi_voice_enabled_default,
            provider=settings.ai_default_provider or "deepseek",
        )
        db.add(row)
        db.commit()
        db.refresh(row)
    return row


def as_dict(db: Session) -> dict:
    row = get_or_create(db)
    return {
        "announceEnabled": bool(row.announce_enabled),
        "voiceInputEnabled": bool(row.voice_input_enabled),
        "provider": row.provider or "deepseek",
        "idleSec": settings.xiaomi_dialogue_idle_sec,
        "ttsCooldownMs": settings.xiaomi_tts_cooldown_ms,
    }


def update(
    db: Session,
    *,
    announce_enabled: bool | None = None,
    voice_input_enabled: bool | None = None,
    provider: str | None = None,
) -> dict:
    row = get_or_create(db)
    if announce_enabled is not None:
        row.announce_enabled = announce_enabled
    if voice_input_enabled is not None:
        row.voice_input_enabled = voice_input_enabled
    if provider is not None:
        p = provider.strip().lower()
        if p not in ("deepseek", "ollama"):
            raise ValueError("provider 须为 deepseek 或 ollama")
        row.provider = p
    db.commit()
    db.refresh(row)
    return as_dict(db)
