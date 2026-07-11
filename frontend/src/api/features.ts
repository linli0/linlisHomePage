import request from '@/utils/request'

export interface FeatureFlags {
  ai: boolean
  tweets: boolean
  quant: boolean
  xiaomi: boolean
}

export function getFeatures() {
  return request.get<FeatureFlags>('/features')
}
