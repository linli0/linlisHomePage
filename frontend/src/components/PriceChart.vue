<template>
  <div class="chart-container">
    <Line 
      v-if="chartData.labels.length > 0"
      :data="chartData"
      :options="chartOptions"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'
import { Line } from 'vue-chartjs'
import type { PricePoint } from '@/api/goldPrice'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

const props = defineProps<{
  data: PricePoint[]
  currency: string
  symbol: string
}>()

const chartData = computed(() => {
  return {
    labels: props.data.map(p => {
      const date = new Date(p.date)
      return `${date.getMonth() + 1}/${date.getDate()}`
    }),
    datasets: [
      {
        label: `金价 (${props.currency})`,
        data: props.data.map(p => p.price),
        borderColor: '#f59e0b',
        backgroundColor: (context: any) => {
          const ctx = context.chart.ctx
          const gradient = ctx.createLinearGradient(0, 0, 0, 300)
          gradient.addColorStop(0, 'rgba(245, 158, 11, 0.3)')
          gradient.addColorStop(1, 'rgba(245, 158, 11, 0.05)')
          return gradient
        },
        borderWidth: 3,
        pointRadius: 0,
        pointHoverRadius: 6,
        pointHoverBackgroundColor: '#f59e0b',
        pointHoverBorderColor: '#fff',
        pointHoverBorderWidth: 2,
        fill: true,
        tension: 0.4
      }
    ]
  }
})

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    intersect: false,
    mode: 'index'
  },
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      backgroundColor: 'rgba(30, 41, 59, 0.95)',
      titleColor: '#94a3b8',
      bodyColor: '#f8fafc',
      borderColor: 'rgba(245, 158, 11, 0.3)',
      borderWidth: 1,
      padding: 12,
      displayColors: false,
      callbacks: {
        label: (context: any) => {
          return props.symbol + context.parsed.y.toLocaleString()
        }
      }
    }
  },
  scales: {
    x: {
      grid: {
        color: 'rgba(148, 163, 184, 0.1)',
        drawBorder: false
      },
      ticks: {
        color: '#94a3b8',
        maxTicksLimit: 8,
        maxRotation: 0
      }
    },
    y: {
      grid: {
        color: 'rgba(148, 163, 184, 0.1)',
        drawBorder: false
      },
      ticks: {
        color: '#94a3b8',
        callback: (value: any) => {
          const num = Number(value)
          return props.symbol + (num >= 1000 ? (num / 1000).toFixed(1) + 'k' : num)
        }
      }
    }
  }
}))
</script>
