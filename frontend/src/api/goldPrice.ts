import request from '@/utils/request'

export interface GoldPrice {
  price: number
  changeAmount: number
  changePercent: number
  currency: string
  symbol: string
  timestamp: string
  high: number
  low: number
  average: number
  volatility: number
}

export interface PricePoint {
  date: string
  price: number
}

export interface Currency {
  code: string
  name: string
  symbol: string
  flag: string
  rate: number
}

export const goldPriceApi = {
  getCurrentPrice: (currency = 'CNY') =>
    request.get(`/gold-price/current?currency=${currency}`),
  getPriceHistory: (currency = 'CNY', days = 30) =>
    request.get(`/gold-price/history?currency=${currency}&days=${days}`),
  getSupportedCurrencies: () => request.get('/gold-price/currencies'),
  refresh: () => request.post('/gold-price/refresh'),
}
