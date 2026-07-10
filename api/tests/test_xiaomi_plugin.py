"""Tests for xiaomi speaker plugin (P3)."""
import os
import sys
import pytest
from fastapi.testclient import TestClient


def test_xiaomi_plugin_disabled_by_default(client):
    """Xiaomi plugin should be disabled by default."""
    response = client.get("/api/xiaomi/status")
    assert response.status_code == 404


def test_xiaomi_plugin_enabled_returns_status():
    """When ENABLE_XIAOMI=true, /api/xiaomi/status should return status."""
    os.environ["ENABLE_XIAOMI"] = "true"
    
    # Remove cached modules to force re-import
    modules_to_remove = [k for k in sys.modules.keys() if k.startswith('app.main')]
    for mod in modules_to_remove:
        del sys.modules[mod]
    
    # Re-import to trigger plugin registration
    from app.main import app
    from app.core.database import get_db
    from sqlalchemy import create_engine
    from sqlalchemy.orm import sessionmaker
    from sqlalchemy.pool import StaticPool
    from app.core.database import Base
    
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    Base.metadata.create_all(bind=engine)
    
    def override_get_db():
        db = TestingSessionLocal()
        try:
            yield db
        finally:
            db.close()
    
    app.dependency_overrides[get_db] = override_get_db
    
    with TestClient(app) as test_client:
        response = test_client.get("/api/xiaomi/status")
        assert response.status_code == 200
        body = response.json()
        assert body["code"] == 200
        assert "enabled" in body["data"]
        assert body["data"]["enabled"] is True
    
    # Cleanup
    del os.environ["ENABLE_XIAOMI"]
    app.dependency_overrides.clear()


def test_xiaomi_plugin_returns_devices():
    """Xiaomi plugin should return mock device list."""
    os.environ["ENABLE_XIAOMI"] = "true"
    
    # Remove cached modules to force re-import
    modules_to_remove = [k for k in sys.modules.keys() if k.startswith('app.main')]
    for mod in modules_to_remove:
        del sys.modules[mod]
    
    from app.main import app
    from app.core.database import get_db
    from sqlalchemy import create_engine
    from sqlalchemy.orm import sessionmaker
    from sqlalchemy.pool import StaticPool
    from app.core.database import Base
    
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    Base.metadata.create_all(bind=engine)
    
    def override_get_db():
        db = TestingSessionLocal()
        try:
            yield db
        finally:
            db.close()
    
    app.dependency_overrides[get_db] = override_get_db
    
    with TestClient(app) as test_client:
        response = test_client.get("/api/xiaomi/devices")
        assert response.status_code == 200
        body = response.json()
        assert body["code"] == 200
        assert isinstance(body["data"], list)
    
    del os.environ["ENABLE_XIAOMI"]
    app.dependency_overrides.clear()
