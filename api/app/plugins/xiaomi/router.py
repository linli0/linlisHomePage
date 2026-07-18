"""Xiaomi speaker plugin — LAN MIOT + web account bind."""

from __future__ import annotations

import logging
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.deps import get_current_user, require_admin
from app.models import User
from app.plugins.xiaomi import account as mi_account
from app.plugins.xiaomi import binding, miot
from app.plugins.xiaomi.dialogue.routes import router as dialogue_router
from app.plugins.xiaomi.voice.worker import status as voice_status
from app.schemas.common import Result

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/xiaomi", tags=["xiaomi"])
router.include_router(dialogue_router)


class TTSRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=500)
    volume: int | None = None


class ChatRequest(BaseModel):
    message: str
    stream: bool = False


class VolumeRequest(BaseModel):
    volume: int = Field(..., ge=0, le=100)


class AccountLoginRequest(BaseModel):
    username: str = Field(..., min_length=1, max_length=100)
    password: str = Field(..., min_length=1, max_length=128)


class AccountVerifyRequest(BaseModel):
    sessionId: str = Field(..., min_length=8, max_length=64)
    code: str = Field(..., min_length=4, max_length=16)


class AccountBindRequest(BaseModel):
    sessionId: str = Field(..., min_length=8, max_length=64)
    did: str = Field(..., min_length=1, max_length=64)


class RebindDeviceRequest(BaseModel):
    did: str = Field(..., min_length=1, max_length=64)


def _http_error(exc: Exception) -> HTTPException:
    msg = str(exc)
    if isinstance(exc, RuntimeError):
        return HTTPException(status_code=400, detail=msg)
    return HTTPException(status_code=502, detail=f"音箱控制失败: {msg}")


