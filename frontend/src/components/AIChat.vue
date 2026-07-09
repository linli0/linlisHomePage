<template>
  <div class="h-full flex flex-col">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <div class="relative">
          <div class="absolute inset-0 bg-accent-400/30 rounded-xl blur-lg animate-pulse-slow"></div>
          <div class="relative w-12 h-12 bg-gradient-to-br from-accent-400 to-accent-600 rounded-xl flex items-center justify-center shadow-lg">
            <span class="text-2xl">🤖</span>
          </div>
        </div>
        <div>
          <h2 class="text-xl font-bold text-surface-900 dark:text-white">AI 对话</h2>
          <div class="flex items-center gap-2 text-sm">
            <span 
              :class="[
                'relative flex h-2 w-2',
                status === 'connected' ? 'text-green-500' : 'text-red-500'
              ]"
            >
              <span 
                :class="[
                  'absolute inline-flex h-full w-full rounded-full opacity-75',
                  status === 'connected' ? 'bg-green-400 animate-ping' : 'bg-red-400'
                ]"
              ></span>
              <span 
                :class="[
                  'relative inline-flex rounded-full h-2 w-2',
                  status === 'connected' ? 'bg-green-500' : 'bg-red-500'
                ]"
              ></span>
            </span>
            <span :class="status === 'connected' ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
              {{ status === 'connected' ? '已连接到 Ollama' : '未连接到 Ollama' }}
            </span>
          </div>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <select 
          v-model="selectedModel" 
          class="input-cyber w-52 text-sm"
          :disabled="models.length === 0 || isLoading"
        >
          <option value="">选择模型</option>
          <option v-for="model in models" :key="model.name" :value="model.name">
            {{ model.name }} ({{ model.parameterSize }})
          </option>
        </select>
        <button 
          @click="refreshModels" 
          class="btn-cyber-secondary p-2.5"
          :disabled="isRefreshing"
          title="刷新模型列表"
        >
          <svg class="w-4 h-4" :class="{ 'animate-spin': isRefreshing }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </button>
        <button 
          @click="clearChat" 
          class="btn-secondary p-2.5 text-red-500 hover:text-red-600 hover:border-red-300 dark:hover:border-red-700"
          title="清空对话"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      </div>
    </div>

    <div ref="messagesContainer" class="flex-1 overflow-y-auto bg-surface-100 dark:bg-surface-800/50 rounded-2xl p-6 mb-6 min-h-[300px] max-h-[500px] border border-surface-200 dark:border-surface-700">
      <div v-if="messages.length === 0" class="h-full flex flex-col items-center justify-center text-surface-400">
        <div class="relative mb-6">
          <div class="absolute inset-0 bg-primary-400/20 rounded-2xl blur-xl"></div>
          <div class="relative w-20 h-20 bg-gradient-to-br from-primary-100 to-primary-200 dark:from-primary-900/30 dark:to-primary-800/30 rounded-2xl flex items-center justify-center">
            <svg class="w-10 h-10 text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
            </svg>
          </div>
        </div>
        <p class="text-lg font-medium text-surface-600 dark:text-surface-300 mb-2">开始与 AI 对话</p>
        <p class="text-sm text-surface-400">选择模型后输入消息</p>
      </div>
      
      <div v-else class="space-y-6">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          class="flex gap-3"
          :class="message.role === 'user' ? 'flex-row-reverse' : 'flex-row'"
        >
          <div 
            class="flex-shrink-0 w-8 h-8 rounded-xl flex items-center justify-center text-sm"
            :class="message.role === 'user' 
              ? 'bg-gradient-to-br from-primary-400 to-primary-600 text-white' 
              : 'bg-gradient-to-br from-accent-400 to-accent-600 text-white'"
          >
            {{ message.role === 'user' ? '你' : 'AI' }}
          </div>
          <div 
            class="max-w-[75%] rounded-2xl px-5 py-3"
            :class="message.role === 'user' 
              ? 'bg-gradient-to-br from-primary-500 to-primary-600 text-white rounded-tr-md' 
              : 'bg-white dark:bg-surface-800 text-surface-900 dark:text-white shadow-soft border border-surface-200 dark:border-surface-700 rounded-tl-md'"
          >
            <div v-if="message.role === 'user'" class="whitespace-pre-wrap leading-relaxed">{{ message.content }}</div>
            <div v-else class="ai-message-content leading-relaxed" v-html="renderAiMessage(message.content)"></div>
          </div>
        </div>
        
        <div v-if="streamingContent" class="flex gap-3">
          <div class="flex-shrink-0 w-8 h-8 rounded-xl bg-gradient-to-br from-accent-400 to-accent-600 text-white flex items-center justify-center text-sm">
            AI
          </div>
          <div class="max-w-[75%] rounded-2xl rounded-tl-md px-5 py-3 bg-white dark:bg-surface-800 text-surface-900 dark:text-white shadow-soft border border-surface-200 dark:border-surface-700">
            <div class="ai-message-content leading-relaxed" v-html="renderAiMessage(streamingContent)"></div>
            <span class="inline-block w-2 h-4 bg-accent-500 animate-pulse ml-1 rounded-sm"></span>
          </div>
        </div>
      </div>
    </div>

    <div class="flex gap-3">
      <textarea
        v-model="inputMessage"
        placeholder="输入消息... (按 Enter 发送，Shift + Enter 换行)"
        class="input-cyber flex-1 resize-none"
        rows="3"
        :disabled="isLoading || !selectedModel"
        @keydown.enter.prevent="handleEnterKey"
      ></textarea>
      <button 
        @click="sendMessage" 
        class="btn-cyber-primary px-8"
        :disabled="isLoading || !inputMessage.trim() || !selectedModel"
      >
        <span v-if="isLoading" class="flex items-center gap-2">
          <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          发送中
        </span>
        <span v-else class="flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
          发送
        </span>
      </button>
    </div>
    
    <div v-if="errorMessage" class="mt-4 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/50 text-red-700 dark:text-red-300 rounded-xl text-sm flex items-start gap-3">
      <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <span>{{ errorMessage }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { aiApi, type AIModel } from '@/api/ai'

marked.setOptions({ breaks: true, gfm: true })

interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp?: number
}

