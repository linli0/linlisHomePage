<template>
  <div class="min-h-screen">
    <!-- Hero Section -->
    <section class="relative bg-gradient-to-br from-blue-600 via-purple-600 to-pink-500 text-white py-20 overflow-hidden">
      <div class="absolute inset-0 bg-black/20"></div>
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center">
          <h1 class="text-4xl md:text-6xl font-bold mb-6 animate-fade-in">
            CoffeeCookies Homepage
          </h1>
          <p class="text-xl md:text-2xl text-white/90 mb-8 max-w-2xl mx-auto">
            集金价追踪、技术文章、实用工具于一体的现代化全栈应用
          </p>
          <div class="flex flex-wrap justify-center gap-4">
            <router-link to="/gold" class="btn bg-white text-blue-600 hover:bg-gray-100 font-semibold px-8 py-3">
              查看金价
            </router-link>
            <router-link to="/articles" class="btn bg-blue-700/50 text-white border border-white/30 hover:bg-blue-700/70 font-semibold px-8 py-3">
              浏览文章
            </router-link>
          </div>
        </div>
      </div>
      
      <!-- Decorative elements -->
      <div class="absolute -bottom-10 -left-10 w-40 h-40 bg-white/10 rounded-full blur-3xl"></div>
      <div class="absolute -top-10 -right-10 w-60 h-60 bg-pink-500/20 rounded-full blur-3xl"></div>
    </section>

    <!-- Features Section -->
    <section class="py-16 bg-gray-50 dark:bg-gray-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">功能特性</h2>
          <p class="text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            基于 Spring Boot + Vue 3 构建的现代化全栈应用
          </p>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <!-- Gold Price Feature -->
          <div class="card p-6 hover:shadow-lg transition-shadow">
            <div class="w-12 h-12 bg-gold-100 dark:bg-gold-900/30 rounded-lg flex items-center justify-center mb-4">
              <span class="text-2xl">💰</span>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">实时金价</h3>
            <p class="text-gray-600 dark:text-gray-400 mb-4">
              支持多货币实时金价查询，包含价格走势图和历史数据分析。
            </p>
            <router-link to="/gold" class="text-gold-600 dark:text-gold-400 font-medium hover:underline">
              查看详情 →
            </router-link>
          </div>
          
          <!-- Articles Feature -->
          <div class="card p-6 hover:shadow-lg transition-shadow">
            <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center mb-4">
              <span class="text-2xl">📝</span>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">技术文章</h3>
            <p class="text-gray-600 dark:text-gray-400 mb-4">
              分享技术心得、投资理念，支持分类和标签管理。
            </p>
            <router-link to="/articles" class="text-blue-600 dark:text-blue-400 font-medium hover:underline">
              浏览文章 →
            </router-link>
          </div>
          
          <!-- Tools Feature -->
          <div class="card p-6 hover:shadow-lg transition-shadow">
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-lg flex items-center justify-center mb-4">
              <span class="text-2xl">🛠️</span>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">实用工具</h3>
            <p class="text-gray-600 dark:text-gray-400 mb-4">
              JSON格式化、Base64编解码、哈希计算、二维码生成等工具。
            </p>
            <router-link to="/tools" class="text-green-600 dark:text-green-400 font-medium hover:underline">
              使用工具 →
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Gold Price Preview -->
    <section class="py-16 bg-white dark:bg-gray-800">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col md:flex-row items-center justify-between mb-8">
          <div>
            <h2 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">实时金价</h2>
            <p class="text-gray-600 dark:text-gray-400">国际金价实时追踪</p>
          </div>
          <router-link to="/gold" class="mt-4 md:mt-0 text-gold-600 dark:text-gold-400 font-medium hover:underline">
            查看完整数据 →
          </router-link>
        </div>
        
        <div v-if="goldPrice" class="grid grid-cols-1 md:grid-cols-4 gap-6">
          <!-- Current Price Card -->
          <div class="card p-6">
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-1">当前金价</p>
            <p class="text-3xl font-bold text-gray-900 dark:text-white">
              {{ goldPrice.symbol }}{{ formatPrice(goldPrice.price) }}
            </p>
            <p :class="[
              'text-sm mt-2 flex items-center',
              goldPrice.changePercent >= 0 ? 'text-green-600' : 'text-red-600'
            ]">
              <span class="mr-1">{{ goldPrice.changePercent >= 0 ? '↑' : '↓' }}</span>
              {{ Math.abs(goldPrice.changePercent).toFixed(2) }}%
            </p>
          </div>
          
          <!-- High Price -->
          <div class="card p-6">
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-1">最高价</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ goldPrice.symbol }}{{ formatPrice(goldPrice.high) }}
            </p>
          </div>
          
          <!-- Low Price -->
          <div class="card p-6">
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-1">最低价</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ goldPrice.symbol }}{{ formatPrice(goldPrice.low) }}
            </p>
          </div>
          
          <!-- Average Price -->
          <div class="card p-6">
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-1">平均值</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ goldPrice.symbol }}{{ formatPrice(goldPrice.average) }}
            </p>
          </div>
        </div>
        
        <div v-else-if="loading" class="flex justify-center py-12">
          <div class="w-8 h-8 border-4 border-gold-500 border-t-transparent rounded-full animate-spin"></div>
        </div>
      </div>
    </section>

    <!-- Recent Articles -->
    <section class="py-16 bg-gray-50 dark:bg-gray-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col md:flex-row items-center justify-between mb-8">
          <div>
            <h2 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">最新文章</h2>
            <p class="text-gray-600 dark:text-gray-400">分享技术与生活</p>
          </div>
          <router-link to="/articles" class="mt-4 md:mt-0 text-blue-600 dark:text-blue-400 font-medium hover:underline">
            查看全部 →
          </router-link>
        </div>
        
        <div v-if="articles.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <article 
            v-for="article in articles" 
            :key="article.id"
            class="card overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
            @click="$router.push(`/article/${article.id}`)"
          >
            <div class="p-6">
              <div v-if="article.category" class="flex items-center mb-3">
                <span class="text-lg mr-2">{{ article.category.icon }}</span>
                <span class="text-sm text-gray-500 dark:text-gray-400">{{ article.category.name }}</span>
              </div>
              <h3 class="text-xl font-semibold text-gray-900 dark:text-white mb-2 line-clamp-2">
                {{ article.title }}
              </h3>
              <p class="text-gray-600 dark:text-gray-400 text-sm line-clamp-3 mb-4">
                {{ article.summary }}
              </p>
              <div class="flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
                <span>{{ formatDate(article.createdAt) }}</span>
                <span class="flex items-center">
                  <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  {{ article.viewCount }}
                </span>
              </div>
            </div>
          </article>
        </div>
        
        <div v-else-if="loadingArticles" class="flex justify-center py-12">
          <div class="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { goldPriceApi, type GoldPrice } from '@/api/goldPrice'
import { articleApi, type Article } from '@/api/article'

const goldPrice = ref<GoldPrice | null>(null)
const articles = ref<Article[]>([])
const loading = ref(true)
const loadingArticles = ref(true)

function formatPrice(price: number): string {
  return price.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(async () => {
  try {
    const [priceRes, articlesRes] = await Promise.all([
      goldPriceApi.getCurrentPrice('USD'),
      articleApi.getRecentArticles()
    ])
    
    goldPrice.value = priceRes.data.data
    articles.value = articlesRes.data.data
  } catch (error) {
    console.error('Failed to load data:', error)
  } finally {
    loading.value = false
    loadingArticles.value = false
  }
})
</script>
