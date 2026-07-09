def test_list_categories(client):
    response = client.get("/api/categories")
    data = response.json()["data"]
    assert len(data) >= 2
    assert data[0]["name"]


def test_list_tags(client):
    response = client.get("/api/tags")
    data = response.json()["data"]
    assert len(data) >= 2


def test_admin_category_crud(client, auth_headers):
    created = client.post(
        "/api/categories",
        headers=auth_headers,
        json={"name": "测试分类", "description": "d", "icon": "star", "sortOrder": 9},
    )
    category_id = created.json()["data"]["id"]

    updated = client.put(
        f"/api/categories/{category_id}",
        headers=auth_headers,
        json={"name": "更新分类", "description": "d2", "icon": "star", "sortOrder": 10},
    )
    assert updated.json()["data"]["name"] == "更新分类"

    deleted = client.delete(f"/api/categories/{category_id}", headers=auth_headers)
    assert deleted.status_code == 200
