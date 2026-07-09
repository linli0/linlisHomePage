/**
 * Tests for AI API module
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { aiApi } from '@/api/ai'

// Mock fetch for streaming tests
const mockFetch = vi.fn()
global.fetch = mockFetch

describe('AI API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getModels', () => {
    test('should get available AI models', async () => {
      const response = await aiApi.getModels()
      
      expect(response).toBeDefined()
      expect(response.data).toHaveProperty('models')
      expect(Array.isArray(response.data.models)).toBe(true)
    })
  })

  describe('getStatus', () => {
    test('should get Ollama service status', async () => {
      const response = await aiApi.getStatus()
      
      expect(response).toBeDefined()
      expect(response.data).toHaveProperty('status')
    })
  })

  describe('chat', () => {
    test('should send chat request with stream disabled', async () => {
      const chatRequest = {
        model: 'llama2',
        prompt: 'Hello, how are you?',
        stream: false
      }
      
      const response = await aiApi.chat(chatRequest)
      
      expect(response).toBeDefined()
      expect(response.data).toHaveProperty('response')
      expect(response.data).toHaveProperty('done', true)
    })
  })

  describe('chatStream', () => {
    test('should handle streaming response', async () => {
      const onMessage = vi.fn()
      const onDone = vi.fn()
      const onError = vi.fn()
      
      // Mock successful streaming response
      mockFetch.mockResolvedValueOnce({
        ok: true,
        body: {
          getReader: () => ({
            read: vi.fn()
              .mockResolvedValueOnce({
                done: false,
                value: new TextEncoder().encode(JSON.stringify({ response: 'Hello' }) + '\n')
              })
              .mockResolvedValueOnce({
                done: false,
                value: new TextEncoder().encode(JSON.stringify({ response: ' there' }) + '\n')
              })
              .mockResolvedValueOnce({
                done: true,
                value: undefined
              })
          })
        }
      })
      
      await aiApi.chatStream(
        { model: 'llama2', prompt: 'Hi' },
        onMessage,
        onDone,
        onError
      )
      
      expect(onMessage).toHaveBeenCalled()
      expect(onDone).toHaveBeenCalled()
      expect(onError).not.toHaveBeenCalled()
    })

    test('should handle fetch error', async () => {
      const onMessage = vi.fn()
      const onDone = vi.fn()
      const onError = vi.fn()
      
      mockFetch.mockRejectedValueOnce(new Error('Network error'))
      
      await aiApi.chatStream(
        { model: 'llama2', prompt: 'Hi' },
        onMessage,
        onDone,
        onError
      )
      
      expect(onError).toHaveBeenCalled()
      expect(onError.mock.calls[0][0]).toBeInstanceOf(Error)
    })

    test('should handle HTTP error status', async () => {
      const onMessage = vi.fn()
      const onDone = vi.fn()
      const onError = vi.fn()
      
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500
      })
      
      await aiApi.chatStream(
        { model: 'llama2', prompt: 'Hi' },
        onMessage,
        onDone,
        onError
      )
      
      expect(onError).toHaveBeenCalled()
    })
  })
})