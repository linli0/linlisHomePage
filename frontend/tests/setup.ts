// Test setup file for Vitest
import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { server } from './mocks/node'

// Set up Pinia for tests
setActivePinia(createPinia())

// Start MSW server for API mocking
beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

// Global test configuration
config.global.mocks = {
  $t: (key: string) => key // Mock i18n if needed
}

config.global.stubs = {
  'router-link': true,
  'font-awesome-icon': true
}

// Mock console.error to avoid noise in tests
console.error = vi.fn()

// Mock localStorage for auth tests
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem(key: string) {
      return store[key] || null
    },
    setItem(key: string, value: string) {
      store[key] = value.toString()
    },
    removeItem(key: string) {
      delete store[key]
    },
    clear() {
      store = {}
    }
  }
})()

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})