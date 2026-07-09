from sqlalchemy.orm import Session

from app.core.security import hash_password
from app.models import Article, Category, Tag, User, UserRole


def seed_database(db: Session) -> None:
    if db.query(User).count() > 0:
        return

    admin = User(
        username="admin",
        password=hash_password("admin123"),
        email="admin@coffeecookies.com",
        display_name="Administrator",
        role=UserRole.ADMIN,
    )
    user = User(
        username="user",
        password=hash_password("user123"),
        email="user@coffeecookies.com",
        display_name="Regular User",
        role=UserRole.USER,
    )
    db.add_all([admin, user])

    tech = Category(name="技术", description="技术文章", icon="code", sort_order=1)
    life = Category(name="生活", description="生活随笔", icon="heart", sort_order=2)
    db.add_all([tech, life])
    db.flush()

    vue_tag = Tag(name="Vue", color="#42b883")
    python_tag = Tag(name="Python", color="#3776ab")
    db.add_all([vue_tag, python_tag])
    db.flush()

    article = Article(
        title="欢迎使用 CoffeeCookies Homepage",
        content="## 轻量化架构\n\n本项目已迁移至 **Vue 3 + FastAPI** 技术栈。",
        summary="项目架构迁移说明",
        published=True,
        view_count=0,
        author_id=admin.id,
        category_id=tech.id,
    )
    article.tags = [vue_tag, python_tag]
    db.add(article)
    db.commit()
