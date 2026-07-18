from contextlib import asynccontextmanager
import os

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.core.config import settings
from app.core.database import SessionLocal, init_db
from app.routers import ai, articles, auth, categories, gold_price, health, tags, tools, tweets
from app.schemas.common import Result
from app.seed import seed_database


@asynccontextmanager
async def lifespan(_: FastAPI):
    scheduler_started = False
    xiaomi_workers = False
    if os.environ.get("SKIP_APP_STARTUP") != "1":
        init_db()
        db = SessionLocal()
        try:
            seed_database(db)
            from app.services.gold_price import maybe_refresh_if_stale

            maybe_refresh_if_stale(db)
            if os.environ.get("ENABLE_XIAOMI", "true").lower() != "false":
                from app.plugins.xiaomi.dialogue import panel as panel_mod
                from app.plugins.xiaomi.dialogue import settings_store

                settings_store.get_or_create(db)
                if settings_store.migrate_legacy_deepseek_default(db):
                    import logging

                    logging.getLogger(__name__).info(
                        "dialogue provider migrated: deepseek -> ollama"
                    )
                panel_mod.seed_presets(db)
        finally:
            db.close()
        from app.services.gold_scheduler import start_gold_scheduler

        start_gold_scheduler()
        scheduler_started = True
        if os.environ.get("ENABLE_XIAOMI", "true").lower() != "false":
            from app.plugins.xiaomi.voice.worker import start_voice_worker
            from app.plugins.xiaomi.watchers.manager import start_watchers

            start_watchers()
            start_voice_worker()
            import asyncio

            from app.plugins.xiaomi.dialogue import hub as xiaomi_hub

            xiaomi_hub.bind_loop(asyncio.get_running_loop())
            xiaomi_workers = True
    yield
    if xiaomi_workers:
        from app.plugins.xiaomi.voice.worker import stop_voice_worker
        from app.plugins.xiaomi.watchers.manager import stop_watchers

        stop_voice_worker()
        await stop_watchers()
    if scheduler_started:
        from app.services.gold_scheduler import stop_gold_scheduler

        stop_gold_scheduler()


app = FastAPI(title=settings.app_name, lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router, prefix="/api")
app.include_router(auth.router, prefix="/api")
app.include_router(articles.router, prefix="/api")
app.include_router(categories.router, prefix="/api")
app.include_router(tags.router, prefix="/api")
app.include_router(gold_price.router, prefix="/api")
app.include_router(tools.router, prefix="/api")
app.include_router(ai.router, prefix="/api")
app.include_router(tweets.router, prefix="/api")

# Quant and xiaomi plugins (enabled by default; set ENABLE_QUANT=false or ENABLE_XIAOMI=false to disable)
if os.environ.get("ENABLE_QUANT", "true").lower() != "false":
    from app.plugins.quant.router import router as quant_router
    app.include_router(quant_router, prefix="/api")

if os.environ.get("ENABLE_XIAOMI", "true").lower() != "false":
    from app.plugins.xiaomi.router import router as xiaomi_router
    app.include_router(xiaomi_router, prefix="/api")


@app.exception_handler(Exception)
async def unhandled_exception_handler(_, exc: Exception):
    return JSONResponse(
        status_code=500,
        content=Result.error(str(exc)).model_dump(),
    )
