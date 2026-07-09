<template>
  <div 
    ref="chartContainer" 
    class="chart-container"
    :style="{ height: `${height}px` }"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { createChart, CandlestickSeries, LineSeries, type IChartApi, type ISeriesApi } from 'lightweight-charts'

// Types
export interface CandleData {
  time: string
  open: number
  high: number
  low: number
  close: number
  volume?: number
}

// Props
const props = withDefaults(defineProps<{
  data: CandleData[]
  showVolume?: boolean
  height?: number
}>(), {
  showVolume: false,
  height: 400
})

// Refs
const chartContainer = ref<HTMLElement | null>(null)
let chart: IChartApi | null = null
let candlestickSeries: ISeriesApi<'Candlestick'> | null = null
let volumeSeries: ISeriesApi<'Histogram'> | null = null

// Computed chart data transformation
const chartData = computed(() => {
  return props.data.map(candle => ({
    time: candle.time as any,
    open: candle.open,
    high: candle.high,
    low: candle.low,
    close: candle.close
  }))
})

const volumeData = computed(() => {
  if (!props.showVolume) return []
  return props.data
    .filter(candle => candle.volume !== undefined)
    .map(candle => ({
      time: candle.time as any,
      value: candle.volume || 0,
      color: candle.close >= candle.open ? 'rgba(34, 197, 94, 0.5)' : 'rgba(239, 68, 68, 0.5)'
    }))
})

// Chart options
const chartOptions = {
  layout: {
    background: { color: 'transparent' },
    textColor: '#94a3b8'
  },
  grid: {
    vertLines: { color: 'rgba(148, 163, 184, 0.1)' },
    horzLines: { color: 'rgba(148, 163, 184, 0.1)' }
  },
  crosshair: {
    mode: 1
  },
  rightPriceScale: {
    borderColor: 'rgba(148, 163, 184, 0.2)'
  },
  timeScale: {
    borderColor: 'rgba(148, 163, 184, 0.2)',
    timeVisible: true,
    secondsVisible: false
  }
}

// Initialize chart
const initChart = () => {
  if (!chartContainer.value) return

  chart = createChart(chartContainer.value, {
    ...chartOptions,
    width: chartContainer.value.clientWidth,
    height: props.height
  })

  // Add candlestick series
  candlestickSeries = chart.addSeries(CandlestickSeries, {
    upColor: '#22c55e',
    downColor: '#ef4444',
    borderUpColor: '#22c55e',
    borderDownColor: '#ef4444',
    wickUpColor: '#22c55e',
    wickDownColor: '#ef4444'
  })

  // Set data
  if (chartData.value.length > 0) {
    candlestickSeries.setData(chartData.value)
  }

  // Add volume series if enabled
  if (props.showVolume) {
    volumeSeries = chart.addSeries(LineSeries, {
      priceFormat: {
        type: 'volume'
      },
      priceScaleId: 'volume'
    })

    chart.priceScale('volume').applyOptions({
      scaleMargins: {
        top: 0.8,
        bottom: 0
      }
    })

    if (volumeData.value.length > 0) {
      volumeSeries.setData(volumeData.value as any)
    }
  }

  // Fit content
  chart.timeScale().fitContent()
}

// Resize handler
const handleResize = () => {
  if (chart && chartContainer.value) {
    chart.applyOptions({
      width: chartContainer.value.clientWidth
    })
  }
}

// Real-time update method
const updateData = (newData: CandleData[]) => {
  if (!candlestickSeries) return

  const formattedData = newData.map(candle => ({
    time: candle.time as any,
    open: candle.open,
    high: candle.high,
    low: candle.low,
    close: candle.close
  }))

  candlestickSeries.setData(formattedData)

  if (volumeSeries && props.showVolume) {
    const volData = newData
      .filter(candle => candle.volume !== undefined)
      .map(candle => ({
        time: candle.time as any,
        value: candle.volume || 0,
        color: candle.close >= candle.open ? 'rgba(34, 197, 94, 0.5)' : 'rgba(239, 68, 68, 0.5)'
      }))
    volumeSeries.setData(volData as any)
  }

  if (chart) {
    chart.timeScale().fitContent()
  }
}

// Expose methods
defineExpose({
  updateData
})

// Watch for data changes
watch(() => props.data, (newData) => {
  if (newData && newData.length > 0) {
    updateData(newData)
  }
}, { deep: true })

// Lifecycle
onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.remove()
    chart = null
  }
})
</script>

<style scoped>
.chart-container {
  @apply w-full rounded-lg overflow-hidden;
  @apply bg-slate-50 dark:bg-slate-800/50;
}
</style>