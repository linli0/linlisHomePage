<template>
  <div v-if="article" class="min-h-screen bg-surface-50 dark:bg-surface-950 py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <button
        @click="$router.back()"
        class="mb-8 inline-flex items-center gap-2 text-surface-600 dark:text-surface-400 hover:text-primary-600 dark:hover:text-primary-400 transition-colors group"
      >
        <svg class="w-5 h-5 group-hover:-translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span class="font-medium">返回文章列表</span>
      </button>

      <div class="card p-8 mb-8">
        <div class="flex flex-wrap items-center gap-3 mb-6">
          <span v-if="article.category" class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 text-sm font-medium">
            <span class="text-lg">{{ article.category.icon }}</span>
            <span>{{ article.category.name }}</span>
          </span>
          <span class="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-400 text-sm">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            {{ formatDate(article.createdAt) }}
          </span>
          <span class="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-400 text-sm">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            {{ article.viewCount }} 阅读
          </span>
        </div>

        <h1 class="text-3xl md:text-4xl lg:text-5xl font-bold text-surface-900 dark:text-white mb-8 leading-tight">
          {{ article.title }}
        </h1>

        <div v-if="article.author" class="flex items-center gap-4 p-4 rounded-xl bg-surface-50 dark:bg-surface-800/50">
          <div class="relative">
            <div class="absolute inset-0 bg-primary-400/20 rounded-xl blur-lg"></div>
            <div class="relative w-12 h-12 rounded-xl bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center text-white font-bold text-lg shadow-lg">
              {{ article.author.displayName?.[0] || article.author.username?.[0] || 'U' }}
            </div>
          </div>
          <div>
            <p class="font-semibold text-surface-900 dark:text-white">{{ article.author.displayName || article.author.username }}</p>
            <p class="text-sm text-surface-500 dark:text-surface-400">作者</p>
          </div>
        </div>

        <div v-if="article.tags && article.tags.length > 0" class="flex flex-wrap gap-2 mt-6 pt-6 border-t border-surface-200 dark:border-surface-800">
          <span
            v-for="tag in article.tags"
            :key="tag.id"
            class="px-4 py-1.5 text-sm font-medium rounded-lg transition-all hover:scale-105"
            :style="{ backgroundColor: tag.color + '15', color: tag.color, borderColor: tag.color + '30' }"
          >
            # {{ tag.name }}
          </span>
        </div>
      </div>

      <div class="card p-8 lg:p-12">
        <div class="prose dark:prose-invert max-w-none article-content" v-html="renderedContent"></div>
      </div>

      <div class="mt-8 text-center">
        <p class="text-sm text-surface-500 dark:text-surface-400">
          最后更新于 {{ formatDate(article.updatedAt) }}
        </p>
      </div>
    </div>
  </div>

  <div v-else class="min-h-screen flex items-center justify-center bg-surface-50 dark:bg-surface-950">
    <div class="text-center">
      <div class="relative inline-block mb-6">
        <div class="absolute inset-0 bg-primary-400/20 rounded-2xl blur-xl animate-pulse-slow"></div>
        <div class="relative w-16 h-16 border-4 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
      <p class="text-surface-600 dark:text-surface-400 font-medium">加载中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { articleApi, type Article } from '@/api/article'
import { formatDate } from '@/utils/format'

marked.setOptions({
  breaks: true,
  gfm: true
})

const route = useRoute()
const article = ref<Article | null>(null)

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  const rawHtml = marked.parse(article.value.content) as string
  return DOMPurify.sanitize(rawHtml)
})

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

<style>
.article-content h1 { 
  font-size: 1.875rem; 
  font-weight: 700; 
  margin-bottom: 1rem; 
  margin-top: 2rem; 
  color: #0f172a;
}
.article-content h2 { 
  font-size: 1.5rem; 
  font-weight: 700; 
  margin-bottom: 0.75rem; 
  margin-top: 1.5rem; 
  color: #0f172a;
}
.article-content h3 { 
  font-size: 1.25rem; 
  font-weight: 600; 
  margin-bottom: 0.5rem; 
  margin-top: 1rem; 
  color: #0f172a;
}
.article-content p { 
  margin-bottom: 1rem; 
  line-height: 1.8; 
  color: #334155;
}
.article-content ul, .article-content ol { 
  margin-bottom: 1rem; 
  padding-left: 1.5rem; 
}
.article-content li { 
  margin-bottom: 0.5rem; 
  line-height: 1.7;
}
.article-content code { 
  background-color: rgba(59, 130, 246, 0.1); 
  padding: 0.15rem 0.5rem; 
  border-radius: 0.375rem; 
  font-size: 0.875rem; 
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: #3b82f6;
}
.article-content pre { 
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  color: #e2e8f0; 
  padding: 1.25rem; 
  border-radius: 0.75rem; 
  overflow-x: auto; 
  margin-bottom: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.article-content pre code { 
  background: none; 
  padding: 0; 
  color: inherit; 
  font-size: 0.875rem; 
}
.article-content blockquote { 
  border-left: 4px solid #3b82f6; 
  padding: 0.75rem 1rem; 
  margin-bottom: 1rem; 
  background-color: rgba(59, 130, 246, 0.05);
  border-radius: 0 0.5rem 0.5rem 0;
  color: #475569;
  font-style: italic;
}
.article-content a { 
  color: #3b82f6; 
  text-decoration: underline; 
  text-underline-offset: 2px;
}
.article-content a:hover { 
  color: #2563eb; 
}
.article-content strong { 
  font-weight: 600; 
  color: #0f172a;
}
.article-content em { 
  font-style: italic; 
}
.article-content img {
  max-width: 100%;
  border-radius: 0.75rem;
  margin: 1.5rem 0;
}
.article-content table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}
.article-content th, .article-content td {
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  text-align: left;
}
.article-content th {
  background-color: #f8fafc;
  font-weight: 600;
}

.dark .article-content h1,
.dark .article-content h2,
.dark .article-content h3,
.dark .article-content strong { 
  color: #f1f5f9;
}
.dark .article-content p { 
  color: #cbd5e1;
}
.dark .article-content code { 
  background-color: rgba(96, 165, 250, 0.15); 
  color: #60a5fa;
}
.dark .article-content blockquote {
  background-color: rgba(59, 130, 246, 0.1);
  color: #94a3b8;
}
.dark .article-content th, .dark .article-content td {
  border-color: #334155;
}
.dark .article-content th {
  background-color: #1e293b;
}
</style>
