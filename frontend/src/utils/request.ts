import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
    // 智能解包：自动处理 Spring Boot 包装格式
    const unwrappedData = unwrapResponse(response.data)
    return {
      ...response,
      data: unwrappedData
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default request
