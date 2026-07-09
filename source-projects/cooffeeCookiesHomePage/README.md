# 🪙 Gold Price Tracker | 国际金价追踪器

一个简洁、现代化的网页应用，用于显示实时国际金价，支持多货币切换和价格走势图。

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)

## ✨ 功能特性

- 📊 **实时金价显示** - 显示当前国际金价（XAU/盎司）
- 💱 **多货币支持** - 支持 USD、EUR、CNY、GBP 四种货币
- 📈 **价格走势图** - 7天、30天、90天的金价走势图表
- 📉 **价格统计** - 显示最高价、最低价、平均值和波动率
- 🎨 **现代化 UI** - 暗色主题设计，简洁美观
- 📱 **响应式布局** - 支持桌面端和移动端
- ⚡ **自动刷新** - 每分钟自动更新数据

## 🚀 快速开始

### 环境要求

- 现代浏览器（Chrome、Firefox、Safari、Edge）
- Node.js 14+ (可选，用于本地开发服务器)

### 安装步骤

1. **克隆仓库**
```bash
git clone <repository-url>
cd cooffeecookieshomepage
```

2. **安装依赖**
```bash
npm install
```

3. **启动开发服务器**
```bash
npm run dev
```

或直接打开 `src/index.html` 文件在浏览器中预览。

### 构建部署

本项目是纯静态网站，无需构建步骤。直接将 `src` 目录部署到任意静态托管服务即可：

- GitHub Pages
- Vercel
- Netlify
- Cloudflare Pages

## 📁 项目结构

```
cooffeecookieshomepage/
├── README.md              # 项目说明文档
├── package.json           # 项目配置和依赖
└── src/                   # 源代码目录
    ├── index.html         # 主页面
    ├── css/               # 样式文件
    │   └── style.css      # 主样式表
    ├── js/                # JavaScript 文件
    │   └── app.js         # 应用主逻辑
    └── assets/            # 静态资源
```

## 🔌 API 说明

本项目使用以下免费 API：

- **Exchange Rate API** - 获取实时汇率数据
  - Endpoint: `https://api.exchangerate-api.com/v4/latest/USD`
  - 免费版限制: 无限制，但请勿滥用

- **金价数据** - 当前使用模拟数据（基于真实市场价格的随机游走算法）
  - 生产环境建议接入专业的金价 API，如：
    - [GoldAPI](https://www.goldapi.io/) (免费额度可用)
    - [Metals-API](https://metals-api.com/)
    - [OpenExchangeRates](https://openexchangerates.org/)

## 🛠️ 技术栈

- **HTML5** - 语义化标记
- **CSS3** - Flexbox/Grid 布局、CSS 变量、动画
- **JavaScript (ES6+)** - 模块化、异步编程
- **Chart.js** - 价格走势图
- **Font Awesome** - 图标库
- **Google Fonts** - Inter 字体

## 🎨 自定义配置

### 修改默认货币

编辑 `src/js/app.js` 中的 `state.currentCurrency`：

```javascript
const state = {
    currentCurrency: 'CNY',  // 修改为默认货币
    // ...
};
```

### 修改金价基准

编辑 `src/js/app.js` 中的 `BASE_GOLD_PRICE_USD`：

```javascript
const CONFIG = {
    BASE_GOLD_PRICE_USD: 2030,  // 修改基准金价
    // ...
};
```

### 添加新货币

1. 在 `CONFIG.CURRENCIES` 中添加新货币配置：

```javascript
CURRENCIES: {
    // ... 现有货币
    JPY: { symbol: '¥', name: '日元', flag: '🇯🇵', locale: 'ja-JP' }
}
```

2. 在 HTML 中添加对应的货币按钮

## 📝 开发计划

- [ ] 接入真实的金价 API
- [ ] 添加更多技术指标（MA、RSI 等）
- [ ] 支持加密货币价格显示
- [ ] 添加价格预警功能
- [ ] 支持历史数据导出
- [ ] 多语言支持

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 [MIT](LICENSE) 许可证开源。

## ⚠️ 免责声明

本项目提供的金价数据仅供参考，不构成投资建议。实际交易请以官方金融机构报价为准。

---

<p align="center">Made with ☕ and 🍪 by CoffeeCookies</p>
