/**
 * Tests for Tools API module
 */

import { describe, test, expect } from 'vitest'
import { toolsApi } from '@/api/tools'

describe('Tools API', () => {
  describe('JSON Tools', () => {
    test('should format JSON', async () => {
      const response = await toolsApi.formatJson('{ "name": "test" }')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBeDefined()
    })

    test('should minify JSON', async () => {
      const response = await toolsApi.minifyJson('{ "name": "test", "value": 123 }')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBeDefined()
    })
  })

  describe('Base64 Tools', () => {
    test('should encode text to Base64', async () => {
      const response = await toolsApi.base64Encode('Hello World')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBeDefined()
    })

    test('should decode Base64 to text', async () => {
      const response = await toolsApi.base64Decode('SGVsbG8gV29ybGQ=')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBe('Hello World')
    })
  })

  describe('URL Tools', () => {
    test('should encode URL', async () => {
      const response = await toolsApi.urlEncode('hello world')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBe('hello%20world')
    })

    test('should decode URL', async () => {
      const response = await toolsApi.urlDecode('hello%20world')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBe('hello world')
    })
  })

  describe('Hash Tools', () => {
    test('should compute MD5 hash', async () => {
      const response = await toolsApi.md5('test')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBeDefined()
    })

    test('should compute SHA1 hash', async () => {
      const response = await toolsApi.sha1('test')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBeDefined()
    })

    test('should compute SHA256 hash', async () => {
      const response = await toolsApi.sha256('test')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBeDefined()
    })

    test('should compute SHA512 hash', async () => {
      const response = await toolsApi.sha512('test')
      
      expect(response).toBeDefined()
      expect(response.data.result).toBeDefined()
    })
  })

  describe('Timestamp Tools', () => {
    test('should convert timestamp', async () => {
      const response = await toolsApi.timestampConvert('1704067200000', 'timestamp_ms')
      
      expect(response).toBeDefined()
      expect(response.data).toHaveProperty('iso')
      expect(response.data).toHaveProperty('formatted')
    })
  })

  describe('QR Code', () => {
    test('should generate QR code', async () => {
      const response = await toolsApi.generateQRCode('https://example.com', 200, 200)
      
      expect(response).toBeDefined()
    })
  })

  describe('Health Check', () => {
    test('should return health status', async () => {
      const response = await toolsApi.healthCheck()
      
      expect(response).toBeDefined()
      expect(response.data).toHaveProperty('status')
    })
  })
})