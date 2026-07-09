import time
from typing import Generic, TypeVar

from pydantic import BaseModel, ConfigDict, Field

T = TypeVar("T")


class Result(BaseModel, Generic[T]):
    code: int = 200
    message: str = "Success"
    data: T | None = None
    timestamp: int = Field(default_factory=lambda: int(time.time() * 1000))

    @classmethod
    def success(cls, data: T | None = None) -> "Result[T]":
        return cls(code=200, message="Success", data=data)

    @classmethod
    def error(cls, message: str, code: int = 500) -> "Result[T]":
        return cls(code=code, message=message, data=None)


class PageResult(BaseModel, Generic[T]):
    content: list[T]
    total_elements: int = Field(serialization_alias="totalElements")
    total_pages: int = Field(serialization_alias="totalPages")
    number: int
    size: int
    first: bool
    last: bool

    model_config = ConfigDict(populate_by_name=True, by_alias=True)
