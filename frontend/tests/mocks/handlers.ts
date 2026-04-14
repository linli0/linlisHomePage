import { http, HttpResponse } from 'msw'

const API_BASE = 'http://localhost/api'

export const authHandlers = [
  http.post(`${API_BASE}/auth/login`, async ({ request }) => {
    const body = await request.json() as { username?: string; password?: string }

    if (body.password === 'admin123' || body.password === 'user123') {
      return HttpResponse.json({
        code: 200,
        message: 'Success',
        data: {
          token: 'mock-jwt-token-for-testing',
          type: 'Bearer',
          id: 1,
          username: body.username || 'testuser',
          email: 'test@example.com',
          displayName: 'Test User',
          avatar: null,
          role: body.username === 'admin' ? 'ADMIN' : 'USER',
          expiresIn: 86400000
        }
      })
    }

    return HttpResponse.json(
      { code: 401, message: 'Invalid credentials', data: null },
      { status: 401 }
    )
  }),

  http.get(`${API_BASE}/auth/me`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        displayName: 'Test User',
        avatar: null,
        role: 'USER'
      }
    })
  }),

  http.put(`${API_BASE}/auth/profile`, async ({ request }) => {
    const body = await request.json() as { displayName?: string; email?: string }
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: {
        id: 1,
        username: 'testuser',
        email: body.email || 'test@example.com',
        displayName: body.displayName || 'Test User',
        avatar: null,
        role: 'USER'
      }
    })
  }),

  http.put(`${API_BASE}/auth/password`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: null
    })
  })
]

export const goldPriceHandlers = [
  http.get(`${API_BASE}/gold-price/current`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: {
        price: 2500.50,
        changeAmount: 10.25,
        changePercent: 0.41,
        currency: 'USD',
        symbol: '$',
        timestamp: new Date().toISOString(),
        high: 2510.75,
        low: 2490.25,
        average: 2500.50,
        volatility: 0.82
      }
    })
  }),

  http.get(`${API_BASE}/gold-price/history`, () => {
    const history = Array.from({ length: 30 }, (_, i) => ({
      timestamp: new Date(Date.now() - i * 24 * 60 * 60 * 1000).toISOString(),
      price: 2500 + Math.random() * 100 - 50
    })).reverse()

    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: history
    })
  }),

  http.get(`${API_BASE}/gold-price/currencies`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: [
        { code: 'USD', name: 'US Dollar', symbol: '$', flag: '🇺🇸', rate: 1 },
        { code: 'CNY', name: 'Chinese Yuan', symbol: '¥', flag: '🇨🇳', rate: 7.25 },
        { code: 'EUR', name: 'Euro', symbol: '€', flag: '🇪🇺', rate: 0.92 },
        { code: 'GBP', name: 'British Pound', symbol: '£', flag: '🇬🇧', rate: 0.79 }
      ]
    })
  })
]

export const articleHandlers = [
  http.get(`${API_BASE}/articles/public/list`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: {
        content: [
          {
            id: 1,
            title: 'Test Article 1',
            summary: 'This is a test article summary',
            coverImage: 'https://example.com/cover1.jpg',
            published: true,
            viewCount: 10,
            createdAt: new Date().toISOString(),
            category: { id: 1, name: 'Technology', icon: 'fas fa-laptop' },
            tags: [{ id: 1, name: 'vue', color: '#42b883' }]
          },
          {
            id: 2,
            title: 'Test Article 2',
            summary: 'Another test article summary',
            coverImage: 'https://example.com/cover2.jpg',
            published: true,
            viewCount: 5,
            createdAt: new Date(Date.now() - 86400000).toISOString(),
            category: { id: 2, name: 'Finance', icon: 'fas fa-chart-line' },
            tags: [{ id: 2, name: 'gold', color: '#f59e0b' }]
          }
        ],
        totalElements: 2,
        totalPages: 1,
        number: 0,
        size: 10,
        first: true,
        last: true,
        numberOfElements: 2
      }
    })
  })
]

// AI handlers
export const aiHandlers = [
  http.get(`${API_BASE}/ai/models`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: {
        models: [
          { name: 'llama2', size: 4000000000, parameterSize: '7B', family: 'llama', format: 'gguf', modifiedAt: '2024-01-01' },
          { name: 'mistral', size: 4100000000, parameterSize: '7B', family: 'mistral', format: 'gguf', modifiedAt: '2024-01-02' }
        ]
      }
    })
  }),
  
  http.get(`${API_BASE}/ai/status`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: { status: 'connected' }
    })
  }),
  
  http.post(`${API_BASE}/ai/chat`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: {
        model: 'llama2',
        response: 'This is a mock AI response.',
        done: true,
        context: []
      }
    })
  })
]

