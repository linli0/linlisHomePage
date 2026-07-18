<template>
  <div class="page-wrap">
    <h1 class="page-title">推特监控</h1>
    <p class="page-desc mb-8">查看已采集的推文与统计</p>

    <div class="grid sm:grid-cols-3 gap-4 mb-8">
      <div class="card p-4">
        <p class="text-xs text-ink-400">总数</p>
        <p class="text-2xl font-bold">{{ stats?.totalTweets ?? '--' }}</p>
      </div>
      <div class="card p-4">
        <p class="text-xs text-ink-400">今日</p>
        <p class="text-2xl font-bold">{{ stats?.tweetsToday ?? '--' }}</p>
      </div>
      <div class="card p-4">
        <p class="text-xs text-ink-400">本周</p>
        <p class="text-2xl font-bold">{{ stats?.tweetsThisWeek ?? '--' }}</p>
      </div>
    </div>

    <form class="card p-4 mb-6 flex flex-col sm:flex-row gap-3" @submit.prevent="doSearch">
      <input v-model="keyword" class="input flex-1" placeholder="关键词搜索…" />
      <button type="submit" class="btn-primary" :disabled="loading">搜索</button>
      <button type="button" class="btn-outline" :disabled="loading" @click="loadLatest">最新</button>
    </form>

    <div v-if="loading" class="text-ink-400 text-center py-12">加载中…</div>
    <div v-else-if="loadError" class="card p-10 text-center space-y-3">
      <p class="text-ink-600">{{ loadError }}</p>
      <button type="button" class="btn-primary" @click="loadLatest">重试</button>
    </div>
    <div v-else-if="tweets.length === 0" class="card p-12 text-center space-y-2">
      <p class="text-ink-600 font-medium">暂无推文</p>
      <p class="text-sm text-ink-400">未配置采集源时列表为空；可在设置页填写关键词后，由后端轮询抓取。</p>
    </div>
    <div v-else class="space-y-4">
      <TweetCard v-for="t in tweets" :key="t.id" :tweet="t" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { tweetsApi } from '@/api/tweets'
import type { TweetDTO, TweetStatsDTO } from '@/types/tweet'
import TweetCard from '@/components/TweetCard.vue'

const tweets = ref<TweetDTO[]>([])
const stats = ref<TweetStatsDTO | null>(null)
const keyword = ref('')
const loading = ref(false)
const loadError = ref('')

async function loadLatest() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await tweetsApi.getLatest({ limit: 30 })
    tweets.value = res.data.data ?? []
  } catch (e) {
    console.error(e)
    tweets.value = []
    loadError.value = '推文加载失败，请确认 API 已启动。'
  } finally {
    loading.value = false
  }
}

async function doSearch() {
  if (!keyword.value.trim()) {
    await loadLatest()
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    const res = await tweetsApi.search({ keyword: keyword.value.trim(), limit: 30 })
    tweets.value = res.data.data ?? []
  } catch (e) {
    console.error(e)
    loadError.value = '搜索失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const s = await tweetsApi.getStats()
    stats.value = s.data.data
  } catch {
    /* optional */
  }
  await loadLatest()
})
</script>
