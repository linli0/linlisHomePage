# ☕ CoffeeCookie'sHomePage 项目文档

> 整合金价查询、工具箱、用户系统的全栈 Web 应用

---

## 📋 项目概述

本项目整合了三个原有项目：
1. **cooffeeCookiesHomePage** - 国际金价查询网页
2. **miAi** - AI 相关功能（部分功能整合）
3. **personal-homepage** - 个人主页前后端

整合后形成一个功能完善的 **Spring Boot + Vue** 全栈应用。

**公网访问地址：** https://bufnig-ip-61-141-167-77.tunnelmole.net

---

## ✨ 功能特性

### 🪙 金价模块
- 实时国际金价查询（XAU/盎司）
- 多货币支持：USD、CNY、EUR、GBP
- 价格走势图表（7天/30天/90天）
- 统计信息：最高价、最低价、平均值、波动率
- 自动定时更新（每分钟）

### 🛠️ 工具箱
- JSON 格式化/压缩
- Base64 编码/解码
- URL 编码/解码
- MD5 / SHA1 / SHA256 / SHA512 哈希计算
- 时间戳转换
- 二维码生成

### 👤 用户系统
- JWT 身份认证
- 用户注册/登录
- 角色管理（管理员/普通用户）
- 个人信息管理

### 📝 文章系统（后台已实现）
- 文章发布、编辑
- 分类管理
- 标签系统
- 浏览统计

---

## 🏗️ 技术架构

### 后端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Node.js | 18+ | 运行时 |
| Express | 4.x | Web 框架 |
| JWT | 9.x | 身份认证 |
| bcryptjs | 2.x | 密码加密 |

### 前端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4+ | 前端框架 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Chart.js | 4.x | 图表绘制 |
| Axios | 1.x | HTTP 客户端 |
| TailwindCSS | 3.x | 样式框架 |

---

## 📁 项目结构

```
D:\AI\coffeeCookie'sHomePage\
├── 📄 server.js              # Express 后端服务
├── 📄 package.json           # 后端依赖
├── 📁 frontend/              # Vue 前端源码
│   ├── 📁 src/
│   │   ├── 📁 components/    # 组件
│   │   ├── 📁 views/         # 页面视图
│   │   ├── 📁 stores/        # Pinia 状态
│   │   ├── 📁 router/        # 路由配置
│   │   └── 📁 api/           # API 接口
│   ├── 📄 package.json       # 前端依赖
│   └── 📄 vite.config.ts     # Vite 配置
├── 📁 frontend-dist/         # 构建后的前端文件
├── 📁 source-projects/       # 原始项目备份
│   ├── 📁 cooffeeCookiesHomePage/
│   ├── 📁 miAi/
│   └── 📁 personal-homepage/
├── 📄 docker-compose.yml     # Docker 编排
├── 📄 start.bat              # Windows 启动脚本
├── 📄 start.sh               # Linux 启动脚本
└── 📄 README.md              # 项目说明
```

---

## 🚀 快速启动

### 方式一：Node.js 直接运行（当前部署方式）

```bash
cd "D:\AI\coffeeCookie'sHomePage"

# 安装依赖
npm install
cd frontend && npm install && cd ..

# 构建前端
cd frontend && npm run build && cd ..

# 启动服务
node server.js
```

服务将运行在 http://localhost:8080

### 方式二：Docker 部署（推荐生产环境）

```bash
cd "D:\AI\coffeeCookie'sHomePage"

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 方式三：开发模式

```bash
# 后端
cd "D:\AI\coffeeCookie'sHomePage"
npm run dev

# 前端（新终端）
cd "D:\AI\coffeeCookie'sHomePage\frontend"
npm run dev
```

---

## 🔑 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |
| 普通用户 | `user` | `user123` |

---

## 📡 API 接口文档

### 基础信息
- **Base URL:** `http://localhost:8080/api`
- **Content-Type:** `application/json`

### 认证相关

#### 登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应：**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@coffee.com",
  "role": "ADMIN"
}
```

### 金价相关

#### 获取当前金价
```http
GET /api/gold-price/current?currency=USD
```

**响应：**
```json
{
  "price": 2045.50,
  "changeAmount": 12.30,
  "changePercent": 0.60,
  "currency": "USD",
  "symbol": "$",
  "timestamp": "2024-03-10T01:30:00.000Z",
  "high": 2058.20,
  "low": 2032.80,
  "average": 2045.00,
  "volatility": 1.85
}
```

#### 获取历史价格
```http
GET /api/gold-price/history?currency=USD&days=7
```

**响应：**
```json
[
  { "date": "2024-03-03", "price": 2038.20 },
  { "date": "2024-03-04", "price": 2041.50 },
  ...
]
```

#### 获取支持货币列表
```http
GET /api/gold-price/currencies
```

### 工具箱相关

#### JSON 格式化
```http
POST /api/tools/json/format
Content-Type: application/json

{
  "json": "{\"name\":\"test\"}"
}
```

#### Base64 编码
```http
POST /api/tools/base64/encode
Content-Type: application/json

{
  "text": "Hello World"
}
```

#### Base64 解码
```http
POST /api/tools/base64/decode
Content-Type: application/json

{
  "text": "SGVsbG8gV29ybGQ="
}
```

#### MD5 哈希
```http
POST /api/tools/hash/md5
Content-Type: application/json

{
  "text": "Hello World"
}
```

#### SHA256 哈希
```http
POST /api/tools/hash/sha256
Content-Type: application/json

{
  "text": "Hello World"
}
```

---

## 🔒 认证方式

所有受保护的接口需要在请求头中携带 JWT Token：

```http
Authorization: Bearer <your-jwt-token>
```

---

## 🌐 公网访问配置

### 使用 Tunnelmole（当前方式）

```bash
npx -y tunnelmole 8080
```

自动生成 HTTPS 公网地址，免密码访问。

### 使用 LocalTunnel

```bash
npx -y localtunnel --port 8080
```

需要输入公网 IP 作为密码。

### 使用 Ngrok（需注册）

```bash
ngrok http 8080
```

---

## ⚙️ 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `PORT` | `8080` | 服务端口 |
| `JWT_SECRET` | `coffee-cookies-secret-key-2024` | JWT 密钥 |
| `NODE_ENV` | `development` | 运行环境 |

---

## 📝 开发计划

- [ ] 接入真实金价 API（如 GoldAPI）
- [ ] 添加更多技术指标（MA、RSI 等）
- [ ] 实现文章发布和管理后台
- [ ] 添加价格预警功能
- [ ] 用户收藏和历史记录
- [ ] 多语言支持（i18n）
- [ ] 移动端 App

---

## 🐛 常见问题

### Q: 无法访问公网地址？
A: 确保本地服务器和 tunnel 都在运行，检查防火墙设置。

### Q: 登录失败？
A: 使用默认账号 `admin/admin123` 或 `user/user123`。

### Q: 如何修改端口？
A: 设置环境变量 `PORT=xxxx` 后重启服务。

### Q: Docker 部署失败？
A: 确保已安装 Docker Desktop 且服务正常运行。

---

## 📄 许可证

MIT License - 详见 LICENSE 文件

---

## 👨‍💻 作者

CoffeeCookies Team

---

**最后更新：** 2024-03-10
