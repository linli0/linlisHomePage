const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

const app = express();
const PORT = process.env.PORT || 8080;
const JWT_SECRET = process.env.JWT_SECRET || 'coffee-cookies-secret-key-2024';

// Ollama 配置
const OLLAMA_BASE_URL = process.env.OLLAMA_URL || 'http://localhost:11434';

// 读取 project.properties
function loadProjectProperties() {
  const configPath = path.join(__dirname, 'project.properties');
  const defaults = { 'site.password': 'admin' };
  try {
    const content = fs.readFileSync(configPath, 'utf8');
    const props = { ...defaults };
    content.split('\n').forEach(line => {
      const trimmed = line.trim();
      if (trimmed && !trimmed.startsWith('#')) {
        const idx = trimmed.indexOf('=');
        if (idx > 0) {
          props[trimmed.substring(0, idx).trim()] = trimmed.substring(idx + 1).trim();
        }
      }
    });
    return props;
  } catch (err) {
    console.warn('Failed to load project.properties, using defaults:', err.message);
    return defaults;
  }
}

const projectProps = loadProjectProperties();
const SITE_PASSWORD = projectProps['site.password'] || 'admin';

// Middleware
app.use(cors());
app.use(express.json());

// 统一响应格式
function success(data) {
  return { code: 200, message: 'success', data };
}

function error(message, code = 400) {
  return { code, message, data: null };
}

// 模拟数据库
const users = [
  { id: 1, username: 'admin', password: '$2a$10$YourHashedPasswordHere', email: 'admin@coffee.com', displayName: 'CoffeeCookie', role: 'ADMIN', createdAt: '2024-01-01T00:00:00.000Z' }
];

// 分类数据（模拟）
const categories = [
  { id: 1, name: '前端开发', description: 'HTML, CSS, JavaScript, Vue, React 等前端技术', icon: '🎨', sortOrder: 1 },
  { id: 2, name: '后端开发', description: 'Node.js, Java, Python, 数据库等后端技术', icon: '⚙️', sortOrder: 2 },
  { id: 3, name: 'AI 与机器学习', description: '人工智能、深度学习、LLM 相关技术', icon: '🤖', sortOrder: 3 },
  { id: 4, name: 'DevOps', description: 'Docker, K8s, CI/CD, 云原生等运维技术', icon: '🚀', sortOrder: 4 },
  { id: 5, name: '生活随笔', description: '技术之外的生活感悟与分享', icon: '☕', sortOrder: 5 }
];

// 标签数据（模拟）
const tags = [
  { id: 1, name: 'Vue', color: '#42b883' },
  { id: 2, name: 'TypeScript', color: '#3178c6' },
  { id: 3, name: 'Node.js', color: '#339933' },
  { id: 4, name: 'Express', color: '#000000' },
  { id: 5, name: 'Python', color: '#3776ab' },
  { id: 6, name: 'Docker', color: '#2496ed' },
  { id: 7, name: 'Ollama', color: '#6366f1' },
  { id: 8, name: 'Tailwind CSS', color: '#06b6d4' }
];

