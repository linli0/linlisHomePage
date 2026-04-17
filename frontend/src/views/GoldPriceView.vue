<template>
  <div class="min-h-screen bg-cyber-void-950 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex flex-col md:flex-row md:items-center justify-between mb-10">
        <div>
          <span class="inline-block px-4 py-1.5 rounded-full bg-gold-100 dark:bg-gold-900/30 text-gold-600 dark:text-gold-400 text-sm font-medium mb-4">
            实时追踪
          </span>
          <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-2">
            国际<span class="text-gold-500">金价</span>追踪
          </h1>
          <p class="text-surface-600 dark:text-surface-400">
            最后更新: {{ lastUpdated || '--' }}
          </p>
        </div>
        <button 
          @click="refreshData"
          :disabled="refreshing"
          class="mt-6 md:mt-0 btn-gold flex items-center gap-2"
        >
          <svg 
            :class="['w-4 h-4', refreshing && 'animate-spin']" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          <span>刷新数据</span>
        </button>
      </div>

      <div class="card-cyber-glow p-6 mb-8">
        <h2 class="text-lg font-semibold text-surface-900 dark:text-white mb-4">选择货币</h2>
        <div class="flex flex-wrap gap-3">
          <button
            v-for="curr in currencies"
            :key="curr.code"
            @click="selectCurrency(curr.code)"
            :class="[
              'flex items-center gap-3 px-5 py-3 rounded-xl font-medium transition-all duration-200',
              currentCurrency === curr.code
                ? 'bg-gradient-to-r from-gold-500 to-gold-600 text-white shadow-lg shadow-gold-500/25'
                : 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700'
            ]"
          >
            <span class="text-2xl">{{ curr.flag }}</span>
            <div class="text-left">
              <div class="font-bold">{{ curr.code }}</div>
              <div class="text-xs opacity-75">{{ curr.name }}</div>
            </div>
          </button>
        </div>
      </div>

      <div v-if="goldPrice" class="space-y-8">
        <div class="card-cyber-glow p-8 border-cyber-yellow-500 shadow-neon-yellow">
          <div class="flex flex-col md:flex-row items-center justify-between gap-6">
            <div class="flex items-center gap-6">
              <div class="relative">
                <div class="absolute inset-0 bg-gold-400/30 rounded-2xl blur-xl animate-pulse-slow"></div>
                <div class="relative w-20 h-20 bg-gradient-to-br from-gold-400 to-gold-600 rounded-2xl flex items-center justify-center shadow-lg">
                  <span class="text-4xl">🥇</span>
                </div>
              </div>
              <div>
                <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">XAU/盎司</p>
                <p class="text-xs text-surface-400 dark:text-surface-500">实时金价</p>
              </div>
            </div>
            
            <div class="text-center md:text-right">
              <div class="flex items-baseline justify-center md:justify-end gap-2 mb-3">
                <span class="text-3xl text-surface-500 dark:text-surface-400">{{ goldPrice.symbol }}</span>
                <span class="text-6xl md:text-7xl font-bold text-surface-900 dark:text-white tracking-tight">
                  {{ formatPrice(goldPrice.price) }}
                </span>
              </div>
              <div 
                :class="[
                  'inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-semibold',
                  goldPrice.changePercent >= 0 
                    ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300'
                    : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'
                ]"
              >
                <span class="text-lg">{{ goldPrice.changePercent >= 0 ? '↑' : '↓' }}</span>
                <span>{{ goldPrice.symbol }}{{ Math.abs(goldPrice.changeAmount).toFixed(2) }}</span>
                <span>({{ goldPrice.changePercent >= 0 ? '+' : '' }}{{ goldPrice.changePercent.toFixed(2) }}%)</span>
              </div>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="card-cyber p-6 text-center border-cyber-yellow-500/30">
            <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-red-400 to-red-600 rounded-xl flex items-center justify-center shadow-lg">
              <span class="text-white text-xl">↑</span>
            </div>
            <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">最高价</p>
            <p class="text-2xl font-bold text-surface-900 dark:text-white">
              {{ goldPrice.symbol }}{{ formatPrice(goldPrice.high) }}
            </p>
          </div>
          
          <div class="card-cyber p-6 text-center border-cyber-yellow-500/30">
            <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-green-400 to-green-600 rounded-xl flex items-center justify-center shadow-lg">
              <span class="text-white text-xl">↓</span>
            </div>
            <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">最低价</p>
            <p class="text-2xl font-bold text-surface-900 dark:text-white">
              {{ goldPrice.symbol }}{{ formatPrice(goldPrice.low) }}
            </p>
          </div>
          
          <div class="card-cyber p-6 text-center border-cyber-yellow-500/30">
            <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-primary-400 to-primary-600 rounded-xl flex items-center justify-center shadow-lg">
              <span class="text-white text-xl">~</span>
            </div>
            <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">平均值</p>
            <p class="text-2xl font-bold text-surface-900 dark:text-white">
              {{ goldPrice.symbol }}{{ formatPrice(goldPrice.average) }}
            </p>
          </div>
          
          <div class="card-cyber p-6 text-center border-cyber-yellow-500/30">
            <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-accent-400 to-accent-600 rounded-xl flex items-center justify-center shadow-lg">
              <span class="text-white text-xl">〰</span>
            </div>
            <p class="text-sm text-surface-500 dark:text-surface-400 mb-1">波动率</p>
            <p class="text-2xl font-bold text-surface-900 dark:text-white">
              {{ goldPrice.volatility.toFixed(2) }}%
            </p>
          </div>
        </div>

        <div class="card-cyber-glow p-8 border-cyber-yellow-500 shadow-neon-yellow">
          <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
            <h2 class="text-xl font-bold text-surface-900 dark:text-white">价格走势</h2>
            <div class="flex gap-2 mt-4 md:mt-0">
              <button
                v-for="period in periods"
                :key="period.value"
                @click="selectPeriod(period.value)"
                :class="[
                  'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200',
                  currentPeriod === period.value
                    ? 'bg-gold-500 text-white shadow-lg shadow-gold-500/25'
                    : 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700'
                ]"
              >
                {{ period.label }}
              </button>
            </div>
          </div>
          
          <div class="h-80">
            <PriceChart 
              v-if="priceHistory.length > 0"
              :data="priceHistory"
              :currency="currentCurrency"
              :symbol="currentSymbol"
            />
            <div v-else class="flex items-center justify-center h-full text-surface-500 dark:text-surface-400">
              加载中...
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="card-cyber p-6 border-cyber-yellow-500/30">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 bg-gradient-to-br from-primary-400 to-primary-600 rounded-xl flex items-center justify-center flex-shrink-0 shadow-lg">
                <span class="text-2xl">ℹ️</span>
              </div>
              <div>
                <h3 class="font-bold text-surface-900 dark:text-white mb-2">关于金价</h3>
                <p class="text-sm text-surface-600 dark:text-surface-400 leading-relaxed">
                  黄金价格受多种因素影响，包括全球经济形势、地缘政治风险、通货膨胀预期和美元汇率等。
                  投资有风险，入市需谨慎。
                </p>
              </div>
            </div>
          </div>
          
          <div class="card-cyber p-6 border-cyber-yellow-500/30">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 bg-gradient-to-br from-green-400 to-green-600 rounded-xl flex items-center justify-center flex-shrink-0 shadow-lg">
                <span class="text-2xl">🕐</span>
              </div>
              <div>
                <h3 class="font-bold text-surface-900 dark:text-white mb-2">交易时间</h3>
                <p class="text-sm text-surface-600 dark:text-surface-400 leading-relaxed">
                  全球黄金市场几乎24小时交易，从悉尼开盘到纽约收盘，周一至周五持续交易。
                  周末市场休市，价格保持不变。
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="w-16 h-16 border-4 border-gold-500 border-t-transparent rounded-full animate-spin mb-4"></div>
        <p class="text-surface-600 dark:text-surface-400">加载金价数据...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { goldPriceApi, type GoldPrice, type Currency, type PricePoint } from '@/api/goldPrice'
