from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    app_name: str = "linlisHomePage API"
    debug: bool = True
    database_url: str = "sqlite:///./data/homepage.db"
    jwt_secret: str = (
        "bXlTdXBlclNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbjEyMzQ1Njc4OQ=="
    )
    jwt_expiration_ms: int = 86400000
    cors_origins: list[str] = ["*"]
    metalprice_api_enabled: bool = False
    metalprice_api_key: str = ""
    timezone: str = "Asia/Shanghai"


settings = Settings()
