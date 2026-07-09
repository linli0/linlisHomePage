def test_current_gold_price(client):
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
