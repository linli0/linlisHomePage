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
    if os.environ.get("SKIP_APP_STARTUP") != "1":
        init_db()
        db = SessionLocal()
        try:
            seed_database(db)
        finally:
            db.close()
    yield


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

# Conditionally register quant plugin
if os.environ.get("ENABLE_QUANT", "").lower() == "true":
    from app.plugins.quant.router import router as quant_router
    app.include_router(quant_router, prefix="/api")

# Conditionally register xiaomi plugin
if os.environ.get("ENABLE_XIAOMI", "").lower() == "true":
    from app.plugins.xiaomi.router import router as xiaomi_router
    app.include_router(xiaomi_router, prefix="/api")


@app.exception_handler(Exception)
async def unhandled_exception_handler(_, exc: Exception):
    return JSONResponse(
        status_code=500,
        content=Result.error(str(exc)).model_dump(),
    )
