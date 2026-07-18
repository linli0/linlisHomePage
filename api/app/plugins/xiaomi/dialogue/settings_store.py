"""Dialogue settings persisted in SQLite."""

from __future__ import annotations

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.database import engine
from app.models import XiaomiDialogueSettings

_SCHEMA_READY = False


def _default_provider() -> str:
    p = (settings.ai_default_provider or "ollama").strip().lower()
    return p if p in ("ollama", "deepseek") else "ollama"


def ensure_announce_columns() -> None:
    """Add announce_* columns on existing SQLite DBs (create_all won't alter)."""
    global _SCHEMA_READY
    if _SCHEMA_READY:
        return
    ddl = [
        ("announce_cursor", "BOOLEAN DEFAULT 1"),
        ("announce_codex", "BOOLEAN DEFAULT 1"),
        ("announce_subagent", "BOOLEAN DEFAULT 0"),
        ("announce_detail", "VARCHAR(20) DEFAULT 'brief'"),
    ]
    with engine.begin() as conn:
        rows = conn.execute(text("PRAGMA table_info(xiaomi_dialogue_settings)")).fetchall()
        if not rows:
            _SCHEMA_READY = True
            return
        names = {r[1] for r in rows}
        for col, typ in ddl:
            if col not in names:
                conn.execute(
                    text(f"ALTER TABLE xiaomi_dialogue_settings ADD COLUMN {col} {typ}")
                )
    _SCHEMA_READY = True


def get_or_create(db: Session) -> XiaomiDialogueSettings:
    ensure_announce_columns()
    row = db.query(XiaomiDialogueSettings).order_by(XiaomiDialogueSettings.id.asc()).first()
    if row is None:
        row = XiaomiDialogueSettings(
            id=1,
            announce_enabled=settings.xiaomi_announce_enabled_default,
            voice_input_enabled=settings.xiaomi_voice_enabled_default,
            provider=_default_provider(),
            announce_cursor=True,
            announce_codex=True,
            announce_subagent=False,
            announce_detail="brief",
        )
        db.add(row)
        db.commit()
        db.refresh(row)
    return row


def migrate_legacy_deepseek_default(db: Session) -> bool:
    """If env prefers ollama, flip stale DB default deepseek → ollama (stops API burn)."""
    if _default_provider() != "ollama":
        return False
    row = get_or_create(db)
    if (row.provider or "").lower() != "deepseek":
        return False
    row.provider = "ollama"
    db.commit()
    db.refresh(row)
    return True


def as_dict(db: Session) -> dict:
    row = get_or_create(db)
    detail = (row.announce_detail or "brief").strip().lower()
    if detail not in ("brief", "detailed"):
        detail = "brief"
    return {
        "announceEnabled": bool(row.announce_enabled),
        "voiceInputEnabled": bool(row.voice_input_enabled),
        "provider": row.provider or _default_provider(),
        "announceCursor": bool(getattr(row, "announce_cursor", True)),
        "announceCodex": bool(getattr(row, "announce_codex", True)),
        "announceSubagent": bool(getattr(row, "announce_subagent", False)),
        "announceDetail": detail,
        "idleSec": settings.xiaomi_dialogue_idle_sec,
        "ttsCooldownMs": settings.xiaomi_tts_cooldown_ms,
    }


def update(
    db: Session,
    *,
    announce_enabled: bool | None = None,
    voice_input_enabled: bool | None = None,
    provider: str | None = None,
    announce_cursor: bool | None = None,
    announce_codex: bool | None = None,
    announce_subagent: bool | None = None,
    announce_detail: str | None = None,
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
    if announce_cursor is not None:
        row.announce_cursor = announce_cursor
    if announce_codex is not None:
        row.announce_codex = announce_codex
    if announce_subagent is not None:
        row.announce_subagent = announce_subagent
    if announce_detail is not None:
        d = announce_detail.strip().lower()
        if d not in ("brief", "detailed"):
            raise ValueError("announceDetail 须为 brief 或 detailed")
        row.announce_detail = d
    db.commit()
    db.refresh(row)
    return as_dict(db)
