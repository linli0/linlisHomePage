"""Xiaomi cloud login, SMS 2FA, and device list (admin bind flow)."""

from __future__ import annotations

import asyncio
import hashlib
import json
import logging
import random
import string
import time
import uuid
from dataclasses import dataclass, field
from typing import Any
from urllib.parse import parse_qs, urlparse

import requests

logger = logging.getLogger(__name__)

UA = "APP/com.xiaomi.mihome APPV/6.0.103 iosPassportSDK/3.9.0 iOS/14.4"
SESSION_TTL_SEC = 600


def _rnd(n: int = 16) -> str:
    return "".join(random.sample(string.ascii_letters + string.digits, n)).upper()


def _parse_json(text: str) -> dict:
    if text.startswith("&&&START&&&"):
        text = text[11:]
    return json.loads(text)


@dataclass
class BindSession:
    session_id: str
    http: requests.Session
    cloud_device_id: str
    username: str
    expires_at: float
    context: str | None = None
    password: str | None = None  # only while waiting for SMS; cleared after verify
    mi_user_id: str | None = None
    pass_token: str | None = None
    devices: list[dict[str, Any]] = field(default_factory=list)


_sessions: dict[str, BindSession] = {}


def _purge() -> None:
    now = time.time()
    dead = [k for k, v in _sessions.items() if v.expires_at < now]
    for k in dead:
        _sessions.pop(k, None)


def get_session(session_id: str) -> BindSession:
    _purge()
    s = _sessions.get(session_id)
    if s is None:
        raise RuntimeError("绑定会话已过期，请重新登录小米账号")
    return s


def drop_session(session_id: str) -> None:
    _sessions.pop(session_id, None)


def _public_device(d: dict[str, Any]) -> dict[str, Any]:
    return {
        "did": str(d.get("did", "")),
        "name": d.get("name") or d.get("model") or "设备",
        "model": d.get("model") or "",
        "ip": d.get("localip") or d.get("ip") or "",
        "isOnline": bool(d.get("isOnline")),
        "hasToken": bool(d.get("token")),
        "isSpeaker": _is_speaker(d.get("model") or ""),
    }


def _is_speaker(model: str) -> bool:
    m = model.lower()
    keys = ("wifispeaker", "speaker", "miai", "lx0", "x08", "sound")
    return any(k in m for k in keys)


def _login_password(http: requests.Session, username: str, password: str, device_id: str) -> dict:
    http.headers.update({"User-Agent": UA})
    http.cookies.set("sdkVersion", "3.9", domain="account.xiaomi.com")
    http.cookies.set("deviceId", device_id, domain="account.xiaomi.com")

    r = http.get(
        "https://account.xiaomi.com/pass/serviceLogin",
        params={"sid": "xiaomiio", "_json": "true"},
        timeout=30,
    )
    step1 = _parse_json(r.text)
    data = {
        "_json": "true",
        "qs": step1["qs"],
        "sid": step1["sid"],
        "_sign": step1["_sign"],
        "callback": step1["callback"],
        "user": username,
        "hash": hashlib.md5(password.encode()).hexdigest().upper(),
    }
    r = http.post("https://account.xiaomi.com/pass/serviceLoginAuth2", data=data, timeout=30)
    return _parse_json(r.text)


def _start_sms(http: requests.Session, notification_url: str) -> str:
    if notification_url.startswith("/"):
        notification_url = "https://account.xiaomi.com" + notification_url
    r = http.get(notification_url, timeout=30)
    r.raise_for_status()
    context = parse_qs(urlparse(notification_url).query)["context"][0]
    http.get(
        "https://account.xiaomi.com/identity/list",
        params={"sid": "xiaomiio", "context": context, "_locale": "zh_CN"},
        timeout=30,
    )
    send_params = {
        "_dc": str(int(time.time() * 1000)),
        "sid": "xiaomiio",
        "context": context,
        "mask": "0",
        "_locale": "zh_CN",
    }
    send_data = {
        "retry": "0",
        "icode": "",
        "_json": "true",
        "ick": http.cookies.get("ick", ""),
    }
    r = http.post(
        "https://account.xiaomi.com/identity/auth/sendPhoneTicket",
        params=send_params,
        data=send_data,
        timeout=30,
    )
    try:
        jr = _parse_json(r.text)
    except Exception:
        jr = {"raw": r.text[:300]}
    if isinstance(jr, dict) and jr.get("code") not in (0, None) and jr.get("result") != "ok":
        raise RuntimeError(f"发送短信失败: {jr.get('desc') or jr.get('description') or jr}")
    return context


