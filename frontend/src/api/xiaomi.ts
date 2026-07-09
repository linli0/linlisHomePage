import request from '@/utils/request'

export interface XiaomiDevice {
  deviceId: string
  name: string
  model: string
  online: boolean
  volume: number
  lastSeen?: string
}

export interface XiaomiStatus {
  connected: boolean
  device?: XiaomiDevice
  error?: string
}

export interface TTSRequest {
  text: string
  volume?: number
}

export interface ChatRequest {
  message: string
  stream?: boolean
}

export interface ChatResponse {
  response: string
  done: boolean
}

export const xiaomiApi = {
  // 获取设备状态
  getStatus: () => request.get('/xiaomi/status'),

  // 文本转语音
  tts: (data: TTSRequest) => request.post('/xiaomi/tts', data),

  // 流式对话
  chatStream: async (
    message: string,
    onMessage: (chunk: string) => void,
    onDone?: () => void,
    onError?: (error: Error) => void
  ) => {
    const baseURL = import.meta.env.VITE_API_BASE_URL || ''
    const token = localStorage.getItem('token')

    try {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      }
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }

      const response = await fetch(`${baseURL}/xiaomi/chat`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          message,
          stream: true
        })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body?.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let doneCalled = false

      if (!reader) {
        throw new Error('No response body')
      }

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (!line.trim()) continue
          try {
            const parsed = JSON.parse(line)
            if (parsed.response) {
              onMessage(parsed.response)
            }
            if (parsed.done && !doneCalled) {
              doneCalled = true
              onDone?.()
            }
          } catch {
            // skip incomplete JSON
          }
        }
      }

      if (buffer.trim()) {
        try {
          const parsed = JSON.parse(buffer)
          if (parsed.response) onMessage(parsed.response)
          if (parsed.done && !doneCalled) {
            doneCalled = true
            onDone?.()
          }
        } catch {
          // skip
        }
      }

      if (!doneCalled) onDone?.()
    } catch (error) {
      onError?.(error as Error)
    }
  },

  // 设置音量
  setVolume: (volume: number) => request.post('/xiaomi/volume', { volume }),

  // 播放/暂停
  play: () => request.post('/xiaomi/play'),

  pause: () => request.post('/xiaomi/pause'),

  // 停止播放
  stop: () => request.post('/xiaomi/stop')
}