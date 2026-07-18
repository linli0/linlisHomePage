<template>
  <div class="page-wrap">
    <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4 mb-8">
      <div>
        <p class="text-sm font-semibold text-brand-600 mb-1">黄金9999 · 元/克</p>
        <h1 class="page-title">国内金价</h1>
        <p class="page-desc">最后更新：{{ lastUpdated || '--' }} · 每天自动同步，也可手动刷新</p>
      </div>
      <button type="button" class="btn-primary" :disabled="refreshing" @click="refreshData">
        {{ refreshing ? '刷新中…' : '刷新数据' }}
      </button>
    </div>

    <div v-if="loading" class="card p-16 text-center text-ink-400">加载中…</div>

    <template v-else-if="goldPrice">
      <div class="card p-8 mb-6">
        <div class="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">
          <div>
            <p class="text-sm text-ink-500 mb-2">{{ goldPrice.currency }} · 当前价</p>
            <div class="flex items-baseline gap-2">
              <span class="text-2xl text-ink-400">{{ goldPrice.symbol }}</span>
              <span class="text-5xl font-bold text-ink-900 dark:text-white">{{ formatPrice(goldPrice.price) }}</span>
            </div>
          </div>
          <div
            :class="[
              'inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-semibold',
              goldPrice.changePercent >= 0 ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            ]"
          >
            {{ goldPrice.changePercent >= 0 ? '↑' : '↓' }}
            {{ goldPrice.symbol }}{{ Math.abs(goldPrice.changeAmount).toFixed(2) }}
            ({{ goldPrice.changePercent >= 0 ? '+' : '' }}{{ goldPrice.changePercent.toFixed(2) }}%)
          </div>
        </div>
        <div class="grid grid-cols-3 gap-3 mt-8">
          <div class="rounded-xl bg-brand-50 dark:bg-ink-950 p-4 text-center">
            <p class="text-xs text-ink-400 mb-1">最高</p>
            <p class="font-bold">{{ goldPrice.symbol }}{{ formatPrice(goldPrice.high) }}</p>
          </div>
          <div class="rounded-xl bg-brand-50 dark:bg-ink-950 p-4 text-center">
            <p class="text-xs text-ink-400 mb-1">最低</p>
            <p class="font-bold">{{ goldPrice.symbol }}{{ formatPrice(goldPrice.low) }}</p>
          </div>
          <div class="rounded-xl bg-brand-50 dark:bg-ink-950 p-4 text-center">
            <p class="text-xs text-ink-400 mb-1">均价</p>
            <p class="font-bold">{{ goldPrice.symbol }}{{ formatPrice(goldPrice.average) }}</p>
          </div>
        </div>
      </div>

      <div class="card p-6 mb-6">
        <h2 class="font-semibold text-ink-900 dark:text-white mb-4">货币</h2>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="c in currencies"
            :key="c.code"
            type="button"
            class="px-4 py-2 rounded-xl text-sm font-medium border transition-colors"
            :class="currentCurrency === c.code
              ? 'bg-brand-500 text-white border-brand-500'
              : 'bg-white dark:bg-ink-900 border-ink-200 dark:border-ink-700 text-ink-600 hover:border-brand-400'"
            @click="selectCurrency(c.code)"
          >
            {{ c.flag }} {{ c.code }}
          </button>
        </div>
      </div>

      <div class="card p-6">
        <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
          <h2 class="font-semibold text-ink-900 dark:text-white">走势</h2>
          <div class="flex gap-2">
            <button
              v-for="p in periods"
              :key="p.value"
              type="button"
              class="px-3 py-1.5 rounded-lg text-sm"
              :class="currentPeriod === p.value ? 'bg-brand-500 text-white' : 'bg-ink-100 dark:bg-ink-800 text-ink-600'"
              @click="selectPeriod(p.value)"
            >
              {{ p.label }}
            </button>
          </div>
        </div>
        <PriceChart :data="priceHistory" :currency="currentCurrency" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { goldPriceApi, type GoldPrice, type Currency, type PricePoint } from '@/api/goldPrice'
import PriceChart from '@/components/PriceChart.vue'
import { formatPrice } from '@/utils/format'

const goldPrice = ref<GoldPrice | null>(null)
const currencies = ref<Currency[]>([])
const priceHistory = ref<PricePoint[]>([])
const currentCurrency = ref('CNY')
const currentPeriod = ref(30)
const loading = ref(true)
const refreshing = ref(false)
const lastUpdated = ref('')

const periods = [
  { value: 7, label: '7天' },
  { value: 30, label: '30天' },
  { value: 90, label: '90天' },
]

function applyPrice(data: GoldPrice) {
  goldPrice.value = data
  lastUpdated.value = data.timestamp
    ? new Date(data.timestamp).toLocaleString('zh-CN')
    : new Date().toLocaleString('zh-CN')
}

async function fetchData() {
  loading.value = true
  try {
    const [priceRes, curRes, histRes] = await Promise.all([
      goldPriceApi.getCurrentPrice(currentCurrency.value),
      goldPriceApi.getSupportedCurrencies(),
      goldPriceApi.getPriceHistory(currentCurrency.value, currentPeriod.value),
    ])
    applyPrice(priceRes.data.data)
    currencies.value = curRes.data.data
    priceHistory.value = histRes.data.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function selectCurrency(code: string) {
  currentCurrency.value = code
  await fetchData()
}

async function selectPeriod(days: number) {
  currentPeriod.value = days
  const res = await goldPriceApi.getPriceHistory(currentCurrency.value, days)
  priceHistory.value = res.data.data
}

async function refreshData() {
  refreshing.value = true
  try {
    const res = await goldPriceApi.refresh()
    applyPrice(res.data.data)
    currentCurrency.value = 'CNY'
    const histRes = await goldPriceApi.getPriceHistory('CNY', currentPeriod.value)
    priceHistory.value = histRes.data.data
  } catch (e) {
    console.error(e)
  } finally {
    refreshing.value = false
  }
}

onMounted(fetchData)
</script>
