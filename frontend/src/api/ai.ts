import request from '@/utils/request'

export interface AIModel {
  name: string
  size?: string
  parameterSize?: string
  family?: string
  format?: string
  modifiedAt?: string
}

export interface AIStatus {
  status: 'connected' | 'disconnected' | string
  message: string
  url?: string | null
  hint?: string | null
  localOnline: boolean
  remoteOnline: boolean
  remoteEnabled: boolean
}

export interface AIChatRequest {
  model: string
  prompt: string
  stream?: boolean
  context?: number[]
}

export const aiApi = {
  getModels: () => request.get('/ai/models'),
  getStatus: () => request.get('/ai/status'),
  chat: (data: AIChatRequest) => request.post('/ai/chat', { ...data, stream: false }),

  async chatStream(
    data: AIChatRequest,
    onMessage: (chunk: string) => void,
    onDone?: () => void,
    onError?: (err: Error) => void,
  ) {
    try {
      const token = localStorage.getItem('token')
      const res = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({ ...data, stream: true }),
      })
      if (!res.ok || !res.body) throw new Error(`AI stream failed: ${res.status}`)
      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        for (const line of lines) {
          const trimmed = line.trim()
          if (!trimmed) continue
          try {
            const json = JSON.parse(trimmed)
            if (json.response) onMessage(json.response)
            if (json.done) {
              onDone?.()
              return
            }
          } catch {
            onMessage(trimmed)
          }
        }
      }
      onDone?.()
    } catch (e) {
      onError?.(e instanceof Error ? e : new Error(String(e)))
    }
  },
}
