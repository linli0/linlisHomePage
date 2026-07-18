"""Persist / load Xiaomi binding from SQLite."""

from __future__ import annotations

from dataclasses import dataclass

from sqlalchemy.orm import Session

from app.models import XiaomiBinding
from app.plugins.xiaomi import crypto


@dataclass
class RuntimeDevice:
    ip: str
    token: str
    name: str
    model: str
    did: str | None = None
    source: str = "env"  # env | db


def get_binding(db: Session) -> XiaomiBinding | None:
    return db.query(XiaomiBinding).order_by(XiaomiBinding.id.asc()).first()


def get_runtime(db: Session | None = None) -> RuntimeDevice | None:
    """DB binding wins over .env."""
    from app.core.config import settings
    from app.core.database import SessionLocal

    own = db is None
    session = db or SessionLocal()
    try:
        row = get_binding(session)
        if row and row.local_ip.strip() and row.device_token_enc:
            return RuntimeDevice(
                ip=row.local_ip.strip(),
                token=crypto.decrypt(row.device_token_enc),
                name=row.name or "小爱音箱",
                model=row.model or "",
                did=row.did,
                source="db",
            )
    finally:
        if own:
            session.close()

    if (
        settings.xiaomi_enabled
        and settings.xiaomi_device_ip.strip()
        and settings.xiaomi_device_token.strip()
    ):
        return RuntimeDevice(
            ip=settings.xiaomi_device_ip.strip(),
            token=settings.xiaomi_device_token.strip(),
            name=settings.xiaomi_device_name or "小爱音箱",
            model=settings.xiaomi_device_model or "LX06",
            did=(settings.xiaomi_device_did or "").strip() or None,
            source="env",
        )
    return None


def upsert_binding(
    db: Session,
    *,
    mi_user_id: str,
    mi_username: str | None,
    cloud_device_id: str,
    pass_token: str,
    did: str,
    name: str,
    model: str,
    local_ip: str,
    device_token: str,
) -> XiaomiBinding:
    row = get_binding(db)
    if row is None:
        row = XiaomiBinding(id=1)
        db.add(row)
    row.mi_user_id = str(mi_user_id)
    row.mi_username = mi_username
    row.cloud_device_id = cloud_device_id
    row.pass_token_enc = crypto.encrypt(pass_token)
    row.did = did
    row.name = name
    row.model = model
    row.local_ip = local_ip
    row.device_token_enc = crypto.encrypt(device_token)
    db.commit()
    db.refresh(row)
    return row


def delete_binding(db: Session) -> bool:
    row = get_binding(db)
    if row is None:
        return False
    db.delete(row)
    db.commit()
    return True


def account_status_dict(db: Session) -> dict:
    row = get_binding(db)
    if row is None:
        return {
            "bound": False,
            "miUsername": None,
            "miUserId": None,
            "hasPassToken": False,
            "device": None,
        }
    return {
        "bound": True,
        "miUsername": row.mi_username,
        "miUserId": row.mi_user_id,
        "hasPassToken": bool(row.pass_token_enc),
        "device": {
            "did": row.did,
            "name": row.name,
            "model": row.model,
            "ip": row.local_ip,
            "hasToken": bool(row.device_token_enc),
        },
        "updatedAt": row.updated_at.isoformat() if row.updated_at else None,
    }


def load_pass_token(db: Session) -> tuple[str, str, str] | None:
    """Return (mi_user_id, cloud_device_id, pass_token) or None."""
    row = get_binding(db)
    if row is None:
        return None
    return row.mi_user_id, row.cloud_device_id, crypto.decrypt(row.pass_token_enc)
