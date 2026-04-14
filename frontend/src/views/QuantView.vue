<template>
  <div class="min-h-screen bg-surface-50 dark:bg-surface-950 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-10">
        <span class="inline-block px-4 py-1.5 rounded-full bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 text-sm font-medium mb-4">
          智能交易
        </span>
        <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-4">
          量化<span class="gradient-text">交易</span>策略
        </h1>
        <p class="text-lg text-surface-600 dark:text-surface-400 max-w-2xl">
          管理量化交易策略，查看交易信号和回测结果
        </p>
      </div>

      <!-- 策略列表 -->
      <div class="card p-8 mb-8">
        <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-surface-900 dark:text-white">交易策略</h2>
          <button @click="refreshStrategies" class="btn-secondary mt-4 md:mt-0 flex items-center gap-2" :disabled="loading">
            <svg class="w-4 h-4" :class="{ 'animate-spin': loading }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>刷新</span>
          </button>
        </div>

        <div v-if="loading" class="flex justify-center py-12">
          <div class="w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <div v-else-if="strategies.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="strategy in strategies" :key="strategy.id" 
               class="card-hover p-6 cursor-pointer"
               @click="selectStrategy(strategy)">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-purple-400 to-purple-600 flex items-center justify-center text-white">
                <span class="text-xl">📊</span>
              </div>
              <div>
                <h3 class="font-bold text-surface-900 dark:text-white">{{ strategy.name }}</h3>
                <span :class="[
                  'px-2 py-0.5 rounded-full text-xs font-medium',
                  strategy.status === 'ACTIVE' ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' : 'bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-400'
                ]">
                  {{ strategy.status }}
                </span>
              </div>
            </div>
            <p class="text-sm text-surface-600 dark:text-surface-400 mb-4">{{ strategy.description }}</p>
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div>
                <span class="text-surface-500 dark:text-surface-400">收益率</span>
                <p :class="strategy.totalReturn >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'" class="font-bold">
                  {{ strategy.totalReturn >= 0 ? '+' : '' }}{{ strategy.totalReturn.toFixed(2) }}%
                </p>
              </div>
              <div>
                <span class="text-surface-500 dark:text-surface-400">夏普比率</span>
                <p class="font-bold text-surface-900 dark:text-white">{{ strategy.sharpeRatio.toFixed(2) }}</p>
              </div>
              <div>
                <span class="text-surface-500 dark:text-surface-400">最大回撤</span>
                <p class="font-bold text-red-600 dark:text-red-400">{{ strategy.maxDrawdown.toFixed(2) }}%</p>
              </div>
              <div>
                <span class="text-surface-500 dark:text-surface-400">胜率</span>
                <p class="font-bold text-surface-900 dark:text-white">{{ strategy.winRate.toFixed(2) }}%</p>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-12">
          <div class="w-16 h-16 mx-auto mb-4 bg-surface-100 dark:bg-surface-800 rounded-2xl flex items-center justify-center">
            <span class="text-3xl">📊</span>
          </div>
          <p class="text-surface-600 dark:text-surface-400">暂无交易策略</p>
          <p class="text-sm text-surface-500 dark:text-surface-500 mt-2">请联系管理员添加策略</p>
        </div>
      </div>

      <!-- 策略详情 -->
      <div v-if="selectedStrategy" class="card p-8 mb-8">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-surface-900 dark:text-white">
            {{ selectedStrategy.name }} - 详情
          </h2>
          <button @click="selectedStrategy = null" class="btn-secondary text-sm">
            关闭
          </button>
        </div>

        <!-- 回测结果 -->
        <div v-if="backtestResult" class="mb-8">
          <h3 class="font-bold text-surface-900 dark:text-white mb-4">回测结果</h3>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div class="card-hover p-4 text-center">
              <div class="w-12 h-12 mx-auto mb-3 bg-gradient-to-br from-green-400 to-green-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-white text-xl">📈</span>
              </div>
              <p class="text-sm text-surface-500 dark:text-surface-400">总收益</p>
              <p :class="backtestResult.totalReturn >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'" class="text-2xl font-bold">
                {{ backtestResult.totalReturn >= 0 ? '+' : '' }}{{ backtestResult.totalReturn.toFixed(2) }}%
              </p>
            </div>
            <div class="card-hover p-4 text-center">
              <div class="w-12 h-12 mx-auto mb-3 bg-gradient-to-br from-blue-400 to-blue-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-white text-xl">📊</span>
              </div>
              <p class="text-sm text-surface-500 dark:text-surface-400">夏普比率</p>
              <p class="text-2xl font-bold text-surface-900 dark:text-white">{{ backtestResult.sharpeRatio.toFixed(2) }}</p>
            </div>
            <div class="card-hover p-4 text-center">
              <div class="w-12 h-12 mx-auto mb-3 bg-gradient-to-br from-red-400 to-red-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-white text-xl">📉</span>
              </div>
              <p class="text-sm text-surface-500 dark:text-surface-400">最大回撤</p>
              <p class="text-2xl font-bold text-red-600 dark:text-red-400">{{ backtestResult.maxDrawdown.toFixed(2) }}%</p>
            </div>
            <div class="card-hover p-4 text-center">
              <div class="w-12 h-12 mx-auto mb-3 bg-gradient-to-br from-purple-400 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-white text-xl">🎯</span>
              </div>
              <p class="text-sm text-surface-500 dark:text-surface-400">胜率</p>
              <p class="text-2xl font-bold text-surface-900 dark:text-white">{{ backtestResult.winRate.toFixed(2) }}%</p>
            </div>
          </div>
          <div class="mt-4 grid grid-cols-3 gap-4 text-sm text-center">
            <div class="p-3 bg-surface-100 dark:bg-surface-800 rounded-xl">
              <span class="text-surface-500 dark:text-surface-400">交易次数</span>
              <p class="font-bold text-surface-900 dark:text-white mt-1">{{ backtestResult.totalTrades }}</p>
            </div>
            <div class="p-3 bg-green-50 dark:bg-green-900/20 rounded-xl">
              <span class="text-surface-500 dark:text-surface-400">盈利次数</span>
              <p class="font-bold text-green-600 dark:text-green-400 mt-1">{{ backtestResult.profitTrades }}</p>
            </div>
            <div class="p-3 bg-red-50 dark:bg-red-900/20 rounded-xl">
              <span class="text-surface-500 dark:text-surface-400">亏损次数</span>
              <p class="font-bold text-red-600 dark:text-red-400 mt-1">{{ backtestResult.lossTrades }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 交易信号 -->
      <div class="card p-8">
        <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-surface-900 dark:text-white">交易信号</h2>
          <button @click="refreshSignals" class="btn-secondary mt-4 md:mt-0 flex items-center gap-2" :disabled="loadingSignals">
            <svg class="w-4 h-4" :class="{ 'animate-spin': loadingSignals }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>刷新</span>
          </button>
        </div>

        <div v-if="loadingSignals" class="flex justify-center py-8">
          <div class="w-8 h-8 border-4 border-purple-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <div v-else-if="signals.length > 0" class="space-y-4">
          <div v-for="signal in signals" :key="signal.id" 
               class="p-4 rounded-xl bg-surface-50 dark:bg-surface-800/50 border border-surface-200 dark:border-surface-700">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <span :class="[
                  'px-2 py-1 rounded-lg text-xs font-medium',
                  signal.signalType === 'BUY' ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' :
                  signal.signalType === 'SELL' ? 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300' :
                  'bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-400'
                ]">
                  {{ signal.signalType }}
                </span>
                <span class="font-medium text-surface-900 dark:text-white">{{ signal.symbol }}</span>
              </div>
              <span :class="[
                'px-2 py-1 rounded-lg text-xs',
                signal.executed ? 'bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400' : 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-600 dark:text-yellow-400'
              ]">
                {{ signal.executed ? '已执行' : '待执行' }}
              </span>
            </div>
            <p class="text-sm text-surface-600 dark:text-surface-400 mb-2">{{ signal.reason }}</p>
            <div class="flex gap-4 text-xs text-surface-500 dark:text-surface-400">
              <span>价格: {{ signal.price }}</span>
              <span>目标: {{ signal.targetPrice }}</span>
              <span>止损: {{ signal.stopLoss }}</span>
              <span>置信度: {{ signal.confidence.toFixed(2) }}%</span>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8">
          <p class="text-surface-500 dark:text-surface-400">暂无交易信号</p>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="mt-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/50 text-red-700 dark:text-red-300 rounded-xl text-sm">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { tradingApi, type TradingStrategy, type TradingSignal, type BacktestResult } from '@/api/tradingApi'

