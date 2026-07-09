from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class CategoryRef(BaseModel):
    id: int | None = None
    name: str | None = None
    description: str | None = None
    icon: str | None = None
    sort_order: int | None = Field(None, alias="sortOrder")

    model_config = ConfigDict(populate_by_name=True)


class TagRef(BaseModel):
    id: int | None = None
    name: str | None = None
    color: str | None = None

    model_config = ConfigDict(populate_by_name=True)


class CategoryDTO(BaseModel):
    id: int
    name: str
    description: str | None = None
    icon: str | None = None
    sort_order: int = Field(0, serialization_alias="sortOrder")

    model_config = ConfigDict(from_attributes=True, populate_by_name=True, by_alias=True)


class CategoryWrite(BaseModel):
    id: int | None = None
    name: str
    description: str | None = None
    icon: str | None = None
    sort_order: int = Field(0, alias="sortOrder")

    model_config = ConfigDict(populate_by_name=True)


class TagDTO(BaseModel):
    id: int
    name: str
    color: str | None = None

    model_config = ConfigDict(from_attributes=True, populate_by_name=True, by_alias=True)


class TagWrite(BaseModel):
    id: int | None = None
    name: str
    color: str | None = None

    model_config = ConfigDict(populate_by_name=True)


class AuthorDTO(BaseModel):
    id: int
    username: str
    display_name: str | None = Field(None, serialization_alias="displayName")
    avatar: str | None = None

    model_config = ConfigDict(from_attributes=True, populate_by_name=True, by_alias=True)


class ArticleDTO(BaseModel):
    id: int
    title: str
    content: str
    summary: str | None = None
    cover_image: str | None = Field(None, serialization_alias="coverImage")
    published: bool = False
    view_count: int = Field(0, serialization_alias="viewCount")
    created_at: datetime | None = Field(None, serialization_alias="createdAt")
    updated_at: datetime | None = Field(None, serialization_alias="updatedAt")
    category: CategoryDTO | None = None
    tags: list[TagDTO] | None = None
    author: AuthorDTO | None = None

    model_config = ConfigDict(from_attributes=True, populate_by_name=True, by_alias=True)


class ArticleWrite(BaseModel):
    id: int | None = None
    title: str
    content: str
    summary: str | None = None
    cover_image: str | None = Field(None, alias="coverImage")
    published: bool = False
    category: CategoryRef | None = None
    tags: list[TagRef] | None = None

    model_config = ConfigDict(populate_by_name=True)
