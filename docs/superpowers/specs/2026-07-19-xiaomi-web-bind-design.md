# 网页绑定小米账号（方案 A）

**日期:** 2026-07-19  
**状态:** 已批准，进入实现

## 目标

管理员在 `/xiaomi` 完成小米账号登录（含短信 2FA）、选择音箱，将账号会话（`passToken`）与设备 IP/TOKEN 持久化到 SQLite。浏览器清缓存不影响控制。

## 决策

- 存储：SQLite `xiaomi_binding` 单行；敏感字段 Fernet 加密
- 权限：绑定/解绑/刷新仅 admin；控制接口保持现有鉴权
- 真相来源：DB 绑定优先于 `api/.env`
- 单台音箱；不存小米明文密码

## API

- `POST /api/xiaomi/account/login` `{ username, password }`
- `POST /api/xiaomi/account/verify` `{ sessionId, code }`
- `POST /api/xiaomi/account/bind` `{ sessionId, did }`
- `GET /api/xiaomi/account/status`
- `POST /api/xiaomi/account/refresh-devices`
- `DELETE /api/xiaomi/account`

## 前端

`XiaomiView`：admin 绑定向导（账号 → 验证码 → 选设备）；已绑定展示状态与解绑。