// 文章数据（模拟）
const articles = [
  {
    id: 1, title: 'Vue 3 Composition API 实战指南', summary: '深入理解 Vue 3 Composition API 的核心概念，掌握 setup 函数、响应式引用和组合式函数的最佳实践。',
    content: '# Vue 3 Composition API 实战指南\n\n## 简介\n\nVue 3 的 Composition API 是一种全新的组织组件逻辑的方式。相比 Options API，它提供了更好的代码组织和逻辑复用能力。\n\n## 核心概念\n\n### setup 函数\n\n`setup` 是 Composition API 的入口点，在组件创建之前执行：\n\n```javascript\nexport default {\n  setup() {\n    const count = ref(0)\n    const increment = () => count.value++\n    return { count, increment }\n  }\n}\n```\n\n### 响应式引用\n\n- `ref()` - 用于基本类型的响应式数据\n- `reactive()` - 用于对象类型的响应式数据\n\n### 计算属性\n\n```javascript\nconst doubleCount = computed(() => count.value * 2)\n```\n\n## 最佳实践\n\n1. 使用 `<script setup>` 语法糖简化代码\n2. 将相关逻辑组织到组合式函数中\n3. 优先使用 `ref` 而非 `reactive`\n4. 注意响应式丢失问题\n\n## 总结\n\nComposition API 让我们能够更灵活地组织组件逻辑，是 Vue 3 开发的推荐方式。',
    coverImage: '', published: true, viewCount: 256, createdAt: '2024-12-15T10:30:00.000Z', updatedAt: '2024-12-15T10:30:00.000Z',
    categoryId: 1, tagIds: [1, 2]
  },
  {
    id: 2, title: 'Express.js 中间件深入解析', summary: '从原理到实践，全面掌握 Express 中间件机制，包括错误处理、自定义中间件和性能优化。',
    content: '# Express.js 中间件深入解析\n\n## 什么是中间件\n\n中间件是 Express 的核心概念，它是一个函数，可以访问请求对象、响应对象和下一个中间件函数。\n\n## 中间件类型\n\n1. **应用级中间件** - `app.use()`\n2. **路由级中间件** - `router.use()`\n3. **错误处理中间件** - 四个参数的函数\n4. **内置中间件** - `express.json()` 等\n\n## 自定义中间件示例\n\n```javascript\nfunction logger(req, res, next) {\n  console.log(`${req.method} ${req.url} - ${new Date().toISOString()}`);\n  next();\n}\n```\n\n## 错误处理\n\n```javascript\napp.use((err, req, res, next) => {\n  console.error(err.stack);\n  res.status(500).json({ error: err.message });\n});\n```\n\n## 总结\n\n掌握中间件机制是精通 Express 的关键。',
    coverImage: '', published: true, viewCount: 189, createdAt: '2024-12-10T14:20:00.000Z', updatedAt: '2024-12-10T14:20:00.000Z',
    categoryId: 2, tagIds: [3, 4]
  },
  {
    id: 3, title: '使用 Ollama 构建本地 AI 助手', summary: '手把手教你使用 Ollama 在本地部署大语言模型，并构建一个实用的 AI 对话应用。',
    content: '# 使用 Ollama 构建本地 AI 助手\n\n## Ollama 简介\n\nOllama 是一个轻量级的本地 LLM 运行框架，让你可以轻松在本地运行各种开源大模型。\n\n## 安装与配置\n\n```bash\ncurl -fsSL https://ollama.com/install.sh | sh\nollama pull llama3.2\nollama serve\n```\n\n## API 集成\n\nOllama 提供了简洁的 REST API：\n\n```javascript\nconst response = await fetch("http://localhost:11434/api/generate", {\n  method: "POST",\n  body: JSON.stringify({ model: "llama3.2", prompt: "Hello!" })\n});\n```\n\n## 流式响应\n\nOllama 支持 NDJSON 格式的流式响应，适合实时对话场景。\n\n## 总结\n\nOllama 让本地 AI 部署变得简单，非常适合个人项目和学习。',
    coverImage: '', published: true, viewCount: 342, createdAt: '2024-12-08T09:15:00.000Z', updatedAt: '2024-12-08T09:15:00.000Z',
    categoryId: 3, tagIds: [7]
  },
  {
    id: 4, title: 'Docker 容器化部署最佳实践', summary: '从 Dockerfile 编写到多容器编排，全面介绍 Docker 在项目部署中的应用和优化技巧。',
    content: '# Docker 容器化部署最佳实践\n\n## Dockerfile 优化\n\n### 多阶段构建\n\n```dockerfile\nFROM node:18 AS builder\nWORKDIR /app\nCOPY package*.json ./\nRUN npm ci\nCOPY . .\nRUN npm run build\n\nFROM nginx:alpine\nCOPY --from=builder /app/dist /usr/share/nginx/html\n```\n\n## Docker Compose\n\n```yaml\nversion: "3.8"\nservices:\n  web:\n    build: .\n    ports:\n      - "8080:80"\n  db:\n    image: mysql:8\n```\n\n## 安全注意事项\n\n1. 不要在镜像中包含敏感信息\n2. 使用 `.dockerignore` 排除不必要文件\n3. 以非 root 用户运行应用\n\n## 总结\n\nDocker 让部署变得标准化和可重复，是现代开发的重要工具。',
    coverImage: '', published: true, viewCount: 178, createdAt: '2024-12-05T16:45:00.000Z', updatedAt: '2024-12-05T16:45:00.000Z',
    categoryId: 4, tagIds: [6]
  },
  {
    id: 5, title: 'Tailwind CSS 实用技巧集锦', summary: '收集整理 Tailwind CSS 开发中的实用技巧，包括自定义配置、响应式设计和暗色模式适配。',
    content: '# Tailwind CSS 实用技巧集锦\n\n## 自定义配置\n\n在 `tailwind.config.js` 中扩展主题：\n\n```javascript\nmodule.exports = {\n  theme: {\n    extend: {\n      colors: {\n        primary: { 500: "#3b82f6" },\n        gold: { 500: "#f59e0b" }\n      }\n    }\n  }\n}\n```\n\n## 暗色模式\n\n使用 `darkMode: "class"` 配置，通过添加 `dark:` 前缀适配暗色模式：\n\n```html\n<div class="bg-white dark:bg-gray-900">\n  <p class="text-gray-900 dark:text-white">Hello</p>\n</div>\n```\n\n## 响应式设计\n\nTailwind 采用移动优先策略：\n- `sm:` - 640px\n- `md:` - 768px\n- `lg:` - 1024px\n- `xl:` - 1280px\n\n## 总结\n\nTailwind CSS 让样式开发更高效，值得深入学习。',
    coverImage: '', published: true, viewCount: 145, createdAt: '2024-12-01T11:00:00.000Z', updatedAt: '2024-12-01T11:00:00.000Z',
    categoryId: 1, tagIds: [8, 2]
  },
  {
    id: 6, title: 'Python 数据分析入门', summary: '使用 Pandas 和 Matplotlib 进行数据分析和可视化的基础教程。',
    content: '# Python 数据分析入门\n\n## 环境搭建\n\n```bash\npip install pandas matplotlib jupyter\n```\n\n## Pandas 基础\n\n```python\nimport pandas as pd\ndf = pd.read_csv("data.csv")\nprint(df.describe())\n```\n\n## 数据可视化\n\n```python\nimport matplotlib.pyplot as plt\ndf["column"].hist()\nplt.show()\n```\n\n## 总结\n\nPython 是数据分析的强大工具，Pandas + Matplotlib 是入门的最佳组合。',
    coverImage: '', published: true, viewCount: 98, createdAt: '2024-11-28T08:30:00.000Z', updatedAt: '2024-11-28T08:30:00.000Z',
    categoryId: 2, tagIds: [5]
  },
  {
    id: 7, title: '咖啡与代码：程序员的日常', summary: '分享作为一名程序员，如何在咖啡与代码之间找到平衡与乐趣。',
    content: '# 咖啡与代码：程序员的日常\n\n## 为什么程序员爱咖啡\n\n咖啡因能提高注意力和专注力，这对需要长时间集中精神的编程工作来说至关重要。\n\n## 我的咖啡装备\n\n1. 手冲壶\n2. 磨豆机\n3. 滤杯\n4. 温度计\n\n## 代码与咖啡的哲学\n\n写代码就像冲咖啡，需要耐心、精确和对细节的关注。一杯好咖啡和一段好代码，都需要反复打磨。\n\n## 推荐搭配\n\n- 早晨：美式咖啡 + 算法题\n- 下午：拿铁 + Bug 修复\n- 深夜：浓缩咖啡 + 新功能开发\n\n## 总结\n\n生活不止代码，还有咖啡和远方。',
    coverImage: '', published: true, viewCount: 312, createdAt: '2024-11-25T20:00:00.000Z', updatedAt: '2024-11-25T20:00:00.000Z',
    categoryId: 5, tagIds: []
  }
];

