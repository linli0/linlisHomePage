import request from '@/utils/request'

export interface AIModel {
  name: string
  size: number
  parameterSize: string
  family: string
  format: string
  modifiedAt: string
}

export interface AIChatRequest {
  model: string
  prompt: string
  stream?: boolean
  context?: number[]
}

export interface AIChatResponse {
  model: string
  response: string
  done: boolean
  context?: number[]
}

export const aiApi = {
  // 获取可用的模型列表
  getModels: () => request.get('/ai/models'),
  
  // 检查 Ollama 服务状态
  getStatus: () => request.get('/ai/status'),
  
  // 与 AI 对话（流式响应，使用 fetch 直接处理）
  chatStream: async (data: AIChatRequest, onMessage: (chunk: string) => void, onDone?: () => void, onError?: (error: Error) => void) => {
    const baseURL = import.meta.env.VITE_API_BASE_URL || ''
    
    try {
      const response = await fetch(`${baseURL}/api/ai/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...data,
          stream: true
        })
      })
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const reader = response.body?.getReader()
      const decoder = new TextDecoder()
      
      if (!reader) {
        throw new Error('No response body')
      }
      
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        
        const chunk = decoder.decode(value, { stream: true })
        // Ollama 的流式响应是 NDJSON 格式，每行一个 JSON 对象
        const lines = chunk.split('\n').filter(line => line.trim())
        
        for (const line of lines) {
          try {
            const parsed = JSON.parse(line)
            if (parsed.response) {
              onMessage(parsed.response)
            }
            if (parsed.done) {
              onDone?.()
            }
          } catch (e) {
            // 忽略解析错误
          }
        }
      }
      
      onDone?.()
    } catch (error) {
      onError?.(error as Error)
    }
  },
  
  // 非流式对话
  chat: (data: AIChatRequest) => request.post('/ai/chat', { ...data, stream: false })
}