@router.get("/status")
async def get_status(
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    return Result.success(await miot.probe())


@router.get("/config")
def get_config(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    rt = binding.get_runtime(db)
    acct = binding.account_status_dict(db)
    return Result.success(
        {
            "enabled": rt is not None,
            "configured": rt is not None,
            "deviceIp": rt.ip if rt else None,
            "deviceName": rt.name if rt else None,
            "deviceModel": rt.model if rt else None,
            "hasToken": bool(rt and rt.token),
            "source": rt.source if rt else None,
            "account": acct,
        }
    )


@router.get("/account/status")
def account_status(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    return Result.success(binding.account_status_dict(db))


@router.post("/account/login")
async def account_login(
    req: AccountLoginRequest,
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    try:
        data = await mi_account.login(req.username, req.password)
        return Result.success(data)
    except Exception as exc:
        logger.exception("xiaomi account login failed")
        raise _http_error(exc) from exc


@router.post("/account/verify")
async def account_verify(
    req: AccountVerifyRequest,
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    try:
        data = await mi_account.verify_sms(req.sessionId, req.code)
        return Result.success(data)
    except Exception as exc:
        logger.exception("xiaomi account verify failed")
        raise _http_error(exc) from exc


@router.post("/account/bind")
def account_bind(
    req: AccountBindRequest,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    try:
        picked = mi_account.pick_device(req.sessionId, req.did)
        binding.upsert_binding(db, **picked)
        mi_account.drop_session(req.sessionId)
        return Result.success(binding.account_status_dict(db))
    except Exception as exc:
        logger.exception("xiaomi account bind failed")
        raise _http_error(exc) from exc


@router.post("/account/refresh-devices")
async def account_refresh_devices(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    creds = binding.load_pass_token(db)
    if creds is None:
        raise HTTPException(status_code=400, detail="尚未绑定小米账号")
    mi_user_id, cloud_device_id, pass_token = creds
    try:
        devices = await mi_account.refresh_devices_with_token(
            mi_user_id, cloud_device_id, pass_token
        )
        return Result.success({"devices": devices})
    except Exception as exc:
        logger.exception("refresh devices failed")
        raise _http_error(exc) from exc


@router.post("/account/rebind-device")
async def account_rebind_device(
    req: RebindDeviceRequest,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    """Update bound speaker using stored passToken (e.g. IP changed)."""
    row = binding.get_binding(db)
    creds = binding.load_pass_token(db)
    if row is None or creds is None:
        raise HTTPException(status_code=400, detail="尚未绑定小米账号")
    mi_user_id, cloud_device_id, pass_token = creds
    try:
        d = await mi_account.find_device_raw(mi_user_id, cloud_device_id, pass_token, req.did)
        ip = d.get("localip") or d.get("ip")
        token = d.get("token")
        if not ip or not token:
            raise RuntimeError("该设备缺少局域网 IP 或 TOKEN")
        binding.upsert_binding(
            db,
            mi_user_id=mi_user_id,
            mi_username=row.mi_username,
            cloud_device_id=cloud_device_id,
            pass_token=pass_token,
            did=str(d.get("did")),
            name=d.get("name") or "小爱音箱",
            model=d.get("model") or "",
            local_ip=str(ip),
            device_token=str(token),
        )
        return Result.success(binding.account_status_dict(db))
    except Exception as exc:
        logger.exception("rebind device failed")
        raise _http_error(exc) from exc


@router.delete("/account")
def account_unbind(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[dict]:
    binding.delete_binding(db)
    return Result.success({"bound": False})


@router.get("/devices")
async def get_devices(
    _: Annotated[User, Depends(get_current_user)],
) -> Result[list]:
    status = await miot.probe()
    device = status.get("device")
    return Result.success([device] if device else [])


@router.post("/tts")
async def tts(
    req: TTSRequest,
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    try:
        # Do NOT await local set_volume before TTS — LX06 miIO volume often
        # hangs ~10s+ (user ack timeout) and blocks cloud speech.
        raw = await miot.tts(req.text.strip())
        return Result.success({"status": "ok", "message": "TTS 已发送", "raw": raw})
    except Exception as exc:
        logger.exception("TTS failed")
        raise _http_error(exc) from exc


@router.post("/wake")
async def wake(_: Annotated[User, Depends(get_current_user)]) -> Result[dict]:
    try:
        raw = await miot.wake()
        return Result.success({"status": "ok", "raw": raw})
    except Exception as exc:
        raise _http_error(exc) from exc


@router.post("/chat")
async def chat(
    req: ChatRequest,
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    try:
        text = req.message.strip()
        if not text:
            raise RuntimeError("消息为空")
        try:
            await miot.wake()
        except Exception:
            logger.warning("wake before chat TTS failed")
        raw = await miot.tts(text)
        return Result.success({"response": text, "done": True, "raw": raw})
    except Exception as exc:
        raise _http_error(exc) from exc


@router.post("/volume")
async def set_volume(
    req: VolumeRequest,
    _: Annotated[User, Depends(get_current_user)],
) -> Result[dict]:
    try:
        raw = await miot.set_volume(req.volume)
        return Result.success({"volume": req.volume, "raw": raw})
    except Exception as exc:
        raise _http_error(exc) from exc


@router.post("/play")
async def play(_: Annotated[User, Depends(get_current_user)]) -> Result[dict]:
    try:
        raw = await miot.play()
        return Result.success({"status": "playing", "raw": raw})
    except Exception as exc:
        raise _http_error(exc) from exc


@router.post("/pause")
async def pause(_: Annotated[User, Depends(get_current_user)]) -> Result[dict]:
    try:
        raw = await miot.pause()
        return Result.success({"status": "paused", "raw": raw})
    except Exception as exc:
        raise _http_error(exc) from exc


@router.post("/stop")
async def stop(_: Annotated[User, Depends(get_current_user)]) -> Result[dict]:
    try:
        raw = await miot.stop()
        return Result.success({"status": "stopped", "raw": raw})
    except Exception as exc:
        raise _http_error(exc) from exc


@router.get("/voice/status")
def get_voice_status(_: Annotated[User, Depends(get_current_user)]) -> Result[dict]:
    return Result.success(voice_status())
