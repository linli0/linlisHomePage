<template>
  <div v-if="article" class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Back Button -->
      <button
        @click="$router.back()"
        class="mb-6 flex items-center space-x-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span>返回</span>
      </button>

      <!-- Article Header -->
      <div class="card p-8 mb-6">
        <div class="flex items-center space-x-2 mb-4">
          <span v-if="article.category" class="flex items-center space-x-1 text-sm text-gray-500 dark:text-gray-400">
            <span class="text-lg">{{ article.category.icon }}</span>
            <span>{{ article.category.name }}</span>
          </span>
          <span class="text-gray-300">|</span>
          <span class="text-sm text-gray-500 dark:text-gray-400">{{ formatDate(article.createdAt) }}</span>
          <span class="text-gray-300">|</span>
          <span class="text-sm text-gray-500 dark:text-gray-400 flex items-center">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            {{ article.viewCount }}
          </span>
        </div>

        <h1 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-6">
          {{ article.title }}
        </h1>

        <!-- Author -->
        <div v-if="article.author" class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-medium">
            {{ article.author.displayName?.[0] || article.author.username?.[0] || 'U' }}
          </div>
          <div>
            <p class="font-medium text-gray-900 dark:text-white">{{ article.author.displayName || article.author.username }}</p>
            <p class="text-sm text-gray-500 dark:text-gray-400">作者</p>
          </div>
        </div>

        <!-- Tags -->
        <div v-if="article.tags && article.tags.length > 0" class="flex flex-wrap gap-2 mt-6">
          <span
            v-for="tag in article.tags"
            :key="tag.id"
            class="px-3 py-1 text-sm rounded-full"
            :style="{ backgroundColor: tag.color + '20', color: tag.color }"
          >
            # {{ tag.name }}
          </span>
        </div>
      </div>

      <!-- Article Content -->
      <div class="card p-8">
        <div class="prose dark:prose-invert max-w-none">
          <div v-html="renderMarkdown(article.content)"></div>
        </div>
      </div>

      <!-- Article Footer -->
      <div class="mt-6 text-center text-sm text-gray-500 dark:text-gray-400">
        最后更新于 {{ formatDate(article.updatedAt) }}
      </div>
    </div>
  </div>

  <!-- Loading -->
  <div v-else class="min-h-screen flex items-center justify-center">
    <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi, type Article } from '@/api/article'

const route = useRoute()
const article = ref<Article | null>(null)

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function renderMarkdown(content: string): string {
  // Simple markdown-like rendering
  // Convert headers
  let html = content
    .replace(/# (.*)/g, '<h1 class="text-3xl font-bold mb-4">$1</h1>')
    .replace(/## (.*)/g, '<h2 class="text-2xl font-bold mb-3 mt-6">$1</h2>')
    .replace(/### (.*)/g, '<h3 class="text-xl font-bold mb-2 mt-4">$1</h3>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/- (.*)/g, '<li class="ml-4">$1</li>')
    .replace(/`([^`]+)`/g, '<code class="bg-gray-100 dark:bg-gray-800 px-1 py-0.5 rounded text-sm font-mono">$1</code>')
    .replace(/\n\n/g, '</p><p class="mb-4">')
  
  return `<p class="mb-4">${html}</p>`
}

onMounted(async () => {
  const articleId = Number(route.params.id)
  if (articleId) {
    try {
      const res = await articleApi.getArticleById(articleId)
      article.value = res.data.data
    } catch (error) {
      console.error('Failed to fetch article:', error)
    }
  }
})
</script>
