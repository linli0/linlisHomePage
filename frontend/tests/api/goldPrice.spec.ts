import { describe, test, expect } from 'vitest'
import { goldPriceApi } from '@/api/goldPrice'

describe('Gold Price API', () => {
  test('should get current gold price', async () => {
    const response = await goldPriceApi.getCurrentPrice('USD')
    
    expect(response).toBeDefined()
    expect(response.price).toBe(2500.50)
    expect(response.currency).toBe('USD')
    expect(response.changeAmount).toBe(10.25)
    expect(response.changePercent).toBe(0.41)
  })

  test('should get gold price history', async () => {
    const response = await goldPriceApi.getPriceHistory('USD', 30)
    
    expect(response).toBeDefined()
    expect(Array.isArray(response)).toBe(true)
    expect(response.length).toBe(30)
    expect(response[0]).toHaveProperty('timestamp')
    expect(response[0]).toHaveProperty('price')
  })

  test('should get supported currencies', async () => {
    const response = await goldPriceApi.getSupportedCurrencies()
    
    expect(response).toBeDefined()
    expect(Array.isArray(response)).toBe(true)
    expect(response.length).toBe(4)
    
    const usd = response.find(c => c.code === 'USD')
    expect(usd).toBeDefined()
    expect(usd?.name).toBe('US Dollar')
    expect(usd?.symbol).toBe('$')
    expect(usd?.flag).toBe('🇺🇸')
    expect(usd?.rate).toBe(1)
    
    const cny = response.find(c => c.code === 'CNY')
    expect(cny).toBeDefined()
    expect(cny?.rate).toBe(7.25)
  })
})