// composables/usePerformance.ts
import { ref, onMounted, onUnmounted } from 'vue'

interface PerformanceMetrics {
  // Web Vitals
  LCP?: number // Largest Contentful Paint
  FID?: number // First Input Delay
  CLS?: number // Cumulative Layout Shift
  FCP?: number // First Contentful Paint
  TTFB?: number // Time to First Byte
  INP?: number // Interaction to Next Paint
  
  // Custom metrics
  componentRenderTime?: number
  apiResponseTime?: number
}

interface PerformanceMeasure {
  name: string
  startTime: number
  endTime?: number
  duration?: number
}

const metrics = ref<PerformanceMetrics>({})
const measures = new Map<string, PerformanceMeasure>()

export function usePerformance() {
  // Start measuring a performance mark
  const startMeasure = (name: string): void => {
    measures.set(name, {
      name,
      startTime: performance.now()
    })
    
    // Also create native performance mark
    if ('mark' in performance) {
      performance.mark(`${name}-start`)
    }
  }

  // End measuring and record duration
  const endMeasure = (name: string): number | undefined => {
    const measure = measures.get(name)
    if (!measure) {
      console.warn(`[Performance] No measure found for: ${name}`)
      return undefined
    }

    const endTime = performance.now()
    const duration = endTime - measure.startTime

    measure.endTime = endTime
    measure.duration = duration
    measures.set(name, measure)

    // Create native performance measure
    if ('measure' in performance) {
      try {
        performance.measure(name, `${name}-start`)
      } catch (e) {
        // Mark might not exist, ignore
      }
    }

    // Log if duration exceeds threshold (200ms for user requirement)
    if (duration > 200) {
      console.warn(`[Performance] ${name} took ${duration.toFixed(2)}ms (exceeds 200ms target)`)
    } else {
      console.log(`[Performance] ${name} took ${duration.toFixed(2)}ms`)
    }

    return duration
  }

  // Measure API call performance
  const measureAPICall = async <T>(
    name: string,
    apiCall: () => Promise<T>
  ): Promise<T> => {
    startMeasure(`api-${name}`)
    try {
      const result = await apiCall()
      const duration = endMeasure(`api-${name}`)
      
      // Update metrics
      if (duration) {
        metrics.value.apiResponseTime = duration
      }
      
      return result
    } catch (error) {
      endMeasure(`api-${name}`)
      throw error
    }
  }

  // Measure component render time
  const measureRender = (componentName: string) => {
    startMeasure(`render-${componentName}`)
    
    return () => {
      const duration = endMeasure(`render-${componentName}`)
      if (duration) {
        metrics.value.componentRenderTime = duration
      }
    }
  }

  // Get all measures
  const getMeasures = (): PerformanceMeasure[] => {
    return Array.from(measures.values())
  }

  // Get current metrics
  const getMetrics = (): PerformanceMetrics => {
    return { ...metrics.value }
  }

  // Web Vitals monitoring
  const observeWebVitals = () => {
    if (typeof window === 'undefined') return

    // Largest Contentful Paint
    if ('PerformanceObserver' in window) {
      // LCP
      try {
        const lcpObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries()
          const lastEntry = entries[entries.length - 1]
          if (lastEntry) {
            metrics.value.LCP = lastEntry.startTime
            console.log('[Web Vitals] LCP:', lastEntry.startTime.toFixed(2), 'ms')
          }
        })
        lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] })
      } catch (e) {
        // Observer might not be supported
      }

      // CLS
      try {
        let clsValue = 0
        const clsObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            if (!(entry as any).hadRecentInput) {
              clsValue += (entry as any).value
            }
          }
          metrics.value.CLS = clsValue
          console.log('[Web Vitals] CLS:', clsValue.toFixed(3))
        })
        clsObserver.observe({ entryTypes: ['layout-shift'] })
      } catch (e) {
        // Observer might not be supported
      }

      // FCP
      try {
        const fcpObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries()
          for (const entry of entries) {
            if (entry.name === 'first-contentful-paint') {
              metrics.value.FCP = entry.startTime
              console.log('[Web Vitals] FCP:', entry.startTime.toFixed(2), 'ms')
            }
          }
        })
        fcpObserver.observe({ entryTypes: ['paint'] })
      } catch (e) {
        // Observer might not be supported
      }
    }

    // First Input Delay (using event timing)
    try {
      if ('PerformanceEventTiming' in window) {
        const observer = new PerformanceObserver((list) => {
          for (const entry of list.getEntries() as any) {
            if (entry.entryType === 'first-input') {
              metrics.value.FID = entry.processingStart - entry.startTime
              console.log('[Web Vitals] FID:', metrics.value.FID.toFixed(2), 'ms')
            }
          }
        })
        observer.observe({ entryTypes: ['first-input'] })
      }
    } catch (e) {
      // Observer might not be supported
    }
  }

  // Initialize Web Vitals monitoring
  onMounted(() => {
    observeWebVitals()
  })

  return {
    startMeasure,
    endMeasure,
    measureAPICall,
    measureRender,
    getMeasures,
    getMetrics,
    metrics
  }
}

// Global performance helper for non-component usage
export const performanceHelper = {
  mark: (name: string) => performance.mark(name),
  measure: (name: string, startMark: string, endMark?: string) => {
    try {
      performance.measure(name, startMark, endMark)
    } catch (e) {
      // Ignore if marks don't exist
    }
  },
  now: () => performance.now()
}