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
  history?: PricePoint[]
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
  getCurrentPrice: (currency: string = 'USD') => 
    request.get(`/gold-price/current?currency=${currency}`),
  
  getPriceHistory: (currency: string = 'USD', days: number = 30) => 
    request.get(`/gold-price/history?currency=${currency}&days=${days}`),
  
  getSupportedCurrencies: () => 
    request.get('/gold-price/currencies')
}
