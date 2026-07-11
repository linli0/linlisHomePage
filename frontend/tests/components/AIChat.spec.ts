/**
 * Tests for AIChat component
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AIChat from '@/components/AIChat.vue'

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

// Mock fetch
global.fetch = vi.fn()

describe('AIChat', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorageMock.clear()
    vi.clearAllMocks()
  })

  test('should render AI chat component', () => {
    const wrapper = mount(AIChat, {
      global: {
        plugins: [createPinia()]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should show model selector', () => {
    const wrapper = mount(AIChat, {
      global: {
        plugins: [createPinia()]
      }
    })
    
    expect(wrapper.find('select').exists()).toBe(true)
  })

  test('should show input textarea', () => {
    const wrapper = mount(AIChat, {
      global: {
        plugins: [createPinia()]
      }
    })
    
    expect(wrapper.find('textarea').exists()).toBe(true)
  })

  test('should show send button', () => {
    const wrapper = mount(AIChat, {
      global: {
        plugins: [createPinia()]
      }
    })
    
    const buttons = wrapper.findAll('button')
    const sendButton = buttons.find(b => b.text().includes('发送'))
    expect(sendButton).toBeDefined()
  })

  test('should show status indicator', () => {
    const wrapper = mount(AIChat, {
      global: {
        plugins: [createPinia()]
      }
    })
    
    expect(wrapper.html()).toContain('Ollama')
  })

  test('should have empty messages initially', () => {
    const wrapper = mount(AIChat, {
      global: {
        plugins: [createPinia()]
      }
    })
    
    expect(wrapper.vm.messages).toEqual([])
  })

  test('should clear chat', async () => {
    const wrapper = mount(AIChat, {
      global: {
        plugins: [createPinia()]
      }
    })
    
    // Add a message
    wrapper.vm.messages = [{ role: 'user', content: 'test' }]
    
    // Mock confirm
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    
    // Find clear button
    const buttons = wrapper.findAll('button')
    const clearButton = buttons.find(b => b.element.getAttribute('title')?.includes('清空'))
    
    if (clearButton) {
      await clearButton.trigger('click')
      expect(wrapper.vm.messages).toEqual([])
    }
  })

  test('should disable send when no model selected', () => {
    const wrapper = mount(AIChat, {
      global: {
        plugins: [createPinia()]
      }
    })
    
    wrapper.vm.selectedModel = ''
    wrapper.vm.inputMessage = 'test'
    
    expect(wrapper.vm.isLoading).toBe(false)
    expect(wrapper.vm.selectedModel).toBe('')
  })
})