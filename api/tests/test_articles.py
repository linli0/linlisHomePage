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
