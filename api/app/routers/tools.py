import time

from fastapi import APIRouter, HTTPException, Response
from fastapi.responses import JSONResponse

from app.schemas.common import Result
from app.schemas.tools import TimestampResult
from app.services import tools as tool_service

router = APIRouter(prefix="/tools", tags=["tools"])


def _body_value(body: dict, *keys: str) -> str:
    for key in keys:
        if key in body and body[key] is not None:
            return str(body[key])
    raise HTTPException(status_code=400, detail=f"Missing one of: {', '.join(keys)}")


@router.post("/json/format")
def json_format(body: dict) -> Result[str]:
    try:
        return Result.success(tool_service.json_format(_body_value(body, "json")))
    except Exception as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/json/minify")
def json_minify(body: dict) -> Result[str]:
    try:
        return Result.success(tool_service.json_minify(_body_value(body, "json")))
    except Exception as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/base64/encode")
def base64_encode(body: dict) -> Result[str]:
    return Result.success(tool_service.base64_encode(_body_value(body, "text")))


@router.post("/base64/decode")
def base64_decode(body: dict) -> Result[str]:
    try:
        return Result.success(tool_service.base64_decode(_body_value(body, "encoded")))
    except Exception as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/url/encode")
def url_encode(body: dict) -> Result[str]:
    return Result.success(tool_service.url_encode(_body_value(body, "text")))


@router.post("/url/decode")
def url_decode(body: dict) -> Result[str]:
    return Result.success(tool_service.url_decode(_body_value(body, "encoded")))


@router.post("/hash/md5")
def hash_md5(body: dict) -> Result[str]:
    return Result.success(tool_service.md5_hash(_body_value(body, "text")))


@router.post("/hash/sha1")
def hash_sha1(body: dict) -> Result[str]:
    return Result.success(tool_service.sha1_hash(_body_value(body, "text")))


@router.post("/hash/sha256")
def hash_sha256(body: dict) -> Result[str]:
    return Result.success(tool_service.sha256_hash(_body_value(body, "text")))


@router.post("/hash/sha512")
def hash_sha512(body: dict) -> Result[str]:
    return Result.success(tool_service.sha512_hash(_body_value(body, "text")))


@router.post("/timestamp/convert")
def timestamp_convert(body: dict) -> Result[TimestampResult]:
    try:
        return Result.success(
            tool_service.timestamp_convert(
                _body_value(body, "input"),
                body.get("fromFormat", "timestamp_ms"),
            )
        )
    except Exception as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/qrcode/generate")
def qrcode_generate(body: dict) -> Response:
    content = _body_value(body, "content", "text")
    width = int(body.get("width", 200))
    height = int(body.get("height", 200))
    png = tool_service.generate_qrcode(content, width, height)
    return Response(content=png, media_type="image/png")


@router.get("/health")
def tools_health() -> Result[dict]:
    return Result.success({"status": "UP", "timestamp": int(time.time() * 1000)})
