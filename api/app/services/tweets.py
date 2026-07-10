import json
from datetime import datetime, timedelta, timezone
from sqlalchemy.orm import Session
from app.models import Tweet
from app.schemas.tweets import TweetDTO, TweetSearchRequest, TweetStatsDTO, AccountStats


def _parse_json_list(value: str | None) -> list[str]:
    """解析 JSON 数组字符串"""
    if not value:
        return []
    try:
        return json.loads(value)
    except (json.JSONDecodeError, TypeError):
        return []


def _tweet_to_dto(tweet: Tweet) -> TweetDTO:
    """将 Tweet 模型转换为 DTO"""
    return TweetDTO(
        id=tweet.id,
        tweet_id=tweet.tweet_id,
        platform=tweet.platform,
        username=tweet.username,
        content=tweet.content,
        display_name=tweet.display_name,
        likes_count=tweet.likes_count,
        retweets_count=tweet.retweets_count,
        replies_count=tweet.replies_count,
        hashtags=_parse_json_list(tweet.hashtags),
        mentions=_parse_json_list(tweet.mentions),
        media_urls=_parse_json_list(tweet.media_urls),
        created_at=tweet.created_at,
    )


def get_latest_tweets(
    db: Session,
    limit: int = 20,
    platform: str | None = None,
    username: str | None = None,
) -> list[TweetDTO]:
    """获取最新的推文列表"""
    query = db.query(Tweet)
    
    if platform:
        query = query.filter(Tweet.platform == platform)
    if username:
        query = query.filter(Tweet.username == username)
    
    tweets = query.order_by(Tweet.created_at.desc()).limit(limit).all()
    return [_tweet_to_dto(t) for t in tweets]


def search_tweets(
    db: Session,
    request: TweetSearchRequest,
) -> list[TweetDTO]:
    """搜索推文"""
    query = db.query(Tweet)
    
    if request.keyword:
        query = query.filter(Tweet.content.contains(request.keyword))
    if request.platform:
        query = query.filter(Tweet.platform == request.platform)
    if request.username:
        query = query.filter(Tweet.username == request.username)
    if request.start_date:
        try:
            start = datetime.fromisoformat(request.start_date)
            query = query.filter(Tweet.created_at >= start)
        except ValueError:
            pass
    if request.end_date:
        try:
            end = datetime.fromisoformat(request.end_date)
            query = query.filter(Tweet.created_at <= end)
        except ValueError:
            pass
    
    limit = request.limit or 50
    tweets = query.order_by(Tweet.created_at.desc()).limit(limit).all()
    return [_tweet_to_dto(t) for t in tweets]


def get_tweet_by_id(db: Session, tweet_id: int) -> TweetDTO | None:
    """根据 ID 获取推文"""
    tweet = db.query(Tweet).filter(Tweet.id == tweet_id).first()
    if not tweet:
        return None
    return _tweet_to_dto(tweet)


def get_stats(db: Session) -> TweetStatsDTO:
    """获取推文统计信息"""
    now = datetime.now(timezone.utc)
    today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)
    week_start = today_start - timedelta(days=today_start.weekday())
    
    total_tweets = db.query(Tweet).count()
    twitter_tweets = db.query(Tweet).filter(Tweet.platform == "twitter").count()
    truth_social_tweets = db.query(Tweet).filter(Tweet.platform == "truth-social").count()
    tweets_today = db.query(Tweet).filter(Tweet.created_at >= today_start).count()
    tweets_this_week = db.query(Tweet).filter(Tweet.created_at >= week_start).count()
    
    # 计算平均互动数
    all_tweets = db.query(Tweet).all()
    if all_tweets:
        total_engagement = sum(
            t.likes_count + t.retweets_count + t.replies_count for t in all_tweets
        )
        average_engagement = total_engagement / len(all_tweets)
    else:
        average_engagement = 0.0
    
    # 获取 top accounts（按推文数量排序）
    from sqlalchemy import func
    account_stats = (
        db.query(
            Tweet.username,
            Tweet.display_name,
            Tweet.platform,
            func.count(Tweet.id).label("tweet_count"),
            func.avg(Tweet.likes_count).label("avg_likes"),
        )
        .group_by(Tweet.username, Tweet.display_name, Tweet.platform)
        .order_by(func.count(Tweet.id).desc())
        .limit(10)
        .all()
    )
    
    top_accounts = [
        AccountStats(
            username=row.username,
            display_name=row.display_name,
            platform=row.platform,
            tweet_count=row.tweet_count,
            average_likes=float(row.avg_likes or 0),
        )
        for row in account_stats
    ]
    
    return TweetStatsDTO(
        total_tweets=total_tweets,
        twitter_tweets=twitter_tweets,
        truth_social_tweets=truth_social_tweets,
        tweets_today=tweets_today,
        tweets_this_week=tweets_this_week,
        top_accounts=top_accounts,
        average_engagement=average_engagement,
    )
