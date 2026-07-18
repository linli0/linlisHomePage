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
    
    # Ollama AI 配置（对话降级用）
    ollama_base_url: str = "http://localhost:11434"
    ollama_remote_url: str = ""
    ollama_remote_enabled: bool = False
    ollama_auto_switch: bool = True

    # DeepSeek（多轮对话默认提供方）
    deepseek_api_key: str = ""
    deepseek_base_url: str = "https://api.deepseek.com"
    deepseek_model: str = "deepseek-v4-flash"
    ai_default_provider: str = "deepseek"  # deepseek | ollama

    # 小爱音箱（token 仅放服务端；DID 用于云端 TTS）
    xiaomi_enabled: bool = False
    xiaomi_device_ip: str = ""
    xiaomi_device_token: str = ""
    xiaomi_device_name: str = "小爱音箱"
    xiaomi_device_model: str = "LX06"
    xiaomi_device_did: str = ""
    xiaomi_timeout_sec: float = 10.0

    # 小爱对话 / 完成播报
    xiaomi_dialogue_idle_sec: int = 120
    xiaomi_tts_cooldown_ms: int = 500
    xiaomi_announce_debounce_sec: int = 30
    xiaomi_voice_enabled_default: bool = True
    xiaomi_announce_enabled_default: bool = True
    cursor_transcripts_dir: str = ""
    codex_sessions_dir: str = ""
    repo_root: str = ""


settings = Settings()
