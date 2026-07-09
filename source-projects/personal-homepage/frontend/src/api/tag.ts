import { get, post, put, del } from '@/utils/request'
import type { Tag } from '@/types'

export function getTags(): Promise<Tag[]> {
  return get('/tags')
}

export function getTagsWithCount(): Promise<Tag[]> {
  return get('/tags/with-count')
}

export function getTag(id: number): Promise<Tag> {
  return get(`/tags/${id}`)
}

export function createTag(data: Partial<Tag>): Promise<Tag> {
  return post('/tags', data)
}

export function updateTag(id: number, data: Partial<Tag>): Promise<Tag> {
  return put(`/tags/${id}`, data)
}

export function deleteTag(id: number): Promise<void> {
  return del(`/tags/${id}`)
}
