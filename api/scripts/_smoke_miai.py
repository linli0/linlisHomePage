"""One-shot live smoke for miAi dialogue + TTS. Not for CI."""
from __future__ import annotations

import json
import time
import urllib.request

BASE = "http://127.0.0.1:8000/api"


def req(method: str, path: str, data=None, token=None, timeout=180):
    h = {"Content-Type": "application/json"}
    if token:
        h["Authorization"] = f"Bearer {token}"
    body = None if data is None else json.dumps(data).encode()
    r = urllib.request.Request(BASE + path, data=body, headers=h, method=method)
    t0 = time.perf_counter()
    with urllib.request.urlopen(r, timeout=timeout) as resp:
        out = json.loads(resp.read().decode())
    return out, time.perf_counter() - t0


def main() -> None:
    results = []
    login, dt = req("POST", "/auth/login", {"password": "admin123"})
    token = login["data"]["token"]
    results.append(("login", login.get("code"), round(dt, 2), login["data"].get("role")))

    st, dt = req("GET", "/xiaomi/status", token=token)
    results.append(
        (
            "status",
            st["data"].get("connected"),
            round(dt, 2),
            (st["data"].get("device") or {}).get("ip"),
        )
    )

    cfg, dt = req("GET", "/xiaomi/dialogue/settings", token=token)
    results.append(
        (
            "settings",
            cfg["data"].get("provider"),
            cfg["data"].get("announceEnabled"),
            round(dt, 2),
        )
    )

    steps = [
        ("小爱同学", "exit_idle"),
        ("小爱小爱", "wake"),
        ("用不超过十五个字介绍你自己", "llm"),
        ("进入panel", "enter_panel"),
        ("今日金价", "panel_gold"),
        ("退出panel", "exit_panel"),
        ("小爱同学", "exit2"),
    ]
    for text, label in steps:
        out, dt = req(
            "POST",
            "/xiaomi/dialogue/utterance",
            {"text": text, "source": "web"},
            token=token,
        )
        d = out.get("data") or {}
        reply = (d.get("reply") or "")[:60].replace("\n", " ")
        results.append((label, d.get("route"), d.get("provider"), round(dt, 2), reply))

    tts, dt = req("POST", "/xiaomi/tts", {"text": "联调测试通过"}, token=token)
    raw = (tts.get("data") or {}).get("raw") or {}
    results.append(("tts", raw.get("via"), raw.get("code"), round(dt, 2)))

    ok = True
    for row in results:
        print(" | ".join(str(x) for x in row))
    # basic assertions
    if results[0][1] != 200:
        ok = False
    llm = next(r for r in results if r[0] == "llm")
    if llm[2] not in ("deepseek", "mock"):
        print("WARN llm provider", llm[2])
    tts_row = next(r for r in results if r[0] == "tts")
    if tts_row[1] != "cloud" or tts_row[2] != 0:
        ok = False
        print("FAIL tts")
    print("RESULT", "PASS" if ok else "FAIL")


if __name__ == "__main__":
    main()
