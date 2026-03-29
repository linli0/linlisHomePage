import { get, post, del } from '@/utils/request'
import type { Comment } from '@/types'

export function getCommentsByArticle(articleId: number): Promise<Comment[]> {
  return get(`/comments/article/${articleId}`)
}

export function createComment(articleId: number, data: Partial<Comment>): Promise<Comment> {
  return post(`/comments/article/${articleId}`, data)
}

export function deleteComment(id: number): Promise<void> {
  return del(`/comments/${id}`)
}
