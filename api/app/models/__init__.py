import enum
from datetime import datetime

from sqlalchemy import Boolean, Column, DateTime, Enum, ForeignKey, Integer, String, Table, Text, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base

article_tags = Table(
    "article_tags",
    Base.metadata,
    Column("article_id", ForeignKey("articles.id"), primary_key=True),
    Column("tag_id", ForeignKey("tags.id"), primary_key=True),
)


class UserRole(str, enum.Enum):
    USER = "USER"
    ADMIN = "ADMIN"


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    username: Mapped[str] = mapped_column(String(50), unique=True, nullable=False)
    password: Mapped[str] = mapped_column(String(255), nullable=False)
    email: Mapped[str | None] = mapped_column(String(100), unique=True)
    avatar: Mapped[str | None] = mapped_column(String(500))
    display_name: Mapped[str | None] = mapped_column(String(100))
    role: Mapped[UserRole] = mapped_column(Enum(UserRole), default=UserRole.USER)
    enabled: Mapped[bool] = mapped_column(Boolean, default=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, server_default=func.now(), onupdate=func.now()
    )

    articles: Mapped[list["Article"]] = relationship(back_populates="author")


class Category(Base):
    __tablename__ = "categories"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(100), unique=True, nullable=False)
    description: Mapped[str | None] = mapped_column(String(500))
    icon: Mapped[str | None] = mapped_column(String(50))
    sort_order: Mapped[int] = mapped_column(Integer, default=0)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    articles: Mapped[list["Article"]] = relationship(back_populates="category")


class Tag(Base):
    __tablename__ = "tags"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(50), unique=True, nullable=False)
    color: Mapped[str | None] = mapped_column(String(20))
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    articles: Mapped[list["Article"]] = relationship(
        secondary=article_tags, back_populates="tags"
    )


class Article(Base):
    __tablename__ = "articles"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    title: Mapped[str] = mapped_column(String(200), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    summary: Mapped[str | None] = mapped_column(String(500))
    cover_image: Mapped[str | None] = mapped_column(String(500))
    published: Mapped[bool] = mapped_column(Boolean, default=False)
    view_count: Mapped[int] = mapped_column(Integer, default=0)
    author_id: Mapped[int | None] = mapped_column(ForeignKey("users.id"))
    category_id: Mapped[int | None] = mapped_column(ForeignKey("categories.id"))
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, server_default=func.now(), onupdate=func.now()
    )

    author: Mapped[User | None] = relationship(back_populates="articles")
    category: Mapped[Category | None] = relationship(back_populates="articles")
    tags: Mapped[list[Tag]] = relationship(secondary=article_tags, back_populates="articles")


class GoldPrice(Base):
    __tablename__ = "gold_prices"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    price_usd: Mapped[float] = mapped_column(nullable=False)
    change_amount: Mapped[float | None] = mapped_column()
    change_percent: Mapped[float | None] = mapped_column()
    currency: Mapped[str] = mapped_column(String(10), default="USD")
    recorded_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


class ExchangeRate(Base):
    __tablename__ = "exchange_rates"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    from_currency: Mapped[str] = mapped_column(String(10), nullable=False)
    to_currency: Mapped[str] = mapped_column(String(10), nullable=False)
    rate: Mapped[float] = mapped_column(nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


class SocialAccount(Base):
    __tablename__ = "social_accounts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    platform: Mapped[str] = mapped_column(String(20), nullable=False)  # twitter, truth-social
    username: Mapped[str] = mapped_column(String(100), nullable=False)
    display_name: Mapped[str | None] = mapped_column(String(100))
    enabled: Mapped[bool] = mapped_column(Boolean, default=True)
    last_polled_at: Mapped[datetime | None] = mapped_column(DateTime)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    tweets: Mapped[list["Tweet"]] = relationship(back_populates="account")


class Tweet(Base):
    __tablename__ = "tweets"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    tweet_id: Mapped[str] = mapped_column(String(100), unique=True, nullable=False)
    platform: Mapped[str] = mapped_column(String(20), nullable=False)
    username: Mapped[str] = mapped_column(String(100), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    display_name: Mapped[str | None] = mapped_column(String(100))
    likes_count: Mapped[int] = mapped_column(Integer, default=0)
    retweets_count: Mapped[int] = mapped_column(Integer, default=0)
    replies_count: Mapped[int] = mapped_column(Integer, default=0)
    hashtags: Mapped[str | None] = mapped_column(Text)  # JSON array
    mentions: Mapped[str | None] = mapped_column(Text)  # JSON array
    media_urls: Mapped[str | None] = mapped_column(Text)  # JSON array
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    account_id: Mapped[int | None] = mapped_column(ForeignKey("social_accounts.id"))

    account: Mapped[SocialAccount | None] = relationship(back_populates="tweets")


class XiaomiBinding(Base):
    """Singleton-style row for Xiaomi account + speaker binding (id=1)."""

    __tablename__ = "xiaomi_binding"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    mi_user_id: Mapped[str] = mapped_column(String(64), nullable=False)
    mi_username: Mapped[str | None] = mapped_column(String(100))
    cloud_device_id: Mapped[str] = mapped_column(String(64), nullable=False)
    pass_token_enc: Mapped[str] = mapped_column(Text, nullable=False)
    did: Mapped[str] = mapped_column(String(64), nullable=False)
    name: Mapped[str] = mapped_column(String(100), default="小爱音箱")
    model: Mapped[str] = mapped_column(String(100), default="")
    local_ip: Mapped[str] = mapped_column(String(64), nullable=False)
    device_token_enc: Mapped[str] = mapped_column(Text, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, server_default=func.now(), onupdate=func.now()
    )


class XiaomiDialogueSettings(Base):
    """Singleton settings row id=1."""

    __tablename__ = "xiaomi_dialogue_settings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    announce_enabled: Mapped[bool] = mapped_column(Boolean, default=True)
    voice_input_enabled: Mapped[bool] = mapped_column(Boolean, default=True)
    provider: Mapped[str] = mapped_column(String(20), default="deepseek")
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, server_default=func.now(), onupdate=func.now()
    )


class XiaomiChatMessage(Base):
    __tablename__ = "xiaomi_chat_messages"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    role: Mapped[str] = mapped_column(String(20), nullable=False)  # user|assistant|system
    content: Mapped[str] = mapped_column(Text, nullable=False)
    route: Mapped[str | None] = mapped_column(String(40))
    provider: Mapped[str | None] = mapped_column(String(40))
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


class XiaomiPanelKeyword(Base):
    __tablename__ = "xiaomi_panel_keywords"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    keyword: Mapped[str] = mapped_column(String(100), nullable=False, unique=True)
    action_type: Mapped[str] = mapped_column(String(40), nullable=False)
    payload: Mapped[str | None] = mapped_column(Text)  # JSON
    enabled: Mapped[bool] = mapped_column(Boolean, default=True)
    sort_order: Mapped[int] = mapped_column(Integer, default=0)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
