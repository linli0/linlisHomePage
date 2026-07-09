from fastapi import APIRouter

from app.schemas.common import Result

router = APIRouter(tags=["health"])


@router.get("/health")
def health() -> Result[dict]:
    return Result.success({"status": "UP", "service": "linlis-homepage-api"})
