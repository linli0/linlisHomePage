"""Quant trading plugin router."""
from fastapi import APIRouter
from app.schemas.common import Result

router = APIRouter(prefix="/quant", tags=["quant"])


@router.get("/status")
def get_status() -> Result[dict]:
    """Get quant plugin status."""
    return Result.success({"enabled": True, "message": "Quant plugin is active"})


@router.get("/positions")
def get_positions() -> Result[list]:
    """Get current trading positions (mock data)."""
    mock_positions = [
        {
            "symbol": "BTC-USDT",
            "side": "long",
            "size": 0.5,
            "entry_price": 45000.0,
            "current_price": 46500.0,
            "pnl": 750.0,
            "pnl_percent": 3.33,
        },
        {
            "symbol": "ETH-USDT",
            "side": "short",
            "size": 10.0,
            "entry_price": 3200.0,
            "current_price": 3150.0,
            "pnl": 500.0,
            "pnl_percent": 1.56,
        },
    ]
    return Result.success(mock_positions)
