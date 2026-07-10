from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.schemas.common import Result
from app.schemas.tweets import TweetDTO, TweetSearchRequest, TweetStatsDTO
from app.services import tweets as tweet_service

router = APIRouter(prefix="/tweets", tags=["tweets"])


@router.get("/latest")
def get_latest(
    limit: int = 20,
    platform: str | None = None,
    username: str | None = None,
    db: Session = Depends(get_db),
) -> Result[list[TweetDTO]]:
    """获取最新的推文列表"""
    tweets = tweet_service.get_latest_tweets(db, limit, platform, username)
    return Result.success(tweets)


@router.post("/search")
def search(
    request: TweetSearchRequest,
    db: Session = Depends(get_db),
) -> Result[list[TweetDTO]]:
    """搜索推文"""
    tweets = tweet_service.search_tweets(db, request)
    return Result.success(tweets)


@router.get("/stats")
def get_stats(db: Session = Depends(get_db)) -> Result[TweetStatsDTO]:
    """获取推文统计信息"""
    stats = tweet_service.get_stats(db)
    return Result.success(stats)


@router.get("/{tweet_id}")
def get_by_id(tweet_id: int, db: Session = Depends(get_db)) -> Result[TweetDTO]:
    """根据 ID 获取推文"""
    tweet = tweet_service.get_tweet_by_id(db, tweet_id)
    if not tweet:
        raise HTTPException(status_code=404, detail="Tweet not found")
    return Result.success(tweet)
