"""Tests for tweets module."""
import json
from datetime import datetime, timezone

from app.models import Tweet
from app.core.database import get_db


def _get_test_db(client):
    """Get the test database session from FastAPI's dependency override."""
    override = client.app.dependency_overrides.get(get_db)
    if override:
        gen = override()
        return next(gen)
    return None


def test_get_latest_tweets_empty(client):
    """GET /api/tweets/latest should return empty list when no tweets."""
    response = client.get("/api/tweets/latest")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert body["data"] == []


def test_get_latest_tweets_with_data(client):
    """GET /api/tweets/latest should return tweets."""
    db = _get_test_db(client)
    tweet = Tweet(
        tweet_id="test_001",
        platform="twitter",
        username="testuser",
        content="Test tweet content",
        display_name="Test User",
        likes_count=10,
        retweets_count=5,
        replies_count=2,
        hashtags=json.dumps(["python", "fastapi"]),
        mentions=json.dumps(["@friend"]),
        media_urls=json.dumps(["https://example.com/image.jpg"]),
        created_at=datetime.now(timezone.utc),
    )
    db.add(tweet)
    db.commit()

    response = client.get("/api/tweets/latest")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert len(body["data"]) == 1
    assert body["data"][0]["username"] == "testuser"
    assert body["data"][0]["likesCount"] == 10
    assert body["data"][0]["hashtags"] == ["python", "fastapi"]


def test_get_latest_tweets_with_filters(client):
    """GET /api/tweets/latest should filter by platform and username."""
    db = _get_test_db(client)
    tweets = [
        Tweet(
            tweet_id="test_002",
            platform="twitter",
            username="user1",
            content="Twitter tweet",
            created_at=datetime.now(timezone.utc),
        ),
        Tweet(
            tweet_id="test_003",
            platform="truth-social",
            username="user2",
            content="Truth Social tweet",
            created_at=datetime.now(timezone.utc),
        ),
    ]
    db.add_all(tweets)
    db.commit()

    # Filter by platform
    response = client.get("/api/tweets/latest?platform=twitter")
    assert response.status_code == 200
    body = response.json()
    assert len(body["data"]) == 1
    assert body["data"][0]["platform"] == "twitter"

    # Filter by username
    response = client.get("/api/tweets/latest?username=user2")
    assert response.status_code == 200
    body = response.json()
    assert len(body["data"]) == 1
    assert body["data"][0]["username"] == "user2"


def test_search_tweets(client):
    """POST /api/tweets/search should search tweets by keyword."""
    db = _get_test_db(client)
    tweet = Tweet(
        tweet_id="test_004",
        platform="twitter",
        username="testuser",
        content="FastAPI is awesome for building APIs",
        created_at=datetime.now(timezone.utc),
    )
    db.add(tweet)
    db.commit()

    response = client.post(
        "/api/tweets/search",
        json={"keyword": "FastAPI"},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert len(body["data"]) == 1
    assert "FastAPI" in body["data"][0]["content"]


def test_get_tweet_stats(client):
    """GET /api/tweets/stats should return statistics."""
    response = client.get("/api/tweets/stats")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    stats = body["data"]
    assert "totalTweets" in stats
    assert "twitterTweets" in stats
    assert "truthSocialTweets" in stats
    assert "tweetsToday" in stats
    assert "tweetsThisWeek" in stats
    assert "topAccounts" in stats
    assert "averageEngagement" in stats


def test_get_tweet_by_id(client):
    """GET /api/tweets/{id} should return tweet by ID."""
    db = _get_test_db(client)
    tweet = Tweet(
        tweet_id="test_005",
        platform="twitter",
        username="testuser",
        content="Test tweet for ID lookup",
        created_at=datetime.now(timezone.utc),
    )
    db.add(tweet)
    db.commit()
    db.refresh(tweet)

    response = client.get(f"/api/tweets/{tweet.id}")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    assert body["data"]["tweetId"] == "test_005"


def test_get_tweet_by_id_not_found(client):
    """GET /api/tweets/{id} should return 404 when tweet not found."""
    response = client.get("/api/tweets/99999")
    assert response.status_code == 404
