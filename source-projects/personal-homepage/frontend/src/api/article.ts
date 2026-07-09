import { get, post, put, del } from '@/utils/request'
import type { Article, PageResult } from '@/types'

export function getArticles(params: {
  page?: number
  size?: number
  categoryId?: number
  tagId?: number
}): Promise<PageResult<Article>> {
  return get('/articles', params)
}

export function getArticle(id: number): Promise<Article> {
  return get(`/articles/${id}`)
}

export function searchArticles(params: {
  keyword: string
  page?: number
  size?: number
}): Promise<PageResult<Article>> {
  return get('/articles/search', params)
}

export function getLatestArticles(limit: number = 5): Promise<Article[]> {
  return get('/articles/latest', { limit })
}

export function getPopularArticles(limit: number = 5): Promise<Article[]> {
  return get('/articles/popular', { limit })
}

export function createArticle(data: Partial<Article>): Promise<Article> {
  return post('/articles', data)
}

export function updateArticle(id: number, data: Partial<Article>): Promise<Article> {
  return put(`/articles/${id}`, data)
}

export function deleteArticle(id: number): Promise<void> {
  return del(`/articles/${id}`)
}
