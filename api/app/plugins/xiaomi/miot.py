"""Xiaomi speaker client — LAN miIO probe + cloud MIoT TTS (LX06)."""

from __future__ import annotations

import asyncio
import json
import logging
from pathlib import Path
from typing import Any

from app.core.config import settings
from app.plugins.xiaomi.binding import RuntimeDevice, get_runtime

logger = logging.getLogger(__name__)


def is_configured() -> bool:
    return get_runtime() is not None


def _runtime() -> RuntimeDevice:
    rt = get_runtime()
    if rt is None:
        raise RuntimeError("小爱未绑定：请管理员在网页绑定小米账号，或配置 api/.env")
    return rt


def _did(rt: RuntimeDevice) -> str:
    if rt.did:
        return str(rt.did)
    did = (settings.xiaomi_device_did or "").strip()
    if did:
        return did
    raise RuntimeError("缺少设备 DID：请在 .env 设置 XIAOMI_DEVICE_DID 或重新绑定")


def _ensure_mi_token_file() -> tuple[Path, str]:
    """Materialize passToken for miservice: DB binding first, then ~/.mi.token."""
    from app.core.database import SessionLocal
    from app.plugins.xiaomi import binding

    data_dir = Path("data")
    data_dir.mkdir(parents=True, exist_ok=True)
    token_path = data_dir / "mi.token"

    db = SessionLocal()
    try:
        creds = binding.load_pass_token(db)
        if creds:
            mi_user_id, cloud_device_id, pass_token = creds
            payload = {
                "deviceId": cloud_device_id,
                "userId": int(mi_user_id) if str(mi_user_id).isdigit() else mi_user_id,
                "passToken": pass_token,
            }
            token_path.write_text(json.dumps(payload, indent=2), encoding="utf-8")
            return token_path, str(mi_user_id)
    finally:
        db.close()

    home = Path.home() / ".mi.token"
    if home.is_file():
        data = json.loads(home.read_text(encoding="utf-8"))
        user_id = str(data.get("userId") or "")
        if user_id:
            token_path.write_text(json.dumps(data, indent=2), encoding="utf-8")
            return token_path, user_id

    raise RuntimeError("缺少小米登录态：请在网页重新绑定账号")


def _local_info(ip: str, token: str) -> Any:
    from miio import Device

    return Device(ip, token).info()


async def player_command(command: list[int], text: str | None = None) -> dict[str, Any]:
    """Best-effort local miIO player command (short timeout — LX06 often hangs)."""
    rt = _runtime()
    params: dict[str, Any] = {"command": command}
    if text is not None:
        params["text"] = text
    timeout = min(float(settings.xiaomi_timeout_sec), 2.5)

    def _send() -> Any:
        from miio import Device

        return Device(rt.ip, rt.token).raw_command("set_player_command", params)

    try:
        result = await asyncio.wait_for(asyncio.to_thread(_send), timeout=timeout)
        return {"ok": True, "raw": result}
    except Exception as exc:
        logger.warning("local player_command failed: %s", exc)
        raise


# Reuse cloud session to avoid re-login cost on every TTS
_cloud_lock = asyncio.Lock()
_cloud_svc: Any = None
_cloud_http: Any = None


async def _get_cloud_service():
    global _cloud_svc, _cloud_http
    import aiohttp
    from miservice import MiAccount, MiIOService

    async with _cloud_lock:
        if _cloud_svc is not None:
            return _cloud_svc
        token_path, user_id = _ensure_mi_token_file()
        _cloud_http = aiohttp.ClientSession()
        acc = MiAccount(_cloud_http, user_id, "x", str(token_path))
        _cloud_svc = MiIOService(acc)
        return _cloud_svc


