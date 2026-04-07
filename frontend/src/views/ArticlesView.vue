<template>
  <div class="min-h-screen bg-surface-50 dark:bg-surface-950 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12">
        <span class="inline-block px-4 py-1.5 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 text-sm font-medium mb-4">
          知识分享
        </span>
        <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-4">
          技术<span class="gradient-text">文章</span>
        </h1>
        <p class="text-lg text-surface-600 dark:text-surface-400 max-w-2xl">
          分享技术心得与投资理念，记录成长与思考
        </p>
      </div>

      <div class="flex flex-wrap gap-3 mb-10">
        <button
          @click="selectedCategory = null"
          :class="[
            'px-5 py-2.5 rounded-xl font-medium transition-all duration-200',
            !selectedCategory
              ? 'bg-gradient-to-r from-primary-500 to-primary-600 text-white shadow-lg shadow-primary-500/25'
              : 'bg-white dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-700 border border-surface-200 dark:border-surface-700'
          ]"
        >
          全部文章
        </button>
        <button
          v-for="category in categories"
          :key="category.id"
          @click="selectedCategory = category.id"
          :class="[
            'px-5 py-2.5 rounded-xl font-medium transition-all duration-200 flex items-center gap-2',
            selectedCategory === category.id
              ? 'bg-gradient-to-r from-primary-500 to-primary-600 text-white shadow-lg shadow-primary-500/25'
              : 'bg-white dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-700 border border-surface-200 dark:border-surface-700'
          ]"
        >
          <span>{{ category.icon }}</span>
          <span>{{ category.name }}</span>
        </button>
      </div>

      <div v-if="articles.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <article
          v-for="(article, index) in articles"
          :key="article.id"
          class="group card-hover overflow-hidden cursor-pointer"
          :class="`animation-delay-${(index % 3 + 1) * 100}`"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="h-52 bg-gradient-to-br from-primary-500 via-accent-500 to-gold-500 relative overflow-hidden">
            <div class="absolute inset-0 bg-black/20 group-hover:bg-black/10 transition-colors duration-300"></div>
            <div class="absolute inset-0 bg-hero-pattern opacity-20"></div>
            <div class="absolute bottom-0 left-0 right-0 p-6 bg-gradient-to-t from-black/70 via-black/40 to-transparent">
              <div v-if="article.category" class="flex items-center gap-2">
                <span class="text-2xl drop-shadow-lg">{{ article.category.icon }}</span>
                <span class="text-white/90 text-sm font-medium drop-shadow-lg">{{ article.category.name }}</span>
              </div>
            </div>
            <div class="absolute top-4 right-4">
              <span class="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-white text-xs font-medium">
                {{ formatDate(article.createdAt) }}
              </span>
            </div>
          </div>
          
          <div class="p-6">
            <h2 class="text-xl font-bold text-surface-900 dark:text-white mb-3 line-clamp-2 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
              {{ article.title }}
            </h2>
            <p class="text-surface-600 dark:text-surface-400 text-sm line-clamp-2 mb-5 leading-relaxed">
              {{ article.summary }}
            </p>
            
            <div class="flex items-center justify-between text-sm text-surface-500 dark:text-surface-400">
              <div class="flex items-center gap-2">
                <div v-if="article.author" class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center text-white text-xs font-medium shadow-lg">
                    {{ article.author.displayName?.[0] || article.author.username?.[0] || 'U' }}
                  </div>
                  <span class="font-medium">{{ article.author.displayName || article.author.username }}</span>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <span class="flex items-center gap-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  {{ article.viewCount }}
                </span>
              </div>
            </div>
            
            <div v-if="article.tags && article.tags.length > 0" class="flex flex-wrap gap-2 mt-5 pt-5 border-t border-surface-100 dark:border-surface-800">
              <span
                v-for="tag in article.tags.slice(0, 3)"
                :key="tag.id"
                class="px-2.5 py-1 text-xs rounded-lg font-medium"
                :style="{ backgroundColor: tag.color + '15', color: tag.color }"
              >
                {{ tag.name }}
              </span>
              <span v-if="article.tags.length > 3" class="px-2.5 py-1 text-xs text-surface-400 dark:text-surface-500 bg-surface-100 dark:bg-surface-800 rounded-lg">
                +{{ article.tags.length - 3 }}
              </span>
            </div>
          </div>
        </article>
      </div>

      <div v-else-if="loading" class="flex justify-center py-20">
        <div class="w-12 h-12 border-4 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <div v-else class="text-center py-20">
        <div class="w-24 h-24 mx-auto mb-6 bg-surface-100 dark:bg-surface-800 rounded-2xl flex items-center justify-center">
          <span class="text-5xl">📝</span>
        </div>
        <p class="text-surface-600 dark:text-surface-400 text-lg mb-2">暂无文章</p>
        <p class="text-surface-500 dark:text-surface-500 text-sm">该分类下还没有发布任何文章</p>
      </div>

      <div v-if="totalPages > 1" class="flex justify-center items-center gap-4 mt-12">
        <button
          @click="currentPage--"
          :disabled="currentPage === 0"
          class="px-5 py-2.5 rounded-xl font-medium transition-all duration-200 flex items-center gap-2"
          :class="currentPage === 0 ? 'bg-surface-100 dark:bg-surface-800 text-surface-400 cursor-not-allowed' : 'bg-white dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-700 border border-surface-200 dark:border-surface-700'"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          上一页
        </button>
        <div class="flex items-center gap-2">
          <span class="px-4 py-2 bg-primary-500 text-white rounded-xl font-medium">
            {{ currentPage + 1 }}
          </span>
          <span class="text-surface-400">/</span>
          <span class="px-4 py-2 text-surface-600 dark:text-surface-400">
            {{ totalPages }}
          </span>
        </div>
        <button
          @click="currentPage++"
          :disabled="currentPage >= totalPages - 1"
          class="px-5 py-2.5 rounded-xl font-medium transition-all duration-200 flex items-center gap-2"
          :class="currentPage >= totalPages - 1 ? 'bg-surface-100 dark:bg-surface-800 text-surface-400 cursor-not-allowed' : 'bg-white dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-700 border border-surface-200 dark:border-surface-700'"
        >
          下一页
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { articleApi, categoryApi, type Article, type Category } from '@/api/article'
import { formatDate } from '@/utils/format'

const articles = ref<Article[]>([])
const categories = ref<Category[]>([])
const selectedCategory = ref<number | null>(null)
const currentPage = ref(0)
const totalPages = ref(0)
const loading = ref(true)

async function fetchArticles() {
  loading.value = true
  try {
    const res = await articleApi.getPublishedArticles(currentPage.value, 9, selectedCategory.value || undefined)
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
watch(selectedCategory, () => {
  currentPage.value = 0
  fetchArticles()
})

onMounted(() => {
  fetchArticles()
  fetchCategories()
})
</script>
