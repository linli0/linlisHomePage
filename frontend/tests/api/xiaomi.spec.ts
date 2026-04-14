/**
 * Tests for Xiaomi API module
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { xiaomiApi } from '@/api/xiaomi'

// Mock fetch for streaming tests
const mockFetch = vi.fn()
global.fetch = mockFetch

describe('Xiaomi API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getStatus', () => {
    test('should get device status', async () => {
      const response = await xiaomiApi.getStatus()
      
      expect(response).toBeDefined()
      expect(response.data).toHaveProperty('connected')
    })
  })

  describe('tts', () => {
    test('should send TTS request', async () => {
      const response = await xiaomiApi.tts({ text: 'Hello' })
      
      expect(response).toBeDefined()
    })

    test('should send TTS request with custom volume', async () => {
      const response = await xiaomiApi.tts({ text: 'Hello', volume: 50 })
      
      expect(response).toBeDefined()
    })
  })

  describe('setVolume', () => {
    test('should set volume', async () => {
      const response = await xiaomiApi.setVolume(50)
      
      expect(response).toBeDefined()
    })
  })

  describe('play/pause/stop', () => {
    test('should send play command', async () => {
      const response = await xiaomiApi.play()
      
      expect(response).toBeDefined()
    })

    test('should send pause command', async () => {
      const response = await xiaomiApi.pause()
      
      expect(response).toBeDefined()
    })

    test('should send stop command', async () => {
      const response = await xiaomiApi.stop()
      
      expect(response).toBeDefined()
    })
  })

  describe('chatStream', () => {
    test('should handle streaming chat', async () => {
      const onMessage = vi.fn()
      const onDone = vi.fn()
      const onError = vi.fn()
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        body: {
          getReader: () => ({
            read: vi.fn()
              .mockResolvedValueOnce({
                done: false,
                value: new TextEncoder().encode(JSON.stringify({ response: 'Hi' }) + '\n')
              })
              .mockResolvedValueOnce({
                done: true,
                value: undefined
              })
          })
        }
      })
      
      await xiaomiApi.chatStream('Hello', onMessage, onDone, onError)
      
      expect(onMessage).toHaveBeenCalled()
      expect(onDone).toHaveBeenCalled()
    })

    test('should handle stream error', async () => {
      const onMessage = vi.fn()
      const onDone = vi.fn()
      const onError = vi.fn()
      
      mockFetch.mockRejectedValueOnce(new Error('Connection failed'))
      
      await xiaomiApi.chatStream('Hello', onMessage, onDone, onError)
      
      expect(onError).toHaveBeenCalled()
    })
  })
})