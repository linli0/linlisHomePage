from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.deps import require_admin
from app.models import User
from app.schemas.article import ArticleDTO, ArticleWrite
from app.schemas.common import Result
from app.services import articles as article_service

router = APIRouter(prefix="/articles", tags=["articles"])


@router.get("/public/list")
def list_public(
    db: Annotated[Session, Depends(get_db)],
    page: int = Query(0, ge=0),
    size: int = Query(10, ge=1, le=100),
    category: int | None = None,
) -> Result:
    return Result.success(article_service.list_published(db, page=page, size=size, category_id=category))


@router.get("/public/recent")
def recent(db: Annotated[Session, Depends(get_db)]) -> Result[list[ArticleDTO]]:
    return Result.success(article_service.recent_articles(db))


@router.get("/public/popular")
def popular(db: Annotated[Session, Depends(get_db)]) -> Result[list[ArticleDTO]]:
    return Result.success(article_service.popular_articles(db))


@router.get("/public/{article_id}")
def get_public(article_id: int, db: Annotated[Session, Depends(get_db)]) -> Result[ArticleDTO]:
    try:
        return Result.success(article_service.get_public_article(db, article_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.post("")
def create(
    payload: ArticleWrite,
    db: Annotated[Session, Depends(get_db)],
    user: Annotated[User, Depends(require_admin)],
) -> Result[ArticleDTO]:
    return Result.success(article_service.create_article(db, payload, user))


@router.put("/{article_id}")
def update(
    article_id: int,
    payload: ArticleWrite,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[ArticleDTO]:
    try:
        return Result.success(article_service.update_article(db, article_id, payload))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.delete("/{article_id}")
def delete(
    article_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result:
    try:
        article_service.delete_article(db, article_id)
        return Result.success()
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
