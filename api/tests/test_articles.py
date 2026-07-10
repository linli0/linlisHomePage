def test_list_published_articles(client):
    response = client.get("/api/articles/public/list?page=0&size=10")
    body = response.json()
    assert body["code"] == 200
    page = body["data"]
    assert page["number"] == 0
    assert page["size"] == 10
    assert len(page["content"]) >= 1


def test_get_article_increments_views(client):
    first = client.get("/api/articles/public/1").json()["data"]["viewCount"]
    second = client.get("/api/articles/public/1").json()["data"]["viewCount"]
    assert second == first + 1


def test_recent_and_popular(client):
    recent = client.get("/api/articles/public/recent").json()["data"]
    popular = client.get("/api/articles/public/popular").json()["data"]
    assert len(recent) >= 1
    assert len(popular) >= 1


def test_get_public_article_not_found(client):
    """GET /api/articles/public/9999 should return 404."""
    response = client.get("/api/articles/public/9999")
    assert response.status_code == 404
    body = response.json()
    assert "detail" in body


def test_get_public_article_not_published(client):
    """Unpublished articles should not be accessible via public API."""
    # Create an unpublished article
    create = client.post(
        "/api/articles",
        headers=auth_headers_fixture(client),
        json={
            "title": "Draft Post",
            "content": "Draft content",
            "summary": "Draft",
            "published": False,
            "category": {"id": 1},
        },
    )
    assert create.status_code == 200
    article_id = create.json()["data"]["id"]

    # Try to access it via public API
    response = client.get(f"/api/articles/public/{article_id}")
    assert response.status_code == 404


def test_get_popular_articles_with_tags_filter(client):
    """GET /api/articles/public/popular?tags=tag1 should filter by tags."""
    # First, get all articles to see what tags exist
    all_articles = client.get("/api/articles/public/list?page=0&size=100").json()["data"]
    
    # Find an article with tags
    article_with_tags = None
    for article in all_articles["content"]:
        if article.get("tags") and len(article["tags"]) > 0:
            article_with_tags = article
            break
    
    if article_with_tags:
        tag_name = article_with_tags["tags"][0]["name"]
        # Filter by this tag
        response = client.get(f"/api/articles/public/popular?tags={tag_name}&limit=10")
        assert response.status_code == 200
        body = response.json()
        assert body["code"] == 200
        popular_articles = body["data"]
        # All returned articles should have this tag
        for article in popular_articles:
            tag_names = [tag["name"] for tag in article.get("tags", [])]
            assert tag_name in tag_names


def test_get_popular_articles_with_limit(client):
    """GET /api/articles/public/popular?limit=5 should return at most 5 articles."""
    response = client.get("/api/articles/public/popular?limit=5")
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    popular_articles = body["data"]
    assert len(popular_articles) <= 5


def auth_headers_fixture(client):
    """Helper to get auth headers for testing."""
    response = client.post("/api/auth/login", json={"password": "admin123"})
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    token = body["data"]["token"]
    return {"Authorization": f"Bearer {token}"}


def test_admin_create_update_delete_article(client, auth_headers):
    create = client.post(
        "/api/articles",
        headers=auth_headers,
        json={
            "title": "New Post",
            "content": "Body",
            "summary": "Summary",
            "published": True,
            "category": {"id": 1},
            "tags": [{"id": 1}],
        },
    )
    assert create.status_code == 200
    article_id = create.json()["data"]["id"]

    update = client.put(
        f"/api/articles/{article_id}",
        headers=auth_headers,
        json={
            "title": "Updated Post",
            "content": "Updated body",
            "summary": "Updated",
            "published": True,
        },
    )
    assert update.json()["data"]["title"] == "Updated Post"

    delete = client.delete(f"/api/articles/{article_id}", headers=auth_headers)
    assert delete.status_code == 200