// Tools handlers
export const toolsHandlers = [
  http.post(`${API_BASE}/tools/json/format`, async ({ request }) => {
    const body = await request.json() as { json: string }
    try {
      const parsed = JSON.parse(body.json)
      return HttpResponse.json({
        code: 200,
        message: 'Success',
        data: { result: JSON.stringify(parsed, null, 2) }
      })
    } catch {
      return HttpResponse.json({ code: 400, message: 'Invalid JSON', data: null }, { status: 400 })
    }
  }),
  
  http.post(`${API_BASE}/tools/json/minify`, async ({ request }) => {
    const body = await request.json() as { json: string }
    try {
      const parsed = JSON.parse(body.json)
      return HttpResponse.json({
        code: 200,
        message: 'Success',
        data: { result: JSON.stringify(parsed) }
      })
    } catch {
      return HttpResponse.json({ code: 400, message: 'Invalid JSON', data: null }, { status: 400 })
    }
  }),
  
  http.post(`${API_BASE}/tools/base64/encode`, async ({ request }) => {
    const body = await request.json() as { text: string }
    const encoded = Buffer.from(body.text).toString('base64')
    return HttpResponse.json({ code: 200, message: 'Success', data: { result: encoded } })
  }),
  
  http.post(`${API_BASE}/tools/base64/decode`, async ({ request }) => {
    const body = await request.json() as { encoded: string }
    try {
      const decoded = Buffer.from(body.encoded, 'base64').toString('utf-8')
      return HttpResponse.json({ code: 200, message: 'Success', data: { result: decoded } })
    } catch {
      return HttpResponse.json({ code: 400, message: 'Invalid Base64', data: null }, { status: 400 })
    }
  }),
  
  http.post(`${API_BASE}/tools/url/encode`, async ({ request }) => {
    const body = await request.json() as { text: string }
    return HttpResponse.json({ code: 200, message: 'Success', data: { result: encodeURIComponent(body.text) } })
  }),
  
  http.post(`${API_BASE}/tools/url/decode`, async ({ request }) => {
    const body = await request.json() as { encoded: string }
    return HttpResponse.json({ code: 200, message: 'Success', data: { result: decodeURIComponent(body.encoded) } })
  }),
  
  http.post(`${API_BASE}/tools/hash/md5`, async ({ request }) => {
    const body = await request.json() as { text: string }
    return HttpResponse.json({ code: 200, message: 'Success', data: { result: 'mock-md5-' + body.text.length } })
  }),
  
  http.post(`${API_BASE}/tools/hash/sha1`, async ({ request }) => {
    const body = await request.json() as { text: string }
    return HttpResponse.json({ code: 200, message: 'Success', data: { result: 'mock-sha1-' + body.text.length } })
  }),
  
  http.post(`${API_BASE}/tools/hash/sha256`, async ({ request }) => {
    const body = await request.json() as { text: string }
    return HttpResponse.json({ code: 200, message: 'Success', data: { result: 'mock-sha256-' + body.text.length } })
  }),
  
  http.post(`${API_BASE}/tools/hash/sha512`, async ({ request }) => {
    const body = await request.json() as { text: string }
    return HttpResponse.json({ code: 200, message: 'Success', data: { result: 'mock-sha512-' + body.text.length } })
  }),
  
  http.post(`${API_BASE}/tools/timestamp/convert`, async ({ request }) => {
    const body = await request.json() as { input: string }
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: {
        timestampMs: 1704067200000,
        timestampSec: 1704067200,
        iso: '2024-01-01T00:00:00Z',
        formatted: '2024-01-01'
      }
    })
  }),
  
  http.get(`${API_BASE}/tools/health`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: { status: 'healthy' } })
  })
]

// Tweets handlers
export const tweetsHandlers = [
  http.get(`${API_BASE}/tweets/latest`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: [
        { id: 1, content: 'Tweet 1', platform: 'twitter', createdAt: '2024-01-01' },
        { id: 2, content: 'Tweet 2', platform: 'twitter', createdAt: '2024-01-02' }
      ]
    })
  }),
  
  http.post(`${API_BASE}/tweets/search`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: [] })
  }),
  
  http.get(`${API_BASE}/tweets/stats`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: { total: 100, platforms: ['twitter', 'telegram'] }
    })
  }),
  
  http.get(`${API_BASE}/tweets/:id`, ({ params }) => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: { id: params.id, content: 'Tweet content', platform: 'twitter' }
    })
  })
]

// Xiaomi handlers
export const xiaomiHandlers = [
  http.get(`${API_BASE}/xiaomi/status`, () => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: { connected: true, device: { deviceId: 'x1', name: 'Xiaomi Speaker', model: 'LX05', online: true, volume: 50 } }
    })
  }),
  
  http.post(`${API_BASE}/xiaomi/tts`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: {} })
  }),
  
  http.post(`${API_BASE}/xiaomi/volume`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: {} })
  }),
  
  http.post(`${API_BASE}/xiaomi/play`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: {} })
  }),
  
  http.post(`${API_BASE}/xiaomi/pause`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: {} })
  }),
  
  http.post(`${API_BASE}/xiaomi/stop`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: {} })
  })
]

// Trading handlers
export const tradingHandlers = [
  http.get(`${API_BASE}/quant/strategies`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: [] })
  }),
  
  http.get(`${API_BASE}/quant/strategies/:id`, ({ params }) => {
    return HttpResponse.json({
      code: 200,
      message: 'Success',
      data: { id: params.id, name: 'Strategy', type: 'momentum', status: 'active' }
    })
  }),
  
  http.get(`${API_BASE}/quant/signals`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: [] })
  }),
  
  http.get(`${API_BASE}/quant/backtest/:strategyId`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: [] })
  }),
  
  http.post(`${API_BASE}/quant/backtest/:strategyId`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: {} })
  }),
  
  http.get(`${API_BASE}/quant/indicators`, () => {
    return HttpResponse.json({ code: 200, message: 'Success', data: [] })
  })
]

export const handlers = [
  ...authHandlers,
  ...goldPriceHandlers,
  ...articleHandlers,
  ...aiHandlers,
  ...toolsHandlers,
  ...tweetsHandlers,
  ...xiaomiHandlers,
  ...tradingHandlers
]