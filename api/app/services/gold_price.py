"""Domestic gold price (AU9999, CNY/gram) via Sina public quote.

Note: GoldPrice.price_usd stores the base price in CNY per gram (column name legacy).
"""

from __future__ import annotations

from datetime import datetime, timedelta, timezone
import logging
import random
import re
import threading
import time

import httpx
from sqlalchemy.orm import Session

from app.models import ExchangeRate, GoldPrice
from app.schemas.gold_price import CurrencyDTO, GoldPriceDTO, PricePoint

logger = logging.getLogger(__name__)

CURRENCIES = {
    "USD": ("美元", "$", "🇺🇸"),
    "CNY": ("人民币", "¥", "🇨🇳"),
    "EUR": ("欧元", "€", "🇪🇺"),
    "GBP": ("英镑", "£", "🇬🇧"),
}

# USD -> currency (1 USD = rate units of currency)
DEFAULT_RATES = {"USD": 1.0, "CNY": 7.25, "EUR": 0.92, "GBP": 0.79}

SINA_AU9999_URL = "https://hq.sinajs.cn/list=gds_AU9999"
SINA_HEADERS = {
    "Referer": "https://finance.sina.com.cn",
    "User-Agent": "Mozilla/5.0 (compatible; CoffeeHomepage/1.0)",
}
MIN_REFRESH_INTERVAL_SEC = 30
STALE_HOURS = 24
DEFAULT_SEED_CNY = 878.0

_lock = threading.Lock()
_last_fetch_mono: float = 0.0
_last_quote: dict[str, float | str] | None = None

_HQ_RE = re.compile(r'hq_str_gds_AU9999="([^"]*)"')


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


def _cny_to_currency(price_cny: float, db: Session, currency: str) -> float:
    """Convert CNY/gram base price to target currency units (still per gram)."""
    currency = currency.upper()
    if currency == "CNY":
        return price_cny
    cny_per_usd = _get_rate(db, "CNY") or DEFAULT_RATES["CNY"]
    price_usd = price_cny / cny_per_usd
    return price_usd * _get_rate(db, currency)


def parse_sina_au9999(body: str) -> dict[str, float | str]:
    match = _HQ_RE.search(body)
    if not match or not match.group(1):
        raise ValueError("Sina AU9999 quote empty or unrecognized")
    parts = match.group(1).split(",")
    if len(parts) < 8:
        raise ValueError(f"Sina AU9999 quote too short: {len(parts)} fields")

    price = float(parts[0])
    high = float(parts[3]) if parts[3] else price
    low = float(parts[5]) if parts[5] else price
    prev_close = float(parts[7]) if parts[7] else price
    change_amount = price - prev_close
    change_percent = (change_amount / prev_close * 100.0) if prev_close else 0.0
    trade_date = parts[12] if len(parts) > 12 else ""
    name = parts[13] if len(parts) > 13 else "黄金9999"

    if price <= 0:
        raise ValueError("Sina AU9999 price invalid")

    return {
        "price": price,
        "high": high,
        "low": low,
        "prev_close": prev_close,
        "change_amount": round(change_amount, 2),
        "change_percent": round(change_percent, 2),
        "trade_date": trade_date,
        "name": name,
    }


def fetch_au9999_quote() -> dict[str, float | str]:
    with httpx.Client(timeout=10.0) as client:
        response = client.get(SINA_AU9999_URL, headers=SINA_HEADERS)
        response.raise_for_status()
        text = response.content.decode("gbk", errors="replace")
    return parse_sina_au9999(text)


def ensure_seed_prices(db: Session) -> GoldPrice:
    latest = db.query(GoldPrice).order_by(GoldPrice.recorded_at.desc()).first()
    if latest:
        return latest
    price = GoldPrice(
        price_usd=DEFAULT_SEED_CNY,
        change_amount=0.0,
        change_percent=0.0,
        currency="CNY",
    )
    db.add(price)
    for code, rate in DEFAULT_RATES.items():
        if code != "USD":
            exists = (
                db.query(ExchangeRate)
                .filter(ExchangeRate.from_currency == "USD", ExchangeRate.to_currency == code)
                .first()
            )
            if not exists:
                db.add(ExchangeRate(from_currency="USD", to_currency=code, rate=rate))
    db.commit()
    db.refresh(price)
    return price


