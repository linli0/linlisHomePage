import { get, post, put, del } from '@/utils/request'
import type { Category } from '@/types'

export function getCategories(): Promise<Category[]> {
  return get('/categories')
}

export function getCategory(id: number): Promise<Category> {
  return get(`/categories/${id}`)
}

export function createCategory(data: Partial<Category>): Promise<Category> {
  return post('/categories', data)
}

export function updateCategory(id: number, data: Partial<Category>): Promise<Category> {
  return put(`/categories/${id}`, data)
}

export function deleteCategory(id: number): Promise<void> {
  return del(`/categories/${id}`)
}
