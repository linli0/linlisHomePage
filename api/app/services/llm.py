"""Unified LLM chat: Ollama primary, DeepSeek fallback (opt-in DeepSeek via provider)."""

from __future__ import annotations

import logging
from typing import Any

import httpx

from app.core.config import settings
from app.services import ai as ai_service

logger = logging.getLogger(__name__)


async def chat_complete(
    prompt: str,
    *,
    system: str | None = None,
    history: list[dict[str, str]] | None = None,
    provider: str | None = None,
    max_tokens: int = 512,
) -> dict[str, Any]:
    """Return { text, provider, model }.

    Default / ollama: try Ollama first, then DeepSeek.
    deepseek: try DeepSeek first, then Ollama.
    """
    pref = (provider or settings.ai_default_provider or "ollama").lower()
    if pref == "deepseek":
        try:
            return await _deepseek_chat(
                prompt, system=system, history=history, max_tokens=max_tokens
            )
        except Exception as exc:
            logger.warning("DeepSeek failed, fallback ollama: %s", exc)
            return await _ollama_chat(prompt, system=system, history=history)

    try:
        return await _ollama_chat(prompt, system=system, history=history)
    except Exception as exc:
        logger.warning("Ollama failed, fallback deepseek: %s", exc)
        return await _deepseek_chat(
            prompt, system=system, history=history, max_tokens=max_tokens
        )


async def _deepseek_chat(
    prompt: str,
    *,
    system: str | None,
    history: list[dict[str, str]] | None,
    max_tokens: int,
) -> dict[str, Any]:
    key = (settings.deepseek_api_key or "").strip()
    if not key:
        raise RuntimeError("未配置 DEEPSEEK_API_KEY")

    messages: list[dict[str, str]] = []
    if system:
        messages.append({"role": "system", "content": system})
    for m in history or []:
        role = m.get("role") or "user"
        content = (m.get("content") or "").strip()
        if content and role in ("user", "assistant", "system"):
            messages.append({"role": role, "content": content})
    messages.append({"role": "user", "content": prompt})

    url = settings.deepseek_base_url.rstrip("/") + "/chat/completions"
    payload = {
        "model": settings.deepseek_model,
        "messages": messages,
        "max_tokens": max_tokens,
        "stream": False,
    }
    headers = {"Authorization": f"Bearer {key}", "Content-Type": "application/json"}
    logger.info("LLM call provider=deepseek model=%s", settings.deepseek_model)
    async with httpx.AsyncClient(timeout=120.0) as client:
        resp = await client.post(url, json=payload, headers=headers)
        resp.raise_for_status()
        data = resp.json()
    msg = data.get("choices", [{}])[0].get("message", {}) or {}
    # v4-flash may put text in reasoning_content when content is empty
    text = (msg.get("content") or msg.get("reasoning_content") or "").strip()
    if not text:
        raise RuntimeError("DeepSeek 返回空内容")
    return {"text": text, "provider": "deepseek", "model": settings.deepseek_model, "raw": data}


async def _ollama_chat(
    prompt: str,
    *,
    system: str | None,
    history: list[dict[str, str]] | None,
) -> dict[str, Any]:
    base = ai_service._get_effective_ollama_url()
    try:
        models = ai_service.get_models().models
    except Exception as exc:
        raise RuntimeError(f"Ollama 不可用: {exc}") from exc
    if not models:
        raise RuntimeError("Ollama 无可用模型")
    model = models[0].name
    full = prompt
    if system:
        full = f"{system}\n\n{prompt}"
    if history:
        lines = []
        for m in history[-8:]:
            lines.append(f"{m.get('role','user')}: {m.get('content','')}")
        full = "\n".join(lines) + f"\nuser: {prompt}\nassistant:"

    logger.info("LLM call provider=ollama model=%s", model)
    async with httpx.AsyncClient(timeout=300.0) as client:
        resp = await client.post(
            f"{base}/api/generate",
            json={"model": model, "prompt": full, "stream": False},
        )
        resp.raise_for_status()
        data = resp.json()
    text = (data.get("response") or "").strip()
    if not text:
        raise RuntimeError("Ollama 返回空内容")
    return {"text": text, "provider": "ollama", "model": model, "raw": data}


def provider_status() -> dict[str, Any]:
    has_key = bool((settings.deepseek_api_key or "").strip())
    ollama = ai_service.get_status()
    return {
        "defaultProvider": settings.ai_default_provider,
        "deepseek": {
            "configured": has_key,
            "model": settings.deepseek_model,
            "baseUrl": settings.deepseek_base_url,
        },
        "ollama": {
            "status": ollama.status,
            "localOnline": ollama.local_online,
            "url": ollama.url,
        },
    }
