<template>
  <div class="bg-white dark:bg-surface-800 rounded-lg shadow-sm p-6">
    <h3 class="text-lg font-semibold text-surface-900 dark:text-white mb-4">
      推文趋势
    </h3>
    <div class="h-64">
      <canvas ref="chartCanvas"></canvas>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Chart, ChartConfiguration, ChartData } from 'chart.js/auto'

interface Props {
  stats?: {
    twitterTweets: number
    truthSocialTweets: number
    tweetsToday: number
    tweetsThisWeek: number
  }
}

const props = defineProps<Props>()

const chartCanvas = ref<HTMLCanvasElement | null>(null)
let chartInstance: Chart | null = null

onMounted(() => {
  if (chartCanvas.value) {
    const config: ChartConfiguration = {
      type: 'bar',
      data: {
        labels: ['Twitter', 'Truth Social', '今日', '本周'],
        datasets: [{
          label: '推文数量',
          data: [
            props.stats?.twitterTweets || 0,
            props.stats?.truthSocialTweets || 0,
            props.stats?.tweetsToday || 0,
            props.stats?.tweetsThisWeek || 0
          ],
          backgroundColor: [
            'rgba(59, 130, 246, 0.8)',
            'rgba(234, 179, 8, 0.8)',
            'rgba(168, 85, 247, 0.8)',
            'rgba(34, 197, 94, 0.8)'
          ],
          borderColor: [
            'rgb(59, 130, 246)',
            'rgb(234, 179, 8)',
            'rgb(168, 85, 247)',
            'rgb(34, 197, 94)'
          ],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              color: 'rgb(107, 114, 128)'
            }
          },
          x: {
            ticks: {
              color: 'rgb(107, 114, 128)'
            }
          }
        }
      }
    }
    
    chartInstance = new Chart(chartCanvas.value, config)
  }
})

watch(() => props.stats, (newStats) => {
  if (chartInstance && newStats) {
    chartInstance.data.datasets[0].data = [
      newStats.twitterTweets,
      newStats.truthSocialTweets,
      newStats.tweetsToday,
      newStats.tweetsThisWeek
    ]
    chartInstance.update()
  }
}, { deep: true })
</script>