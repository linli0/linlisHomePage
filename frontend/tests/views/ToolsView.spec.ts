/**
 * Tests for ToolsView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import ToolsView from '@/views/ToolsView.vue'

// Mock toolsApi
vi.mock('@/api/tools', () => ({
  toolsApi: {
    formatJson: vi.fn().mockResolvedValue({ data: { result: '{}' } }),
    minifyJson: vi.fn().mockResolvedValue({ data: { result: '{}' } }),
    base64Encode: vi.fn().mockResolvedValue({ data: { result: 'encoded' } }),
    base64Decode: vi.fn().mockResolvedValue({ data: { result: 'decoded' } }),
    urlEncode: vi.fn().mockResolvedValue({ data: { result: 'encoded' } }),
    urlDecode: vi.fn().mockResolvedValue({ data: { result: 'decoded' } }),
    md5: vi.fn().mockResolvedValue({ data: { result: 'hash' } }),
    sha256: vi.fn().mockResolvedValue({ data: { result: 'hash' } }),
    timestampConvert: vi.fn().mockResolvedValue({ data: { iso: '2024-01-01', formatted: 'date' } }),
    generateQRCode: vi.fn().mockResolvedValue({ data: new Blob() })
  }
}))

describe('ToolsView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/tools', name: 'Tools', component: { template: '<div>Tools</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render tools view', async () => {
    router.push('/tools')
    await router.isReady()
    
    const wrapper = mount(ToolsView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should show tool categories', async () => {
    router.push('/tools')
    await router.isReady()
    
    const wrapper = mount(ToolsView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const html = wrapper.html()
    // Check for various tool names - be flexible
    expect(html.includes('JSON') || html.includes('Base64') || html.includes('URL') || html.includes('工具')).toBe(true)
  })

  test('should have tool inputs', async () => {
    router.push('/tools')
    await router.isReady()
    
    const wrapper = mount(ToolsView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const inputs = wrapper.findAll('textarea')
    expect(inputs.length).toBeGreaterThan(0)
  })

  test('should have tool buttons', async () => {
    router.push('/tools')
    await router.isReady()
    
    const wrapper = mount(ToolsView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })
})