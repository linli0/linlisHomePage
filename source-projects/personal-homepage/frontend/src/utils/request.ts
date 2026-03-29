import axios from 'axios'
import type { AxiosInstance, AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response: AxiosResponse<Result<any>>) => {
    const { data } = response
    if (data.code !== 200) {
      ElMessage.error(data.message || 'Request failed')
      return Promise.reject(new Error(data.message))
    }
    return response
  },
  (error: AxiosError) => {
    const message = error.response?.data?.message || error.message || 'Network error'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request

export function get<T>(url: string, params?: object): Promise<T> {
  return request.get(url, { params }).then(res => res.data.data)
}

export function post<T>(url: string, data?: object): Promise<T> {
  return request.post(url, data).then(res => res.data.data)
}

export function put<T>(url: string, data?: object): Promise<T> {
  return request.put(url, data).then(res => res.data.data)
}

export function del<T>(url: string): Promise<T> {
  return request.delete(url).then(res => res.data.data)
}
