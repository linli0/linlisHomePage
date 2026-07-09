<template>
  <!-- IndicatorOverlay is a non-visual overlay component -->
  <!-- It adds indicator lines to an existing chart -->
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { LineSeries, type IChartApi, type ISeriesApi, type LineData, type LineSeriesOptions } from 'lightweight-charts'

// Types
export interface IndicatorData {
  time: string
  value: number
}

export type IndicatorType = 'MA' | 'EMA' | 'BOLL_UPPER' | 'BOLL_LOWER' | 'BOLL_MIDDLE' | 'CUSTOM'

// Props
const props = withDefaults(defineProps<{
  chart: IChartApi | null
  indicatorData: IndicatorData[]
  color?: string
  lineWidth?: number
  lineStyle?: 'solid' | 'dotted' | 'dashed'
  indicatorName?: string
  indicatorType?: IndicatorType
  crosshairMarkerVisible?: boolean
}>(), {
  color: '#2196F3',
  lineWidth: 2,
  lineStyle: 'solid',
  indicatorName: 'Indicator',
  indicatorType: 'CUSTOM',
  crosshairMarkerVisible: true
})

// Refs
let series: ISeriesApi<'Line'> | null = null

// Computed series options
const seriesOptions = computed<LineSeriesOptions>(() => {
  const styleMap: Record<string, '0' | '1' | '2'> = {
    solid: '0',
    dotted: '1',
    dashed: '2'
  }

  return {
    color: props.color,
    lineWidth: props.lineWidth,
    lineStyle: styleMap[props.lineStyle] || '0',
    crosshairMarkerVisible: props.crosshairMarkerVisible,
    crosshairMarkerRadius: 4,
    title: props.indicatorName
  }
})

// Format indicator data for lightweight-charts
const formattedData = computed<LineData[]>(() => {
  return props.indicatorData.map(item => ({
    time: item.time as any,
    value: item.value
  }))
})

// Initialize series on mount
const initSeries = () => {
  if (!props.chart) return

  series = props.chart.addSeries(LineSeries, seriesOptions.value)

  if (formattedData.value.length > 0) {
    series.setData(formattedData.value)
  }
}

// Update indicator data
const updateIndicator = (data: IndicatorData[]) => {
  if (!series) return

  const formatted = data.map(item => ({
    time: item.time as any,
    value: item.value
  }))

  series.setData(formatted)
}

// Update series options
const updateOptions = (options: Partial<LineSeriesOptions>) => {
  if (!series) return
  series.applyOptions(options)
}

// Expose methods
defineExpose({
  updateIndicator,
  updateOptions,
  getSeries: () => series
})

// Lifecycle
onMounted(() => {
  initSeries()
})

onUnmounted(() => {
  if (series && props.chart) {
    props.chart.removeSeries(series)
    series = null
  }
})
</script>

<style scoped>
/* No visual styles needed - this is an overlay component */
</style>