async def _cloud_miot_action(did: str, siid: int, aiid: int, args: list[Any]) -> Any:
    """Call Xiaomi cloud MIoT action (siid/aiid) using cached service."""
    svc = await _get_cloud_service()
    try:
        return await svc.miot_action(did, (siid, aiid), args)
    except Exception:
        # reset cache and retry once
        global _cloud_svc, _cloud_http
        async with _cloud_lock:
            if _cloud_http is not None:
                try:
                    await _cloud_http.close()
                except Exception:
                    pass
            _cloud_svc = None
            _cloud_http = None
        svc = await _get_cloud_service()
        return await svc.miot_action(did, (siid, aiid), args)


async def _cloud_miot_set_prop(did: str, siid: int, piid: int, value: Any) -> Any:
    """Set MIoT property via cloud (e.g. LX06 volume siid=2 piid=1)."""
    svc = await _get_cloud_service()
    try:
        return await svc.miot_set_prop(did, (siid, piid), value)
    except Exception:
        global _cloud_svc, _cloud_http
        async with _cloud_lock:
            if _cloud_http is not None:
                try:
                    await _cloud_http.close()
                except Exception:
                    pass
            _cloud_svc = None
            _cloud_http = None
        svc = await _get_cloud_service()
        return await svc.miot_set_prop(did, (siid, piid), value)


async def tts(text: str) -> dict[str, Any]:
    """Cloud MIoT TTS only (local set_player_command is slow/unreliable on LX06)."""
    rt = _runtime()
    did = _did(rt)
    code = await _cloud_miot_action(did, 5, 1, [text])
    return {"ok": code == 0, "code": code, "via": "cloud"}


async def wake() -> dict[str, Any]:
    rt = _runtime()
    did = _did(rt)
    try:
        code = await _cloud_miot_action(did, 5, 3, [])
        return {"ok": code == 0, "code": code, "via": "cloud"}
    except Exception:
        return await player_command([5, 3])


async def play() -> dict[str, Any]:
    return await player_command([1])


async def pause() -> dict[str, Any]:
    return await player_command([2])


async def stop() -> dict[str, Any]:
    return await player_command([3])


async def set_volume(volume: int) -> dict[str, Any]:
    """Cloud MIoT volume (siid=2, piid=1); local miIO often hangs on LX06."""
    rt = _runtime()
    did = _did(rt)
    vol = max(6, min(100, int(volume)))
    try:
        code = await _cloud_miot_set_prop(did, 2, 1, vol)
        return {"ok": code == 0, "code": code, "via": "cloud", "volume": vol}
    except Exception as exc:
        logger.warning("cloud set_volume failed, trying local: %s", exc)
        vol_local = max(0, min(100, int(volume)))
        try:
            raw = await player_command([4, vol_local])
            return {"ok": True, "via": "local", "volume": vol_local, "raw": raw}
        except Exception as local_exc:
            logger.warning("local set_volume failed: %s", local_exc)
            raise RuntimeError(f"音量设置失败: {local_exc}") from local_exc


async def probe() -> dict[str, Any]:
    rt = get_runtime()
    if rt is None:
        return {
            "connected": False,
            "configured": False,
            "error": "未绑定设备（网页绑定或配置 .env）",
            "device": None,
            "source": None,
        }

    device = {
        "deviceId": rt.token.strip()[:16],
        "name": rt.name or "小爱音箱",
        "model": rt.model or "LX06",
        "online": False,
        "volume": None,
        "ip": rt.ip.strip(),
        "did": rt.did or settings.xiaomi_device_did or None,
    }

    try:
        info = await asyncio.to_thread(_local_info, rt.ip, rt.token)
        device["online"] = True
        device["model"] = getattr(info, "model", None) or device["model"]
        return {
            "connected": True,
            "configured": True,
            "error": None,
            "device": device,
            "source": rt.source,
            "fw": getattr(info, "firmware_version", None),
        }
    except Exception as exc:
        logger.warning("xiaomi probe failed: %s", exc)
        return {
            "connected": False,
            "configured": True,
            "error": str(exc),
            "device": device,
            "source": rt.source,
        }
