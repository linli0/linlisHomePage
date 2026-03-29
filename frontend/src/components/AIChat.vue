<template>
  <div class="h-full flex flex-col">
    <!-- Header -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 bg-purple-100 dark:bg-purple-900/30 rounded-lg flex items-center justify-center">
          <span class="text-xl">🤖</span>
        </div>
        <div>
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white">AI 对话</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400">
            {{ status === 'connected' ? '✓ 已连接到 Ollama' : '✗ 未连接到 Ollama' }}
          </p>
        </div>
      </div>
      <div class="flex items-center space-x-2">
        <select 
          v-model="selectedModel" 
          class="input w-48 text-sm"
          :disabled="models.length === 0 || isLoading"
        >
          <option value="">选择模型</option>
          <option v-for="model in models" :key="model.name" :value="model.name">
            {{ model.name }} ({{ model.parameterSize }})
          </option>
        </select>
        <button 
          @click="refreshModels" 
          class="btn-secondary p-2"
          :disabled="isRefreshing"
          title="刷新模型列表"
        >
          <svg class="w-4 h-4" :class="{ 'animate-spin': isRefreshing }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </button>
        <button 
          @click="clearChat" 
          class="btn-secondary p-2 text-red-500 hover:text-red-600"
          title="清空对话"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Messages Area -->
    <div ref="messagesContainer" class="flex-1 overflow-y-auto bg-gray-50 dark:bg-gray-900 rounded-lg p-4 mb-4 min-h-[300px] max-h-[500px]">
      <div v-if="messages.length === 0" class="h-full flex flex-col items-center justify-center text-gray-400">
        <svg class="w-16 h-16 mb-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
        </svg>
        <p>开始与 AI 对话</p>
        <p class="text-sm mt-1">选择模型后输入消息</p>
      </div>
      
      <div v-else class="space-y-4">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          class="flex"
          :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
        >
          <div 
            class="max-w-[80%] rounded-lg px-4 py-2"
            :class="message.role === 'user' 
              ? 'bg-blue-500 text-white' 
              : 'bg-white dark:bg-gray-800 text-gray-900 dark:text-white shadow-sm border border-gray-200 dark:border-gray-700'"
          >
            <div class="text-sm mb-1 opacity-70">
              {{ message.role === 'user' ? '你' : 'AI' }}
            </div>
            <div class="whitespace-pre-wrap">{{ message.content }}</div>
          </div>
        </div>
        
        <!-- Streaming Response -->
        <div v-if="streamingContent" class="flex justify-start">
          <div class="max-w-[80%] rounded-lg px-4 py-2 bg-white dark:bg-gray-800 text-gray-900 dark:text-white shadow-sm border border-gray-200 dark:border-gray-700">
            <div class="text-sm mb-1 opacity-70">AI</div>
            <div class="whitespace-pre-wrap">{{ streamingContent }}</div>
            <span class="inline-block w-2 h-4 bg-blue-500 animate-pulse ml-1"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Area -->
    <div class="flex space-x-2">
      <textarea
        v-model="inputMessage"
        placeholder="输入消息..."
        class="input flex-1 resize-none"
        rows="3"
        :disabled="isLoading || !selectedModel"
        @keydown.enter.prevent="handleEnterKey"
      ></textarea>
      <button 
        @click="sendMessage" 
        class="btn-primary px-6"
        :disabled="isLoading || !inputMessage.trim() || !selectedModel"
      >
        <span v-if="isLoading" class="flex items-center">
          <svg class="w-4 h-4 animate-spin mr-2" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          发送中
        </span>
        <span v-else>发送</span>
      </button>
    </div>
    
    <!-- Error Message -->
    <div v-if="errorMessage" class="mt-2 p-3 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg text-sm">
      {{ errorMessage }}
    </div>
    
    <!-- Tips -->
    <div class="mt-2 text-xs text-gray-400 dark:text-gray-500">
      提示: 按 Enter 发送消息，Shift + Enter 换行
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { aiApi, type AIModel } from '@/api/ai'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const models = ref<AIModel[]>([])
const selectedModel = ref('')
const messages = ref<Message[]>([])
const inputMessage = ref('')
const streamingContent = ref('')
const isLoading = ref(false)
const isRefreshing = ref(false)
const status = ref<'connected' | 'disconnected'>('disconnected')
const errorMessage = ref('')
const messagesContainer = ref<HTMLDivElement>()

// 获取模型列表
async function fetchModels() {
  isRefreshing.value = true
  errorMessage.value = ''
  
  try {
    const res = await aiApi.getModels()
    models.value = res.data.models || []
    
    if (models.value.length > 0 && !selectedModel.value) {
      selectedModel.value = models.value[0].name
    }
    
    status.value = 'connected'
  } catch (error: any) {
    console.error('Failed to fetch models:', error)
    status.value = 'disconnected'
    if (error.response?.data?.message) {
      errorMessage.value = error.response.data.message
    }
  } finally {
    isRefreshing.value = false
  }
}

// 刷新模型列表
async function refreshModels() {
  await fetchModels()
}

// 检查 Ollama 状态
async function checkStatus() {
  try {
    const res = await aiApi.getStatus()
    status.value = res.data.status === 'connected' ? 'connected' : 'disconnected'
  } catch {
    status.value = 'disconnected'
  }
}

// 发送消息
async function sendMessage() {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value || !selectedModel.value) return
  
  // 添加用户消息
  messages.value.push({ role: 'user', content: message })
  inputMessage.value = ''
  isLoading.value = true
  errorMessage.value = ''
  streamingContent.value = ''
  
  scrollToBottom()
  
  // 调用 AI API
  await aiApi.chatStream(
    {
      model: selectedModel.value,
      prompt: message,
      stream: true
    },
    // onMessage - 收到流式数据
    (chunk) => {
      streamingContent.value += chunk
      scrollToBottom()
    },
    // onDone - 完成
    () => {
      if (streamingContent.value) {
        messages.value.push({ 
          role: 'assistant', 
          content: streamingContent.value 
        })
        streamingContent.value = ''
      }
      isLoading.value = false
      scrollToBottom()
    },
    // onError - 错误
    (error) => {
      console.error('Chat error:', error)
      errorMessage.value = '发送消息失败: ' + error.message
      isLoading.value = false
      streamingContent.value = ''
    }
  )
}

// 处理 Enter 键
function handleEnterKey(event: KeyboardEvent) {
  if (event.shiftKey) {
    // Shift + Enter 换行，不处理
    return
  }
  sendMessage()
}

// 清空对话
function clearChat() {
  if (confirm('确定要清空所有对话记录吗？')) {
    messages.value = []
    streamingContent.value = ''
    errorMessage.value = ''
  }
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  checkStatus()
  fetchModels()
})
</script>
