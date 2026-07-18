from app.core.database import SessionLocal, init_db
from app.plugins.xiaomi import binding, crypto


def test_crypto_roundtrip():
    token = "abc123device-token"
    enc = crypto.encrypt(token)
    assert enc != token
    assert crypto.decrypt(enc) == token


def test_upsert_and_runtime():
    init_db()
    db = SessionLocal()
    try:
        binding.delete_binding(db)
        binding.upsert_binding(
            db,
            mi_user_id="123",
            mi_username="demo",
            cloud_device_id="DEV1",
            pass_token="pass-token-value",
            did="did-1",
            name="测试音箱",
            model="xiaomi.wifispeaker.lx06",
            local_ip="192.168.1.50",
            device_token="device-token-value",
        )
        st = binding.account_status_dict(db)
        assert st["bound"] is True
        assert st["device"]["ip"] == "192.168.1.50"
        assert "pass_token" not in str(st).lower() or st["hasPassToken"] is True

        rt = binding.get_runtime(db)
        assert rt is not None
        assert rt.ip == "192.168.1.50"
        assert rt.token == "device-token-value"
        assert rt.source == "db"

        creds = binding.load_pass_token(db)
        assert creds is not None
        assert creds[2] == "pass-token-value"
    finally:
        binding.delete_binding(db)
        db.close()
