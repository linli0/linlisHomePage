def test_password_only_login_admin(client):
    response = client.post("/api/auth/login", json={"password": "admin123"})
    assert response.status_code == 200
    body = response.json()
    assert body["code"] == 200
    data = body["data"]
    assert data["type"] == "Bearer"
    assert data["username"] == "admin"
    assert data["role"] == "ADMIN"
    assert "token" in data


def test_password_only_login_user(client):
    response = client.post("/api/auth/login", json={"password": "user123"})
    body = response.json()
    assert body["data"]["username"] == "user"
    assert body["data"]["role"] == "USER"


def test_login_invalid_password(client):
    response = client.post("/api/auth/login", json={"password": "wrong"})
    assert response.status_code == 401
    body = response.json()
    assert body["code"] == 401
    assert body["data"] is None


def test_register_disabled(client):
    response = client.post(
        "/api/auth/register",
        json={"username": "newbie", "password": "secret", "email": "a@b.com"},
    )
    assert response.status_code == 403
    body = response.json()
    assert body["code"] == 403


def test_me_requires_auth(client):
    response = client.get("/api/auth/me")
    assert response.status_code == 401


def test_me_returns_profile(client, auth_headers):
    response = client.get("/api/auth/me", headers=auth_headers)
    assert response.status_code == 200
    body = response.json()
    assert body["data"]["username"] == "admin"


def test_update_profile(client, auth_headers):
    response = client.put(
        "/api/auth/profile",
        headers=auth_headers,
        json={"displayName": "Admin Updated"},
    )
    assert response.status_code == 200
    assert response.json()["data"]["displayName"] == "Admin Updated"


def test_change_password(client, auth_headers):
    response = client.put(
        "/api/auth/password",
        headers=auth_headers,
        json={"currentPassword": "admin123", "newPassword": "admin456"},
    )
    assert response.status_code == 200
    assert response.json()["data"] == "Password changed successfully"

    login = client.post("/api/auth/login", json={"password": "admin456"})
    assert login.json()["data"]["username"] == "admin"

    # restore password for other tests in same session if re-run
    token = login.json()["data"]["token"]
    client.put(
        "/api/auth/password",
        headers={"Authorization": f"Bearer {token}"},
        json={"currentPassword": "admin456", "newPassword": "admin123"},
    )
