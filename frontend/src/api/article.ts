import request from '@/utils/request'

export interface Article {
  id: number
  title: string
  content: string
  summary: string
  coverImage: string
  published: boolean
  viewCount: number
  createdAt: string
  updatedAt: string
  category?: Category
  tags?: Tag[]
  author?: User
}

export interface Category {
  id: number
  name: string
  description: string
  icon: string
  sortOrder: number
}

export interface Tag {
  id: number
  name: string
  color: string
}

export interface User {
  id: number
  username: string
  displayName: string
  avatar: string
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
  getPublishedArticles: (page: number = 0, size: number = 10, category?: number) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) })
    if (category) params.append('category', String(category))
    return request.get(`/articles/public/list?${params.toString()}`)
  },
  
  getArticleById: (id: number) => 
    request.get(`/articles/public/${id}`),
  
  getRecentArticles: () => 
    request.get('/articles/public/recent'),
  
  getPopularArticles: () => 
    request.get('/articles/public/popular'),
  
  createArticle: (data: Partial<Article>) => 
    request.post('/articles', data),
  
  updateArticle: (id: number, data: Partial<Article>) => 
    request.put(`/articles/${id}`, data),
  
  deleteArticle: (id: number) => 
    request.delete(`/articles/${id}`)
}

export const categoryApi = {
  getAllCategories: () => request.get('/categories'),
  getCategoryById: (id: number) => request.get(`/categories/${id}`)
}

export const tagApi = {
  getAllTags: () => request.get('/tags'),
  getTagById: (id: number) => request.get(`/tags/${id}`)
}
