from sqlalchemy.orm import Session, joinedload

from app.models import Article, Category, Tag, User
from app.schemas.article import ArticleDTO, ArticleWrite, AuthorDTO, CategoryDTO, TagDTO
from app.schemas.common import PageResult
import math


def _author_dto(user: User | None) -> AuthorDTO | None:
    if user is None:
        return None
    return AuthorDTO(
        id=user.id,
        username=user.username,
        display_name=user.display_name,
        avatar=user.avatar,
    )


def article_to_dto(article: Article) -> ArticleDTO:
    return ArticleDTO(
        id=article.id,
        title=article.title,
        content=article.content,
        summary=article.summary,
        cover_image=article.cover_image,
        published=article.published,
        view_count=article.view_count,
        created_at=article.created_at,
        updated_at=article.updated_at,
        category=CategoryDTO.model_validate(article.category) if article.category else None,
        tags=[TagDTO.model_validate(t) for t in article.tags] if article.tags else [],
        author=_author_dto(article.author),
    )


def list_published(
    db: Session, *, page: int, size: int, category_id: int | None = None
) -> PageResult[ArticleDTO]:
    query = db.query(Article).filter(Article.published.is_(True))
    if category_id is not None:
        query = query.filter(Article.category_id == category_id)
    total = query.count()
    articles = (
        query.options(
            joinedload(Article.category),
            joinedload(Article.tags),
            joinedload(Article.author),
        )
        .order_by(Article.created_at.desc())
        .offset(page * size)
        .limit(size)
        .all()
    )
    total_pages = math.ceil(total / size) if size else 0
    return PageResult(
        content=[article_to_dto(a) for a in articles],
        total_elements=total,
        total_pages=total_pages,
        number=page,
        size=size,
        first=page == 0,
        last=page >= max(total_pages - 1, 0),
    )


def get_public_article(db: Session, article_id: int) -> ArticleDTO:
    article = (
        db.query(Article)
        .options(joinedload(Article.category), joinedload(Article.tags), joinedload(Article.author))
        .filter(Article.id == article_id, Article.published.is_(True))
        .first()
    )
    if article is None:
        raise ValueError("Article not found")
    article.view_count += 1
    db.commit()
    db.refresh(article)
    return article_to_dto(article)


def recent_articles(db: Session, limit: int = 5) -> list[ArticleDTO]:
    articles = (
        db.query(Article)
        .options(joinedload(Article.category), joinedload(Article.tags), joinedload(Article.author))
        .filter(Article.published.is_(True))
        .order_by(Article.created_at.desc())
        .limit(limit)
        .all()
    )
    return [article_to_dto(a) for a in articles]


def popular_articles(db: Session, limit: int = 5) -> list[ArticleDTO]:
    articles = (
        db.query(Article)
        .options(joinedload(Article.category), joinedload(Article.tags), joinedload(Article.author))
        .filter(Article.published.is_(True))
        .order_by(Article.view_count.desc())
        .limit(limit)
        .all()
    )
    return [article_to_dto(a) for a in articles]


def _apply_tags(db: Session, article: Article, tags: list | None) -> None:
    if tags is None:
        return
    tag_ids = [t.id for t in tags if t.id is not None]
    article.tags = db.query(Tag).filter(Tag.id.in_(tag_ids)).all() if tag_ids else []


def create_article(db: Session, payload: ArticleWrite, author: User) -> ArticleDTO:
    article = Article(
        title=payload.title,
        content=payload.content,
        summary=payload.summary,
        cover_image=payload.cover_image,
        published=payload.published,
        author_id=author.id,
        category_id=payload.category.id if payload.category and payload.category.id else None,
    )
    db.add(article)
    db.flush()
    _apply_tags(db, article, payload.tags)
    db.commit()
    db.refresh(article)
    return article_to_dto(article)


def update_article(db: Session, article_id: int, payload: ArticleWrite) -> ArticleDTO:
    article = db.query(Article).filter(Article.id == article_id).first()
    if article is None:
        raise ValueError("Article not found")
    article.title = payload.title
    article.content = payload.content
    article.summary = payload.summary
    article.cover_image = payload.cover_image
    article.published = payload.published
    if payload.category and payload.category.id:
        article.category_id = payload.category.id
    if payload.tags is not None:
        _apply_tags(db, article, payload.tags)
    db.commit()
    db.refresh(article)
    return article_to_dto(article)


def delete_article(db: Session, article_id: int) -> None:
    article = db.query(Article).filter(Article.id == article_id).first()
    if article is None:
        raise ValueError("Article not found")
    db.delete(article)
    db.commit()
