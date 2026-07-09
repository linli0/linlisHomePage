from datetime import datetime, timedelta, timezone
import random

from sqlalchemy.orm import Session

from app.models import ExchangeRate, GoldPrice
from app.schemas.gold_price import CurrencyDTO, GoldPriceDTO, PricePoint

CURRENCIES = {
    "USD": ("美元", "$", "🇺🇸"),
    "CNY": ("人民币", "¥", "🇨🇳"),
    "EUR": ("欧元", "€", "🇪🇺"),
    "GBP": ("英镑", "£", "🇬🇧"),
}

DEFAULT_RATES = {"USD": 1.0, "CNY": 7.25, "EUR": 0.92, "GBP": 0.79}


def _get_rate(db: Session, currency: str) -> float:
    if currency == "USD":
        return 1.0
    rate = (
        db.query(ExchangeRate)
        .filter(ExchangeRate.from_currency == "USD", ExchangeRate.to_currency == currency)
        .order_by(ExchangeRate.updated_at.desc())
        .first()
    )
    if rate:
        return rate.rate
    return DEFAULT_RATES.get(currency, 1.0)


def ensure_seed_prices(db: Session) -> GoldPrice:
    latest = db.query(GoldPrice).order_by(GoldPrice.recorded_at.desc()).first()
    if latest:
        return latest
    price = GoldPrice(price_usd=2350.0, change_amount=5.5, change_percent=0.23, currency="USD")
    db.add(price)
    for code, rate in DEFAULT_RATES.items():
        if code != "USD":
            db.add(ExchangeRate(from_currency="USD", to_currency=code, rate=rate))
    db.commit()
    db.refresh(price)
    return price


def get_current_price(db: Session, currency: str) -> GoldPriceDTO:
    currency = currency.upper()
    latest = ensure_seed_prices(db)
    rate = _get_rate(db, currency)
    name, symbol, _flag = CURRENCIES.get(currency, (currency, currency, ""))
    price = round(latest.price_usd * rate, 2)
    change_amount = round((latest.change_amount or 0) * rate, 2)
    return GoldPriceDTO(
        price=price,
        change_amount=change_amount,
        change_percent=latest.change_percent,
        currency=currency,
        symbol=symbol,
        timestamp=latest.recorded_at,
        high=round(price * 1.01, 2),
        low=round(price * 0.99, 2),
        average=price,
        volatility=0.5,
    )


def get_price_history(db: Session, currency: str, days: int) -> list[PricePoint]:
    currency = currency.upper()
    start = datetime.now(timezone.utc) - timedelta(days=days)
    prices = (
        db.query(GoldPrice)
        .filter(GoldPrice.recorded_at >= start)
        .order_by(GoldPrice.recorded_at.asc())
        .all()
    )
    rate = _get_rate(db, currency)
    if not prices:
        base = ensure_seed_prices(db).price_usd
        points = []
        for i in range(days):
            day = (datetime.now(timezone.utc) - timedelta(days=days - i - 1)).date()
            jitter = random.uniform(-20, 20)
            points.append(
                PricePoint(date=day.isoformat(), price=round((base + jitter) * rate, 2))
            )
        return points
    return [
        PricePoint(
            date=p.recorded_at.date().isoformat(),
            price=round(p.price_usd * rate, 2),
        )
        for p in prices
    ]


def list_currencies(db: Session) -> list[CurrencyDTO]:
    return [
        CurrencyDTO(
            code=code,
            name=name,
            symbol=symbol,
            flag=flag,
            rate=_get_rate(db, code),
        )
        for code, (name, symbol, flag) in CURRENCIES.items()
    ]
