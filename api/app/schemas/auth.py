from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class LoginRequest(BaseModel):
    username: str | None = None
    password: str


class RegisterRequest(BaseModel):
    username: str
    password: str
    email: str | None = None


class AuthResponse(BaseModel):
    token: str
    type: str = "Bearer"
    id: int
    username: str
    email: str | None = None
    display_name: str | None = Field(None, serialization_alias="displayName")
    avatar: str | None = None
    role: str
    expires_in: int = Field(86400000, serialization_alias="expiresIn")

    model_config = ConfigDict(populate_by_name=True, by_alias=True)


class UserDTO(BaseModel):
    id: int
    username: str
    email: str | None = None
    display_name: str | None = Field(None, serialization_alias="displayName")
    avatar: str | None = None
    role: str
    created_at: datetime | None = Field(None, serialization_alias="createdAt")

    model_config = ConfigDict(from_attributes=True, populate_by_name=True, by_alias=True)


class ProfileUpdateRequest(BaseModel):
    display_name: str | None = Field(None, alias="displayName")
    email: str | None = None

    model_config = ConfigDict(populate_by_name=True)


class PasswordChangeRequest(BaseModel):
    current_password: str = Field(alias="currentPassword")
    new_password: str = Field(alias="newPassword")

    model_config = ConfigDict(populate_by_name=True)
