/**
 * Tests for HomeView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import HomeView from '@/views/HomeView.vue'

// Mock goldPriceApi and articleApi
vi.mock('@/api/goldPrice', () => ({
  goldPriceApi: {
    getCurrentPrice: vi.fn().mockResolvedValue({
      data: {
        data: {
          price: 2350.50,
          changeAmount: 10,
          changePercent: 0.42,
          currency: 'USD',
          symbol: '$',
          high: 2360,
          low: 2340,
          average: 2350
        }
      }
    })
  }
}))

vi.mock('@/api/article', () => ({
  articleApi: {
    getRecentArticles: vi.fn().mockResolvedValue({
      data: {
        data: [
          { id: 1, title: 'Test Article', summary: 'Summary', viewCount: 100 }
        ]
      }
    })
  }
}))

describe('HomeView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/gold', name: 'Gold', component: { template: '<div>Gold</div>' } },
        { path: '/articles', name: 'Articles', component: { template: '<div>Articles</div>' } },
        { path: '/ai', name: 'AI', component: { template: '<div>AI</div>' } },
        { path: '/tools', name: 'Tools', component: { template: '<div>Tools</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render home view', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should show CoffeeCookies branding', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.text()).toContain('Coffee')
    expect(wrapper.text()).toContain('Cookies')
  })

  test('should show navigation links', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const links = wrapper.findAllComponents({ name: 'RouterLink' })
    expect(links.length).toBeGreaterThan(0)
  })

  test('should show feature cards', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Should have sections for features
    const html = wrapper.html()
    expect(html.includes('金价') || html.includes('Gold')).toBe(true)
    expect(html.includes('Wiki') || html.includes('文章')).toBe(true)
  })

  test('should load gold price on mount', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Wait for data loading
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Check if loading state was set
    expect(wrapper.vm.loading).toBe(false)
  })

  test('should have loading state', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.vm.loading).toBeDefined()
    expect(wrapper.vm.loadingArticles).toBeDefined()
  })

  test('should show call-to-action buttons', async () => {
    router.push('/')
    await router.isReady()
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const html = wrapper.html()
    expect(html.includes('金价') || html.includes('gold') || html.includes('AI')).toBe(true)
  })
})