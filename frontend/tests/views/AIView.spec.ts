/**
 * Tests for AIView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import AIView from '@/views/AIView.vue'

// Mock AIChat component
vi.mock('@/components/AIChat.vue', () => ({
  default: {
    template: '<div class="mock-ai-chat"></div>'
  }
}))

describe('AIView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/ai', name: 'AI', component: { template: '<div>AI</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render AI view', async () => {
    router.push('/ai')
    await router.isReady()
    
    const wrapper = mount(AIView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should contain AI chat component', async () => {
    router.push('/ai')
    await router.isReady()
    
    const wrapper = mount(AIView, {
      global: {
        plugins: [router, pinia],
        stubs: {
          AIChat: { template: '<div class="ai-chat-stub"></div>' }
        }
      }
    })
    
    expect(wrapper.html()).toContain('ai-chat-stub')
  })

  test('should show AI title', async () => {
    router.push('/ai')
    await router.isReady()
    
    const wrapper = mount(AIView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.text()).toContain('AI')
  })
})