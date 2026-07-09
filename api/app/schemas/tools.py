from pydantic import BaseModel, Field


class TimestampResult(BaseModel):
    timestamp_ms: int = Field(serialization_alias="timestampMs")
    timestamp_sec: int = Field(serialization_alias="timestampSec")
    iso: str
    formatted: str

    model_config = {"populate_by_name": True, "by_alias": True}
