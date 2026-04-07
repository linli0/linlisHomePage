<template>
  <div class="min-h-screen">
    <section class="relative min-h-[90vh] flex items-center justify-center overflow-hidden">
      <div class="absolute inset-0 hero-gradient-animated"></div>
      <div class="absolute inset-0 bg-hero-pattern opacity-30"></div>
      <div class="absolute inset-0 bg-gradient-to-b from-transparent via-transparent to-surface-50 dark:to-surface-950"></div>
      
      <div class="absolute top-20 left-10 w-72 h-72 bg-white/10 rounded-full blur-3xl animate-float"></div>
      <div class="absolute bottom-20 right-10 w-96 h-96 bg-accent-500/20 rounded-full blur-3xl animate-float-slow"></div>
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gold-500/10 rounded-full blur-3xl animate-pulse-slow"></div>
      
      <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div class="animate-fade-in">
          <div class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/10 backdrop-blur-sm border border-white/20 text-white/90 text-sm mb-8">
            <span class="relative flex h-2 w-2">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
            </span>
            全新设计 · 现代化体验
          </div>
          
          <h1 class="text-5xl md:text-7xl lg:text-8xl font-bold text-white mb-6 tracking-tight">
            <span class="block">Coffee</span>
            <span class="block bg-clip-text text-transparent bg-gradient-to-r from-white via-gold-200 to-white">Cookies</span>
          </h1>
          
          <p class="text-xl md:text-2xl text-white/80 mb-12 max-w-2xl mx-auto leading-relaxed">
            集金价追踪、技术文章、实用工具于一体的<br class="hidden sm:block" />
            <span class="font-semibold text-white">现代化全栈应用</span>
          </p>
          
          <div class="flex flex-col sm:flex-row flex-wrap justify-center gap-4">
            <router-link 
              to="/gold" 
              class="group relative px-8 py-4 bg-white text-surface-900 rounded-2xl font-semibold text-lg overflow-hidden transition-all duration-300 hover:shadow-2xl hover:-translate-y-1"
            >
              <span class="relative z-10 flex items-center justify-center gap-2">
                <span class="text-2xl">💰</span>
                查看实时金价
              </span>
              <div class="absolute inset-0 bg-gradient-to-r from-gold-400 to-gold-500 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
              <span class="absolute inset-0 flex items-center justify-center gap-2 text-white opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                <span class="text-2xl">💰</span>
                查看实时金价
              </span>
            </router-link>
            
            <router-link 
              to="/ai" 
              class="group px-8 py-4 bg-white/10 backdrop-blur-sm border border-white/30 text-white rounded-2xl font-semibold text-lg transition-all duration-300 hover:bg-white/20 hover:border-white/50 hover:-translate-y-1"
            >
              <span class="flex items-center justify-center gap-2">
                <span class="text-2xl">🤖</span>
                AI 智能对话
              </span>
            </router-link>
            
            <router-link 
              to="/articles" 
              class="group px-8 py-4 bg-white/10 backdrop-blur-sm border border-white/30 text-white rounded-2xl font-semibold text-lg transition-all duration-300 hover:bg-white/20 hover:border-white/50 hover:-translate-y-1"
            >
              <span class="flex items-center justify-center gap-2">
                <span class="text-2xl">📝</span>
                浏览技术文章
              </span>
            </router-link>
          </div>
        </div>
        
        <div class="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce-subtle">
          <svg class="w-6 h-6 text-white/50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
          </svg>
        </div>
      </div>
    </section>

    <section class="py-24 bg-surface-50 dark:bg-surface-950 relative overflow-hidden">
      <div class="absolute inset-0 dot-pattern opacity-50"></div>
      
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <span class="inline-block px-4 py-1.5 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 text-sm font-medium mb-4">
            核心功能
          </span>
          <h2 class="section-title">
            一站式<span class="gradient-text">多功能</span>平台
          </h2>
          <p class="section-subtitle">
            基于 Express + Vue 3 构建的现代化全栈应用，为您提供丰富的实用功能
          </p>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <router-link 
            to="/gold" 
            class="group card-hover p-8 text-center"
          >
            <div class="relative mb-6 inline-flex">
              <div class="absolute inset-0 bg-gold-400/20 rounded-2xl blur-xl group-hover:bg-gold-400/30 transition-colors"></div>
              <div class="relative w-16 h-16 bg-gradient-to-br from-gold-400 to-gold-600 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
                <span class="text-3xl">💰</span>
              </div>
            </div>
            <h3 class="text-xl font-bold text-surface-900 dark:text-white mb-3 group-hover:text-gold-600 dark:group-hover:text-gold-400 transition-colors">
              实时金价
            </h3>
            <p class="text-surface-600 dark:text-surface-400 text-sm leading-relaxed">
              支持多货币实时金价查询，包含价格走势图和历史数据分析
            </p>
            <div class="mt-4 flex items-center justify-center text-gold-600 dark:text-gold-400 font-medium text-sm opacity-0 group-hover:opacity-100 transition-opacity">
              <span>立即查看</span>
              <svg class="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </router-link>
          
          <router-link 
            to="/articles" 
            class="group card-hover p-8 text-center"
          >
            <div class="relative mb-6 inline-flex">
              <div class="absolute inset-0 bg-primary-400/20 rounded-2xl blur-xl group-hover:bg-primary-400/30 transition-colors"></div>
              <div class="relative w-16 h-16 bg-gradient-to-br from-primary-400 to-primary-600 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
                <span class="text-3xl">📝</span>
              </div>
            </div>
            <h3 class="text-xl font-bold text-surface-900 dark:text-white mb-3 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
              技术文章
            </h3>
            <p class="text-surface-600 dark:text-surface-400 text-sm leading-relaxed">
              分享技术心得、投资理念，支持分类和标签管理
            </p>
            <div class="mt-4 flex items-center justify-center text-primary-600 dark:text-primary-400 font-medium text-sm opacity-0 group-hover:opacity-100 transition-opacity">
              <span>浏览文章</span>
              <svg class="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </router-link>
          
          <router-link 
            to="/tools" 
            class="group card-hover p-8 text-center"
          >
            <div class="relative mb-6 inline-flex">
              <div class="absolute inset-0 bg-green-400/20 rounded-2xl blur-xl group-hover:bg-green-400/30 transition-colors"></div>
              <div class="relative w-16 h-16 bg-gradient-to-br from-green-400 to-green-600 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
                <span class="text-3xl">🛠️</span>
              </div>
            </div>
            <h3 class="text-xl font-bold text-surface-900 dark:text-white mb-3 group-hover:text-green-600 dark:group-hover:text-green-400 transition-colors">
              实用工具
            </h3>
            <p class="text-surface-600 dark:text-surface-400 text-sm leading-relaxed">
              JSON格式化、Base64编解码、哈希计算、二维码生成等工具
            </p>
            <div class="mt-4 flex items-center justify-center text-green-600 dark:text-green-400 font-medium text-sm opacity-0 group-hover:opacity-100 transition-opacity">
              <span>使用工具</span>
              <svg class="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </router-link>
          
          <router-link 
            to="/ai" 
            class="group card-hover p-8 text-center"
          >
            <div class="relative mb-6 inline-flex">
              <div class="absolute inset-0 bg-accent-400/20 rounded-2xl blur-xl group-hover:bg-accent-400/30 transition-colors"></div>
              <div class="relative w-16 h-16 bg-gradient-to-br from-accent-400 to-accent-600 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
                <span class="text-3xl">🤖</span>
              </div>
            </div>
            <h3 class="text-xl font-bold text-surface-900 dark:text-white mb-3 group-hover:text-accent-600 dark:group-hover:text-accent-400 transition-colors">
              AI 对话
            </h3>
            <p class="text-surface-600 dark:text-surface-400 text-sm leading-relaxed">
              与本地 Ollama 模型实时对话，支持多种本地模型
            </p>
            <div class="mt-4 flex items-center justify-center text-accent-600 dark:text-accent-400 font-medium text-sm opacity-0 group-hover:opacity-100 transition-opacity">
              <span>开始对话</span>
              <svg class="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </router-link>
        </div>
      </div>
    </section>

    <section class="py-24 bg-white dark:bg-surface-900 relative overflow-hidden">
      <div class="absolute top-0 right-0 w-1/2 h-full bg-gradient-to-l from-gold-50 to-transparent dark:from-gold-950/20 dark:to-transparent"></div>
      
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col lg:flex-row items-center gap-12">
          <div class="flex-1 text-center lg:text-left">
            <span class="inline-block px-4 py-1.5 rounded-full bg-gold-100 dark:bg-gold-900/30 text-gold-600 dark:text-gold-400 text-sm font-medium mb-4">
              实时追踪
            </span>
            <h2 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-6">
              国际<span class="text-gold-500">金价</span>动态
            </h2>
            <p class="text-lg text-surface-600 dark:text-surface-400 mb-8 max-w-lg">
              实时追踪国际金价走势，支持多种货币单位，帮助您把握投资时机
            </p>
            <router-link to="/gold" class="btn-gold inline-flex items-center gap-2">
              <span>查看完整数据</span>
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </router-link>
          </div>
          
          <div class="flex-1 w-full">
            <div v-if="goldPrice" class="space-y-6">
              <div class="card p-8 bg-gradient-to-br from-gold-50 to-white dark:from-gold-950/30 dark:to-surface-900 border-gold-200 dark:border-gold-800/50">
                <div class="flex items-center justify-between mb-4">
                  <div class="flex items-center gap-3">
                    <span class="text-4xl">🥇</span>
                    <div>
                      <p class="text-sm text-surface-500 dark:text-surface-400">XAU/盎司</p>
                      <p class="text-xs text-surface-400 dark:text-surface-500">实时金价</p>
                    </div>
                  </div>
                  <div :class="[
                    'px-4 py-2 rounded-full text-sm font-semibold flex items-center gap-1',
                    goldPrice.changePercent >= 0 
                      ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300'
                      : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'
                  ]">
                    <span>{{ goldPrice.changePercent >= 0 ? '↑' : '↓' }}</span>
                    <span>{{ Math.abs(goldPrice.changePercent).toFixed(2) }}%</span>
                  </div>
                </div>
                <div class="flex items-baseline gap-2">
                  <span class="text-2xl text-surface-500 dark:text-surface-400">{{ goldPrice.symbol }}</span>
                  <span class="text-5xl md:text-6xl font-bold text-surface-900 dark:text-white tracking-tight">
                    {{ formatPrice(goldPrice.price) }}
                  </span>
                </div>
              </div>
              
              <div class="grid grid-cols-3 gap-4">
                <div class="card p-4 text-center">
                  <p class="text-xs text-surface-500 dark:text-surface-400 mb-1">最高价</p>
                  <p class="text-lg font-bold text-surface-900 dark:text-white">
                    {{ goldPrice.symbol }}{{ formatPrice(goldPrice.high) }}
                  </p>
                </div>
                <div class="card p-4 text-center">
                  <p class="text-xs text-surface-500 dark:text-surface-400 mb-1">最低价</p>
                  <p class="text-lg font-bold text-surface-900 dark:text-white">
                    {{ goldPrice.symbol }}{{ formatPrice(goldPrice.low) }}
                  </p>
                </div>
                <div class="card p-4 text-center">
                  <p class="text-xs text-surface-500 dark:text-surface-400 mb-1">平均值</p>
                  <p class="text-lg font-bold text-surface-900 dark:text-white">
                    {{ goldPrice.symbol }}{{ formatPrice(goldPrice.average) }}
                  </p>
                </div>
              </div>
            </div>
            
            <div v-else-if="loading" class="card p-12 flex flex-col items-center justify-center">
              <div class="w-12 h-12 border-4 border-gold-500 border-t-transparent rounded-full animate-spin mb-4"></div>
              <p class="text-surface-500 dark:text-surface-400">加载金价数据...</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="py-24 bg-surface-50 dark:bg-surface-950 relative overflow-hidden">
      <div class="absolute inset-0 grid-pattern opacity-30"></div>
      
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <span class="inline-block px-4 py-1.5 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 text-sm font-medium mb-4">
            最新动态
          </span>
          <h2 class="section-title">
            技术<span class="gradient-text">文章</span>精选
          </h2>
          <p class="section-subtitle">
            分享技术心得与投资理念，记录成长与思考
          </p>
        </div>
        
        <div v-if="articles.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <article 
            v-for="(article, index) in articles" 
            :key="article.id"
            class="group card-hover overflow-hidden cursor-pointer"
            :class="`animation-delay-${(index + 1) * 100}`"
            @click="$router.push(`/article/${article.id}`)"
          >
            <div class="h-48 bg-gradient-to-br from-primary-500 via-accent-500 to-gold-500 relative overflow-hidden">
              <div class="absolute inset-0 bg-black/20 group-hover:bg-black/10 transition-colors duration-300"></div>
              <div class="absolute inset-0 bg-hero-pattern opacity-20"></div>
              <div class="absolute bottom-0 left-0 right-0 p-6 bg-gradient-to-t from-black/60 to-transparent">
                <div v-if="article.category" class="flex items-center gap-2 mb-2">
                  <span class="text-2xl drop-shadow-lg">{{ article.category.icon }}</span>
                  <span class="text-white/90 text-sm font-medium drop-shadow-lg">{{ article.category.name }}</span>
                </div>
              </div>
            </div>
            
            <div class="p-6">
              <h3 class="text-xl font-bold text-surface-900 dark:text-white mb-3 line-clamp-2 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
                {{ article.title }}
              </h3>
              <p class="text-surface-600 dark:text-surface-400 text-sm line-clamp-2 mb-4 leading-relaxed">
                {{ article.summary }}
              </p>
              
              <div class="flex items-center justify-between text-sm text-surface-500 dark:text-surface-400">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center text-white text-xs font-medium">
                    {{ article.author?.displayName?.[0] || article.author?.username?.[0] || 'U' }}
                  </div>
                  <span>{{ article.author?.displayName || article.author?.username || '匿名' }}</span>
                </div>
                <div class="flex items-center gap-3">
                  <span class="flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    {{ article.viewCount }}
                  </span>
                  <span>{{ formatDate(article.createdAt) }}</span>
                </div>
              </div>
              
              <div v-if="article.tags && article.tags.length > 0" class="flex flex-wrap gap-2 mt-4 pt-4 border-t border-surface-100 dark:border-surface-800">
                <span
                  v-for="tag in article.tags.slice(0, 3)"
                  :key="tag.id"
                  class="px-2.5 py-1 text-xs rounded-full font-medium"
                  :style="{ backgroundColor: tag.color + '15', color: tag.color }"
                >
                  {{ tag.name }}
                </span>
                <span v-if="article.tags.length > 3" class="px-2.5 py-1 text-xs text-surface-400 dark:text-surface-500">
                  +{{ article.tags.length - 3 }}
                </span>
              </div>
            </div>
          </article>
        </div>
        
        <div v-else-if="loadingArticles" class="flex justify-center py-20">
          <div class="w-12 h-12 border-4 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
        </div>
        
        <div v-else class="text-center py-20">
          <span class="text-6xl mb-4 block">📝</span>
          <p class="text-surface-500 dark:text-surface-400 text-lg">暂无文章</p>
        </div>
        
        <div v-if="articles.length > 0" class="text-center mt-12">
          <router-link to="/articles" class="btn-primary inline-flex items-center gap-2">
            <span>查看全部文章</span>
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
            </svg>
          </router-link>
        </div>
      </div>
    </section>

    <section class="py-24 bg-gradient-to-br from-surface-900 via-surface-800 to-surface-900 relative overflow-hidden">
      <div class="absolute inset-0 hero-pattern opacity-10"></div>
      <div class="absolute top-0 left-1/4 w-96 h-96 bg-primary-500/10 rounded-full blur-3xl"></div>
      <div class="absolute bottom-0 right-1/4 w-96 h-96 bg-accent-500/10 rounded-full blur-3xl"></div>
      
      <div class="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 class="text-4xl md:text-5xl font-bold text-white mb-6">
          准备好开始了吗？
        </h2>
        <p class="text-xl text-surface-300 mb-10 max-w-2xl mx-auto">
          探索更多功能，发现更多可能。立即开始您的旅程。
        </p>
        <div class="flex flex-col sm:flex-row justify-center gap-4">
          <router-link to="/gold" class="btn bg-white text-surface-900 hover:bg-surface-100 font-semibold text-lg px-8 py-4">
            💰 查看金价
          </router-link>
          <router-link to="/ai" class="btn bg-white/10 backdrop-blur-sm border border-white/30 text-white hover:bg-white/20 font-semibold text-lg px-8 py-4">
            🤖 AI 对话
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { goldPriceApi, type GoldPrice } from '@/api/goldPrice'
import { articleApi, type Article } from '@/api/article'
import { formatPrice, formatDate } from '@/utils/format'

const goldPrice = ref<GoldPrice | null>(null)
const articles = ref<Article[]>([])
const loading = ref(true)
const loadingArticles = ref(true)

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
