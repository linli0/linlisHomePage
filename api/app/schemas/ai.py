from pydantic import BaseModel, ConfigDict, Field


class AIModel(BaseModel):
    name: str
    size: int = 0
    parameter_size: str = Field("", serialization_alias="parameterSize")
    family: str = ""
    format: str = ""
    modified_at: str = Field("", serialization_alias="modifiedAt")

    model_config = ConfigDict(populate_by_name=True, by_alias=True)


class AIModelsResponse(BaseModel):
    models: list[AIModel] = []


class AIChatRequest(BaseModel):
    model: str
    prompt: str
    stream: bool = True
    context: list[int] | None = None


class AIStatusResponse(BaseModel):
    status: str
    message: str = ""
    url: str | None = None
    hint: str | None = None
    local_online: bool = Field(False, serialization_alias="localOnline")
    remote_online: bool = Field(False, serialization_alias="remoteOnline")
    remote_enabled: bool = Field(False, serialization_alias="remoteEnabled")

    model_config = ConfigDict(populate_by_name=True, by_alias=True)
