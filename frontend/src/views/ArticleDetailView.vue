<template>
  <div class="page-wrap max-w-3xl">
    <div v-if="loading" class="text-ink-400 py-16 text-center">加载中…</div>
    <div v-else-if="!article" class="card p-12 text-center text-ink-400">文章不存在</div>
    <article v-else class="card p-8 md:p-10">
      <p v-if="article.category" class="text-sm font-medium text-brand-600 mb-2">{{ article.category.name }}</p>
      <h1 class="text-3xl font-bold text-ink-900 dark:text-white mb-3">{{ article.title }}</h1>
      <p class="text-sm text-ink-400 mb-8">
        {{ formatDate(article.createdAt) }}
        <span v-if="article.author"> · {{ article.author.displayName || article.author.username }}</span>
      </p>
      <div class="prose-article text-ink-700 dark:text-ink-200 leading-relaxed whitespace-pre-wrap" v-html="html" />
      <div class="mt-10 pt-6 border-t border-ink-100">
        <router-link to="/articles" class="text-sm font-semibold text-brand-600">← 返回 Wiki</router-link>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { articleApi, type Article } from '@/api/article'
import { formatDate } from '@/utils/format'

const route = useRoute()
const article = ref<Article | null>(null)
const loading = ref(true)

const html = computed(() => {
  if (!article.value?.content) return ''
  return DOMPurify.sanitize(marked.parse(article.value.content, { async: false }) as string)
})

onMounted(async () => {
  try {
    const res = await articleApi.getArticleById(route.params.id as string)
    article.value = res.data.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>
