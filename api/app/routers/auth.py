from typing import Annotated

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.deps import get_current_user_optional
from app.models import User
from app.schemas.auth import (
    LoginRequest,
    PasswordChangeRequest,
    ProfileUpdateRequest,
    RegisterRequest,
    UserDTO,
)
from app.schemas.common import Result
from app.services import auth as auth_service

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/login")
def login(payload: LoginRequest, db: Annotated[Session, Depends(get_db)]):
    try:
        return Result.success(auth_service.login(db, username=payload.username, password=payload.password))
    except ValueError:
        return JSONResponse(
            status_code=401,
            content=Result.error("Invalid credentials", code=401).model_dump(),
        )


@router.post("/register")
def register(_: RegisterRequest):
    return JSONResponse(
        status_code=403,
        content=Result.error("Registration is disabled", code=403).model_dump(),
    )


@router.get("/me")
def me(
    user: Annotated[User | None, Depends(get_current_user_optional)],
    db: Annotated[Session, Depends(get_db)],
):
    if user is None:
        return JSONResponse(
            status_code=401,
            content=Result.error("Not authenticated", code=401).model_dump(),
        )
    return Result.success(auth_service.get_user_dto(db, user.username))


@router.put("/profile")
def update_profile(
    payload: ProfileUpdateRequest,
    user: Annotated[User | None, Depends(get_current_user_optional)],
    db: Annotated[Session, Depends(get_db)],
):
    if user is None:
        return JSONResponse(
            status_code=401,
            content=Result.error("Not authenticated", code=401).model_dump(),
        )
    try:
        return Result.success(auth_service.update_profile(db, user.username, payload))
    except ValueError as exc:
        return JSONResponse(
            status_code=400,
            content=Result.error(str(exc), code=400).model_dump(),
        )


@router.put("/password")
def change_password(
    payload: PasswordChangeRequest,
    user: Annotated[User | None, Depends(get_current_user_optional)],
    db: Annotated[Session, Depends(get_db)],
):
    if user is None:
        return JSONResponse(
            status_code=401,
            content=Result.error("Not authenticated", code=401).model_dump(),
        )
    try:
        auth_service.change_password(db, user.username, payload)
        return Result.success("Password changed successfully")
    except ValueError as exc:
        return JSONResponse(
            status_code=401,
            content=Result.error(str(exc), code=401).model_dump(),
        )
