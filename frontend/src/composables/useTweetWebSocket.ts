import { ref, onMounted, onUnmounted } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export function useTweetWebSocket() {
  const connected = ref(false)
  const connecting = ref(false)
  const error = ref<string | null>(null)
  const stompClient = ref<Client | null>(null)

  const tweets = ref<any[]>([])
  const connectionStatus = ref<string>('disconnected')
  const stats = ref<any>(null)

  // Subscriptions
  let newTweetSubscription: any = null
  let statusSubscription: any = null
  let statsSubscription: any = null

  const connect = () => {
    if (connecting.value) return

    connecting.value = true
    error.value = null

    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws/tweets'
    const socket = new SockJS(wsUrl)
    stompClient.value = Client.over(socket)

    stompClient.value.connect({}, () => {
      connected.value = true
      connecting.value = false
      connectionStatus.value = 'connected'

      // Subscribe to topics
      newTweetSubscription = stompClient.value.subscribe('/topic/tweets/new', (message) => {
        const tweet = JSON.parse(message.body)
        tweets.value.unshift(tweet)
        if (tweets.value.length > 100) tweets.value.pop()
      })

      statusSubscription = stompClient.value.subscribe('/topic/tweets/status', (message) => {
        connectionStatus.value = JSON.parse(message.body).status
      })

      statsSubscription = stompClient.value.subscribe('/topic/tweets/stats', (message) => {
        stats.value = JSON.parse(message.body)
      })
    }, (err) => {
      error.value = err.message || 'Connection failed'
      connected.value = false
      connecting.value = false
      connectionStatus.value = 'disconnected'
    })
  }

  const disconnect = () => {
    if (stompClient.value && stompClient.value.connected) {
      newTweetSubscription?.unsubscribe()
      statusSubscription?.unsubscribe()
      statsSubscription?.unsubscribe()
      stompClient.value.disconnect()
    }
    connected.value = false
    connecting.value = false
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    connecting,
    error,
    tweets,
    connectionStatus,
    stats,
    reconnect: connect,
    disconnect
  }
}