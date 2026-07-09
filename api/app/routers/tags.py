from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.deps import require_admin
from app.models import Tag, User
from app.schemas.article import TagDTO, TagWrite
from app.schemas.common import Result

router = APIRouter(prefix="/tags", tags=["tags"])


@router.get("")
def list_tags(db: Annotated[Session, Depends(get_db)]) -> Result[list[TagDTO]]:
    tags = db.query(Tag).order_by(Tag.name.asc()).all()
    return Result.success([TagDTO.model_validate(t) for t in tags])


@router.get("/{tag_id}")
def get_tag(tag_id: int, db: Annotated[Session, Depends(get_db)]) -> Result[TagDTO]:
    tag = db.query(Tag).filter(Tag.id == tag_id).first()
    if tag is None:
        raise HTTPException(status_code=404, detail="Tag not found")
    return Result.success(TagDTO.model_validate(tag))


@router.post("")
def create_tag(
    payload: TagWrite,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[TagDTO]:
    tag = Tag(name=payload.name, color=payload.color)
    db.add(tag)
    db.commit()
    db.refresh(tag)
    return Result.success(TagDTO.model_validate(tag))


@router.put("/{tag_id}")
def update_tag(
    tag_id: int,
    payload: TagWrite,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[TagDTO]:
    tag = db.query(Tag).filter(Tag.id == tag_id).first()
    if tag is None:
        raise HTTPException(status_code=404, detail="Tag not found")
    tag.name = payload.name
    tag.color = payload.color
    db.commit()
    db.refresh(tag)
    return Result.success(TagDTO.model_validate(tag))


@router.delete("/{tag_id}")
def delete_tag(
    tag_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result:
    tag = db.query(Tag).filter(Tag.id == tag_id).first()
    if tag is None:
        raise HTTPException(status_code=404, detail="Tag not found")
    db.delete(tag)
    db.commit()
    return Result.success()