// 金价数据（模拟）
let goldPrice = {
  USD: { price: 2045.50, change: 12.30, changePercent: 0.60, symbol: '$', name: '美元', flag: '🇺🇸' },
  CNY: { price: 14727.60, change: 88.56, changePercent: 0.60, symbol: '¥', name: '人民币', flag: '🇨🇳' },
  EUR: { price: 1881.86, change: 11.32, changePercent: 0.60, symbol: '€', name: '欧元', flag: '🇪🇺' },
  GBP: { price: 1615.94, change: 9.72, changePercent: 0.60, symbol: '£', name: '英镑', flag: '🇬🇧' }
};

// 生成模拟历史数据
function generateHistory(days, basePrice) {
  const history = [];
  let currentPrice = basePrice;
  for (let i = days; i >= 0; i--) {
    const date = new Date();
    date.setDate(date.getDate() - i);
    const variation = (Math.random() - 0.45) * 20;
    currentPrice = basePrice + variation * (days - i) / days;
    history.push({
      date: date.toISOString().split('T')[0],
      price: parseFloat(currentPrice.toFixed(2))
    });
  }
  return history;
}

// 统计信息
function getStats(currency) {
  const prices = generateHistory(30, goldPrice[currency].price).map(h => h.price);
  return {
    high: Math.max(...prices),
    low: Math.min(...prices),
    average: prices.reduce((a, b) => a + b, 0) / prices.length,
    volatility: 1.85
  };
}

