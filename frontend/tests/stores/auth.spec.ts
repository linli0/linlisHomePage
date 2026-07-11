import { describe, test, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

// Mock the authApi
vi.mock('@/api/auth', () => ({
  authApi: {
    login: vi.fn(),
    getCurrentUser: vi.fn(),
    updateProfile: vi.fn(),
    changePassword: vi.fn()
  }
}))

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  test('should initialize with no token', () => {
    const store = useAuthStore()
    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  test('should be authenticated when token exists', () => {
    localStorage.setItem('token', 'test-token')
    
    const store = useAuthStore()
    
    expect(store.token).toBe('test-token')
    expect(store.isAuthenticated).toBe(true)
  })

  test('should clear token on logout', () => {
    localStorage.setItem('token', 'test-token')
    
    const store = useAuthStore()
    store.logout()
    
    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })

  test('should have correct initial loading state', () => {
    const store = useAuthStore()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  test('should not be admin by default', () => {
    const store = useAuthStore()
    expect(store.isAdmin).toBe(false)
  })
})