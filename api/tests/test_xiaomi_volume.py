import asyncio
from unittest.mock import AsyncMock

import pytest

from app.plugins.xiaomi import binding, miot


@pytest.fixture
def mock_runtime(monkeypatch):
    rt = binding.RuntimeDevice(
        ip="192.168.1.10",
        token="0123456789abcdef",
        name="Test Speaker",
        model="xiaomi.wifispeaker.lx06",
        did="267090026",
    )
    monkeypatch.setattr(miot, "get_runtime", lambda: rt)
    return rt


def test_set_volume_uses_cloud(mock_runtime, monkeypatch):
    set_prop = AsyncMock(return_value=0)
    monkeypatch.setattr(miot, "_cloud_miot_set_prop", set_prop)

    result = asyncio.run(miot.set_volume(42))

    set_prop.assert_awaited_once_with("267090026", 2, 1, 42)
    assert result["via"] == "cloud"
    assert result["volume"] == 42


def test_set_volume_clamps_to_lx06_min(mock_runtime, monkeypatch):
    set_prop = AsyncMock(return_value=0)
    monkeypatch.setattr(miot, "_cloud_miot_set_prop", set_prop)

    asyncio.run(miot.set_volume(3))

    set_prop.assert_awaited_once_with("267090026", 2, 1, 6)


def test_set_volume_api(client, auth_headers, monkeypatch):
    monkeypatch.setattr(
        miot,
        "set_volume",
        AsyncMock(return_value={"ok": True, "via": "cloud", "volume": 55}),
    )
    response = client.post(
        "/api/xiaomi/volume",
        json={"volume": 55},
        headers=auth_headers,
    )
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert body["data"]["volume"] == 55
