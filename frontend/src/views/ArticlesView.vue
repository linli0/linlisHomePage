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

      <!-- 创建文章按钮 (管理员可见) -->
      <div v-if="authStore.isAdmin" class="mb-8">
        <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          创建新文章
        </button>
      </div>

      <!-- 创建文章模态框 -->
      <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
        <div class="bg-white dark:bg-surface-900 rounded-2xl shadow-soft-lg max-w-2xl w-full mx-4 p-8 max-h-[90vh] overflow-y-auto">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-surface-900 dark:text-white">创建新文章</h2>
            <button @click="showCreateModal = false" class="p-2 rounded-xl hover:bg-surface-100 dark:hover:bg-surface-800 transition-colors">
              <svg class="w-6 h-6 text-surface-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          
          <form @submit.prevent="createArticle" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">标题 *</label>
              <input v-model="newArticle.title" type="text" required class="input" placeholder="文章标题" />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">摘要</label>
              <textarea v-model="newArticle.summary" rows="2" class="input resize-none" placeholder="简短摘要..."></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">内容 *</label>
              <textarea v-model="newArticle.content" rows="10" required class="input resize-none font-mono" placeholder="文章内容 (支持 Markdown)..."></textarea>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">分类</label>
                <select v-model="newArticle.categoryId" class="input">
                  <option value="">无分类</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">封面图 URL</label>
                <input v-model="newArticle.coverImage" type="url" class="input" placeholder="https://..." />
              </div>
            </div>
            
            <div class="flex items-center gap-3">
              <label class="flex items-center gap-2 cursor-pointer">
                <input v-model="newArticle.published" type="checkbox" class="w-4 h-4 rounded border-surface-300 text-primary-500 focus:ring-primary-500 dark:border-surface-600 dark:bg-surface-800" />
                <span class="text-sm text-surface-700 dark:text-surface-300">立即发布</span>
              </label>
            </div>
            
            <div class="flex gap-3 pt-4">
              <button type="submit" :disabled="creating" class="btn-primary flex-1">
                <span v-if="creating" class="flex items-center justify-center gap-2">
                  <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  创建中...
                </span>
                <span v-else>创建文章</span>
              </button>
              <button type="button" @click="showCreateModal = false" class="btn-secondary flex-1">取消</button>
            </div>
            
            <div v-if="createError" class="p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/50 text-red-600 dark:text-red-400 rounded-xl text-sm">
              {{ createError }}
            </div>
          </form>
        </div>
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
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const articles = ref<Article[]>([])
const categories = ref<Category[]>([])
const selectedCategory = ref<number | null>(null)
const currentPage = ref(0)
const totalPages = ref(0)
const loading = ref(true)

// 创建文章相关
const showCreateModal = ref(false)
const creating = ref(false)
const createError = ref('')
const newArticle = ref({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  published: true,
  categoryId: null as number | null
})

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

async function createArticle() {
  if (!newArticle.value.title.trim() || !newArticle.value.content.trim()) {
    createError.value = '标题和内容不能为空'
    return
  }
  
  creating.value = true
  createError.value = ''
  
  try {
    await articleApi.createArticle({
      title: newArticle.value.title.trim(),
      content: newArticle.value.content.trim(),
      summary: newArticle.value.summary.trim() || newArticle.value.content.trim().slice(0, 200),
      coverImage: newArticle.value.coverImage.trim() || '',
      published: newArticle.value.published,
      category: newArticle.value.categoryId ? { id: newArticle.value.categoryId } as Category : undefined
    })
    
    // 重置表单
    newArticle.value = {
      title: '',
      content: '',
      summary: '',
      coverImage: '',
      published: true,
      categoryId: null
    }
    showCreateModal.value = false
    
    // 刷新文章列表
    await fetchArticles()
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    createError.value = e.response?.data?.message || '创建文章失败'
  } finally {
    creating.value = false
  }
}
</script>
