"""Tests for xiaomi speaker plugin (P3)."""


def test_xiaomi_plugin_requires_auth(client):
    """Xiaomi routes are mounted by default and require login."""
    assert client.get("/api/xiaomi/status").status_code == 401
    assert client.get("/api/xiaomi/devices").status_code == 401
    assert client.get("/api/xiaomi/config").status_code == 401


def test_xiaomi_plugin_config_with_auth(client, auth_headers):
    """Authenticated /api/xiaomi/config returns binding shape."""
    response = client.get("/api/xiaomi/config", headers=auth_headers)
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert "enabled" in body["data"]
    assert "configured" in body["data"]


def test_xiaomi_plugin_status_and_devices(client, auth_headers, monkeypatch):
    """Status/devices succeed when probe is available (mocked; no real speaker)."""

    async def fake_probe():
        return {
            "connected": False,
            "configured": False,
            "error": "未绑定设备",
            "device": None,
            "source": None,
            "enabled": True,
        }

    monkeypatch.setattr("app.plugins.xiaomi.miot.probe", fake_probe)

    status = client.get("/api/xiaomi/status", headers=auth_headers)
    assert status.status_code == 200
    assert status.json()["code"] == 200

    devices = client.get("/api/xiaomi/devices", headers=auth_headers)
    assert devices.status_code == 200
    body = devices.json()
    assert body["code"] == 200
    assert isinstance(body["data"], list)
