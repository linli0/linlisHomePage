import { ref, onMounted, watch } from 'vue'

export interface Alert {
  id: string
  targetPrice: number
  type: 'above' | 'below'
  currency: string
  createdAt: string
  sound?: boolean
}

const STORAGE_KEY = 'gold-price-alerts'

export function usePriceAlerts() {
  const alerts = ref<Alert[]>([])

  // Load alerts from localStorage
  function loadAlerts() {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      try {
        alerts.value = JSON.parse(stored)
      } catch (error) {
        console.error('Failed to parse alerts from localStorage:', error)
        alerts.value = []
      }
    }
  }

  // Save alerts to localStorage
  function saveAlerts() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(alerts.value))
  }

  // Get all alerts
  function getAlerts(): Alert[] {
    return alerts.value
  }

  // Add a new alert
  function addAlert(alert: Omit<Alert, 'id' | 'createdAt'>) {
    const newAlert: Alert = {
      ...alert,
      id: Date.now().toString(),
      createdAt: new Date().toISOString()
    }
    alerts.value.push(newAlert)
    saveAlerts()
    return newAlert
  }

  // Remove an alert by id
  function removeAlert(id: string) {
    alerts.value = alerts.value.filter(alert => alert.id !== id)
    saveAlerts()
  }

  // Check for triggered alerts
  function checkAlerts(currentPrice: number, currency: string): Alert[] {
    const triggered: Alert[] = []
    alerts.value.forEach(alert => {
      if (alert.currency !== currency) return
      
      if (alert.type === 'above' && currentPrice >= alert.targetPrice) {
        triggered.push(alert)
      } else if (alert.type === 'below' && currentPrice <= alert.targetPrice) {
        triggered.push(alert)
      }
    })
    return triggered
  }

  onMounted(() => {
    loadAlerts()
  })

  return {
    alerts,
    getAlerts,
    addAlert,
    removeAlert,
    checkAlerts
  }
}
