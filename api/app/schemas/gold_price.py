from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class GoldPriceDTO(BaseModel):
    price: float
    change_amount: float | None = Field(None, serialization_alias="changeAmount")
    change_percent: float | None = Field(None, serialization_alias="changePercent")
    currency: str
    symbol: str
    timestamp: datetime | None = None
    high: float | None = None
    low: float | None = None
    average: float | None = None
    volatility: float | None = None

    model_config = ConfigDict(populate_by_name=True, by_alias=True)


class PricePoint(BaseModel):
    date: str
    price: float


class CurrencyDTO(BaseModel):
    code: str
    name: str
    symbol: str
    flag: str
    rate: float

    model_config = ConfigDict(populate_by_name=True, by_alias=True)
