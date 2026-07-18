"""Invoke Codex CLI against the most recent session (best-effort)."""

from __future__ import annotations

import asyncio
import logging
import os
import shutil
from pathlib import Path

from app.core.config import settings

logger = logging.getLogger(__name__)


def _sessions_root() -> Path:
    if settings.codex_sessions_dir:
        return Path(settings.codex_sessions_dir)
    return Path.home() / ".codex" / "sessions"


def latest_session_file() -> Path | None:
    root = _sessions_root()
    if not root.is_dir():
        return None
    files = sorted(root.rglob("*.jsonl"), key=lambda p: p.stat().st_mtime, reverse=True)
    return files[0] if files else None


def repo_root() -> Path:
    if settings.repo_root:
        return Path(settings.repo_root)
    # api/app/plugins/xiaomi/dialogue/codex.py -> repo root = parents[5]
    return Path(__file__).resolve().parents[5]


async def run_codex_prompt(prompt: str, *, timeout_sec: float = 180.0) -> str:
    """Non-interactive codex exec; falls back to echo error if CLI missing."""
    exe = shutil.which("codex")
    if not exe:
        raise RuntimeError("未找到 codex CLI，请确认已安装并在 PATH 中")

    cwd = str(repo_root())
    session = latest_session_file()
    # Prefer `codex exec` style; if unsupported, try bare prompt pipe
    cmd = [exe, "exec", "--skip-git-repo-check", prompt]
    env = os.environ.copy()
    if session:
        env["CODEX_LAST_SESSION"] = str(session)

    try:
        proc = await asyncio.create_subprocess_exec(
            *cmd,
            cwd=cwd,
            env=env,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE,
        )
        try:
            stdout, stderr = await asyncio.wait_for(proc.communicate(), timeout=timeout_sec)
        except asyncio.TimeoutError as exc:
            proc.kill()
            raise RuntimeError("Codex 执行超时") from exc
        out = (stdout or b"").decode("utf-8", errors="replace").strip()
        err = (stderr or b"").decode("utf-8", errors="replace").strip()
        if proc.returncode != 0 and not out:
            raise RuntimeError(err or f"codex exit {proc.returncode}")
        text = out or err
        # Keep TTS short
        if len(text) > 400:
            text = text[:400] + "…"
        return text or "Codex 已完成，无文本输出"
    except FileNotFoundError as exc:
        raise RuntimeError("无法启动 codex") from exc
