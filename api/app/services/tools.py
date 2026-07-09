import base64
import hashlib
import io
import json
import urllib.parse
from datetime import datetime, timezone

import qrcode

from app.schemas.tools import TimestampResult


def json_format(value: str) -> str:
    parsed = json.loads(value)
    return json.dumps(parsed, indent=2, ensure_ascii=False)


def json_minify(value: str) -> str:
    parsed = json.loads(value)
    return json.dumps(parsed, separators=(",", ":"), ensure_ascii=False)


def base64_encode(text: str) -> str:
    return base64.b64encode(text.encode()).decode()


def base64_decode(encoded: str) -> str:
    return base64.b64decode(encoded).decode()


def url_encode(text: str) -> str:
    return urllib.parse.quote(text, safe="")


def url_decode(encoded: str) -> str:
    return urllib.parse.unquote(encoded)


def md5_hash(text: str) -> str:
    return hashlib.md5(text.encode()).hexdigest()


def sha1_hash(text: str) -> str:
    return hashlib.sha1(text.encode()).hexdigest()


def sha256_hash(text: str) -> str:
    return hashlib.sha256(text.encode()).hexdigest()


def sha512_hash(text: str) -> str:
    return hashlib.sha512(text.encode()).hexdigest()


def timestamp_convert(input_value: str, from_format: str = "timestamp_ms") -> TimestampResult:
    if from_format == "timestamp_ms":
        ts_ms = int(input_value)
    elif from_format == "timestamp_s":
        ts_ms = int(input_value) * 1000
    elif from_format == "iso":
        ts_ms = int(datetime.fromisoformat(input_value).timestamp() * 1000)
    else:
        raise ValueError(f"Unknown format: {from_format}")
    dt = datetime.fromtimestamp(ts_ms / 1000, tz=timezone.utc)
    return TimestampResult(
        timestamp_ms=ts_ms,
        timestamp_sec=ts_ms // 1000,
        iso=dt.isoformat(),
        formatted=dt.strftime("%Y-%m-%d %H:%M:%S"),
    )


def generate_qrcode(content: str, width: int = 200, height: int = 200) -> bytes:
    qr = qrcode.QRCode(version=1, box_size=10, border=2)
    qr.add_data(content)
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    img = img.resize((width, height))
    buffer = io.BytesIO()
    img.save(buffer, format="PNG")
    return buffer.getvalue()