def refresh_from_sina(db: Session, *, force: bool = False) -> GoldPrice:
    """Fetch AU9999 and insert a new GoldPrice row. Min interval 30s between fetches."""
    global _last_fetch_mono, _last_quote
    with _lock:
        now = time.monotonic()
        if _last_fetch_mono and (now - _last_fetch_mono) < MIN_REFRESH_INTERVAL_SEC:
            latest = db.query(GoldPrice).order_by(GoldPrice.recorded_at.desc()).first()
            if latest:
                return latest
        quote = fetch_au9999_quote()
        _last_quote = quote
        row = GoldPrice(
            price_usd=float(quote["price"]),
            change_amount=float(quote["change_amount"]),
            change_percent=float(quote["change_percent"]),
            currency="CNY",
            recorded_at=datetime.now(timezone.utc),
        )
        db.add(row)
        for code, rate in DEFAULT_RATES.items():
            if code == "USD":
                continue
            exists = (
                db.query(ExchangeRate)
                .filter(ExchangeRate.from_currency == "USD", ExchangeRate.to_currency == code)
                .first()
            )
            if not exists:
                db.add(ExchangeRate(from_currency="USD", to_currency=code, rate=rate))
        db.commit()
        db.refresh(row)
        _last_fetch_mono = time.monotonic()
        logger.info("Refreshed AU9999 gold price: %s CNY/g", row.price_usd)
        return row


def maybe_refresh_if_stale(db: Session) -> GoldPrice | None:
    latest = db.query(GoldPrice).order_by(GoldPrice.recorded_at.desc()).first()
    if latest is None:
        try:
            return refresh_from_sina(db, force=True)
        except Exception:
            logger.exception("Initial gold refresh failed; using seed")
            return ensure_seed_prices(db)

    recorded = latest.recorded_at
    if recorded.tzinfo is None:
        recorded = recorded.replace(tzinfo=timezone.utc)
    # Legacy seed stored ~2350 USD/oz in price_usd; AU9999 CNY/g is typically < 2000
    looks_like_legacy_usd = latest.price_usd > 2000
    is_stale = datetime.now(timezone.utc) - recorded > timedelta(hours=STALE_HOURS)
    if looks_like_legacy_usd or is_stale:
        try:
            return refresh_from_sina(db, force=True)
        except Exception:
            logger.exception("Stale gold refresh failed; keeping previous price")
            return latest
    return latest


def get_current_price(db: Session, currency: str) -> GoldPriceDTO:
    currency = currency.upper()
    latest = ensure_seed_prices(db)
    base_cny = latest.price_usd
    price = round(_cny_to_currency(base_cny, db, currency), 2)
    change_amount = round(_cny_to_currency(latest.change_amount or 0.0, db, currency), 2)
    _name, symbol, _flag = CURRENCIES.get(currency, (currency, currency, ""))
    if _last_quote:
        high_cny = float(_last_quote["high"])
        low_cny = float(_last_quote["low"])
    else:
        high_cny = base_cny * 1.01
        low_cny = base_cny * 0.99
    return GoldPriceDTO(
        price=price,
        change_amount=change_amount,
        change_percent=latest.change_percent,
        currency=currency,
        symbol=symbol,
        timestamp=latest.recorded_at,
        high=round(_cny_to_currency(high_cny, db, currency), 2),
        low=round(_cny_to_currency(low_cny, db, currency), 2),
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
    if not prices:
        base = ensure_seed_prices(db).price_usd
        points = []
        for i in range(days):
            day = (datetime.now(timezone.utc) - timedelta(days=days - i - 1)).date()
            jitter = random.uniform(-8, 8)
            points.append(
                PricePoint(
                    date=day.isoformat(),
                    price=round(_cny_to_currency(base + jitter, db, currency), 2),
                )
            )
        return points
    return [
        PricePoint(
            date=p.recorded_at.date().isoformat(),
            price=round(_cny_to_currency(p.price_usd, db, currency), 2),
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
