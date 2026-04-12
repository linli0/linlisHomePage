import { describe, test, expect } from 'vitest'
import { articleApi } from '@/api/article'

describe('Article API', () => {
  test('should get published articles list', async () => {
    const response = await articleApi.getPublishedArticles({ page: 0, size: 10 })
    
    expect(response).toBeDefined()
    expect(response.content).toBeDefined()
    expect(Array.isArray(response.content)).toBe(true)
    expect(response.content.length).toBe(2)
    
    const article = response.content[0]
    expect(article.id).toBe(1)
    expect(article.title).toBe('Test Article 1')
    expect(article.published).toBe(true)
  })

  test('should have correct pagination info', async () => {
    const response = await articleApi.getPublishedArticles({ page: 0, size: 10 })
    
    expect(response.totalElements).toBe(2)
    expect(response.totalPages).toBe(1)
    expect(response.number).toBe(0)
    expect(response.size).toBe(10)
    expect(response.first).toBe(true)
    expect(response.last).toBe(true)
  })

  test('should get article with category and tags', async () => {
    const response = await articleApi.getPublishedArticles({ page: 0, size: 10 })
    
    const article = response.content[0]
    expect(article.category).toBeDefined()
    expect(article.category.name).toBe('Technology')
    expect(article.tags).toBeDefined()
    expect(Array.isArray(article.tags)).toBe(true)
    expect(article.tags.length).toBeGreaterThan(0)
  })
})