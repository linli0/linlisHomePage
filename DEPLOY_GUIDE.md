# ☕ CoffeeCookie'sHomePage - 公网部署指南

## 🌐 当前部署状态

| 项目 | 值 |
|------|-----|
| **公网地址** | https://social-schools-throw.loca.lt |
| **访问密码** | 61.141.167.77 (公网IP) |
| **本地地址** | http://localhost:8080 |
| **部署方式** | Express + Vue 全栈（Node.js）|
| **端口** | 8080 |

---

## 🚀 快速启动

### 1. 确保本地服务器运行

```bash
cd "D:\AI\coffeeCookie'sHomePage"
node server.js
```

或使用启动脚本：
```bash
start.bat
```

### 2. 验证本地服务

浏览器访问 http://localhost:8080，确认页面正常加载。

---

## 📡 公网部署方式

### 方式一：LocalTunnel（当前使用）

**优点：**
- 稳定可靠
- 支持 HTTPS
- 免费使用

**缺点：**
- 需要密码验证（公网IP）
- 每次启动域名会变

**部署步骤：**

```bash
# 1. 确保本地服务器在 8080 端口运行
netstat -ano | findstr :8080

# 2. 启动 LocalTunnel
npx -y localtunnel --port 8080

# 3. 记录生成的公网地址
# 例如: https://social-schools-throw.loca.lt

# 4. 获取访问密码（公网IP）
# 访问 https://loca.lt/mytunnelpassword 查看
# 或在命令行执行: curl https://loca.lt/mytunnelpassword
```

**访问方式：**
1. 浏览器打开生成的公网地址
2. 在密码框输入公网IP（如 61.141.167.77）
3. 点击提交即可访问

---

### 方式二：Tunnelmole

**优点：**
- 无需密码
- 自动生成 HTTPS

**缺点：**
- 偶尔不稳定
- 域名可能会失效

**部署步骤：**

```bash
# 1. 确保本地服务器在 8080 端口运行

# 2. 启动 Tunnelmole
npx -y tunnelmole 8080

# 3. 直接使用生成的 HTTPS 地址访问
# 例如: https://xxxxx.tunnelmole.net
```

---

## 🔑 登录信息

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user | user123 |

---

## 📋 主要 API 端点

### 认证
```
POST /api/auth/login
Body: { "username": "admin", "password": "admin123" }
```

### 金价
```
GET  /api/gold-price/current?currency=USD
GET  /api/gold-price/history?currency=USD&days=7
GET  /api/gold-price/currencies
```

### 工具箱
```
POST /api/tools/json/format
POST /api/tools/base64/encode
POST /api/tools/base64/decode
POST /api/tools/hash/md5
POST /api/tools/hash/sha256
```

---

## 📁 项目文件结构

```
D:\AI\coffeeCookie'sHomePage\
├── server.js              # Express 后端主文件
├── package.json           # 后端依赖
├── frontend/              # Vue 前端源码
│   ├── src/
│   ├── package.json
│   └── dist/              # 构建输出
├── frontend-dist/         # 部署用的前端文件
├── source-projects/       # 原始项目备份
├── PROJECT_DOCUMENTATION.md   # 详细文档
├── TASK_STATUS.md             # 任务记录
└── start.bat                  # 启动脚本
```

---

## 🛠️ 常用操作

### 修改后端代码
1. 编辑 `server.js`
2. 重启服务器：`node server.js`

### 修改前端代码
1. 编辑 `frontend/src/` 下的文件
2. 构建：`cd frontend && npm run build`
3. 复制到部署目录：`Copy-Item -Path "dist" -Destination "../frontend-dist" -Recurse -Force`
4. 重启服务器

---

## 🐛 故障排查

### 端口被占用
```bash
# 查找占用 8080 的进程
netstat -ano | findstr :8080
# 结束进程
taskkill /PID <PID> /F
```

### 前端显示空白
检查 `frontend-dist` 文件夹是否存在且包含 `index.html`。

### 无法访问公网
1. 确保本地服务器正在运行
2. 检查 tunnel 服务是否正常
3. 尝试重新启动 tunnel

### LocalTunnel 密码错误
- 确保使用当前机器的公网IP
- 访问 https://loca.lt/mytunnelpassword 获取正确IP

---

## 📝 注意事项

1. **每次使用 `kimi-cli` 修改项目，必须在 `D:\AI` 目录下执行**
2. **修改后端代码后需要重启 `server.js`**
3. **修改前端代码后需要重新构建**
4. **公网地址每次重启 tunnel 会变化，记得更新文档**
5. **LocalTunnel 需要密码验证，Tunnelmole 不需要**

---

## 🔄 部署流程总结

```
1. 启动本地服务器
   └─> node server.js

2. 启动公网隧道（选一个）
   ├─> LocalTunnel: npx -y localtunnel --port 8080
   └─> Tunnelmole: npx -y tunnelmole 8080

3. 记录新地址并更新 DEPLOY_GUIDE.md

4. 测试访问
   ├─> LocalTunnel: 输入公网IP密码
   └─> Tunnelmole: 直接访问
```

---

**最后更新：** 2025-03-10