// JWT 验证中间件
const authMiddleware = (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1];
  if (!token) return res.status(401).json(error('Unauthorized', 401));

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    res.status(401).json(error('Invalid token', 401));
  }
};

// ========== API 路由 ==========

// 健康检查
app.get('/api/health', (req, res) => {
  res.json(success({ status: 'OK', timestamp: new Date().toISOString() }));
});

// 用户登录（仅密码）
app.post('/api/auth/login', async (req, res) => {
  const { password } = req.body;
  const user = users[0];

  if (password === SITE_PASSWORD) {
    const token = jwt.sign(
      { id: user.id, username: user.username, role: user.role },
      JWT_SECRET,
      { expiresIn: '24h' }
    );
    return res.json(success({
      token,
      type: 'Bearer',
      id: user.id,
      username: user.username,
      email: user.email,
      displayName: user.displayName || user.username,
      avatar: user.avatar || '',
      role: user.role,
      createdAt: user.createdAt,
      expiresIn: 86400000
    }));
  }

  res.status(401).json(error('密码错误', 401));
});

// 禁止注册
app.post('/api/auth/register', (req, res) => {
  res.status(403).json(error('本站为私人网站，不开放注册', 403));
});

// 获取当前用户信息
app.get('/api/auth/me', authMiddleware, (req, res) => {
  const user = users.find(u => u.username === req.user.username);
  if (!user) return res.status(404).json(error('User not found', 404));
  res.json(success({
    id: user.id,
    username: user.username,
    email: user.email,
    displayName: user.displayName || user.username,
    avatar: user.avatar || '',
    role: user.role,
    createdAt: user.createdAt
  }));
});

// 获取当前金价
app.get('/api/gold-price/current', (req, res) => {
  const { currency = 'USD' } = req.query;
  const data = goldPrice[currency];
  const stats = getStats(currency);

  if (!data) {
    return res.status(400).json(error('Unsupported currency'));
  }

  res.json(success({
    price: data.price,
    changeAmount: data.change,
    changePercent: data.changePercent,
    currency,
    symbol: data.symbol,
    timestamp: new Date().toISOString(),
    high: stats.high,
    low: stats.low,
    average: stats.average,
    volatility: stats.volatility
  }));
});