def _verify_sms(http: requests.Session, context: str, code: str) -> dict:
    verify_params = {
        "_flag": "4",
        "_json": "true",
        "sid": "xiaomiio",
        "context": context,
        "mask": "0",
        "_locale": "zh_CN",
    }
    verify_data = {
        "_flag": "4",
        "ticket": code.strip(),
        "trust": "true",
        "_json": "true",
        "ick": http.cookies.get("ick", ""),
    }
    r = http.post(
        "https://account.xiaomi.com/identity/auth/verifyPhone",
        params=verify_params,
        data=verify_data,
        timeout=30,
    )
    jr = _parse_json(r.text)
    if jr.get("code") not in (0, None) and jr.get("result") != "ok":
        raise RuntimeError(jr.get("desc") or jr.get("description") or "验证码错误")

    finish_loc = jr.get("location")
    if finish_loc and finish_loc.startswith("/"):
        finish_loc = "https://account.xiaomi.com" + finish_loc

    if finish_loc and "identity/result/check" in finish_loc:
        r = http.get(finish_loc, allow_redirects=False, timeout=30)
        finish_loc = r.headers.get("Location") or finish_loc

    if finish_loc:
        r = http.get(finish_loc, allow_redirects=False, timeout=30)
        for _ in range(6):
            loc = r.headers.get("Location")
            if not loc:
                break
            if loc.startswith("/"):
                loc = "https://account.xiaomi.com" + loc
            r = http.get(loc, allow_redirects=False, timeout=30)

    return jr


def _extract_tokens(http: requests.Session, step2: dict | None = None) -> tuple[str, str]:
    user_id = None
    pass_token = None
    if step2:
        user_id = step2.get("userId")
        pass_token = step2.get("passToken")
    user_id = user_id or http.cookies.get("userId")
    pass_token = pass_token or http.cookies.get("passToken")
    if not user_id or not pass_token:
        raise RuntimeError("登录未拿到 passToken，请重试或检查二次验证是否完成")
    return str(user_id), str(pass_token)


async def _list_devices(mi_user_id: str, cloud_device_id: str, pass_token: str) -> list[dict]:
    """Use miservice MiIOService.device_list with cached passToken."""
    import tempfile
    from pathlib import Path

    import aiohttp
    from miservice import MiAccount, MiIOService

    token_path = Path(tempfile.gettempdir()) / f"mi_token_{mi_user_id}.json"
    token_path.write_text(
        json.dumps(
            {
                "deviceId": cloud_device_id,
                "userId": int(mi_user_id) if str(mi_user_id).isdigit() else mi_user_id,
                "passToken": pass_token,
            },
            indent=2,
        ),
        encoding="utf-8",
    )

    async with aiohttp.ClientSession() as session:
        acc = MiAccount(session, str(mi_user_id), "x", str(token_path))
        svc = MiIOService(acc)
        devices = await svc.device_list()
    return list(devices or [])


def _store_session(bs: BindSession) -> None:
    _purge()
    _sessions[bs.session_id] = bs


