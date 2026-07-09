# CoffeeCookie'sHomePage 从可启动到可用 - 实施计划

## 项目现状评估

项目整体完成度约 **70%**，存在以下关键问题：
- Express 后端缺少文章/分类/标签 API（前端已定义但后端未实现）
- Express 后端缺少部分工具 API（前端定义了 12 个，后端只实现了约一半）
- 用户状态初始化缺失（刷新页面后 user 为 null）
- 注册功能死代码（路由未注册 + Store 无方法）
- 个人中心保存功能未实现
- 文章分类筛选未生效
- Markdown 渲染极差且有 XSS 风险
- AI 流式请求未携带 JWT
- 多处文案错误（显示 Spring Boot 而非 Express）
- 工具函数重复定义、错误处理缺失等

---

## 阶段一：环境就绪（可启动）

### 1.1 安装后端依赖
- 在根目录执行 `npm install`，安装 Express 后端依赖
- 验证 `node server.js` 能正常启动

### 1.2 安装前端依赖
- 在 `frontend/` 目录执行 `npm install`
- 验证 `npm run build` 能正常构建

### 1.3 验证启动流程
- 启动 Express 后端，确认 8080 端口可访问
- 确认 `/api/health` 返回正常
- 确认前端页面可正常加载

---

## 阶段二：后端 API 补全（可运行）

### 2.1 补全文章相关 API
当前 `server.js` 完全没有文章相关的路由，需要新增：
- `GET /api/articles` — 获取文章列表（支持分页 page/size、分类筛选 category）
- `GET /api/articles/:id` — 获取文章详情
- `GET /api/articles/recent` — 获取最近文章
- `GET /api/articles/popular` — 获取热门文章
- `GET /api/categories` — 获取分类列表
- `GET /api/tags` — 获取标签列表
- 使用内存模拟数据（与金价数据模式一致）

### 2.2 补全工具 API
当前后端缺少以下前端已定义的接口：
- `POST /api/tools/json/minify` — JSON 压缩（✅ 已存在）
- `POST /api/tools/base64/decode` — Base64 解码（✅ 已存在）
- `POST /api/tools/url/encode` — URL 编码（✅ 已存在）
- `POST /api/tools/url/decode` — URL 解码（✅ 已存在）
- `POST /api/tools/hash/sha1` — SHA1（✅ 已存在）
- `POST /api/tools/hash/sha256` — SHA256（✅ 已存在）
- `POST /api/tools/hash/sha512` — SHA512（✅ 已存在）
- `POST /api/tools/timestamp/convert` — 时间戳转换（✅ 已存在）
- `POST /api/tools/qrcode/generate` — 二维码生成（❌ 返回 501）

> 经核实，大部分工具 API 已存在，仅需实现二维码生成或移除前端对应功能。
> 二维码生成可使用 `qrcode` npm 包实现。

### 2.3 补全用户相关 API
- `PUT /api/auth/profile` — 更新用户资料（显示名、邮箱）
- `PUT /api/auth/password` — 修改密码
- 这两个接口是 ProfileView 页面功能所必需的

---

## 阶段三：核心功能修复（可用）

### 3.1 用户状态初始化
- 在 `App.vue` 的 `onMounted` 中调用 `authStore.fetchUser()`
- 或在路由守卫中，当有 token 但 user 为 null 时自动调用 `fetchUser()`
- 确保 `isAuthenticated` 不仅检查 token 存在性，还检查 token 有效性

### 3.2 移除注册功能死代码
- 删除 `RegisterView.vue` 文件
- 从 `NavigationBar.vue` 中移除"注册"链接和按钮
- 从路由表中确认无 `/register` 路由（当前已无）

### 3.3 实现个人中心保存功能
- `ProfileView.vue`：为"保存修改"按钮绑定 `@click` 事件
- 调用新增的 `PUT /api/auth/profile` API
- 添加密码修改功能，调用 `PUT /api/auth/password` API
- 添加密码确认校验
- 添加操作成功/失败的 Toast 提示

### 3.4 修复文章分类筛选
- `ArticlesView.vue`：在 `fetchArticles()` 中将 `selectedCategory` 作为参数传递给 API
- 切换分类时重置 `currentPage` 为 0
- 后端 API 需支持 `category` 查询参数

### 3.5 升级 Markdown 渲染
- 安装 `marked` 库替代手写正则渲染器
- 安装 `DOMPurify` 防止 XSS 攻击
- 在 `ArticleDetailView.vue` 中使用 `marked` + `DOMPurify` 渲染文章内容
- 支持代码块语法高亮（可选，安装 `highlight.js`）

### 3.6 修复 AI 流式请求认证
- `ai.ts` 的 `chatStream()` 方法：从 localStorage 读取 token 并注入 `Authorization` 头
- 与 Axios 拦截器保持一致的认证逻辑

---

## 阶段四：文案与 UI 修正（好用）

