import request from '@/utils/request'
import type { TweetSearchRequest } from '@/types/tweet'

export const tweetsApi = {
  getLatest: (params?: { limit?: number; platform?: string; username?: string }) =>
    request.get('/tweets/latest', { params }),
  search: (req: TweetSearchRequest) => request.post('/tweets/search', req),
  getStats: () => request.get('/tweets/stats'),
  getById: (id: number | string) => request.get(`/tweets/${id}`),
}
