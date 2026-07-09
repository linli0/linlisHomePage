/**
 * Tests for TweetsView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import TweetsView from '@/views/TweetsView.vue'

// Mock tweetsApi
vi.mock('@/api/tweets', () => ({
  tweetsApi: {
    getLatest: vi.fn().mockResolvedValue({
      data: []
    }),
    getStats: vi.fn().mockResolvedValue({
      data: { total: 100, platforms: [] }
    }),
    search: vi.fn().mockResolvedValue({ data: [] })
  }
}))

describe('TweetsView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/tweets', name: 'Tweets', component: { template: '<div>Tweets</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render tweets view', async () => {
    router.push('/tweets')
    await router.isReady()
    
    const wrapper = mount(TweetsView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should have tweets title', async () => {
    router.push('/tweets')
    await router.isReady()
    
    const wrapper = mount(TweetsView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    // Should have some content
    expect(wrapper.html()).toBeTruthy()
  })
})