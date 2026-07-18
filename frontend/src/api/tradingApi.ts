import request from '@/utils/request'

export interface TradingStrategy {
  id: number
  name: string
  description?: string
  symbol?: string
  enabled?: boolean
  params?: Record<string, unknown>
}

export interface TradingSignal {
  id: number
  strategyId?: number
  symbol: string
  side: string
  price?: number
  createdAt?: string
  note?: string
}

export const tradingApi = {
  getStrategies: () => request.get('/quant/strategies'),
  getStrategyById: (id: number) => request.get(`/quant/strategies/${id}`),
  createStrategy: (data: Record<string, unknown>) => request.post('/quant/strategies', data),
  updateStrategy: (id: number, data: Record<string, unknown>) =>
    request.put(`/quant/strategies/${id}`, data),
  deleteStrategy: (id: number) => request.delete(`/quant/strategies/${id}`),
  getSignals: (strategyId?: number, limit = 50) =>
    request.get('/quant/signals', { params: { strategyId, limit } }),
  getBacktestResults: (strategyId: number) => request.get(`/quant/backtest/${strategyId}`),
  runBacktest: (strategyId: number, params: Record<string, unknown>) =>
    request.post(`/quant/backtest/${strategyId}`, params),
  getIndicators: (symbol: string, indicatorType: string, period: number) =>
    request.get('/quant/indicators', { params: { symbol, indicatorType, period } }),
}
