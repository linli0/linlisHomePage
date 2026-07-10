import httpx
from fastapi import HTTPException
from app.core.config import settings
from app.schemas.ai import AIModel, AIModelsResponse, AIStatusResponse

# 当前使用的 Ollama URL（支持自动切换）
_current_ollama_url: str | None = None


def _check_ollama_online(url: str) -> bool:
    """检查指定 URL 的 Ollama 是否在线"""
    try:
        response = httpx.get(f"{url}/api/tags", timeout=3.0)
        return response.status_code == 200
    except Exception:
        return False


def _get_effective_ollama_url() -> str:
    """获取当前有效的 Ollama URL"""
    global _current_ollama_url
    
    if _current_ollama_url:
        return _current_ollama_url
    
    # 先检查本地状态
    local_online = _check_ollama_online(settings.ollama_base_url)
    
    if local_online:
        _current_ollama_url = settings.ollama_base_url
        return _current_ollama_url
    
    # 本地不在线，检查远程
    if settings.ollama_remote_enabled and settings.ollama_remote_url:
        remote_online = _check_ollama_online(settings.ollama_remote_url)
        if remote_online:
            _current_ollama_url = settings.ollama_remote_url
            return _current_ollama_url
    
    # 默认使用本地
    _current_ollama_url = settings.ollama_base_url
    return _current_ollama_url


def reset_ollama_url() -> None:
    """重置 Ollama URL（用于强制重新检测）"""
    global _current_ollama_url
    _current_ollama_url = None


def get_models() -> AIModelsResponse:
    """获取可用的 AI 模型列表"""
    try:
        url = _get_effective_ollama_url()
        response = httpx.get(f"{url}/api/tags", timeout=10.0)
        response.raise_for_status()
        
        data = response.json()
        models_data = data.get("models", [])
        
        models = []
        for m in models_data:
            models.append(AIModel(
                name=m.get("name", ""),
                size=m.get("size", 0),
                parameter_size=m.get("details", {}).get("parameter_size", ""),
                family=m.get("details", {}).get("family", ""),
                format=m.get("details", {}).get("format", ""),
                modified_at=m.get("modified_at", ""),
            ))
        
        return AIModelsResponse(models=models)
    
    except Exception as e:
        raise HTTPException(
            status_code=503,
            detail=f"请确保 Ollama 服务已启动 ({str(e)})"
        )


def get_status() -> AIStatusResponse:
    """检查 Ollama 服务状态"""
    local_online = _check_ollama_online(settings.ollama_base_url)
    remote_online = False
    
    if settings.ollama_remote_enabled and settings.ollama_remote_url:
        remote_online = _check_ollama_online(settings.ollama_remote_url)
    
    if local_online:
        return AIStatusResponse(
            status="connected",
            message="本地 Ollama 服务运行正常",
            url=settings.ollama_base_url,
            local_online=True,
            remote_online=remote_online,
            remote_enabled=settings.ollama_remote_enabled,
        )
    elif remote_online:
        return AIStatusResponse(
            status="connected",
            message="本地 Ollama 离线，已切换到远程服务",
            url=settings.ollama_remote_url,
            hint="远程模式运行中",
            local_online=False,
            remote_online=True,
            remote_enabled=settings.ollama_remote_enabled,
        )
    else:
        hint = "请确保 Ollama 已安装并运行: ollama serve"
        if settings.ollama_remote_enabled and settings.ollama_remote_url:
            hint = f"请检查本地服务 ({settings.ollama_base_url}) 或配置远程服务 ({settings.ollama_remote_url})"
        
        return AIStatusResponse(
            status="disconnected",
            message="无法连接到 Ollama 服务",
            hint=hint,
            local_online=False,
            remote_online=False,
            remote_enabled=settings.ollama_remote_enabled,
        )
