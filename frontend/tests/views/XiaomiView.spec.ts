/**
 * Tests for XiaomiView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import XiaomiView from '@/views/XiaomiView.vue'

// Mock xiaomiApi
vi.mock('@/api/xiaomi', () => ({
  xiaomiApi: {
    getStatus: vi.fn().mockResolvedValue({
      data: { connected: true, device: { name: 'Xiaomi Speaker' } }
    }),
    tts: vi.fn().mockResolvedValue({ data: {} }),
    setVolume: vi.fn().mockResolvedValue({ data: {} }),
    play: vi.fn().mockResolvedValue({ data: {} }),
    pause: vi.fn().mockResolvedValue({ data: {} }),
    stop: vi.fn().mockResolvedValue({ data: {} })
  }
}))

describe('XiaomiView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/xiaomi', name: 'Xiaomi', component: { template: '<div>Xiaomi</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render xiaomi view', async () => {
    router.push('/xiaomi')
    await router.isReady()
    
    const wrapper = mount(XiaomiView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should have xiaomi controls', async () => {
    router.push('/xiaomi')
    await router.isReady()
    
    const wrapper = mount(XiaomiView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })
})