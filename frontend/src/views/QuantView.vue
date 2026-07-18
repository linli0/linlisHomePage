<template>
  <div class="page-wrap">
    <h1 class="page-title">量化</h1>
    <p class="page-desc mb-8">策略与信号一览</p>

    <div v-if="loading" class="text-ink-400 py-12 text-center">加载中…</div>
    <template v-else>
      <section class="mb-10">
        <h2 class="font-semibold text-ink-900 dark:text-white mb-4">策略</h2>
        <div v-if="strategies.length === 0" class="card p-8 text-ink-400 text-sm">暂无策略</div>
        <div v-else class="grid md:grid-cols-2 gap-4">
          <div v-for="s in strategies" :key="s.id" class="card p-5">
            <div class="flex justify-between items-start gap-2">
              <h3 class="font-bold text-ink-900 dark:text-white">{{ s.name }}</h3>
              <span
                class="text-xs px-2 py-0.5 rounded-full"
                :class="s.enabled ? 'bg-green-100 text-green-700' : 'bg-ink-100 text-ink-500'"
              >
                {{ s.enabled ? '启用' : '停用' }}
              </span>
            </div>
            <p class="text-sm text-ink-500 mt-2">{{ s.description || s.symbol || '—' }}</p>
          </div>
        </div>
      </section>

      <section>
        <h2 class="font-semibold text-ink-900 dark:text-white mb-4">最近信号</h2>
        <div v-if="signals.length === 0" class="card p-8 text-ink-400 text-sm">暂无信号</div>
        <div v-else class="card overflow-hidden">
          <table class="w-full text-sm">
            <thead class="bg-ink-50 dark:bg-ink-950 text-ink-500 text-left">
              <tr>
                <th class="px-4 py-3 font-medium">品种</th>
                <th class="px-4 py-3 font-medium">方向</th>
                <th class="px-4 py-3 font-medium">价格</th>
                <th class="px-4 py-3 font-medium">时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sig in signals" :key="sig.id" class="border-t border-ink-100 dark:border-ink-800">
                <td class="px-4 py-3">{{ sig.symbol }}</td>
                <td class="px-4 py-3">{{ sig.side }}</td>
                <td class="px-4 py-3">{{ sig.price ?? '--' }}</td>
                <td class="px-4 py-3 text-ink-400">{{ sig.createdAt || '--' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { tradingApi, type TradingStrategy, type TradingSignal } from '@/api/tradingApi'

const strategies = ref<TradingStrategy[]>([])
const signals = ref<TradingSignal[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [s, sig] = await Promise.all([
      tradingApi.getStrategies(),
      tradingApi.getSignals(undefined, 30),
    ])
    strategies.value = s.data.data ?? []
    signals.value = sig.data.data ?? []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>