const strategies = ref<TradingStrategy[]>([])
const signals = ref<TradingSignal[]>([])
const selectedStrategy = ref<TradingStrategy | null>(null)
const backtestResult = ref<BacktestResult | null>(null)
const loading = ref(true)
const loadingSignals = ref(false)
const error = ref('')

async function refreshStrategies() {
  loading.value = true
  error.value = ''
  try {
    const res = await tradingApi.getStrategies()
    strategies.value = res.data.data || []
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    console.error('Failed to fetch strategies:', err)
    error.value = e.response?.data?.message || '获取策略失败'
  } finally {
    loading.value = false
  }
}

async function refreshSignals() {
  loadingSignals.value = true
  try {
    const res = await tradingApi.getSignals(undefined, 50)
    signals.value = res.data.data || []
  } catch (err: unknown) {
    console.error('Failed to fetch signals:', err)
  } finally {
    loadingSignals.value = false
  }
}

async function selectStrategy(strategy: TradingStrategy) {
  selectedStrategy.value = strategy
  backtestResult.value = null
  
  try {
    const res = await tradingApi.getBacktestResults(strategy.id)
    backtestResult.value = res.data.data || null
  } catch (err: unknown) {
    console.error('Failed to fetch backtest results:', err)
  }
}

onMounted(() => {
  refreshStrategies()
  refreshSignals()
})
</script>