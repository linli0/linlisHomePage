/**
 * Tests for auth store
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('Initial state', () => {
    it('should have null token initially', () => {
      const store = useAuthStore()
      expect(store.token).toBeNull()
    })

    it('should have null user initially', () => {
      const store = useAuthStore()
      expect(store.user).toBeNull()
    })

    it('should not be authenticated initially', () => {
      const store = useAuthStore()
      expect(store.isAuthenticated).toBe(false)
    })

    it('should not be admin initially', () => {
      const store = useAuthStore()
      expect(store.isAdmin).toBe(false)
    })

    it('should have correct initial loading state', () => {
      const store = useAuthStore()
      expect(store.loading).toBe(false)
      expect(store.error).toBe('')
    })
  })

  describe('Logout action', () => {
    it('should clear all auth state on logout', () => {
      const store = useAuthStore()
      
      // Set token manually
      store.token = 'test-token'
      store.user = { id: 1, username: 'test', email: 'test@test.com', displayName: 'Test', avatar: '', role: 'USER' }
      
      // Logout
      store.logout()
      
      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
      expect(store.isAuthenticated).toBe(false)
    })
  })

  describe('Token persistence', () => {
    it('should initialize with token from localStorage', () => {
      localStorage.setItem('token', 'saved-token')
      
      const store = useAuthStore()
      
      expect(store.token).toBe('saved-token')
      expect(store.isAuthenticated).toBe(true)
    })
  })
})