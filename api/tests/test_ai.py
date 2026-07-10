"""Tests for AI module (P1)."""
from unittest.mock import patch, MagicMock

import httpx


def test_ai_status_returns_result(client):
    """GET /api/ai/status should return status result."""
    response = client.get("/api/ai/status")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert "status" in body["data"]
    assert body["data"]["status"] in ("connected", "disconnected")


def test_ai_status_fields(client):
    """GET /api/ai/status should include all expected fields."""
    response = client.get("/api/ai/status")
    body = response.json()
    data = body["data"]
    assert "status" in data
    assert "message" in data
    assert "localOnline" in data
    assert "remoteOnline" in data
    assert "remoteEnabled" in data


def test_ai_models_when_ollama_offline(client):
    """GET /api/ai/models should return 503 when Ollama is unreachable."""
    response = client.get("/api/ai/models")
    # In test environment, Ollama is not running, so expect 503
    assert response.status_code == 503


def test_ai_models_when_ollama_online(client):
    """GET /api/ai/models should return models when Ollama responds."""
    mock_response = {
        "models": [
            {
                "name": "llama3:latest",
                "size": 4700000000,
                "modified_at": "2026-01-01T00:00:00Z",
                "details": {
                    "parameter_size": "8B",
                    "family": "llama",
                    "format": "gguf",
                },
            }
        ]
    }

    with patch("app.services.ai.httpx.get") as mock_get:
        mock_get.return_value = MagicMock(
            status_code=200,
            json=lambda: mock_response,
        )
        # Reset cached URL to force re-detection
        from app.services.ai import reset_ollama_url
        reset_ollama_url()

        response = client.get("/api/ai/models")
        assert response.status_code == 200
        body = response.json()
        assert body["code"] == 200
        assert len(body["data"]["models"]) == 1
        assert body["data"]["models"][0]["name"] == "llama3:latest"
        assert body["data"]["models"][0]["parameterSize"] == "8B"