// LocalStorage keys
const STORAGE_KEY_MESSAGES = 'ai_chat_messages'
const STORAGE_KEY_MODEL = 'ai_chat_model'

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
let lastContext: number[] = []

// Load messages from localStorage
function loadMessages() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY_MESSAGES)
    if (saved) {
      messages.value = JSON.parse(saved)
    }
  } catch (error) {
    console.error('Failed to load messages:', error)
  }
}

// Save messages to localStorage
function saveMessages() {
  try {
    localStorage.setItem(STORAGE_KEY_MESSAGES, JSON.stringify(messages.value))
  } catch (error) {
    console.error('Failed to save messages:', error)
  }
}

// Load selected model from localStorage
function loadSelectedModel() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY_MODEL)
    if (saved) {
      selectedModel.value = saved
    }
  } catch (error) {
    console.error('Failed to load model:', error)
  }
}

// Save selected model to localStorage
function saveSelectedModel() {
  try {
    localStorage.setItem(STORAGE_KEY_MODEL, selectedModel.value)
  } catch (error) {
    console.error('Failed to save model:', error)
  }
}

// Watch for changes and save
watch(messages, saveMessages, { deep: true })
watch(selectedModel, saveSelectedModel)

function renderAiMessage(content: string): string {
  const rawHtml = marked.parse(content) as string
  return DOMPurify.sanitize(rawHtml)
}

