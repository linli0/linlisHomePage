/**
 * Tests for Trading API module (Quant endpoints)
 */

import { describe, test, expect } from 'vitest'
import { tradingApi } from '@/api/tradingApi'

describe('Trading API', () => {
  describe('Strategy Management', () => {
    test('should get all strategies', async () => {
      const response = await tradingApi.getStrategies()
      
      expect(response).toBeDefined()
    })

    test('should get strategy by ID', async () => {
      const response = await tradingApi.getStrategyById('strategy-1')
      
      expect(response).toBeDefined()
    })
  })

  describe('Signals', () => {
    test('should get all signals', async () => {
      const response = await tradingApi.getSignals()
      
      expect(response).toBeDefined()
    })

    test('should get signals by strategy ID', async () => {
      const response = await tradingApi.getSignals('strategy-1')
      
      expect(response).toBeDefined()
    })

    test('should get signals with limit', async () => {
      const response = await tradingApi.getSignals(undefined, 10)
      
      expect(response).toBeDefined()
    })
  })

  describe('Backtest', () => {
    test('should get backtest results', async () => {
      const response = await tradingApi.getBacktestResults('strategy-1')
      
      expect(response).toBeDefined()
    })

    test('should run backtest', async () => {
      const params = {
        startDate: '2024-01-01',
        endDate: '2024-12-31',
        initialCapital: 10000
      }
      
      const response = await tradingApi.runBacktest('strategy-1', params)
      
      expect(response).toBeDefined()
    })
  })

  describe('Indicators', () => {
    test('should get indicator values', async () => {
      const response = await tradingApi.getIndicators('GOLD', 'RSI', 14)
      
      expect(response).toBeDefined()
    })
  })
})