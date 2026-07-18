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
const requestCache = new Map<string, Promise<unknown>>() // Cache pending requests to avoid duplicates
const DEFAULT_CACHE_TTL = 30000 // 30 seconds

// Check if cache is valid
function isCacheValid(key: string): boolean {
  const cached = apiCache.get(key)
  if (!cached) return false
  return Date.now() - cached.timestamp < cached.ttl
}

// Generate cache key from request config
function getCacheKey(config: AxiosRequestConfig): string {
  const method = config.method?.toLowerCase() || 'get'
  const url = config.url || ''
  const params = JSON.stringify(config.params || {})
  const data = JSON.stringify(config.data || {})
  return `${method}:${url}:${params}:${data}`
}

request.interceptors.request.use(
  async (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // Cache optimization: Handle GET requests
    if (config.method?.toLowerCase() === 'get') {
      const cacheKey = getCacheKey(config)
      
      // Return cached promise if request is already in progress
      if (requestCache.has(cacheKey)) {
        // This will cause the original request to be resolved with the cached promise
        throw {
          isCachedPromise: true,
          cacheKey
        }
      }
      
      // Return cached data if available and valid
      if (isCacheValid(cacheKey)) {
        const cached = apiCache.get(cacheKey)
        if (cached) {
          // Create a resolved promise with cached data
          const cachedPromise = Promise.resolve({
            data: cached.data,
            status: 200,
            statusText: 'OK',
            headers: {},
            config
          })
          requestCache.set(cacheKey, cachedPromise)
          throw {
            isCachedPromise: true,
            cacheKey
          }
        }
      }
      
      // Store the actual request promise to prevent duplicates
      // The actual request will be made in the response interceptor
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 鏅鸿兘鍝嶅簲瑙ｅ寘锛氬吋瀹?Express 鍜?Spring Boot 涓ょ鍚庣鏍煎紡
// Express: { price: 123, ... }
// Spring Boot: { code: 200, message: "success", data: {...} }
const unwrapResponse = (response: unknown): unknown => {
  if (response && typeof response === 'object' && !Array.isArray(response)) {
    const resp = response as Record<string, unknown>
    // 妫€娴?Spring Boot 鍖呰鏍煎紡锛氭湁 data 瀛楁涓旀湁 code 鎴?message
    if ('data' in resp && ('code' in resp || 'message' in resp)) {
      return resp.data
    }
  }
  return response
}

request.interceptors.response.use(
  (response) => {
    // 鏅鸿兘瑙ｅ寘锛氳嚜鍔ㄥ鐞?Spring Boot 鍖呰鏍煎紡
    const unwrappedData = unwrapResponse(response.data)
    
    // Cache GET responses for performance optimization
    if (response.config.method?.toLowerCase() === 'get') {
      const cacheKey = getCacheKey(response.config)
      apiCache.set(cacheKey, {
        data: unwrappedData,
        timestamp: Date.now(),
        ttl: DEFAULT_CACHE_TTL
      })
      
      // Resolve the cached promise
      if (requestCache.has(cacheKey)) {
        requestCache.delete(cacheKey)
      }
    }
    
    return {
      ...response,
      data: unwrappedData
    }
  },
  (error) => {
    // Handle cached promise errors
    if (error.isCachedPromise) {
      const cacheKey = error.cacheKey
      const cachedPromise = requestCache.get(cacheKey)
      if (cachedPromise) {
        return cachedPromise
      }
    }
    
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      // Clear cache on logout for security
      apiCache.clear()
      requestCache.clear()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    
    // Clean up request cache on error
    if (error.config?.method?.toLowerCase() === 'get') {
      const cacheKey = getCacheKey(error.config)
      requestCache.delete(cacheKey)
    }
    
    return Promise.reject(error)
  }
)

// Export cache utilities for manual cache control
export const clearApiCache = () => {
  apiCache.clear()
  requestCache.clear()
}
export const invalidateCacheKey = (key: string) => {
  apiCache.delete(key)
  requestCache.delete(key)
}

export default request
