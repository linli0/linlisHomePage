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
    st.route = Route.MULTI
    st.history.clear()
    st.speaking = False
    st.listening = False

    async def fake_llm(*_a, **_k):
        return {"text": "llm-ok", "provider": "mock", "model": "m"}

    async def fake_tts(_text):
        return {"ok": True}

    monkeypatch.setattr(llm, "chat_complete", fake_llm)
    monkeypatch.setattr(miot, "tts", fake_tts)


@pytest.mark.parametrize("mode", ["idle", "panel", "codex", "debug"])
def test_set_mode_from_multi(mode):
    async def run():
        result = await orchestrator.set_mode(mode, speak=False)
        assert result["route"] == mode
        assert get_state().route.value == mode

    asyncio.run(run())


def test_set_mode_api(client, auth_headers, monkeypatch):
    from app.core.database import init_db
    from app.plugins.xiaomi.dialogue import orchestrator

    init_db()

    async def noop_persist(*_args, **_kwargs):
        return None

    monkeypatch.setattr(orchestrator, "_persist_and_broadcast", noop_persist)
    get_state().route = Route.MULTI
    for mode in ("idle", "panel", "codex", "debug", "multi"):
        response = client.post(
            "/api/xiaomi/dialogue/mode",
            json={"mode": mode, "speak": False},
            headers=auth_headers,
        )
        assert response.status_code == 200, response.text
        body = response.json()
        assert body["code"] == 200
        assert body["data"]["route"] == mode
        assert get_state().route.value == mode
