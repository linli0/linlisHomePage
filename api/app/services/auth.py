from sqlalchemy.orm import Session

from app.core.security import create_access_token, hash_password, verify_password
from app.models import User, UserRole
from app.schemas.auth import (
    AuthResponse,
    PasswordChangeRequest,
    ProfileUpdateRequest,
    UserDTO,
)


def _auth_response(user: User) -> AuthResponse:
    token = create_access_token(
        username=user.username,
        user_id=user.id,
        email=user.email,
        role=user.role.value,
    )
    return AuthResponse(
        token=token,
        id=user.id,
        username=user.username,
        email=user.email,
        display_name=user.display_name,
        avatar=user.avatar,
        role=user.role.value,
    )


def login(db: Session, *, username: str | None, password: str) -> AuthResponse:
    if username and username.strip():
        user = db.query(User).filter(User.username == username, User.enabled.is_(True)).first()
        if user is None or not verify_password(password, user.password):
            raise ValueError("Invalid credentials")
        return _auth_response(user)

    for candidate in ("admin", "user"):
        user = db.query(User).filter(User.username == candidate, User.enabled.is_(True)).first()
        if user and verify_password(password, user.password):
            return _auth_response(user)
    raise ValueError("Invalid credentials")


def get_user_dto(db: Session, username: str) -> UserDTO:
    user = db.query(User).filter(User.username == username).first()
    if user is None:
        raise ValueError("User not found")
    return UserDTO.model_validate(user)


def update_profile(db: Session, username: str, payload: ProfileUpdateRequest) -> UserDTO:
    user = db.query(User).filter(User.username == username).first()
    if user is None:
        raise ValueError("User not found")
    if payload.display_name is not None:
        user.display_name = payload.display_name
    if payload.email is not None:
        existing = db.query(User).filter(User.email == payload.email).first()
        if existing and existing.id != user.id:
            raise ValueError("Email already exists")
        user.email = payload.email
    db.commit()
    db.refresh(user)
    return UserDTO.model_validate(user)


def change_password(db: Session, username: str, payload: PasswordChangeRequest) -> None:
    user = db.query(User).filter(User.username == username).first()
    if user is None:
        raise ValueError("User not found")
    if not verify_password(payload.current_password, user.password):
        raise ValueError("Current password is incorrect")
    user.password = hash_password(payload.new_password)
    db.commit()
