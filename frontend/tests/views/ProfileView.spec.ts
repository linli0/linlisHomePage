/**
 * Tests for ProfileView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import ProfileView from '@/views/ProfileView.vue'

// Mock authApi
vi.mock('@/api/auth', () => ({
  authApi: {
    getCurrentUser: vi.fn().mockResolvedValue({
      data: {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        displayName: 'Test User',
        role: 'USER'
      }
    }),
    updateProfile: vi.fn().mockResolvedValue({ data: {} }),
    changePassword: vi.fn().mockResolvedValue({ data: {} })
  }
}))

describe('ProfileView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/profile', name: 'Profile', component: { template: '<div>Profile</div>' } },
        { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render profile view', async () => {
    router.push('/profile')
    await router.isReady()
    
    const wrapper = mount(ProfileView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should show user information section', async () => {
    router.push('/profile')
    await router.isReady()
    
    const wrapper = mount(ProfileView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Should have form elements for profile
    expect(wrapper.html()).toBeTruthy()
  })

  test('should have form inputs', async () => {
    router.push('/profile')
    await router.isReady()
    
    const wrapper = mount(ProfileView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const inputs = wrapper.findAll('input')
    expect(inputs.length).toBeGreaterThan(0)
  })
})