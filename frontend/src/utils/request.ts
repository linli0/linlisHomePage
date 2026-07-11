import axios, { AxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'

// Performance Optimization: Reduce timeout from 10s to 5s
// Goal: All operations respond within 200ms
const request = axios.create({
  baseURL: import.meta.env.MODE === 'test' ? 'http://localhost/api' : '/api',
  timeout: 5000,  // Reduced from 10000ms to 5000ms for faster response
  headers: {
    'Content-Type': 'application/json'
  }
})

// API Response Cache for performance optimization
// Cache GET requests for 30 seconds to reduce server calls
const apiCache = new Map<string, { data: unknown; timestamp: number; ttl: number }>()
const DEFAULT_CACHE_TTL = 30000 // 30 seconds

// Check if cache is valid
function isCacheValid(key: string): boolean {
  const cached = apiCache.get(key)
  if (!cached) return false
  return Date.now() - cached.timestamp < cached.ttl
}

// Generate cache key from request config
function getCacheKey(config: AxiosRequestConfig): string {
  const method = config.method || 'get'
  const url = config.url || ''
  const params = JSON.stringify(config.params || {})
  const data = JSON.stringify(config.data || {})
  return `${method}:${url}:${params}:${data}`
}

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // Cache optimization: Check cache for GET requests
    if (config.method === 'get' || !config.method) {
      const cacheKey = getCacheKey(config)
      if (isCacheValid(cacheKey)) {
        // Return cached data immediately (skip network request)
        const cached = apiCache.get(cacheKey)
        if (cached) {
          // Mark as cached response to skip actual request
          config.headers['X-Cache-Key'] = cacheKey
          config.headers['X-Cache-Data'] = JSON.stringify(cached.data)
        }
      }
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 智能响应解包：兼容 Express 和 Spring Boot 两种后端格式
// Express: { price: 123, ... }
// Spring Boot: { code: 200, message: "success", data: {...} }
const unwrapResponse = (response: unknown): unknown => {
  if (response && typeof response === 'object' && !Array.isArray(response)) {
    const resp = response as Record<string, unknown>
    // 检测 Spring Boot 包装格式：有 data 字段且有 code 或 message
    if ('data' in resp && ('code' in resp || 'message' in resp)) {
      return resp.data
    }
  }
  return response
}

request.interceptors.response.use(
  (response) => {
    // Check if this is a cached response
    const cacheKey = response.config.headers?.['X-Cache-Key'] as string
    const cachedData = response.config.headers?.['X-Cache-Data'] as string
    
    if (cacheKey && cachedData) {
      // Return cached data directly (simulated response)
      return {
        ...response,
        data: JSON.parse(cachedData)
      }
    }
    
    // 智能解包：自动处理 Spring Boot 包装格式
    const unwrappedData = unwrapResponse(response.data)
    
    // Cache GET responses for performance optimization
    if (response.config.method === 'get' || !response.config.method) {
      const newCacheKey = getCacheKey(response.config)
      apiCache.set(newCacheKey, {
        data: unwrappedData,
        timestamp: Date.now(),
        ttl: DEFAULT_CACHE_TTL
      })
    }
    
    return {
      ...response,
      data: unwrappedData
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      // Clear cache on logout for security
      apiCache.clear()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// Export cache utilities for manual cache control
export const clearApiCache = () => apiCache.clear()
export const invalidateCacheKey = (key: string) => apiCache.delete(key)

export default request
