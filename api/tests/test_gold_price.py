from unittest.mock import patch

SINA_SAMPLE = (
    'var hq_str_gds_AU9999="878.00,0,870.00,878.00,878.00,866.00,'
    '02:30:00,872.48,872.00,5988,10.00,22.00,2026-07-18,黄金99";\n'
)


def test_parse_sina_au9999():
    from app.services.gold_price import parse_sina_au9999

    quote = parse_sina_au9999(SINA_SAMPLE)
    assert quote["price"] == 878.0
    assert quote["high"] == 878.0
    assert quote["low"] == 866.0
    assert quote["change_amount"] == 5.52
    assert abs(quote["change_percent"] - 0.63) < 0.01


def test_current_gold_price_default_cny(client):
    response = client.get("/api/gold-price/current")
    body = response.json()
    assert body["code"] == 200
    data = body["data"]
    assert data["currency"] == "CNY"
    assert data["symbol"] == "¥"
    assert data["price"] > 0


def test_current_gold_price_usd(client):
    response = client.get("/api/gold-price/current?currency=USD")
    body = response.json()
    assert body["code"] == 200
    data = body["data"]
    assert data["currency"] == "USD"
    assert data["price"] > 0


def test_gold_price_history(client):
    response = client.get("/api/gold-price/history?currency=CNY&days=7")
    points = response.json()["data"]
    assert len(points) == 7
    assert "date" in points[0]
    assert points[0]["price"] > 0


def test_currencies(client):
    response = client.get("/api/gold-price/currencies")
    currencies = response.json()["data"]
    codes = {c["code"] for c in currencies}
    assert {"USD", "CNY", "EUR", "GBP"}.issubset(codes)


def test_refresh_writes_new_price(client):
    with patch("app.services.gold_price.fetch_au9999_quote") as mock_fetch:
        mock_fetch.return_value = {
            "price": 900.0,
            "high": 905.0,
            "low": 890.0,
            "prev_close": 880.0,
            "change_amount": 20.0,
            "change_percent": 2.27,
            "trade_date": "2026-07-19",
            "name": "黄金99",
        }
        # reset throttle
        import app.services.gold_price as gp

        gp._last_fetch_mono = 0.0
        response = client.post("/api/gold-price/refresh")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert body["data"]["price"] == 900.0
    assert body["data"]["currency"] == "CNY"

    current = client.get("/api/gold-price/current?currency=CNY").json()["data"]
    assert current["price"] == 900.0


def test_refresh_failure_keeps_current(client):
    before = client.get("/api/gold-price/current?currency=CNY").json()["data"]["price"]
    with patch("app.services.gold_price.fetch_au9999_quote") as mock_fetch:
        mock_fetch.side_effect = RuntimeError("network down")
        import app.services.gold_price as gp

        gp._last_fetch_mono = 0.0
        response = client.post("/api/gold-price/refresh")
    assert response.status_code == 502
    after = client.get("/api/gold-price/current?currency=CNY").json()["data"]["price"]
    assert after == before
