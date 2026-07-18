# 前端全量重写（方案 A）设计

**日期:** 2026-07-19  
**状态:** 已实现  
**栈:** Vue 3 + Vite + TypeScript + Tailwind + Pinia + Vue Router（不变）  
**API:** 对齐现有 FastAPI `/api/*`（不变）

---

## 1. 目标

删除并重写 `frontend/src` 全部业务代码，交付统一的暖色「咖啡台」UI，保留全部现有路由与后端契约。不换技术栈、不新建并行工程目录。

## 2. 非目标

- 不改 FastAPI 后端契约（除已有金价逻辑）
- 不换 React / Nuxt
- 本期不强制重写 `frontend/tests`（可后续对齐；构建不被测试阻塞）
- 不引入新的重度 UI 框架（无 Element Plus / Vuetify）

## 3. 删除与保留

| 操作 | 路径 |
|------|------|
| **删除并重写** | `frontend/src/**`（views、components、api、stores、router、utils、composables、types、assets、App.vue、main.ts） |
| **保留并微调** | `package.json`、`vite.config.ts`、`tailwind.config.js`、`tsconfig*.json`、`index.html`、`postcss.config.js` |
| **保留不动** | `frontend/tests/**`（可能暂时与新结构不兼容，后续再修） |
| **不碰** | `api/`、`legacy/`、`source-projects/` |

## 4. 目标目录

```
frontend/src/
  main.ts
  App.vue
  assets/main.css          # token + btn/card/input/section
  api/                     # auth, goldPrice, article, tools, ai, tweets, xiaomi, tradingApi
  components/
    AppHeader.vue
    AppFooter.vue
    PriceChart.vue         # 金价图表（Chart.js）
    AIChat.vue             # AI 流式对话
    TweetCard.vue          # 推文卡片（若推特页需要）
  layouts/AppLayout.vue    # header + main + footer
  router/index.ts
  stores/auth.ts
  stores/settings.ts
  composables/             # 仅保留确需：useTweetWebSocket 等
  types/
  utils/request.ts
  utils/format.ts
  views/
    HomeView.vue
    GoldPriceView.vue
    ArticlesView.vue
    ArticleDetailView.vue
    ToolsView.vue
    AIView.vue
    TweetsView.vue
    XiaomiView.vue
    QuantView.vue
    LoginView.vue
    ProfileView.vue
    SettingsView.vue
```

说明：注册页若后端禁用则不恢复路由；`RegisterView` 不重建。

## 5. 路由与鉴权（与现网一致）

| 路径 | 页面 | meta |
|------|------|------|
| `/` | Home | — |
| `/gold` | GoldPrice | — |
| `/articles` | Articles | — |
| `/article/:id` | ArticleDetail | — |
| `/tools` | Tools | — |
| `/ai` | AI | — |
| `/tweets` | Tweets | requiresAuth |
| `/xiaomi` | Xiaomi | requiresAuth |
| `/quant` | Quant | requiresAuth |
| `/settings` | Settings | requiresAuth |
| `/profile` | Profile | requiresAuth |
| `/login` | Login | guest |

守卫：`requiresAuth` 未登录 → `/login?redirect=`；`guest` 已登录 → `/`。

## 6. API 契约（重写时对齐）

响应体保持 `{ code, message, data }`；`request.ts` **不**自动 unwrap，视图读 `res.data.data`。

| 模块 | 关键端点 |
|------|----------|
| auth | `POST /auth/login`，`GET /auth/me` |
| gold | `GET /gold-price/current\|history\|currencies`，`POST /gold-price/refresh` |
| article | 列表/详情/CRUD（与现 `article.ts` 路径一致） |
| tools | 现有工具接口 |
| ai | 模型列表 + 流式 chat |
| tweets | 搜索/统计 + WS composable |
| xiaomi | 状态/TTS/chat/volume |
| trading | strategies/signals 等（量化页） |

金价默认 **CNY**；文案「国内金价 / 黄金9999 · 元/克」；无 60s 轮询，手动 refresh + 后端日更。

## 7. 视觉系统

- **色板:** `brand`（琥珀）、`ink`（灰墨）、奶油底 `brand-50`；暗色 `ink-950`
- **禁止:** 旧 cyber 霓虹、无效 `gold-*` / `surface-*` / `accent-500`（对象色除外如需）
- **壳层:** Header 在首页 Hero 上半透明深底+白字；滚动/内页白底墨字；Footer 浅底
- **首页:** 满幅琥珀 Hero、完整 slogan、三 CTA；下文功能/金价/Wiki 分区清晰
- **内页:** 统一页头（标题+一句说明）、`card` 容器、主色按钮；图表区干净

## 8. 实现顺序

1. 脚手架：`main.ts` / `App.vue` / `main.css` / `tailwind` token / `request` / `auth` store / `AppLayout` + Header/Footer / router  
2. 公开页：Login → Home → Gold → Articles/Detail → Tools → AI  
3. 需登录：Profile、Settings、Tweets、Xiaomi、Quant  
4. 联调：`npm run build` + 关键路径手测  
5. 更新本 spec 状态为「已实现」

## 9. 验收

- [ ] `npm run build` 通过  
- [ ] 全部上表路由可打开（鉴权页未登录会跳转登录）  
- [ ] 登录 `admin123` 可用；金价显示元/克量级；刷新按钮调用 `/refresh`  
- [ ] 首页/导航/页脚对比正常，无白字叠白底、无残留 cyber 样式  
- [ ] 无注册路由  

## 10. 风险

| 风险 | 缓解 |
|------|------|
| 旧测试大量失败 | 本期不挡发布；后续单独修 tests |
| 复杂页（Xiaomi/Quant）功能回退 | 先保证 API 接通与基础 UI，交互对齐旧行为要点 |
| 误删依赖配置 | 只删 `src`，不动 `package.json` 依赖列表（可后续精简） |

---

确认本 spec 后开始执行：清空 `frontend/src` 并按第 8 节顺序重写。
