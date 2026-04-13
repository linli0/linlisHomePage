import { describe, test, expect } from 'vitest'
import { goldPriceApi } from '@/api/goldPrice'

describe('Gold Price API', () => {
  test('should get current gold price', async () => {
    const response = await goldPriceApi.getCurrentPrice('USD')
    
    expect(response).toBeDefined()
    expect(response.data.price).toBe(2500.50)
    expect(response.data.currency).toBe('USD')
    expect(response.data.changeAmount).toBe(10.25)
    expect(response.data.changePercent).toBe(0.41)
  })

  test('should get gold price history', async () => {
    const response = await goldPriceApi.getPriceHistory('USD', 30)
    
    expect(response).toBeDefined()
    expect(Array.isArray(response.data)).toBe(true)
    expect(response.data.length).toBe(30)
    expect(response.data[0]).toHaveProperty('timestamp')
    expect(response.data[0]).toHaveProperty('price')
  })

  test('should get supported currencies', async () => {
    const response = await goldPriceApi.getSupportedCurrencies()
    
    expect(response).toBeDefined()
    expect(Array.isArray(response.data)).toBe(true)
    expect(response.data.length).toBe(4)
    
    const usd = response.data.find(c => c.code === 'USD')
    expect(usd).toBeDefined()
    expect(usd?.name).toBe('US Dollar')
    expect(usd?.symbol).toBe('$')
    expect(usd?.flag).toBe('🇺🇸')
    expect(usd?.rate).toBe(1)
    
    const cny = response.data.find(c => c.code === 'CNY')
    expect(cny).toBeDefined()
    expect(cny?.rate).toBe(7.25)
  })
})