async def login(username: str, password: str) -> dict[str, Any]:
    username = username.strip()
    password = password.strip()
    if not username or not password:
        raise RuntimeError("请输入小米账号和密码")

    cloud_device_id = _rnd()
    http = requests.Session()
    step2 = await asyncio.to_thread(_login_password, http, username, password, cloud_device_id)

    session_id = uuid.uuid4().hex
    bs = BindSession(
        session_id=session_id,
        http=http,
        cloud_device_id=cloud_device_id,
        username=username,
        expires_at=time.time() + SESSION_TTL_SEC,
    )

    if step2.get("userId") and step2.get("passToken"):
        bs.mi_user_id, bs.pass_token = str(step2["userId"]), str(step2["passToken"])
        devices = await _list_devices(bs.mi_user_id, cloud_device_id, bs.pass_token)
        bs.devices = devices
        _store_session(bs)
        return {
            "needSms": False,
            "sessionId": session_id,
            "devices": [_public_device(d) for d in devices],
        }

    if step2.get("securityStatus") == 16 and step2.get("notificationUrl"):
        context = await asyncio.to_thread(_start_sms, http, step2["notificationUrl"])
        bs.context = context
        bs.password = password  # needed to finish login after SMS on same deviceId
        _store_session(bs)
        return {
            "needSms": True,
            "sessionId": session_id,
            "message": "验证码已发送到账号绑定手机，请输入短信验证码",
            "devices": [],
        }

    raise RuntimeError(
        step2.get("desc")
        or step2.get("description")
        or f"小米登录失败 code={step2.get('code')} securityStatus={step2.get('securityStatus')}"
    )


async def verify_sms(session_id: str, code: str) -> dict[str, Any]:
    """Verify SMS then password-login again with same deviceId."""
    bs = get_session(session_id)
    if not bs.context:
        raise RuntimeError("当前会话不需要验证码")
    if not code.strip():
        raise RuntimeError("请输入验证码")
    if not bs.password:
        raise RuntimeError("会话缺少密码上下文，请重新登录小米账号")

    await asyncio.to_thread(_verify_sms, bs.http, bs.context, code)

    step2 = await asyncio.to_thread(
        _login_password, bs.http, bs.username, bs.password, bs.cloud_device_id
    )
    bs.password = None
    if step2.get("userId") and step2.get("passToken"):
        uid, ptok = str(step2["userId"]), str(step2["passToken"])
    else:
        try:
            uid, ptok = _extract_tokens(bs.http, step2)
        except RuntimeError as exc:
            raise RuntimeError(
                "验证码可能正确，但登录态仍未建立，请重新绑定。"
                f" securityStatus={step2.get('securityStatus')}"
            ) from exc

    bs.mi_user_id = uid
    bs.pass_token = ptok
    devices = await _list_devices(bs.mi_user_id, bs.cloud_device_id, bs.pass_token)
    bs.devices = devices
    bs.context = None
    _store_session(bs)
    return {
        "needSms": False,
        "sessionId": session_id,
        "devices": [_public_device(d) for d in devices],
    }


def pick_device(session_id: str, did: str) -> dict[str, Any]:
    bs = get_session(session_id)
    if not bs.mi_user_id or not bs.pass_token:
        raise RuntimeError("尚未完成登录")
    for d in bs.devices:
        if str(d.get("did")) == str(did):
            ip = d.get("localip") or d.get("ip")
            token = d.get("token")
            if not ip or not token:
                raise RuntimeError("该设备缺少局域网 IP 或 TOKEN，请确认音箱在线且同一账号")
            return {
                "mi_user_id": bs.mi_user_id,
                "mi_username": bs.username,
                "cloud_device_id": bs.cloud_device_id,
                "pass_token": bs.pass_token,
                "did": str(d.get("did")),
                "name": d.get("name") or "小爱音箱",
                "model": d.get("model") or "",
                "local_ip": str(ip),
                "device_token": str(token),
            }
    raise RuntimeError("未找到该设备，请刷新列表后重试")


async def refresh_devices_with_token(
    mi_user_id: str, cloud_device_id: str, pass_token: str
) -> list[dict[str, Any]]:
    devices = await _list_devices(mi_user_id, cloud_device_id, pass_token)
    return [_public_device(d) for d in devices]


async def find_device_raw(
    mi_user_id: str, cloud_device_id: str, pass_token: str, did: str
) -> dict[str, Any]:
    devices = await _list_devices(mi_user_id, cloud_device_id, pass_token)
    for d in devices:
        if str(d.get("did")) == str(did):
            return d
    raise RuntimeError("云端未找到该设备")
