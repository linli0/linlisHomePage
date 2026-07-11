# 架构优化方案与落地状态

## 结论

对个人主页场景，原 `feature/frontend-redesign` 架构偏冗余。已落地为 **Core + Plugins 单体**：默认只加载核心能力，扩展模块通过 feature flags 关闭。

## 已完成

| 项 | 状态 |
| --- | --- |
| 合并功能分支到优化分支 | ✅ |
| 删除 `source-projects/`、`.m2/`、缓存与 archive 文档 | ✅ |
| 收敛嵌套 `AGENTS.md` 与重复测试文件 | ✅ |
| 扩展模块 `@ConditionalOnProperty` | ✅ AI / Tweets / Quant / Xiaomi |
| WebSocket 仅在 tweets 启用时加载 | ✅ |
| 公开页免登录（首页/金价/文章/工具） | ✅ |
| 移除硬编码 Twitter bearer token | ✅ |
| 单容器优先部署说明 | ✅ |
| GitHub Actions CI | ✅ |

## 目标架构

```
Core (默认启用)
├── Auth / JWT
├── Gold Price
├── Articles / Categories / Tags
└── Tools

Plugins (默认关闭)
├── features.ai.enabled
├── features.tweets.enabled
├── features.quant.enabled
└── features.xiaomi.enabled  (依赖 AI)
```

推荐部署：Spring Boot 单进程同时提供 `/api` 与前端静态资源；MySQL 仅生产可选。

## 后续建议

1. 跑通 `mvn test` 与 `npm run test:run`，修复因条件装配导致的测试依赖。
2. NavigationBar 根据 `/api/features`（可选）隐藏未启用模块入口。
3. 工具箱逐步前端化，后端仅保留确需服务端的能力（如二维码）。
4. 量化模块完成真实实现前保持默认关闭。
5. 统一默认分支为 `main`，归档 `master` 分叉。