// 获取历史价格
app.get('/api/gold-price/history', (req, res) => {
  const { currency = 'USD', days = 7 } = req.query;
  const data = goldPrice[currency];

  if (!data) {
    return res.status(400).json(error('Unsupported currency'));
  }

  const history = generateHistory(parseInt(days), data.price);
  res.json(success(history));
});

// 获取支持的货币
app.get('/api/gold-price/currencies', (req, res) => {
  const currencies = Object.entries(goldPrice).map(([code, data]) => ({
    code,
    name: data.name,
    symbol: data.symbol,
    flag: data.flag,
    rate: code === 'USD' ? 1 : data.price / goldPrice.USD.price
  }));
  res.json(success(currencies));
});

// 工具 API - JSON 格式化
app.post('/api/tools/json/format', (req, res) => {
  try {
    const { json } = req.body;
    const parsed = JSON.parse(json);
    res.json(success(JSON.stringify(parsed, null, 2)));
  } catch (err) {
    res.status(400).json(error('Invalid JSON'));
  }
});

// 工具 API - JSON 压缩
app.post('/api/tools/json/minify', (req, res) => {
  try {
    const { json } = req.body;
    const parsed = JSON.parse(json);
    res.json(success(JSON.stringify(parsed)));
  } catch (err) {
    res.status(400).json(error('Invalid JSON'));
  }
});

// 工具 API - Base64 编码
app.post('/api/tools/base64/encode', (req, res) => {
  const { text } = req.body;
  res.json(success(Buffer.from(text).toString('base64')));
});

// 工具 API - Base64 解码
app.post('/api/tools/base64/decode', (req, res) => {
  try {
    const { encoded } = req.body;
    res.json(success(Buffer.from(encoded, 'base64').toString('utf8')));
  } catch (err) {
    res.status(400).json(error('Invalid Base64'));
  }
});

// 工具 API - URL 编码
app.post('/api/tools/url/encode', (req, res) => {
  const { text } = req.body;
  res.json(success(encodeURIComponent(text)));
});

// 工具 API - URL 解码
app.post('/api/tools/url/decode', (req, res) => {
  try {
    const { encoded } = req.body;
    res.json(success(decodeURIComponent(encoded)));
  } catch (err) {
    res.status(400).json(error('Invalid URL encoding'));
  }
});

// 工具 API - MD5
app.post('/api/tools/hash/md5', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
  if (!text) return res.status(400).json(error('Text is required'));
  const hash = crypto.createHash('md5').update(text).digest('hex');
  res.json(success(hash));
});

app.post('/api/tools/hash/sha1', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
  if (!text) return res.status(400).json(error('Text is required'));
  const hash = crypto.createHash('sha1').update(text).digest('hex');
  res.json(success(hash));
});

app.post('/api/tools/hash/sha256', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
  if (!text) return res.status(400).json(error('Text is required'));
  const hash = crypto.createHash('sha256').update(text).digest('hex');
  res.json(success(hash));
});

app.post('/api/tools/hash/sha512', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
  if (!text) return res.status(400).json(error('Text is required'));
  const hash = crypto.createHash('sha512').update(text).digest('hex');
  res.json(success(hash));
});

// 工具 API - 时间戳转换
app.post('/api/tools/timestamp/convert', (req, res) => {
  try {
    const { input, fromFormat = 'timestamp_ms' } = req.body;
    let date;
    if (fromFormat === 'timestamp_ms') {
      date = new Date(parseInt(input));
    } else if (fromFormat === 'timestamp_s') {
      date = new Date(parseInt(input) * 1000);
    } else if (fromFormat === 'iso') {
      date = new Date(input);
    } else {
      date = new Date(input);
    }
    if (isNaN(date.getTime())) throw new Error('Invalid date');
    res.json(success({
      timestampMs: date.getTime(),
      timestampSec: Math.floor(date.getTime() / 1000),
      iso: date.toISOString(),
      formatted: date.toLocaleString('zh-CN')
    }));
  } catch (err) {
    res.status(400).json(error('Invalid timestamp'));
  }
});

