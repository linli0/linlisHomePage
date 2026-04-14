/**
 * Tests for LoginView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import LoginView from '@/views/LoginView.vue'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value.toString()
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key]
    }),
    clear: vi.fn(() => {
      store = {}
    })
  }
})()

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  configurable: true
})

describe('LoginView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    localStorageMock.clear()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
        { path: '/gold', name: 'Gold', component: { template: '<div>Gold</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render login form', async () => {
    router.push('/login')
    await router.isReady()
    
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.find('form').exists()).toBe(true)
  })

  test('should have password input', async () => {
    router.push('/login')
    await router.isReady()
    
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const passwordInput = wrapper.find('input[type="password"]')
    expect(passwordInput.exists()).toBe(true)
  })

  test('should have submit button', async () => {
    router.push('/login')
    await router.isReady()
    
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const submitButton = wrapper.find('button[type="submit"]')
    expect(submitButton.exists()).toBe(true)
  })

  test('should have show password toggle', async () => {
    router.push('/login')
    await router.isReady()
    
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Find toggle button
    const buttons = wrapper.findAll('button')
    const toggleButton = buttons.find(b => b.element.getAttribute('type') === 'button')
    expect(toggleButton).toBeDefined()
  })

  test('should show CoffeeCookies branding', async () => {
    router.push('/login')
    await router.isReady()
    
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.text()).toContain('CoffeeCookies')
  })

  test('should bind password to input', async () => {
    router.push('/login')
    await router.isReady()
    
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const input = wrapper.find('input')
    await input.setValue('test123')
    
    expect(wrapper.vm.password).toBe('test123')
  })

  test('should toggle password visibility', async () => {
    router.push('/login')
    await router.isReady()
    
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.vm.showPassword).toBe(false)
    
    // Find toggle button and click
    const buttons = wrapper.findAll('button[type="button"]')
    const toggleButton = buttons[0]
    await toggleButton.trigger('click')
    
    expect(wrapper.vm.showPassword).toBe(true)
  })

  test('should show error message when login fails', async () => {
    router.push('/login')
    await router.isReady()
    
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Set error through store
    const authStore = wrapper.vm.authStore
    authStore.error = 'Invalid password'
    
    await wrapper.vm.$nextTick()
    
    expect(wrapper.html()).toContain('Invalid password')
  })
})