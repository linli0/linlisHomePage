<template>
  <div class="w-full" :style="{ height: `${height}px` }">
    <Line v-if="chartData" :data="chartData" :options="options" />
    <p v-else class="text-sm text-ink-400 py-12 text-center">暂无走势数据</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Filler,
  Legend,
} from 'chart.js'
import type { PricePoint } from '@/api/goldPrice'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Filler, Legend)

const props = withDefaults(
  defineProps<{
    data: PricePoint[]
    currency?: string
    height?: number
  }>(),
  { currency: 'CNY', height: 320 },
)

const chartData = computed(() => {
  if (!props.data?.length) return null
  return {
    labels: props.data.map((p) => p.date),
    datasets: [
      {
        label: `金价 (${props.currency})`,
        data: props.data.map((p) => p.price),
        borderColor: '#f5971f',
        backgroundColor: 'rgb(245 151 31 / 0.12)',
        fill: true,
        tension: 0.3,
        pointRadius: 0,
        borderWidth: 2,
      },
    ],
  }
})

const options = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: { grid: { display: false }, ticks: { maxTicksLimit: 8, color: '#9da1ab' } },
    y: { grid: { color: 'rgb(0 0 0 / 0.04)' }, ticks: { color: '#9da1ab' } },
  },
}
</script>