import PriceChart from '@/components/PriceChart.vue'
import { formatPrice } from '@/utils/format'

const goldPrice = ref<GoldPrice | null>(null)
const currencies = ref<Currency[]>([])
const priceHistory = ref<PricePoint[]>([])
const currentCurrency = ref('USD')
const currentPeriod = ref(30)
const loading = ref(true)
const refreshing = ref(false)
const lastUpdated = ref('')

const periods = [
  { value: 7, label: '7天' },
  { value: 30, label: '30天' },
  { value: 90, label: '90天' }
]

const currentSymbol = computed(() => {
  const curr = currencies.value.find(c => c.code === currentCurrency.value)
  return curr?.symbol || '$'
})

async function fetchData() {
  try {
    loading.value = true
    const [priceRes, currenciesRes, historyRes] = await Promise.all([
      goldPriceApi.getCurrentPrice(currentCurrency.value),
      goldPriceApi.getSupportedCurrencies(),
      goldPriceApi.getPriceHistory(currentCurrency.value, currentPeriod.value)
    ])
    
    goldPrice.value = priceRes.data.data
    currencies.value = currenciesRes.data.data
    priceHistory.value = historyRes.data.data
    
    lastUpdated.value = new Date().toLocaleString('zh-CN')
  } catch (error) {
    console.error('Failed to fetch gold price:', error)
  } finally {
    loading.value = false
  }
}

async function selectCurrency(currency: string) {
  currentCurrency.value = currency
  await fetchData()
}

async function selectPeriod(days: number) {
  currentPeriod.value = days
  try {
    const res = await goldPriceApi.getPriceHistory(currentCurrency.value, days)
    priceHistory.value = res.data.data
  } catch (error) {
    console.error('Failed to fetch history:', error)
  }
}

async function refreshData() {
  refreshing.value = true
  await fetchData()
  refreshing.value = false
}

let refreshInterval: number | null = null

onMounted(() => {
  fetchData()
  refreshInterval = window.setInterval(() => {
    fetchData()
  }, 60000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>
