<template>
  <div class="min-h-screen bg-surface-50 dark:bg-surface-950 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex flex-col md:flex-row md:items-center justify-between mb-10">
        <div>
          <span class="inline-block px-4 py-1.5 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 text-sm font-medium mb-4">
            实时监控
          </span>
          <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-2">
            Tweet<span class="text-primary-500">监控</span>
          </h1>
          <p class="text-surface-600 dark:text-surface-400">
            监控实时动态
          </p>
        </div>
        <div class="mt-6 md:mt-0 flex items-center gap-3">
          <span 
            :class="[
              'inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium',
              isConnected 
                ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300'
                : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'
            ]"
          >
            <span class="w-2 h-2 rounded-full" :class="isConnected ? 'bg-green-500' : 'bg-red-500'"></span>
            {{ isConnected ? '已连接' : '未连接' }}
          </span>
        </div>
      </div>

      <!-- Search and Filter Section -->
      <div class="card p-6 mb-8">
        <h2 class="text-lg font-semibold text-surface-900 dark:text-white mb-4">搜索与筛选</h2>
        <div class="flex flex-wrap gap-3">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索关键词..."
            class="input flex-1 min-w-[200px]"
            @input="handleSearch"
          />
          <select
            v-model="searchPlatform"
            class="input min-w-[150px]"
            @change="handleSearch"
          >
            <option value="">所有平台</option>
            <option value="twitter">Twitter</option>
            <option value="truth-social">Truth Social</option>
          </select>
          <input
            v-model="searchUsername"
            type="text"
            placeholder="用户名..."
            class="input min-w-[150px]"
            @input="handleSearch"
          />
          <input
            v-model="searchStartDate"
            type="date"
            class="input min-w-[150px]"
            @change="handleSearch"
          />
          <input
            v-model="searchEndDate"
            type="date"
            class="input min-w-[150px]"
            @change="handleSearch"
          />
        </div>
      </div>

      <!-- Metrics Section -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <div class="card-hover p-6 text-center">
          <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-primary-400 to-primary-600 rounded-xl flex items-center justify-center shadow-lg">
            <span class="text-white text-xl">📊</span>
          </div>
          <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">总推文数</p>
          <p class="text-2xl font-bold text-surface-900 dark:text-white">
            {{ stats?.totalTweets || 0 }}
          </p>
        </div>

        <div class="card-hover p-6 text-center">
          <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-gold-400 to-gold-600 rounded-xl flex items-center justify-center shadow-lg">
            <span class="text-white text-xl">👁</span>
          </div>
          <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">今日推文</p>
          <p class="text-2xl font-bold text-surface-900 dark:text-white">
            {{ stats?.tweetsToday || 0 }}
          </p>
        </div>

        <div class="card-hover p-6 text-center">
          <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-accent-400 to-accent-600 rounded-xl flex items-center justify-center shadow-lg">
            <span class="text-white text-xl">❤️</span>
          </div>
          <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">平均互动</p>
          <p class="text-2xl font-bold text-surface-900 dark:text-white">
            {{ stats?.averageEngagement?.toFixed(1) || 0 }}
          </p>
        </div>

        <div class="card-hover p-6 text-center">
          <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-green-400 to-green-600 rounded-xl flex items-center justify-center shadow-lg">
            <span class="text-white text-xl">🔄</span>
          </div>
          <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">本周推文</p>
          <p class="text-2xl font-bold text-surface-900 dark:text-white">
            {{ stats?.tweetsThisWeek || 0 }}
          </p>
        </div>
      </div>

      <!-- Tweet List Section -->
      <div class="card p-8">
        <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-surface-900 dark:text-white">推文列表</h2>
          <div class="flex gap-2 mt-4 md:mt-0">
            <button
              @click="loadLatestTweets"
              :class="[
                'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200',
                currentFilter === 'all'
                  ? 'bg-primary-500 text-white shadow-lg shadow-primary-500/25'
                  : 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700'
              ]"
            >
              全部
            </button>
            <button
              @click="loadLatestTweets"
              :class="[
                'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200',
                currentFilter === 'recent'
                  ? 'bg-primary-500 text-white shadow-lg shadow-primary-500/25'
                  : 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700'
              ]"
            >
              最近
            </button>
            <button
              @click="loadLatestTweets"
              :class="[
                'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200',
                currentFilter === 'popular'
                  ? 'bg-primary-500 text-white shadow-lg shadow-primary-500/25'
                  : 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700'
              ]"
            >
              热门
            </button>
          </div>
        </div>

        <!-- 推文列表渲染 -->
        <div v-if="tweets.length > 0" class="space-y-4">
          <div
            v-for="tweet in tweets"
            :key="tweet.id"
            class="p-6 rounded-xl bg-surface-50 dark:bg-surface-800/50 border border-surface-200 dark:border-surface-700 hover:border-primary-300 dark:hover:border-primary-600 transition-all duration-200"
          >
            <div class="flex items-start gap-4">
              <div class="flex-shrink-0">
                <div class="w-12 h-12 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-white font-bold text-lg">
                  {{ tweet.displayName.charAt(0).toUpperCase() }}
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-2">
                  <span class="font-semibold text-surface-900 dark:text-white">
                    {{ tweet.displayName }}
                  </span>
                  <span class="text-surface-500 dark:text-surface-400">@{{ tweet.username }}</span>
                  <span
                    :class="[
                      'px-2 py-0.5 rounded-full text-xs font-medium',
                      tweet.platform === 'twitter'
                        ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300'
                        : 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300'
                    ]"
                  >
                    {{ tweet.platform === 'twitter' ? 'Twitter' : 'Truth Social' }}
                  </span>
                  <span class="text-surface-400 dark:text-surface-500 text-sm">
                    {{ new Date(tweet.createdAt).toLocaleString('zh-CN') }}
                  </span>
                </div>
                <p class="text-surface-700 dark:text-surface-300 mb-3 whitespace-pre-wrap">
                  {{ tweet.content }}
                </p>
                <div v-if="tweet.hashtags.length > 0" class="flex flex-wrap gap-2 mb-3">
                  <span
                    v-for="tag in tweet.hashtags"
                    :key="tag"
                    class="px-2 py-1 rounded-md bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300 text-sm"
                  >
                    #{{ tag }}
                  </span>
                </div>
                <div class="flex items-center gap-4 text-sm text-surface-500 dark:text-surface-400">
                  <span class="flex items-center gap-1">
                    ❤️ {{ tweet.likesCount }}
                  </span>
                  <span class="flex items-center gap-1">
                    🔄 {{ tweet.retweetsCount }}
                  </span>
                  <span class="flex items-center gap-1">
                    💬 {{ tweet.repliesCount }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 text-surface-500 dark:text-surface-400">
          暂无推文数据
        </div>
      </div>

      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="w-16 h-16 border-4 border-primary-500 border-t-transparent rounded-full animate-spin mb-4"></div>
        <p class="text-surface-600 dark:text-surface-400">加载中...</p>
      </div>

      <div v-if="error" class="flex flex-col items-center justify-center py-20">
        <div class="w-16 h-16 bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center mb-4">
          <span class="text-2xl">⚠️</span>
        </div>
        <p class="text-red-600 dark:text-red-400 mb-2">{{ error }}</p>
        <button @click="retryConnection" class="btn-primary">
          重试
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { tweetsApi } from '@/api/tweets'
import type { TweetDTO, TweetStatsDTO } from '@/types/tweet'

// 状态
const loading = ref(false)
const error = ref('')
const isConnected = ref(false)
const currentFilter = ref('all')

// 搜索状态
const searchKeyword = ref('')
const searchPlatform = ref('')
const searchUsername = ref('')
const searchStartDate = ref('')
const searchEndDate = ref('')

// 数据
const tweets = ref<TweetDTO[]>([])
const stats = ref<TweetStatsDTO | null>(null)

// 防抖定时器
let searchTimeout: number | null = null

// 搜索处理函数（带防抖）
function handleSearch() {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  searchTimeout = window.setTimeout(() => {
    performSearch()
  }, 300)
}

// 执行搜索
async function performSearch() {
  loading.value = true
  error.value = ''

  try {
    const searchRequest = {
      keyword: searchKeyword.value || undefined,
      platform: searchPlatform.value || undefined,
      username: searchUsername.value || undefined,
      startDate: searchStartDate.value || undefined,
      endDate: searchEndDate.value || undefined,
      limit: 50
    }

    const response = await tweetsApi.search(searchRequest)
    tweets.value = response.data
  } catch (err: any) {
    error.value = err.response?.data?.message || '搜索失败'
    console.error('[TweetsView] 搜索失败:', err)
  } finally {
    loading.value = false
  }
}

// 加载统计数据
async function loadStats() {
  try {
    const response = await tweetsApi.getStats()
    stats.value = response.data
  } catch (err: any) {
    console.error('[TweetsView] 加载统计数据失败:', err)
  }
}

// 加载最新推文
async function loadLatestTweets() {
  loading.value = true
  error.value = ''

  try {
    const response = await tweetsApi.getLatest({ limit: 50 })
    tweets.value = response.data
  } catch (err: any) {
    error.value = err.response?.data?.message || '加载推文失败'
    console.error('[TweetsView] 加载推文失败:', err)
  } finally {
    loading.value = false
  }
}

function retryConnection() {
  error.value = ''
  loadLatestTweets()
}

onMounted(() => {
  console.log('[TweetsView] 组件已挂载')
  loadLatestTweets()
  loadStats()
})

onUnmounted(() => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
})
</script>