// 工具 API - 二维码生成
app.post('/api/tools/qrcode/generate', async (req, res) => {
  try {
    const QRCode = require('qrcode');
    const { text, width = 256, height = 256 } = req.body;
    if (!text) return res.status(400).json(error('Text is required'));
    const buffer = await QRCode.toBuffer(text, { width: parseInt(width), margin: 2 });
    res.setHeader('Content-Type', 'image/png');
    res.setHeader('Content-Length', buffer.length);
    res.end(buffer);
  } catch (err) {
    res.status(500).json(error('Failed to generate QR code'));
  }
});

app.get('/api/tools/health', (req, res) => {
  res.json(success({ status: 'ok', timestamp: new Date().toISOString() }));
});

// ========== 文章/分类/标签 API 路由 ==========

function enrichArticle(article) {
  const category = categories.find(c => c.id === article.categoryId) || null;
  const articleTags = article.tagIds.map(tid => tags.find(t => t.id === tid)).filter(Boolean);
  const author = { id: users[0].id, username: users[0].username, displayName: users[0].displayName, avatar: users[0].avatar || '' };
  const { categoryId, tagIds, ...rest } = article;
  return { ...rest, category, tags: articleTags, author };
}

app.get('/api/articles/public/list', (req, res) => {
  const { page = 0, size = 10, category } = req.query;
  let filtered = articles.filter(a => a.published);
  if (category) {
    filtered = filtered.filter(a => a.categoryId === parseInt(category));
  }
  const totalElements = filtered.length;
  const totalPages = Math.ceil(totalElements / parseInt(size));
  const start = parseInt(page) * parseInt(size);
  const content = filtered.slice(start, start + parseInt(size)).map(enrichArticle);
  res.json(success({
    content,
    totalElements,
    totalPages,
    number: parseInt(page),
    size: parseInt(size),
    first: parseInt(page) === 0,
    last: parseInt(page) >= totalPages - 1
  }));
});

app.get('/api/articles/public/recent', (req, res) => {
  const recent = [...articles]
    .filter(a => a.published)
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 5)
    .map(enrichArticle);
  res.json(success(recent));
});

app.get('/api/articles/public/popular', (req, res) => {
  const popular = [...articles]
    .filter(a => a.published)
    .sort((a, b) => b.viewCount - a.viewCount)
    .slice(0, 5)
    .map(enrichArticle);
  res.json(success(popular));
});

app.get('/api/articles/public/:id', (req, res) => {
  const article = articles.find(a => a.id === parseInt(req.params.id) && a.published);
  if (!article) return res.status(404).json(error('Article not found', 404));
  res.json(success(enrichArticle(article)));
});

app.get('/api/categories', (req, res) => {
  res.json(success(categories));
});

app.get('/api/categories/:id', (req, res) => {
  const category = categories.find(c => c.id === parseInt(req.params.id));
  if (!category) return res.status(404).json(error('Category not found', 404));
  res.json(success(category));
});

app.get('/api/tags', (req, res) => {
  res.json(success(tags));
});

app.get('/api/tags/:id', (req, res) => {
  const tag = tags.find(t => t.id === parseInt(req.params.id));
  if (!tag) return res.status(404).json(error('Tag not found', 404));
  res.json(success(tag));
});

// ========== 用户资料/密码 API ==========

app.put('/api/auth/profile', authMiddleware, (req, res) => {
  const { displayName, email } = req.body;
  const user = users.find(u => u.username === req.user.username);
  if (!user) return res.status(404).json(error('User not found', 404));
  if (displayName) user.displayName = displayName;
  if (email) user.email = email;
  res.json(success({
    id: user.id,
    username: user.username,
    email: user.email,
    displayName: user.displayName,
    avatar: user.avatar || '',
    role: user.role
  }));
});

