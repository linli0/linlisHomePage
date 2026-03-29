<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">技术文章</h1>
        <p class="text-gray-600 dark:text-gray-400">分享技术与生活</p>
      </div>

      <!-- Categories -->
      <div class="flex flex-wrap gap-2 mb-8">
        <button
          @click="selectedCategory = null"
          :class="[
            'px-4 py-2 rounded-lg font-medium transition-all',
            !selectedCategory
              ? 'bg-blue-600 text-white'
              : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
          ]"
        >
          全部
        </button>
        <button
          v-for="category in categories"
          :key="category.id"
          @click="selectedCategory = category.id"
          :class="[
            'px-4 py-2 rounded-lg font-medium transition-all flex items-center space-x-2',
            selectedCategory === category.id
              ? 'bg-blue-600 text-white'
              : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
          ]"
        >
          <span>{{ category.icon }}</span>
          <span>{{ category.name }}</span>
        </button>
      </div>

      <!-- Articles Grid -->
      <div v-if="articles.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <article
          v-for="article in articles"
          :key="article.id"
          class="card overflow-hidden hover:shadow-lg transition-all cursor-pointer group"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="h-48 bg-gradient-to-br from-blue-500 to-purple-600 relative overflow-hidden">
            <div class="absolute inset-0 bg-black/20 group-hover:bg-black/10 transition-colors"></div>
            <div class="absolute bottom-4 left-4 right-4">
              <div v-if="article.category" class="flex items-center space-x-2 mb-2">
                <span class="text-2xl">{{ article.category.icon }}</span>
                <span class="text-white/90 text-sm font-medium">{{ article.category.name }}</span>
              </div>
            </div>
          </div>
          
          <div class="p-6">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-2 line-clamp-2 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
              {{ article.title }}
            </h2>
            <p class="text-gray-600 dark:text-gray-400 text-sm line-clamp-3 mb-4">
              {{ article.summary }}
            </p>
            
            <div class="flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
              <div class="flex items-center space-x-2">
                <div v-if="article.author" class="flex items-center space-x-1">
                  <div class="w-6 h-6 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white text-xs">
                    {{ article.author.displayName?.[0] || article.author.username?.[0] || 'U' }}
                  </div>
                  <span>{{ article.author.displayName || article.author.username }}</span>
                </div>
              </div>
              <div class="flex items-center space-x-3">
                <span class="flex items-center">
                  <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  {{ article.viewCount }}
                </span>
                <span>{{ formatDate(article.createdAt) }}</span>
              </div>
            </div>
            
            <!-- Tags -->
            <div v-if="article.tags && article.tags.length > 0" class="flex flex-wrap gap-2 mt-4">
              <span
                v-for="tag in article.tags.slice(0, 3)"
                :key="tag.id"
                class="px-2 py-1 text-xs rounded-full"
                :style="{ backgroundColor: tag.color + '20', color: tag.color }"
              >
                {{ tag.name }}
              </span>
              <span v-if="article.tags.length > 3" class="px-2 py-1 text-xs text-gray-500 dark:text-gray-400">
                +{{ article.tags.length - 3 }}
              </span>
            </div>
          </div>
        </article>
      </div>

      <!-- Loading -->
      <div v-else-if="loading" class="flex justify-center py-20">
        <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-20">
        <span class="text-6xl mb-4 block">📝</span>
        <p class="text-gray-600 dark:text-gray-400">暂无文章</p>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center mt-8 space-x-2">
        <button
          @click="currentPage--"
          :disabled="currentPage === 0"
          class="px-4 py-2 rounded-lg font-medium transition-colors"
          :class="currentPage === 0 ? 'bg-gray-200 dark:bg-gray-700 text-gray-400 cursor-not-allowed' : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
        >
          上一页
        </button>
        <span class="px-4 py-2 text-gray-600 dark:text-gray-400">
          {{ currentPage + 1 }} / {{ totalPages }}
        </span>
        <button
          @click="currentPage++"
          :disabled="currentPage >= totalPages - 1"
          class="px-4 py-2 rounded-lg font-medium transition-colors"
          :class="currentPage >= totalPages - 1 ? 'bg-gray-200 dark:bg-gray-700 text-gray-400 cursor-not-allowed' : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { articleApi, categoryApi, type Article, type Category } from '@/api/article'

const articles = ref<Article[]>([])
const categories = ref<Category[]>([])
const selectedCategory = ref<number | null>(null)
const currentPage = ref(0)
const totalPages = ref(0)
const loading = ref(true)

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

async function fetchArticles() {
  loading.value = true
  try {
    const res = await articleApi.getPublishedArticles(currentPage.value, 9)
    articles.value = res.data.data.content
    totalPages.value = res.data.data.totalPages
  } catch (error) {
    console.error('Failed to fetch articles:', error)
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await categoryApi.getAllCategories()
    categories.value = res.data.data
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

watch(currentPage, fetchArticles)

onMounted(() => {
  fetchArticles()
  fetchCategories()
})
</script>
