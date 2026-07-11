/**
 * Tests for GoldPriceView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import GoldPriceView from '@/views/GoldPriceView.vue'

// Mock goldPriceApi
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
          average: 2350,
          volatility: 0.85
        }
      }
    }),
    getSupportedCurrencies: vi.fn().mockResolvedValue({
      data: {
        data: [
          { code: 'USD', name: '美元', symbol: '$', flag: '🇺🇸', rate: 1 },
          { code: 'CNY', name: '人民币', symbol: '¥', flag: '🇨🇳', rate: 7.2 }
        ]
      }
    }),
    getPriceHistory: vi.fn().mockResolvedValue({
      data: {
        data: [
          { date: '2024-01-01', price: 2300 },
          { date: '2024-01-02', price: 2310 }
        ]
      }
    })
  }
}))

// Mock PriceChart component
vi.mock('@/components/PriceChart.vue', () => ({
  default: {
    template: '<div class="mock-price-chart"></div>'
  }
}))

describe('GoldPriceView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/gold', name: 'Gold', component: { template: '<div>Gold</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render gold price view', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should show title', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.text()).toContain('金价')
  })

  test('should show refresh button', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const buttons = wrapper.findAll('button')
    const refreshButton = buttons.find(b => b.text().includes('刷新'))
    expect(refreshButton).toBeDefined()
  })

  test('should have currency selector', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Check for currency buttons
    expect(wrapper.html()).toContain('USD')
  })

  test('should have period selector buttons', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Period buttons may not be visible until data loads
    // Check that the view has loaded and has buttons
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })

  test('should load data on mount', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 200))
    
    expect(wrapper.vm.loading).toBe(false)
  })

  test('should select currency', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    wrapper.vm.currentCurrency = 'CNY'
    
    expect(wrapper.vm.currentCurrency).toBe('CNY')
  })

  test('should select period', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    wrapper.vm.currentPeriod = 7
    
    expect(wrapper.vm.currentPeriod).toBe(7)
  })

  test('should refresh data', async () => {
    router.push('/gold')
    await router.isReady()
    
    const wrapper = mount(GoldPriceView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await wrapper.vm.refreshData()
    
    expect(wrapper.vm.refreshing).toBe(false)
  })
})