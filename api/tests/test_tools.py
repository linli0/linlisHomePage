def test_json_format(client):
    response = client.post("/api/tools/json/format", json={"json": '{"a":1}'})
    body = response.json()
    assert body["code"] == 200
    assert '"a"' in body["data"]


def test_json_minify(client):
    response = client.post("/api/tools/json/minify", json={"json": '{\n  "a": 1\n}'})
    assert response.json()["data"] == '{"a":1}'


def test_base64_roundtrip(client):
    encoded = client.post("/api/tools/base64/encode", json={"text": "hello"}).json()["data"]
    decoded = client.post("/api/tools/base64/decode", json={"encoded": encoded}).json()["data"]
    assert decoded == "hello"


def test_url_roundtrip(client):
    encoded = client.post("/api/tools/url/encode", json={"text": "a b"}).json()["data"]
    decoded = client.post("/api/tools/url/decode", json={"encoded": encoded}).json()["data"]
    assert decoded == "a b"


def test_hashes(client):
    text = "test"
    md5 = client.post("/api/tools/hash/md5", json={"text": text}).json()["data"]
    assert len(md5) == 32
    sha256 = client.post("/api/tools/hash/sha256", json={"text": text}).json()["data"]
    assert len(sha256) == 64


def test_timestamp_convert(client):
    response = client.post(
        "/api/tools/timestamp/convert",
        json={"input": "1700000000000", "fromFormat": "timestamp_ms"},
    )
    data = response.json()["data"]
    assert data["timestampMs"] == 1700000000000
    assert data["timestampSec"] == 1700000000


def test_qrcode_png(client):
    response = client.post(
        "/api/tools/qrcode/generate",
        json={"text": "hello", "width": 100, "height": 100},
    )
    assert response.status_code == 200
    assert response.headers["content-type"] == "image/png"
    assert response.content.startswith(b"\x89PNG")
