from datetime import datetime
from pydantic import BaseModel, Field


class TweetDTO(BaseModel):
    id: int
    tweet_id: str = Field(..., alias="tweetId")
    platform: str
    username: str
    content: str
    display_name: str | None = Field(None, alias="displayName")
    likes_count: int = Field(0, alias="likesCount")
    retweets_count: int = Field(0, alias="retweetsCount")
    replies_count: int = Field(0, alias="repliesCount")
    hashtags: list[str] = []
    mentions: list[str] = []
    media_urls: list[str] = Field([], alias="mediaUrls")
    created_at: datetime = Field(..., alias="createdAt")

    model_config = {"from_attributes": True, "populate_by_name": True}


class TweetSearchRequest(BaseModel):
    keyword: str | None = None
    platform: str | None = None
    username: str | None = None
    start_date: str | None = Field(None, alias="startDate")
    end_date: str | None = Field(None, alias="endDate")
    limit: int | None = None

    model_config = {"populate_by_name": True}


class AccountStats(BaseModel):
    username: str
    display_name: str | None = Field(None, alias="displayName")
    platform: str
    tweet_count: int = Field(0, alias="tweetCount")
    average_likes: float = Field(0.0, alias="averageLikes")

    model_config = {"from_attributes": True, "populate_by_name": True}


class TweetStatsDTO(BaseModel):
    total_tweets: int = Field(0, alias="totalTweets")
    twitter_tweets: int = Field(0, alias="twitterTweets")
    truth_social_tweets: int = Field(0, alias="truthSocialTweets")
    tweets_today: int = Field(0, alias="tweetsToday")
    tweets_this_week: int = Field(0, alias="tweetsThisWeek")
    top_accounts: list[AccountStats] = Field([], alias="topAccounts")
    average_engagement: float = Field(0.0, alias="averageEngagement")

    model_config = {"from_attributes": True, "populate_by_name": True}
