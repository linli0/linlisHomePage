import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { extractApiError } from '@/utils/apiError'

export interface AuthUser {
  id: number
  username: string
  email?: string
  displayName?: string
  avatar?: string
  role: string
  createdAt?: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<AuthUser | null>(null)
  const loading = ref(false)
  const error = ref('')

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(password: string): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      const response = await authApi.login({ password })
      const data = response.data.data
      token.value = data.token
      localStorage.setItem('token', data.token)
      user.value = {
        id: data.id,
        username: data.username,
        email: data.email,
        displayName: data.displayName,
        avatar: data.avatar,
        role: data.role,
      }
      return true
    } catch (e: unknown) {
      error.value = extractApiError(e, '密码错误或登录失败')
      return false
    } finally {
      loading.value = false
    }
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const response = await authApi.getCurrentUser()
      user.value = response.data.data
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, loading, error, isAuthenticated, isAdmin, login, fetchUser, logout }
})
