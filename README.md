# CoffeeCookies Homepage

一个基于 Spring Boot + Vue 3 的全栈个人主页应用，集成了国际金价追踪、技术文章管理和实用工具箱等功能。

## 📚 完整文档

完整的项目文档现在位于 [`doc/`](doc/) 目录中，包含：

- **[架构概述](doc/architecture/overview.md)** - 系统架构和设计决策
- **[后端文档](doc/backend/)** - Spring Boot 后端实现细节
- **[前端文档](doc/frontend/)** - Vue 3 前端实现细节  
- **[设计系统](doc/design/)** - UI/UX 设计指南
- **[测试框架](doc/testing/)** - 测试策略和结果
- **[部署指南](doc/deployment/)** - 部署配置和生产设置

## 🚀 功能特性

### 💰 金价追踪模块
- 实时国际金价查询
- 支持多货币显示（USD、CNY、EUR、GBP）
- 价格走势图表（7天/30天/90天）
- 价格统计（最高/最低/平均/波动率）
- 自动刷新（每分钟）

### 📝 文章管理
- 文章发布与分类管理
- Markdown 内容支持
- 标签系统
- 文章浏览统计
- 文章搜索

### 🛠️ 实用工具箱
- JSON 格式化/压缩
- Base64 编码/解码
- URL 编码/解码
- 哈希计算（MD5、SHA1、SHA256、SHA512）
- 时间戳转换
- 二维码生成

### 🔐 用户系统
- JWT 认证
- 用户注册/登录
- 个人中心
- 角色管理（用户/管理员）

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.2
- **语言**: Java 17
- **数据库**: H2 (开发) / MySQL (生产)
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **API 风格**: RESTful

### 前端
- **框架**: Vue 3 + Composition API
- **语言**: TypeScript
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI 框架**: Tailwind CSS
- **图表**: Chart.js

### 部署
- **容器化**: Docker + Docker Compose
- **Web 服务器**: Nginx

## 📁 项目结构

```
coffeeCookie'sHomePage/
├── backend/                # Spring Boot 后端
│   ├── src/main/java/      # Java 源代码
│   │   └── com/coffeecookies/homepage/
│   │       ├── config/     # 配置类
│   │       ├── controller/ # API 控制器
│   │       ├── dto/        # 数据传输对象
│   │       ├── entity/     # 实体类
│   │       ├── repository/ # 数据访问层
│   │       ├── security/   # 安全相关
│   │       └── service/    # 业务逻辑层
│   ├── src/main/resources/ # 配置文件
│   ├── Dockerfile          # 后端 Docker 镜像
│   └── pom.xml            # Maven 配置
│
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── components/    # 组件
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # Pinia 状态管理
│   │   ├── utils/         # 工具函数
│   │   ├── views/         # 页面视图
│   │   ├── App.vue        # 根组件
│   │   └── main.ts        # 入口文件
│   ├── Dockerfile         # 前端 Docker 镜像
│   └── package.json       # 依赖配置
│
├── docker-compose.yml     # Docker Compose 配置
└── README.md             # 项目说明
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 20+
- Maven 3.8+
- Docker & Docker Compose (可选)

### 本地开发

#### 1. 克隆项目
```bash
git clone <repository-url>
cd coffeeCookie'sHomePage
```

#### 2. 启动后端
```bash
cd backend
./mvnw spring-boot:run
```
后端服务将在 http://localhost:8080 启动

#### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```
前端服务将在 http://localhost:3000 启动

### Docker 部署

#### 1. 构建并启动所有服务
```bash
docker-compose up -d
```

#### 2. 查看服务状态
```bash
docker-compose ps
```

#### 3. 停止服务
```bash
docker-compose down
```

部署后访问：
- 前端: http://localhost
- 后端 API: http://localhost:8080
- MySQL: localhost:3306

## 🔑 默认账号

- 管理员: `admin` / `admin123`
- 普通用户: `user` / `user123`

## 📚 API 文档

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息

### 金价相关
- `GET /api/gold-price/current?currency=USD` - 获取当前金价
- `GET /api/gold-price/history?currency=USD&days=30` - 获取历史价格
- `GET /api/gold-price/currencies` - 获取支持的货币列表

### 文章相关
- `GET /api/articles/public/list` - 获取文章列表
- `GET /api/articles/public/{id}` - 获取文章详情
- `GET /api/articles/public/recent` - 获取最新文章
- `GET /api/articles/public/popular` - 获取热门文章

### 工具相关
- `POST /api/tools/json/format` - JSON 格式化
- `POST /api/tools/base64/encode` - Base64 编码
- `POST /api/tools/hash/md5` - MD5 哈希
- `POST /api/tools/qrcode/generate` - 生成二维码

## 🔧 配置说明

### 后端配置
编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/homepage
    username: your_username
    password: your_password

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours
```

### 前端配置
编辑 `frontend/vite.config.ts`：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
      changeOrigin: true,
    },
  },
}
```

## 📝 开发计划

- [x] 基础项目架构
- [x] 用户认证系统
- [x] 金价追踪模块
- [x] 文章管理系统
- [x] 实用工具箱
- [x] Docker 部署
- [ ] 文件上传功能
- [ ] 评论系统
- [ ] 站内搜索
- [ ] 主题切换

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Chart.js](https://www.chartjs.org/)

---

Made with ☕ by CoffeeCookies
