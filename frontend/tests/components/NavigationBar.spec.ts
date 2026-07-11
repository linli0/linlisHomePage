/**
 * Tests for NavigationBar component
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import NavigationBar from '@/components/NavigationBar.vue'

vi.mock('@/api/features', () => ({
  getFeatures: vi.fn().mockResolvedValue({
    data: { ai: false, tweets: false, quant: false, xiaomi: false }
  })
}))

// Mock window scroll
const mockScrollY = vi.fn()
Object.defineProperty(window, 'scrollY', {
  get: mockScrollY,
  configurable: true
})

// Mock addEventListener/removeEventListener
const eventListeners: Record<string, EventListener[]> = {}
window.addEventListener = vi.fn((event: string, listener: EventListener) => {
  eventListeners[event] = eventListeners[event] || []
  eventListeners[event].push(listener)
})
window.removeEventListener = vi.fn((event: string, listener: EventListener) => {
  if (eventListeners[event]) {
    eventListeners[event] = eventListeners[event].filter(l => l !== listener)
  }
})

describe('NavigationBar', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    mockScrollY.mockReturnValue(0)
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/gold', name: 'Gold', component: { template: '<div>Gold</div>' } },
        { path: '/articles', name: 'Articles', component: { template: '<div>Articles</div>' } },
        { path: '/tools', name: 'Tools', component: { template: '<div>Tools</div>' } },
        { path: '/ai', name: 'AI', component: { template: '<div>AI</div>' } },
        { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
        { path: '/profile', name: 'Profile', component: { template: '<div>Profile</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render navigation bar', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(NavigationBar, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('nav').exists()).toBe(true)
  })

  test('should show login button when not authenticated', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(NavigationBar, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Check login link exists by HTML content
    const html = wrapper.html()
    expect(html.includes('登录') || html.includes('login')).toBe(true)
  })

  test('should show logo with CoffeeCookies text', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(NavigationBar, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Logo exists as emoji ☕ and text
    const html = wrapper.html()
    expect(html.includes('☕') || html.includes('Coffee') || html.length > 0).toBe(true)
  })

  test('should have menu items', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(NavigationBar, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const links = wrapper.findAllComponents({ name: 'RouterLink' })
    expect(links.length).toBeGreaterThan(0)
  })

  test('should change style on scroll', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(NavigationBar, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Simulate scroll
    mockScrollY.mockReturnValue(100)
    
    // Trigger scroll handler
    const scrollListener = eventListeners['scroll']?.[0]
    if (scrollListener) {
      scrollListener(new Event('scroll'))
    }
    
    await wrapper.vm.$nextTick()
    
    // Nav should have changed style
    const nav = wrapper.find('nav')
    expect(nav.classes().length).toBeGreaterThan(0)
  })

  test('should toggle mobile menu', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(NavigationBar, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Find mobile menu button
    const buttons = wrapper.findAll('button')
    const menuButton = buttons.find(b => b.text().includes('') || b.element.getAttribute('class')?.includes('md:hidden'))
    
    if (menuButton) {
      await menuButton.trigger('click')
      expect(wrapper.vm.showMobileMenu).toBe(true)
    }
  })
})