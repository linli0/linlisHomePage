"""Fixed system replies must not call the LLM (DeepSeek/Ollama)."""

import asyncio

from app.core.database import init_db
from app.plugins.xiaomi.dialogue import orchestrator
from app.plugins.xiaomi.dialogue.session import Route, get_state
import app.plugins.xiaomi.miot as miot
import app.services.llm as llm


def test_wake_and_exit_skip_llm(monkeypatch):
    init_db()
    st = get_state()
    st.route = Route.IDLE
    st.history.clear()
    st.speaking = False

    calls = {"n": 0}

    async def boom(*_a, **_k):
        calls["n"] += 1
        raise AssertionError("LLM must not be called for fixed phrases")

    async def fake_tts(_text):
        return {"ok": True}

    monkeypatch.setattr(llm, "chat_complete", boom)
    monkeypatch.setattr(miot, "tts", fake_tts)

    async def run():
        r1 = await orchestrator.handle_utterance("小爱小爱", source="voice")
        assert r1["provider"] == "system"
        assert get_state().route == Route.MULTI
        r2 = await orchestrator.handle_utterance("小爱同学", source="voice")
        assert r2.get("provider") == "system"
        assert r2["route"] == "idle"
        assert calls["n"] == 0

    asyncio.run(run())
