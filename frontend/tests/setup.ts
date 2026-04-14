/**
 * Global test setup for Vitest
 * Configures MSW (Mock Service Worker) for API mocking
 */

import { beforeAll, afterAll, afterEach, vi } from 'vitest'
import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { server } from './mocks/node'

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
    }),
    get length() {
      return Object.keys(store).length
    },
    key: vi.fn((index: number) => Object.keys(store)[index] || null),
  }
})()

// Set localStorage mock on window (happy-dom provides window)
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true,
  configurable: true
})

// Mock window.location
const locationMock = {
  href: 'http://localhost/',
  pathname: '/',
  origin: 'http://localhost',
  protocol: 'http:',
  host: 'localhost',
  hostname: 'localhost',
  port: '',
  search: '',
  hash: '',
  assign: vi.fn(),
  reload: vi.fn(),
  replace: vi.fn(),
}

Object.defineProperty(window, 'location', {
  value: locationMock,
  writable: true,
  configurable: true
})

// Mock fetch for streaming tests
global.fetch = vi.fn()

// Set up Pinia for tests
setActivePinia(createPinia())

// Start MSW server for API mocking
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))

// Reset handlers and mocks after each test
afterEach(() => {
  server.resetHandlers()
  localStorageMock.clear()
  vi.clearAllMocks()
})

// Close server after all tests
afterAll(() => server.close())

// Global test configuration
config.global.mocks = {
  $t: (key: string) => key // Mock i18n if needed
}

config.global.stubs = {
  'router-link': true,
  'font-awesome-icon': true
}

// Suppress console.error in tests
vi.spyOn(console, 'error').mockImplementation(() => {})

// Export for use in individual tests
export { localStorageMock, locationMock }