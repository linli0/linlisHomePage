"""Xiaomi speaker plugin router."""
from fastapi import APIRouter
from app.schemas.common import Result

router = APIRouter(prefix="/xiaomi", tags=["xiaomi"])


@router.get("/status")
def get_status() -> Result[dict]:
    """Get xiaomi plugin status."""
    return Result.success({"enabled": True, "message": "Xiaomi plugin is active"})


@router.get("/devices")
def get_devices() -> Result[list]:
    """Get xiaomi devices (mock data)."""
    mock_devices = [
        {
            "device_id": "xiaomi_001",
            "name": "客厅音箱",
            "model": "X08A",
            "status": "online",
            "volume": 50,
            "playing": False,
        },
        {
            "device_id": "xiaomi_002",
            "name": "卧室音箱",
            "model": "LX06",
            "status": "offline",
            "volume": 30,
            "playing": False,
        },
    ]
    return Result.success(mock_devices)
