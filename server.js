const express = require('express');
const cors = require('cors');
const path = require('path');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

const app = express();
const PORT = process.env.PORT || 8080;
const JWT_SECRET = process.env.JWT_SECRET || 'coffee-cookies-secret-key-2024';

// Ollama 配置
const OLLAMA_BASE_URL = process.env.OLLAMA_URL || 'http://localhost:11434';

// Middleware
app.use(cors());
app.use(express.json());

// 模拟数据库
const users = [
  { id: 1, username: 'admin', password: '$2a$10$YourHashedPasswordHere', email: 'admin@coffee.com', role: 'ADMIN' },
  { id: 2, username: 'user', password: '$2a$10$YourHashedPasswordHere', email: 'user@coffee.com', role: 'USER' }
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
  if (!token) return res.status(401).json({ error: 'Unauthorized' });
  
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    res.status(401).json({ error: 'Invalid token' });
  }
};

// ========== API 路由 ==========

// 健康检查
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// 用户登录
app.post('/api/auth/login', async (req, res) => {
  const { username, password } = req.body;
  const user = users.find(u => u.username === username);
  
  if (!user) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }
  
  // 简化版：直接比较明文密码（生产环境应该使用 bcrypt）
  if (username === 'admin' && password === 'admin123') {
    const token = jwt.sign(
      { id: user.id, username: user.username, role: user.role },
      JWT_SECRET,
      { expiresIn: '24h' }
    );
    return res.json({
      token,
      type: 'Bearer',
      id: user.id,
      username: user.username,
      email: user.email,
      role: user.role
    });
  }
  
  if (username === 'user' && password === 'user123') {
    const token = jwt.sign(
      { id: user.id, username: user.username, role: user.role },
      JWT_SECRET,
      { expiresIn: '24h' }
    );
    return res.json({
      token,
      type: 'Bearer',
      id: user.id,
      username: user.username,
      email: user.email,
      role: user.role
    });
  }
  
  res.status(401).json({ error: 'Invalid credentials' });
});

// 获取当前金价
app.get('/api/gold-price/current', (req, res) => {
  const { currency = 'USD' } = req.query;
  const data = goldPrice[currency];
  const stats = getStats(currency);
  
  if (!data) {
    return res.status(400).json({ error: 'Unsupported currency' });
  }
  
  res.json({
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
  });
});

// 获取历史价格
app.get('/api/gold-price/history', (req, res) => {
  const { currency = 'USD', days = 7 } = req.query;
  const data = goldPrice[currency];
  
  if (!data) {
    return res.status(400).json({ error: 'Unsupported currency' });
  }
  
  const history = generateHistory(parseInt(days), data.price);
  res.json(history);
});

// 获取支持的货币
app.get('/api/gold-price/currencies', (req, res) => {
  res.json(Object.entries(goldPrice).map(([code, data]) => ({
    code,
    name: data.name,
    symbol: data.symbol,
    flag: data.flag,
    rate: code === 'USD' ? 1 : data.price / goldPrice.USD.price
  })));
});

// 工具 API - JSON 格式化
app.post('/api/tools/json/format', (req, res) => {
  try {
    const { json } = req.body;
    const parsed = JSON.parse(json);
    res.json({ result: JSON.stringify(parsed, null, 2) });
  } catch (err) {
    res.status(400).json({ error: 'Invalid JSON' });
  }
});

// 工具 API - Base64 编码
app.post('/api/tools/base64/encode', (req, res) => {
  const { text } = req.body;
  res.json({ result: Buffer.from(text).toString('base64') });
});

// 工具 API - Base64 解码
app.post('/api/tools/base64/decode', (req, res) => {
  try {
    const { text } = req.body;
    res.json({ result: Buffer.from(text, 'base64').toString('utf8') });
  } catch (err) {
    res.status(400).json({ error: 'Invalid Base64' });
  }
});

// 工具 API - URL 编码
app.post('/api/tools/url/encode', (req, res) => {
  const { text } = req.body;
  res.json({ result: encodeURIComponent(text) });
});

// 工具 API - URL 解码
app.post('/api/tools/url/decode', (req, res) => {
  try {
    const { text } = req.body;
    res.json({ result: decodeURIComponent(text) });
  } catch (err) {
    res.status(400).json({ error: 'Invalid URL encoding' });
  }
});

// 工具 API - MD5
app.post('/api/tools/hash/md5', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
  const hash = crypto.createHash('md5').update(text).digest('hex');
  res.json({ result: hash });
});

// 工具 API - SHA256
app.post('/api/tools/hash/sha256', (req, res) => {
  const crypto = require('crypto');
  const { text } = req.body;
  const hash = crypto.createHash('sha256').update(text).digest('hex');
  res.json({ result: hash });
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
    res.json({ models });
  } catch (error) {
    console.error('Error fetching Ollama models:', error.message);
    res.status(503).json({ 
      error: 'Failed to connect to Ollama service', 
      message: '请确保 Ollama 服务已启动 (localhost:11434)',
      models: []
    });
  }
});

// 与 Ollama 模型对话（支持流式响应）
app.post('/api/ai/chat', async (req, res) => {
  const { model, prompt, stream = true, context = [] } = req.body;
  
  if (!model || !prompt) {
    return res.status(400).json({ error: 'Model and prompt are required' });
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
      res.json(data);
    }
  } catch (error) {
    console.error('Error in AI chat:', error.message);
    res.status(503).json({ 
      error: 'Failed to communicate with Ollama service',
      message: error.message
    });
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
      res.json({ status: 'connected', message: 'Ollama 服务运行正常' });
    } else {
      throw new Error('Ollama service not responding');
    }
  } catch (error) {
    res.json({ 
      status: 'disconnected', 
      message: '无法连接到 Ollama 服务',
      hint: '请确保 Ollama 已安装并运行: ollama serve'
    });
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
