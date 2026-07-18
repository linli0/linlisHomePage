"""Quant trading plugin router."""
from fastapi import APIRouter, Query
from app.schemas.common import Result

router = APIRouter(prefix="/quant", tags=["quant"])


@router.get("/status")
def get_status() -> Result[dict]:
    """Get quant plugin status."""
    return Result.success({"enabled": True, "message": "Quant plugin is active"})


@router.get("/positions")
def get_positions() -> Result[list]:
    """Get current trading positions (mock data)."""
    return Result.success([
        {"symbol": "BTC-USDT", "side": "long", "size": 0.5, "entry_price": 45000.0,
         "current_price": 46500.0, "pnl": 750.0, "pnl_percent": 3.33},
        {"symbol": "ETH-USDT", "side": "short", "size": 10.0, "entry_price": 3200.0,
         "current_price": 3150.0, "pnl": 500.0, "pnl_percent": 1.56},
    ])


_MOCK_STRATEGIES = [
    {
        "id": 1,
        "name": "均线交叉",
        "description": "SMA20 / SMA50 金叉死叉（演示数据）",
        "symbol": "BTC-USDT",
        "enabled": True,
    },
    {
        "id": 2,
        "name": "RSI 反转",
        "description": "RSI 超买超卖（演示数据）",
        "symbol": "ETH-USDT",
        "enabled": False,
    },
]

_MOCK_SIGNALS = [
    {
        "id": 1,
        "strategyId": 1,
        "symbol": "BTC-USDT",
        "side": "long",
        "price": 46500.0,
        "createdAt": "2026-07-18T10:00:00",
        "note": "SMA 金叉",
    },
    {
        "id": 2,
        "strategyId": 1,
        "symbol": "BTC-USDT",
        "side": "short",
        "price": 47200.0,
        "createdAt": "2026-07-18T15:30:00",
        "note": "SMA 死叉",
    },
]


@router.get("/strategies")
def get_strategies() -> Result[list]:
    """Get trading strategies (demo stubs)."""
    return Result.success(_MOCK_STRATEGIES)


@router.get("/strategies/{strategy_id}")
def get_strategy(strategy_id: str) -> Result[dict | None]:
    """Get a single strategy by ID."""
    for s in _MOCK_STRATEGIES:
        if str(s["id"]) == str(strategy_id):
            return Result.success(s)
    return Result.success(None)


@router.post("/strategies")
def create_strategy() -> Result[dict]:
    """Create a new strategy (placeholder)."""
    return Result.success({"id": "mock", "status": "created"})


@router.put("/strategies/{strategy_id}")
def update_strategy(strategy_id: str) -> Result[dict]:
    """Update a strategy (placeholder)."""
    return Result.success({"id": strategy_id, "status": "updated"})


@router.delete("/strategies/{strategy_id}")
def delete_strategy(strategy_id: str) -> Result[dict]:
    """Delete a strategy (placeholder)."""
    return Result.success({"id": strategy_id, "status": "deleted"})


@router.get("/signals")
def get_signals(
    strategy_id: str | None = Query(None, alias="strategyId"),
    limit: int | None = Query(None),
) -> Result[list]:
    """Get trading signals (demo stubs)."""
    items = _MOCK_SIGNALS
    if strategy_id is not None:
        items = [s for s in items if str(s.get("strategyId")) == str(strategy_id)]
    if limit is not None:
        items = items[:limit]
    return Result.success(items)


@router.get("/backtest/{strategy_id}")
def get_backtest(strategy_id: str) -> Result[dict | None]:
    """Get backtest results for a strategy (placeholder)."""
    return Result.success(None)


@router.post("/backtest/{strategy_id}")
def run_backtest(strategy_id: str) -> Result[dict]:
    """Run backtest (placeholder)."""
    return Result.success({"id": "mock", "strategy_id": strategy_id, "status": "complete"})


@router.get("/indicators")
def get_indicators(
    symbol: str = Query(...),
    indicator_type: str = Query(..., alias="indicatorType"),
    period: int = Query(14),
) -> Result[list]:
    """Get technical indicators (placeholder)."""
    return Result.success([])
