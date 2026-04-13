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

export const handlers = [
  ...authHandlers,
  ...goldPriceHandlers,
  ...articleHandlers
]