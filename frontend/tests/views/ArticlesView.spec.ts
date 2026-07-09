/**
 * Tests for ArticlesView
 */

import { describe, test, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import ArticlesView from '@/views/ArticlesView.vue'

// Mock articleApi
vi.mock('@/api/article', () => ({
  articleApi: {
    getPublishedArticles: vi.fn().mockResolvedValue({
      data: {
        data: {
          content: [
            { id: 1, title: 'Article 1', summary: 'Summary 1', viewCount: 100 },
            { id: 2, title: 'Article 2', summary: 'Summary 2', viewCount: 200 }
          ],
          totalElements: 2,
          totalPages: 1
        }
      }
    }),
    getRecentArticles: vi.fn().mockResolvedValue({
      data: { data: [] }
    }),
    getPopularArticles: vi.fn().mockResolvedValue({
      data: { data: [] }
    })
  },
  categoryApi: {
    getAllCategories: vi.fn().mockResolvedValue({
      data: {
        data: [
          { id: 1, name: 'Technology' },
          { id: 2, name: 'Finance' }
        ]
      }
    })
  }
}))

describe('ArticlesView', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    vi.clearAllMocks()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/articles', name: 'Articles', component: { template: '<div>Articles</div>' } },
        { path: '/article/:id', name: 'ArticleDetail', component: { template: '<div>Detail</div>' } }
      ]
    })
    
    pinia = createPinia()
    setActivePinia(pinia)
  })

  test('should render articles view', async () => {
    router.push('/articles')
    await router.isReady()
    
    const wrapper = mount(ArticlesView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  test('should load articles on mount', async () => {
    router.push('/articles')
    await router.isReady()
    
    const wrapper = mount(ArticlesView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    expect(wrapper.vm.loading).toBe(false)
  })

  test('should have articles data', async () => {
    router.push('/articles')
    await router.isReady()
    
    const wrapper = mount(ArticlesView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    expect(wrapper.vm.articles).toBeDefined()
  })
})