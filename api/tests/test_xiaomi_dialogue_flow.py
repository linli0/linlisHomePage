import asyncio

import pytest

from app.core.database import init_db
from app.plugins.xiaomi.dialogue import orchestrator
from app.plugins.xiaomi.dialogue.session import Route, get_state
import app.plugins.xiaomi.miot as miot
import app.services.llm as llm


@pytest.fixture(autouse=True)
def _setup(monkeypatch):
    init_db()
    st = get_state()
    st.route = Route.IDLE
    st.history.clear()
    st.speaking = False

    async def fake_llm(*_a, **_k):
        return {"text": "llm-ok", "provider": "mock", "model": "m"}

    async def fake_tts(_text):
        return {"ok": True}

    monkeypatch.setattr(llm, "chat_complete", fake_llm)
    monkeypatch.setattr(miot, "tts", fake_tts)


def test_wake_chat_exit():
    async def run():
        r1 = await orchestrator.handle_utterance("小爱小爱", source="web")
        assert r1["route"] == "multi"
        r2 = await orchestrator.handle_utterance("hi", source="web")
        assert r2["reply"] == "llm-ok"
        r3 = await orchestrator.handle_utterance("小爱同学", source="web")
        assert r3["route"] == "idle"
        assert get_state().route == Route.IDLE

    asyncio.run(run())


def test_panel_help():
    async def run():
        await orchestrator.handle_utterance("小爱小爱", source="web")
        await orchestrator.handle_utterance("进入panel", source="web")
        r = await orchestrator.handle_utterance("帮助", source="web")
        assert r["provider"] == "panel"
        assert "Panel" in r["reply"] or "panel" in r["reply"].lower() or "关键词" in r["reply"]

    asyncio.run(run())
