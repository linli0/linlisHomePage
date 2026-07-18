from fastapi import APIRouter
from fastapi.responses import StreamingResponse

from app.schemas.ai import AIChatRequest, AIModelsResponse, AIStatusResponse
from app.schemas.common import Result
from app.services import ai as ai_service

router = APIRouter(prefix="/ai", tags=["ai"])


@router.get("/models")
def get_models() -> Result[AIModelsResponse]:
    """获取可用的 AI 模型列表"""
    models = ai_service.get_models()
    return Result.success(models)


@router.get("/status")
def get_status() -> Result[AIStatusResponse]:
    """检查 Ollama 服务状态"""
    status = ai_service.get_status()
    return Result.success(status)


async def _announce_ai_done() -> None:
    try:
        from app.plugins.xiaomi.dialogue.orchestrator import announce

        await announce("本站 AI 对话完成", kind="ollama")
    except Exception:
        pass


@router.post("/chat")
async def chat(request: AIChatRequest):
    """与 AI 对话（支持流式和非流式）"""
    if request.stream:

        async def generate():
            done = False
            try:
                import httpx

                url = ai_service._get_effective_ollama_url()

                async with httpx.AsyncClient(timeout=300.0) as client:
                    async with client.stream(
                        "POST",
                        f"{url}/api/generate",
                        json={
                            "model": request.model,
                            "prompt": request.prompt,
                            "stream": True,
                            "context": request.context or [],
                        },
                    ) as response:
                        async for line in response.aiter_lines():
                            if line:
                                yield line + "\n"
                                if '"done":true' in line.replace(" ", "") or '"done": true' in line:
                                    done = True
            except Exception as e:
                yield f'{{"error": "{str(e)}"}}\n'
            if done:
                await _announce_ai_done()

        return StreamingResponse(
            generate(),
            media_type="text/plain",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "Transfer-Encoding": "chunked",
            },
        )
    else:
        try:
            import httpx

            url = ai_service._get_effective_ollama_url()

            response = httpx.post(
                f"{url}/api/generate",
                json={
                    "model": request.model,
                    "prompt": request.prompt,
                    "stream": False,
                    "context": request.context or [],
                },
                timeout=300.0,
            )
            response.raise_for_status()
            await _announce_ai_done()
            return Result.success(response.json())
        except Exception as e:
            return Result.error(f"Failed to communicate with Ollama service: {str(e)}")
