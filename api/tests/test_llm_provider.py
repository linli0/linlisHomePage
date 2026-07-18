"""LLM provider preference: Ollama primary, DeepSeek opt-in."""

from unittest.mock import AsyncMock, patch

import pytest

from app.services import llm as llm_service


@pytest.mark.asyncio
async def test_chat_complete_defaults_to_ollama():
    with (
        patch.object(llm_service.settings, "ai_default_provider", "ollama"),
        patch.object(
            llm_service, "_ollama_chat", new_callable=AsyncMock
        ) as ollama,
        patch.object(
            llm_service, "_deepseek_chat", new_callable=AsyncMock
        ) as deepseek,
    ):
        ollama.return_value = {"text": "hi", "provider": "ollama", "model": "m"}
        result = await llm_service.chat_complete("你好")
        assert result["provider"] == "ollama"
        ollama.assert_awaited_once()
        deepseek.assert_not_awaited()


@pytest.mark.asyncio
async def test_chat_complete_deepseek_when_requested():
    with (
        patch.object(
            llm_service, "_ollama_chat", new_callable=AsyncMock
        ) as ollama,
        patch.object(
            llm_service, "_deepseek_chat", new_callable=AsyncMock
        ) as deepseek,
    ):
        deepseek.return_value = {"text": "hi", "provider": "deepseek", "model": "m"}
        result = await llm_service.chat_complete("你好", provider="deepseek")
        assert result["provider"] == "deepseek"
        deepseek.assert_awaited_once()
        ollama.assert_not_awaited()