### 4.1 修正技术栈描述
- `FooterBar.vue`：将 "Spring Boot 3" 改为 "Express 4"，"Java 17" 改为 "Node.js"
- `HomeView.vue`：将 "基于 Spring Boot + Vue 3" 改为 "基于 Express + Vue 3"

### 4.2 导航栏修正
- `NavigationBar.vue`：移除注册链接
- `NavigationBar.vue`：添加 AI 对话入口链接

### 4.3 工具函数提取
- 创建 `frontend/src/utils/format.ts`
- 提取 `formatDate()` 和 `formatPrice()` 为公共函数
- 更新所有引用文件：HomeView、GoldPriceView、ArticlesView、ArticleDetailView、ProfileView

### 4.4 AIChat 组件去重
- 从 `ToolsView.vue` 中移除 `<AIChat />` 嵌入
- AI 对话功能仅保留在 `AIView.vue` 中
- 或者在 ToolsView 中改为一个小的 AI 助手入口链接，跳转到 /ai

---

## 阶段五：健壮性提升（稳定）

### 5.1 统一错误处理
- `request.ts`：添加全局错误提示（非 401 的网络错误、500 等）
- `request.ts`：修复 401 循环跳转风险（判断当前是否已在登录页）
- 各视图：API 调用失败时显示用户友好的错误状态，而非仅 console.error

### 5.2 AI 对话增强
- `AIChat.vue`：传递历史消息上下文，支持多轮对话
- `ai.ts`：修复 `onDone` 双重调用问题
- `ai.ts`：修复 NDJSON 流式解析的 chunk 边界问题（使用缓冲区拼接）
- `AIChat.vue`：AI 回复添加 Markdown 渲染支持

### 5.3 认证增强
- `auth.ts` Store：检查 JWT token 过期时间（解析 exp 字段）
- `LoginView.vue`：支持 redirect 参数，登录后返回来源页面
- `LoginView.vue`：添加密码可见性切换

### 5.4 前端工具离线化（可选）
- JSON 格式化/压缩、Base64 编解码、URL 编解码、哈希计算改为前端本地实现
- 减少不必要的网络请求，提升响应速度
- 仅二维码生成保留后端调用

### 5.5 其他改进
- `ToolsView.vue`：复制到剪贴板添加成功/失败反馈
- `ToolsView.vue`：二维码生成后组件销毁时 `revokeObjectURL` 防内存泄漏
- `NavigationBar.vue`：移动端菜单添加路由高亮
- `PriceChart.vue`：Chart.js 注册移到 main.ts 统一处理

---

## 实施优先级总结

| 优先级 | 阶段 | 关键任务 | 预期效果 |
|--------|------|----------|----------|
| P0 | 阶段一 | 安装依赖、验证启动 | 项目可启动 |
| P0 | 阶段二 | 补全后端 API | 所有前端页面有数据 |
| P1 | 阶段三 | 修复核心功能 | 主要功能可用 |
| P2 | 阶段四 | 文案修正、工具提取 | 体验一致、代码整洁 |
| P3 | 阶段五 | 健壮性提升 | 稳定可靠 |

---

## 文件变更预估

### 新增文件
- `frontend/src/utils/format.ts` — 公共格式化工具函数

### 修改文件
- `server.js` — 新增文章/分类/标签/用户资料 API、实现二维码生成
- `frontend/src/App.vue` — 添加用户状态初始化
- `frontend/src/main.ts` — 可选：Chart.js 全局注册
- `frontend/src/views/HomeView.vue` — 修正文案、使用公共工具函数
- `frontend/src/views/GoldPriceView.vue` — 使用公共工具函数
- `frontend/src/views/ArticlesView.vue` — 修复分类筛选、使用公共工具函数
- `frontend/src/views/ArticleDetailView.vue` — 升级 Markdown 渲染、使用公共工具函数
- `frontend/src/views/ToolsView.vue` — 移除 AIChat、添加复制反馈
- `frontend/src/views/ProfileView.vue` — 实现保存功能、使用公共工具函数
- `frontend/src/views/LoginView.vue` — 添加 redirect 支持、密码可见性切换
- `frontend/src/views/AIView.vue` — 保持不变
- `frontend/src/components/NavigationBar.vue` — 移除注册链接、添加 AI 入口
- `frontend/src/components/FooterBar.vue` — 修正技术栈描述
- `frontend/src/components/AIChat.vue` — 多轮上下文、Markdown 渲染
- `frontend/src/components/PriceChart.vue` — 移除 Chart.js 全局注册
- `frontend/src/api/ai.ts` — 修复 JWT 注入、onDone 双重调用、流式解析
- `frontend/src/api/article.ts` — 统一 User 类型
- `frontend/src/stores/auth.ts` — token 过期检查、统一 User 类型
- `frontend/src/utils/request.ts` — 全局错误提示、401 循环修复

### 删除文件
- `frontend/src/views/RegisterView.vue` — 死代码移除

### 新增依赖
- `marked` — Markdown 渲染
- `@types/dompurify` + `dompurify` — XSS 防护
- `qrcode` — 后端二维码生成（可选）
