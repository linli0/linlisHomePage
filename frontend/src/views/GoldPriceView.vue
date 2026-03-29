<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="flex flex-col md:flex-row md:items-center justify-between mb-8">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
            <span class="mr-2">💰</span>国际金价追踪
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            最后更新: {{ lastUpdated || '--' }}
          </p>
        </div>
        <button 
          @click="refreshData"
          :disabled="refreshing"
          class="mt-4 md:mt-0 btn-secondary flex items-center space-x-2"
        >
          <svg 
            :class="['w-4 h-4', refreshing && 'animate-spin']" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          <span>刷新</span>
        </button>
      </div>

      <!-- Currency Selector -->
      <div class="card p-4 mb-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">选择货币</h2>
        <div class="flex flex-wrap gap-3">
          <button
            v-for="curr in currencies"
            :key="curr.code"
            @click="selectCurrency(curr.code)"
            :class="[
              'flex items-center space-x-2 px-4 py-2 rounded-lg font-medium transition-all',
              currentCurrency === curr.code
                ? 'bg-gold-500 text-white shadow-lg'
                : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
            ]"
          >
            <span class="text-xl">{{ curr.flag }}</span>
            <span>{{ curr.code }}</span>
            <span class="text-sm opacity-75">{{ curr.name }}</span>
          </button>
        </div>
      </div>

      <!-- Main Price Card -->
      <div v-if="goldPrice" class="card p-8 mb-6 text-center">
        <div class="flex items-center justify-center space-x-3 mb-4">
          <span class="text-4xl">🥇</span>
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">当前金价</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400">XAU/盎司</p>
          </div>
        </div>
        
        <div class="flex items-center justify-center space-x-2 mb-4">
          <span class="text-2xl text-gray-600 dark:text-gray-400">{{ goldPrice.symbol }}</span>
          <span class="text-6xl font-bold text-gray-900 dark:text-white tracking-tight">
            {{ formatPrice(goldPrice.price) }}
          </span>
        </div>
        
        <div 
          :class="[
            'inline-flex items-center space-x-2 px-4 py-2 rounded-full text-sm font-medium',
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

      <!-- Stats Grid -->
      <div v-if="goldPrice" class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="card p-4 text-center">
          <div class="w-10 h-10 mx-auto mb-2 bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center">
            <span class="text-red-600 dark:text-red-400">↑</span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">最高价</p>
          <p class="text-xl font-bold text-gray-900 dark:text-white">
            {{ goldPrice.symbol }}{{ formatPrice(goldPrice.high) }}
          </p>
        </div>
        
        <div class="card p-4 text-center">
          <div class="w-10 h-10 mx-auto mb-2 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center">
            <span class="text-green-600 dark:text-green-400">↓</span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">最低价</p>
          <p class="text-xl font-bold text-gray-900 dark:text-white">
            {{ goldPrice.symbol }}{{ formatPrice(goldPrice.low) }}
          </p>
        </div>
        
        <div class="card p-4 text-center">
          <div class="w-10 h-10 mx-auto mb-2 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
            <span class="text-blue-600 dark:text-blue-400">~</span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">平均值</p>
          <p class="text-xl font-bold text-gray-900 dark:text-white">
            {{ goldPrice.symbol }}{{ formatPrice(goldPrice.average) }}
          </p>
        </div>
        
        <div class="card p-4 text-center">
          <div class="w-10 h-10 mx-auto mb-2 bg-purple-100 dark:bg-purple-900/30 rounded-full flex items-center justify-center">
            <span class="text-purple-600 dark:text-purple-400">〰</span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">波动率</p>
          <p class="text-xl font-bold text-gray-900 dark:text-white">
            {{ goldPrice.volatility.toFixed(2) }}%
          </p>
        </div>
      </div>

      <!-- Chart Section -->
      <div class="card p-6">
        <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white">价格走势</h2>
          <div class="flex space-x-2 mt-4 md:mt-0">
            <button
              v-for="period in periods"
              :key="period.value"
              @click="selectPeriod(period.value)"
              :class="[
                'px-3 py-1 rounded-md text-sm font-medium transition-colors',
                currentPeriod === period.value
                  ? 'bg-gold-500 text-white'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
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
          <div v-else class="flex items-center justify-center h-full text-gray-500 dark:text-gray-400">
            加载中...
          </div>
        </div>
      </div>

      <!-- Info Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
        <div class="card p-6">
          <div class="flex items-start space-x-4">
            <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center flex-shrink-0">
              <span class="text-2xl">ℹ️</span>
            </div>
            <div>
              <h3 class="font-semibold text-gray-900 dark:text-white mb-2">关于金价</h3>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                黄金价格受多种因素影响，包括全球经济形势、地缘政治风险、通货膨胀预期和美元汇率等。
                投资有风险，入市需谨慎。
              </p>
            </div>
          </div>
        </div>
        
        <div class="card p-6">
          <div class="flex items-start space-x-4">
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-lg flex items-center justify-center flex-shrink-0">
              <span class="text-2xl">🕐</span>
            </div>
            <div>
              <h3 class="font-semibold text-gray-900 dark:text-white mb-2">交易时间</h3>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                全球黄金市场几乎24小时交易，从悉尼开盘到纽约收盘，周一至周五持续交易。
                周末市场休市，价格保持不变。
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="!goldPrice && loading" class="flex flex-col items-center justify-center py-20">
        <div class="w-12 h-12 border-4 border-gold-500 border-t-transparent rounded-full animate-spin mb-4"></div>
        <p class="text-gray-600 dark:text-gray-400">加载金价数据...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { goldPriceApi, type GoldPrice, type Currency, type PricePoint } from '@/api/goldPrice'
import PriceChart from '@/components/PriceChart.vue'

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

function formatPrice(price: number): string {
  return price.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

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

// Auto refresh every 60 seconds
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