app.put('/api/auth/password', authMiddleware, (req, res) => {
  const { currentPassword, newPassword } = req.body;
  if (!currentPassword || !newPassword) {
    return res.status(400).json(error('当前密码和新密码不能为空'));
  }
  if (currentPassword !== SITE_PASSWORD) {
    return res.status(401).json(error('当前密码错误', 401));
  }
  res.json(success({ message: '密码修改成功' }));
});

// ========== AI API 路由 (Ollama) ==========

// 获取可用的模型列表
app.get('/api/ai/models', async (req, res) => {
  try {
    const response = await fetch(`${OLLAMA_BASE_URL}/api/tags`);
    if (!response.ok) {
      throw new Error(`Ollama API error: ${response.status}`);
    }
    const data = await response.json();
    // 格式化模型列表
    const models = data.models?.map(model => ({
      name: model.name,
      size: model.size,
      parameterSize: model.details?.parameter_size || 'Unknown',
      family: model.details?.family || 'Unknown',
      format: model.details?.format || 'Unknown',
      modifiedAt: model.modified_at
    })) || [];
    res.json(success({ models }));
  } catch (error) {
    console.error('Error fetching Ollama models:', error.message);
    res.status(503).json(error(
      '请确保 Ollama 服务已启动 (localhost:11434)',
      503
    ));
  }
});

// 与 Ollama 模型对话（支持流式响应）
app.post('/api/ai/chat', async (req, res) => {
  const { model, prompt, stream = true, context = [] } = req.body;

  if (!model || !prompt) {
    return res.status(400).json(error('Model and prompt are required'));
  }

  try {
    const response = await fetch(`${OLLAMA_BASE_URL}/api/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model,
        prompt,
        stream,
        context
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Ollama API error: ${response.status} - ${errorText}`);
    }

    // 如果是流式响应，直接透传给客户端
    if (stream) {
      res.setHeader('Content-Type', 'text/plain; charset=utf-8');
      res.setHeader('Transfer-Encoding', 'chunked');
      res.setHeader('Cache-Control', 'no-cache');
      res.setHeader('Connection', 'keep-alive');

      const reader = response.body.getReader();

      try {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          res.write(value);
        }
      } catch (streamError) {
        console.error('Stream error:', streamError);
      } finally {
        res.end();
      }
    } else {
      // 非流式响应
      const data = await response.json();
      res.json(success(data));
    }
  } catch (error) {
    console.error('Error in AI chat:', error.message);
    res.status(503).json(error(
      'Failed to communicate with Ollama service: ' + error.message,
      503
    ));
  }
});

// 检查 Ollama 服务状态
app.get('/api/ai/status', async (req, res) => {
  try {
    const response = await fetch(`${OLLAMA_BASE_URL}/api/tags`, {
      method: 'GET',
      signal: AbortSignal.timeout(5000)
    });
    if (response.ok) {
      res.json(success({ status: 'connected', message: 'Ollama 服务运行正常' }));
    } else {
      throw new Error('Ollama service not responding');
    }
  } catch (error) {
    res.json(success({
      status: 'disconnected',
      message: '无法连接到 Ollama 服务',
      hint: '请确保 Ollama 已安装并运行: ollama serve'
    }));
  }
});

// 静态文件服务
app.use(express.static(path.join(__dirname, 'frontend', 'dist')));

// 前端路由处理
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'frontend', 'dist', 'index.html'));
});

// 定时更新金价（每分钟）
setInterval(() => {
  Object.keys(goldPrice).forEach(currency => {
    const variation = (Math.random() - 0.5) * 2;
    goldPrice[currency].price += variation;
    goldPrice[currency].change = variation;
    goldPrice[currency].changePercent = (variation / goldPrice[currency].price * 100);
  });
  console.log('Gold price updated:', new Date().toISOString());
}, 60000);

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 CoffeeCookie'sHomePage Server running on port ${PORT}`);
  console.log(`📊 API: http://localhost:${PORT}/api`);
  console.log(`🪙 Gold Price: http://localhost:${PORT}/api/gold-price/current`);
  console.log(`🤖 AI Chat: http://localhost:${PORT}/api/ai/models`);
});
