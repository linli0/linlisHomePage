<template>
  <div class="page-wrap">
    <h1 class="page-title">Wiki</h1>
    <p class="page-desc mb-8">技术文档与知识库</p>

    <div v-if="loading" class="text-ink-400 py-16 text-center">加载中…</div>
    <div v-else-if="articles.length === 0" class="card p-16 text-center text-ink-400">暂无文章</div>
    <div v-else class="grid md:grid-cols-2 lg:grid-cols-3 gap-5">
      <article
        v-for="a in articles"
        :key="a.id"
        class="card p-6 cursor-pointer hover:border-brand-300 hover:shadow-md transition-all"
        @click="$router.push(`/article/${a.id}`)"
      >
        <p v-if="a.category" class="text-xs font-medium text-brand-600 mb-2">{{ a.category.name }}</p>
        <h2 class="text-lg font-bold text-ink-900 dark:text-white mb-2 line-clamp-2">{{ a.title }}</h2>
        <p class="text-sm text-ink-500 line-clamp-3 mb-4">{{ a.summary }}</p>
        <p class="text-xs text-ink-400">{{ formatDate(a.createdAt) }} · {{ a.viewCount }} 阅读</p>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi, type Article } from '@/api/article'
import { formatDate } from '@/utils/format'

const articles = ref<Article[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await articleApi.getPublishedArticles(0, 20)
    const data = res.data.data
    articles.value = Array.isArray(data) ? data : (data?.content ?? [])
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>
