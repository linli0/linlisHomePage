import { describe, test, expect } from 'vitest'
import { articleApi } from '@/api/article'

describe('Article API', () => {
  test('should get published articles list', async () => {
    const response = await articleApi.getPublishedArticles(0, 10)
    
    expect(response).toBeDefined()
    expect(response.data.content).toBeDefined()
    expect(Array.isArray(response.data.content)).toBe(true)
    expect(response.data.content.length).toBe(2)
    
    const article = response.data.content[0]
    expect(article.id).toBe(1)
    expect(article.title).toBe('Test Article 1')
    expect(article.published).toBe(true)
  })

  test('should have correct pagination info', async () => {
    const response = await articleApi.getPublishedArticles(0, 10)
    
    expect(response.data.totalElements).toBe(2)
    expect(response.data.totalPages).toBe(1)
    expect(response.data.number).toBe(0)
    expect(response.data.size).toBe(10)
    expect(response.data.first).toBe(true)
    expect(response.data.last).toBe(true)
  })

  test('should get article with category and tags', async () => {
    const response = await articleApi.getPublishedArticles(0, 10)
    
    const article = response.data.content[0]
    expect(article.category).toBeDefined()
    expect(article.category.name).toBe('Technology')
    expect(article.tags).toBeDefined()
    expect(Array.isArray(article.tags)).toBe(true)
    expect(article.tags.length).toBeGreaterThan(0)
  })
})
