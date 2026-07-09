from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.deps import require_admin
from app.models import Category, User
from app.schemas.article import CategoryDTO, CategoryWrite
from app.schemas.common import Result

router = APIRouter(prefix="/categories", tags=["categories"])


@router.get("")
def list_categories(db: Annotated[Session, Depends(get_db)]) -> Result[list[CategoryDTO]]:
    categories = db.query(Category).order_by(Category.sort_order.asc()).all()
    return Result.success([CategoryDTO.model_validate(c) for c in categories])


@router.get("/{category_id}")
def get_category(category_id: int, db: Annotated[Session, Depends(get_db)]) -> Result[CategoryDTO]:
    category = db.query(Category).filter(Category.id == category_id).first()
    if category is None:
        raise HTTPException(status_code=404, detail="Category not found")
    return Result.success(CategoryDTO.model_validate(category))


@router.post("")
def create_category(
    payload: CategoryWrite,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[CategoryDTO]:
    category = Category(
        name=payload.name,
        description=payload.description,
        icon=payload.icon,
        sort_order=payload.sort_order,
    )
    db.add(category)
    db.commit()
    db.refresh(category)
    return Result.success(CategoryDTO.model_validate(category))


@router.put("/{category_id}")
def update_category(
    category_id: int,
    payload: CategoryWrite,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result[CategoryDTO]:
    category = db.query(Category).filter(Category.id == category_id).first()
    if category is None:
        raise HTTPException(status_code=404, detail="Category not found")
    category.name = payload.name
    category.description = payload.description
    category.icon = payload.icon
    category.sort_order = payload.sort_order
    db.commit()
    db.refresh(category)
    return Result.success(CategoryDTO.model_validate(category))


@router.delete("/{category_id}")
def delete_category(
    category_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[User, Depends(require_admin)],
) -> Result:
    category = db.query(Category).filter(Category.id == category_id).first()
    if category is None:
        raise HTTPException(status_code=404, detail="Category not found")
    db.delete(category)
    db.commit()
    return Result.success()
