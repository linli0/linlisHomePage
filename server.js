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
  { id: 1, username: 'admin', password: '$2a$10$YourHashedPasswordHere', email: 'admin@coffee.com', role: 'ADMIN' }
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
      displayName: user.username,
      avatar: '',
      role: user.role,
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
    displayName: user.username,
    avatar: '',
    role: user.role
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
  const hash = crypto.createHash('md5').update(text).digest('hex');
  res.json(success(hash));
});

// 工具 API - SHA1
app.post('/api/tools/hash/sha1', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
  const hash = crypto.createHash('sha1').update(text).digest('hex');
  res.json(success(hash));
});

// 工具 API - SHA256
app.post('/api/tools/hash/sha256', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
  const hash = crypto.createHash('sha256').update(text).digest('hex');
  res.json(success(hash));
});

// 工具 API - SHA512
app.post('/api/tools/hash/sha512', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
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
app.post('/api/tools/qrcode/generate', (req, res) => {
  res.status(501).json(error('QR Code generation not implemented in Express backend'));
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
app.use(express.static(path.join(__dirname, 'frontend-dist')));

// 前端路由处理
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'frontend-dist', 'index.html'));
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
