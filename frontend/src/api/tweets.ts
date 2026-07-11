import request from '@/utils/request'
import type { TweetDTO, TweetSearchRequest, TweetStatsDTO } from '@/types/tweet'

export const tweetsApi = {
  getLatest(params?: {
    limit?: number
    platform?: string
    username?: string
  }) {
    return request.get<TweetDTO[]>('/tweets/latest', { params })
  },

  search(request: TweetSearchRequest) {
    return request.post<TweetDTO[]>('/tweets/search', request)
  },

  getStats() {
    return request.get<TweetStatsDTO>('/tweets/stats')
  },

  getById(id: number) {
    return request.get<TweetDTO>(`/tweets/${id}`)
  }
}