function buildPrompt(): string {
  const recentMessages = messages.value.slice(-6)
  if (recentMessages.length === 0) return inputMessage.value.trim()
  let prompt = ''
  for (const msg of recentMessages) {
    prompt += msg.role === 'user' ? `User: ${msg.content}\n\n` : `Assistant: ${msg.content}\n\n`
  }
  prompt += `User: ${inputMessage.value.trim()}\n\nAssistant:`
  return prompt
}

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
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    console.error('Failed to fetch models:', err)
    status.value = 'disconnected'
    if (e.response?.data?.message) {
      errorMessage.value = e.response.data.message
    }
  } finally {
    isRefreshing.value = false
  }
}

async function refreshModels() {
  await fetchModels()
}

async function checkStatus() {
  try {
    const res = await aiApi.getStatus()
    status.value = res.data.status === 'connected' ? 'connected' : 'disconnected'
  } catch {
    status.value = 'disconnected'
  }
}

async function sendMessage() {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value || !selectedModel.value) return

  messages.value.push({
    role: 'user',
    content: message,
    timestamp: Date.now()
  })
  inputMessage.value = ''
  isLoading.value = true
  errorMessage.value = ''
  streamingContent.value = ''

  scrollToBottom()
  
  const prompt = buildPrompt()
  
  await aiApi.chatStream(
    {
      model: selectedModel.value,
      prompt,
      stream: true,
      context: lastContext
    },
    (chunk) => {
      streamingContent.value += chunk
      scrollToBottom()
    },
    () => {
      if (streamingContent.value) {
        messages.value.push({
          role: 'assistant',
          content: streamingContent.value,
          timestamp: Date.now()
        })
        streamingContent.value = ''
      }
      isLoading.value = false
      scrollToBottom()
    },
    (error) => {
      console.error('Chat error:', error)
      errorMessage.value = '发送消息失败: ' + error.message
      isLoading.value = false
      streamingContent.value = ''
    }
  )
}

function handleEnterKey(event: KeyboardEvent) {
  if (event.shiftKey) {
    return
  }
  sendMessage()
}

function clearChat() {
  if (confirm('确定要清空所有对话记录吗？')) {
    messages.value = []
    streamingContent.value = ''
    errorMessage.value = ''
    lastContext = []
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  loadMessages()
  loadSelectedModel()
  checkStatus()
  fetchModels()
})
</script>

<style>
.ai-message-content p { margin-bottom: 0.75rem; line-height: 1.7; }
.ai-message-content ul, .ai-message-content ol { margin-bottom: 0.75rem; padding-left: 1.5rem; }
.ai-message-content li { margin-bottom: 0.25rem; }
.ai-message-content code { 
  background-color: rgba(59, 130, 246, 0.1); 
  padding: 0.15rem 0.4rem; 
  border-radius: 0.375rem; 
  font-size: 0.875rem; 
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: #3b82f6;
}
.ai-message-content pre { 
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  color: #e2e8f0; 
  padding: 1rem; 
  border-radius: 0.75rem; 
  overflow-x: auto; 
  margin-bottom: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.ai-message-content pre code { 
  background: none; 
  padding: 0; 
  color: inherit; 
  font-size: 0.875rem; 
}
.ai-message-content blockquote { 
  border-left: 3px solid #3b82f6; 
  padding-left: 1rem; 
  margin-bottom: 0.75rem; 
  color: #64748b;
  font-style: italic;
}
.ai-message-content a { color: #3b82f6; text-decoration: underline; text-underline-offset: 2px; }
.ai-message-content a:hover { color: #2563eb; }
.ai-message-content strong { font-weight: 600; }
.ai-message-content h1, .ai-message-content h2, .ai-message-content h3 { 
  font-weight: 700; 
  margin-top: 1rem; 
  margin-bottom: 0.5rem; 
  line-height: 1.3;
}
.ai-message-content h1 { font-size: 1.5rem; }
.ai-message-content h2 { font-size: 1.25rem; }
.ai-message-content h3 { font-size: 1.125rem; }
.dark .ai-message-content code { 
  background-color: rgba(96, 165, 250, 0.15); 
  color: #60a5fa;
}
</style>
