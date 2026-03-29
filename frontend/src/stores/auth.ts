import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export interface User {
  id: number
  username: string
  email: string
  displayName: string
  avatar: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  // Actions
  async function login(password: string) {
    loading.value = true
    error.value = null

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
        role: data.role
      }

      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Login failed'
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
    } catch (err) {
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    user,
    loading,
    error,
    isAuthenticated,
    isAdmin,
    login,
    fetchUser,
    logout
  }
})
