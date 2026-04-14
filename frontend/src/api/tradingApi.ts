import request from '@/utils/request'

export interface TradingStrategy {
  id: string
  name: string
  description: string
  type: string
  status: string
  parameters: Record<string, any>
  symbol: string
  totalReturn: number
  sharpeRatio: number
  maxDrawdown: number
  winRate: number
  createdAt: string
  updatedAt: string
}

export interface TradingSignal {
  id: string
  strategyId: string
  strategyName: string
  symbol: string
  signalType: string
  price: number
  targetPrice: number
  stopLoss: number
  confidence: number
  reason: string
  executed: boolean
  executedAt: string | null
  createdAt: string
}

export interface BacktestResult {
  id: string
  strategyId: string
  strategyName: string
  startDate: string
  endDate: string
  totalReturn: number
  sharpeRatio: number
  maxDrawdown: number
  winRate: number
  totalTrades: number
  profitTrades: number
  lossTrades: number
  createdAt: string
}

export interface IndicatorValue {
  date: string
  value: number
}

export interface CandleData {
  time: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

export const tradingApi = {
  getStrategies: () => 
    request.get('/quant/strategies'),
  
  getStrategyById: (id: string) => 
    request.get(`/quant/strategies/${id}`),
  
  createStrategy: (data: Omit<TradingStrategy, 'id' | 'createdAt' | 'updatedAt'>) => 
    request.post('/quant/strategies', data),
  
  updateStrategy: (id: string, data: Partial<Omit<TradingStrategy, 'id' | 'createdAt' | 'updatedAt'>>) => 
    request.put(`/quant/strategies/${id}`, data),
  
  deleteStrategy: (id: string) => 
    request.delete(`/quant/strategies/${id}`),
  
  getSignals: (strategyId?: string, limit?: number) => {
    const params: Record<string, any> = {}
    if (strategyId) params.strategyId = strategyId
    if (limit) params.limit = limit
    return request.get('/quant/signals', { params })
  },
  
  getBacktestResults: (strategyId: string) => 
    request.get(`/quant/backtest/${strategyId}`),
  
  runBacktest: (strategyId: string, params: { startDate: string; endDate: string; initialCapital: number }) => 
    request.post(`/quant/backtest/${strategyId}`, params),
  
  getIndicators: (symbol: string, indicatorType: string, period: number) => {
    const params = { symbol, indicatorType, period }
    return request.get('/quant/indicators', { params })
  }
}