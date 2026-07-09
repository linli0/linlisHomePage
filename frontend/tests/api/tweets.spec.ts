/**
 * Tests for Tweets API module
 */

import { describe, test, expect } from 'vitest'
import { tweetsApi } from '@/api/tweets'

describe('Tweets API', () => {
  describe('getLatest', () => {
    test('should get latest tweets without params', async () => {
      const response = await tweetsApi.getLatest()
      
      expect(response).toBeDefined()
      expect(Array.isArray(response.data)).toBe(true)
    })

    test('should get latest tweets with limit', async () => {
      const response = await tweetsApi.getLatest({ limit: 10 })
      
      expect(response).toBeDefined()
    })

    test('should get latest tweets with platform filter', async () => {
      const response = await tweetsApi.getLatest({ platform: 'twitter' })
      
      expect(response).toBeDefined()
    })

    test('should get latest tweets with username filter', async () => {
      const response = await tweetsApi.getLatest({ username: 'testuser' })
      
      expect(response).toBeDefined()
    })
  })

  describe('search', () => {
    test('should search tweets', async () => {
      const searchRequest = {
        query: 'test',
        platform: 'twitter',
        startDate: '2024-01-01',
        endDate: '2024-12-31'
      }
      
      const response = await tweetsApi.search(searchRequest)
      
      expect(response).toBeDefined()
      expect(Array.isArray(response.data)).toBe(true)
    })
  })

  describe('getStats', () => {
    test('should get tweet statistics', async () => {
      const response = await tweetsApi.getStats()
      
      expect(response).toBeDefined()
      expect(response.data).toBeDefined()
    })
  })

  describe('getById', () => {
    test('should get tweet by ID', async () => {
      const response = await tweetsApi.getById(1)
      
      expect(response).toBeDefined()
      expect(response.data).toBeDefined()
    })
  })
})