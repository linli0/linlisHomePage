def test_health_endpoint(client):
    response = client.get("/api/health")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert body["data"]["status"] == "UP"


def test_tools_health_endpoint(client):
    response = client.get("/api/tools/health")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert body["data"]["status"] == "UP"
