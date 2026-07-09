from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.schemas.common import Result
from app.services import gold_price as gold_service

router = APIRouter(prefix="/gold-price", tags=["gold-price"])


@router.get("/current")
def current(
    db: Annotated[Session, Depends(get_db)],
    currency: str = Query("USD"),
) -> Result:
    return Result.success(gold_service.get_current_price(db, currency))


@router.get("/history")
def history(
    db: Annotated[Session, Depends(get_db)],
    currency: str = Query("USD"),
    days: int = Query(30, ge=1, le=365),
) -> Result:
    return Result.success(gold_service.get_price_history(db, currency, days))


@router.get("/currencies")
def currencies(db: Annotated[Session, Depends(get_db)]) -> Result:
    return Result.success(gold_service.list_currencies(db))
