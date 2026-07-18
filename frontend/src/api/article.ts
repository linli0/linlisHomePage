import request from '@/utils/request'

export interface User {
  id: number
  username: string
  displayName?: string
  avatar?: string
}

export interface Category {
  id: number
  name: string
  description?: string
  icon?: string
}

export interface Tag {
  id: number
  name: string
  color?: string
}

export interface Article {
  id: number
  title: string
  content?: string
  summary?: string
  published?: boolean
  viewCount: number
  createdAt: string
  updatedAt?: string
  author?: User
  category?: Category
  tags?: Tag[]
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export const articleApi = {
  getPublishedArticles: (page = 0, size = 10, category?: number) => {
    let url = `/articles/public/list?page=${page}&size=${size}`
    if (category != null) url += `&category=${category}`
    return request.get(url)
  },
  getArticleById: (id: number | string) => request.get(`/articles/public/${id}`),
  getRecentArticles: () => request.get('/articles/public/recent'),
  getPopularArticles: () => request.get('/articles/public/popular'),
  createArticle: (data: Record<string, unknown>) => request.post('/articles', data),
  updateArticle: (id: number, data: Record<string, unknown>) => request.put(`/articles/${id}`, data),
  deleteArticle: (id: number) => request.delete(`/articles/${id}`),
}

export const categoryApi = {
  getAllCategories: () => request.get('/categories'),
  getCategoryById: (id: number) => request.get(`/categories/${id}`),
}

export const tagApi = {
  getAllTags: () => request.get('/tags'),
  getTagById: (id: number) => request.get(`/tags/${id}`),
}
