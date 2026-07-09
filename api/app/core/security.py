import base64
from datetime import datetime, timedelta, timezone
from typing import Any

import bcrypt
from jose import JWTError, jwt

from app.core.config import settings

ALGORITHM = "HS256"


def _signing_key() -> bytes:
    return base64.b64decode(settings.jwt_secret)


def verify_password(plain: str, hashed: str) -> bool:
    return bcrypt.checkpw(plain.encode(), hashed.encode())


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()


def create_access_token(
    *,
    username: str,
    user_id: int,
    email: str | None,
    role: str,
) -> str:
    expire = datetime.now(timezone.utc) + timedelta(milliseconds=settings.jwt_expiration_ms)
    payload: dict[str, Any] = {
        "sub": username,
        "id": user_id,
        "email": email,
        "role": role,
        "exp": expire,
        "iat": datetime.now(timezone.utc),
    }
    return jwt.encode(payload, _signing_key(), algorithm=ALGORITHM)


def decode_token(token: str) -> dict[str, Any]:
    return jwt.decode(token, _signing_key(), algorithms=[ALGORITHM])


def validate_token(token: str) -> dict[str, Any] | None:
    try:
        return decode_token(token)
    except JWTError:
        return None
