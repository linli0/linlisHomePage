<template>
  <div class="min-h-screen bg-surface-50 dark:bg-surface-950 py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-10">
        <span class="inline-block px-4 py-1.5 rounded-full bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 text-sm font-medium mb-4">
          智能音箱
        </span>
        <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-4">
          小爱<span class="gradient-text">音箱控制</span>
        </h1>
        <p class="text-lg text-surface-600 dark:text-surface-400 max-w-2xl">
          控制小爱音箱 Pro (LX06)，支持语音对话和 TTS 语音合成
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 设备状态面板 -->
        <div class="card-hover p-6 lg:col-span-1">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-orange-400/20 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-orange-400 to-orange-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">🔊</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">设备状态</h2>
              <div class="flex items-center gap-2 text-sm">
                <span 
                  :class="[
                    'relative flex h-2 w-2',
                    deviceStatus === 'online' ? 'text-green-500' : 'text-red-500'
                  ]"
                >
                  <span 
                    :class="[
                      'absolute inline-flex h-full w-full rounded-full opacity-75',
                      deviceStatus === 'online' ? 'bg-green-400 animate-ping' : 'bg-red-400'
                    ]"
                  ></span>
                  <span 
                    :class="[
                      'relative inline-flex rounded-full h-2 w-2',
                      deviceStatus === 'online' ? 'bg-green-500' : 'bg-red-500'
                    ]"
                  ></span>
                </span>
                <span :class="deviceStatus === 'online' ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
                  {{ deviceStatus === 'online' ? '在线' : '离线' }}
                </span>
              </div>
            </div>
          </div>

          <!-- 设备信息 -->
          <div v-if="deviceInfo" class="space-y-4 mb-6">
            <div class="p-4 bg-surface-100 dark:bg-surface-800 rounded-xl">
              <div class="flex items-center gap-3 mb-3">
                <svg class="w-5 h-5 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                <span class="text-sm font-medium text-surface-700 dark:text-surface-300">设备信息</span>
              </div>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-surface-500 dark:text-surface-400">名称</span>
                  <span class="text-surface-900 dark:text-white font-medium">{{ deviceInfo.name }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-surface-500 dark:text-surface-400">型号</span>
                  <span class="text-surface-900 dark:text-white font-medium">{{ deviceInfo.model }}</span>
                </div>
                <div v-if="deviceInfo.lastSeen" class="flex justify-between">
                  <span class="text-surface-500 dark:text-surface-400">最后在线</span>
                  <span class="text-surface-900 dark:text-white font-medium">{{ deviceInfo.lastSeen }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 音量控制 -->
          <div class="mb-6">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-medium text-surface-700 dark:text-surface-300">音量</span>
              <span class="text-sm text-surface-500 dark:text-surface-400">{{ volume }}%</span>
            </div>
            <div class="flex items-center gap-3">
              <button 
                @click="decreaseVolume" 
                class="btn-secondary p-2"
                :disabled="volume <= 0"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                </svg>
              </button>
              <input
                v-model.number="volume"
                type="range"
                min="0"
                max="100"
                class="flex-1 h-2 bg-surface-200 dark:bg-surface-700 rounded-lg appearance-none cursor-pointer accent-orange-500"
                @change="setVolume"
              />
              <button 
                @click="increaseVolume" 
                class="btn-secondary p-2"
                :disabled="volume >= 100"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
              </button>
            </div>
          </div>

          <!-- 播放控制 -->
          <div class="flex gap-2">
            <button @click="playAudio" class="btn-primary flex-1 flex items-center justify-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              播放
            </button>
            <button @click="pauseAudio" class="btn-secondary flex-1 flex items-center justify-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              暂停
            </button>
            <button @click="stopAudio" class="btn-secondary flex-1 flex items-center justify-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 10a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z" />
              </svg>
              停止
            </button>
          </div>

          <!-- 刷新状态按钮 -->
          <button 
            @click="checkStatus" 
            class="btn-secondary w-full mt-4 flex items-center justify-center gap-2"
            :disabled="isRefreshing"
          >
            <svg class="w-4 h-4" :class="{ 'animate-spin': isRefreshing }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            刷新状态
          </button>
        </div>

        <!-- TTS 和 AI 对话面板 -->
        <div class="card-hover p-6 lg:col-span-2">
          <!-- TTS 部分 -->
          <div class="mb-8">
            <div class="flex items-center gap-4 mb-4">
              <div class="relative">
                <div class="absolute inset-0 bg-primary-400/20 rounded-xl blur-lg"></div>
                <div class="relative w-10 h-10 bg-gradient-to-br from-primary-400 to-primary-600 rounded-xl flex items-center justify-center shadow-lg">
                  <span class="text-xl">📢</span>
                </div>
              </div>
              <div>
                <h3 class="text-lg font-bold text-surface-900 dark:text-white">语音合成 (TTS)</h3>
                <p class="text-sm text-surface-500 dark:text-surface-400">输入文本，音箱将朗读</p>
              </div>
            </div>
            
            <div class="flex gap-3">
              <textarea
                v-model="ttsText"
                placeholder="输入要朗读的文本..."
                class="input flex-1 resize-none"
                rows="2"
                :disabled="isTtsLoading"
              ></textarea>
              <button 
                @click="playTts" 
                class="btn-primary px-6 self-end"
                :disabled="isTtsLoading || !ttsText.trim()"
              >
                <span v-if="isTtsLoading" class="flex items-center gap-2">
                  <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                </span>
                <span v-else class="flex items-center gap-2">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
                  </svg>
                  播放
                </span>
              </button>
            </div>
          </div>

          <hr class="border-surface-200 dark:border-surface-700 my-6" />

          <!-- AI 对话部分 -->
          <div>
            <div class="flex items-center gap-4 mb-4">
              <div class="relative">
                <div class="absolute inset-0 bg-accent-400/20 rounded-xl blur-lg"></div>
                <div class="relative w-10 h-10 bg-gradient-to-br from-accent-400 to-accent-600 rounded-xl flex items-center justify-center shadow-lg">
                  <span class="text-xl">🤖</span>
                </div>
              </div>
              <div>
                <h3 class="text-lg font-bold text-surface-900 dark:text-white">AI 对话</h3>
                <p class="text-sm text-surface-500 dark:text-surface-400">与 AI 智能对话，答案将通过音箱播放</p>
              </div>
            </div>

            <div ref="messagesContainer" class="bg-surface-100 dark:bg-surface-800/50 rounded-2xl p-4 mb-4 min-h-[250px] max-h-[350px] overflow-y-auto border border-surface-200 dark:border-surface-700">
              <div v-if="messages.length === 0" class="h-full flex flex-col items-center justify-center text-surface-400 py-8">
                <svg class="w-12 h-12 mb-3 text-surface-300 dark:text-surface-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
                </svg>
                <p class="text-sm">开始与 AI 对话</p>
              </div>
              
              <div v-else class="space-y-4">
                <div 
                  v-for="(message, index) in messages" 
                  :key="index"
                  class="flex gap-3"
                  :class="message.role === 'user' ? 'flex-row-reverse' : 'flex-row'"
                >
                  <div 
                    class="flex-shrink-0 w-7 h-7 rounded-lg flex items-center justify-center text-xs"
                    :class="message.role === 'user' 
                      ? 'bg-gradient-to-br from-primary-400 to-primary-600 text-white' 
                      : 'bg-gradient-to-br from-accent-400 to-accent-600 text-white'"
                  >
                    {{ message.role === 'user' ? '你' : 'AI' }}
                  </div>
                  <div 
                    class="max-w-[80%] rounded-xl px-4 py-2 text-sm"
                    :class="message.role === 'user' 
                      ? 'bg-gradient-to-br from-primary-500 to-primary-600 text-white' 
                      : 'bg-white dark:bg-surface-800 text-surface-900 dark:text-white shadow-soft border border-surface-200 dark:border-surface-700'"
                  >
                    {{ message.content }}
                  </div>
                </div>
                
                <div v-if="streamingContent" class="flex gap-3">
                  <div class="flex-shrink-0 w-7 h-7 rounded-lg bg-gradient-to-br from-accent-400 to-accent-600 text-white flex items-center justify-center text-xs">
                    AI
                  </div>
                  <div class="max-w-[80%] rounded-xl px-4 py-2 text-sm bg-white dark:bg-surface-800 text-surface-900 dark:text-white shadow-soft border border-surface-200 dark:border-surface-700">
                    {{ streamingContent }}<span class="inline-block w-1.5 h-3 bg-accent-500 animate-pulse ml-1 rounded-sm"></span>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex gap-3">
              <textarea
                v-model="inputMessage"
                placeholder="输入消息... (按 Enter 发送，Shift + Enter 换行)"
                class="input flex-1 resize-none"
                rows="2"
                :disabled="isChatLoading"
                @keydown.enter.prevent="sendMessage"
              ></textarea>
              <button 
                @click="sendMessage" 
                class="btn-primary px-6 self-end"
                :disabled="isChatLoading || !inputMessage.trim()"
              >
                <span v-if="isChatLoading" class="flex items-center gap-2">
                  <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                </span>
                <span v-else class="flex items-center gap-2">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                  </svg>
                  发送
                </span>
              </button>
            </div>

            <div class="flex items-center gap-4 mt-4">
              <label class="flex items-center gap-2 text-sm text-surface-600 dark:text-surface-400">
                <input 
                  v-model="autoPlayResponse" 
                  type="checkbox" 
                  class="w-4 h-4 rounded border-surface-300 text-orange-500 focus:ring-orange-500 dark:border-surface-600 dark:bg-surface-800"
                />
                自动播放 AI 回答
              </label>
              <button 
                @click="clearChat" 
                class="text-sm text-red-500 hover:text-red-600 dark:hover:text-red-400"
              >
                清空对话
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMessage" class="mt-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/50 text-red-700 dark:text-red-300 rounded-xl text-sm flex items-start gap-3">
        <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span>{{ errorMessage }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { xiaomiApi, type XiaomiDevice } from '@/api/xiaomi'

interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp?: number
}

// LocalStorage keys
const STORAGE_KEY_MESSAGES = 'xiaomi_chat_messages'
const STORAGE_KEY_VOLUME = 'xiaomi_volume'

const deviceStatus = ref<'online' | 'offline'>('offline')
const deviceInfo = ref<XiaomiDevice | null>(null)
const volume = ref(50)
const isRefreshing = ref(false)
const errorMessage = ref('')

// TTS
const ttsText = ref('')
const isTtsLoading = ref(false)

// Chat
const messages = ref<Message[]>([])
const inputMessage = ref('')
const streamingContent = ref('')
const isChatLoading = ref(false)
const autoPlayResponse = ref(true)
const messagesContainer = ref<HTMLDivElement>()

// Load messages from localStorage
function loadMessages(): void {
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
function saveMessages(): void {
  try {
    localStorage.setItem(STORAGE_KEY_MESSAGES, JSON.stringify(messages.value))
  } catch (error) {
    console.error('Failed to save messages:', error)
  }
}

// Load volume from localStorage
function loadVolume(): void {
  try {
    const saved = localStorage.getItem(STORAGE_KEY_VOLUME)
    if (saved) {
      volume.value = parseInt(saved, 10)
    }
  } catch (error) {
    console.error('Failed to load volume:', error)
  }
}

// Save volume to localStorage
function saveVolume(): void {
  try {
    localStorage.setItem(STORAGE_KEY_VOLUME, volume.value.toString())
  } catch (error) {
    console.error('Failed to save volume:', error)
  }
}

async function checkStatus(): Promise<void> {
  isRefreshing.value = true
  errorMessage.value = ''
  
  try {
    const res = await xiaomiApi.getStatus()
    const status = res.data
    deviceStatus.value = status.connected ? 'online' : 'offline'
    deviceInfo.value = status.device || null
    
    if (status.device) {
      volume.value = status.device.volume
      saveVolume()
    }
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    console.error('Failed to check status:', err)
    deviceStatus.value = 'offline'
    if (e.response?.data?.message) {
      errorMessage.value = e.response.data.message
    }
  } finally {
    isRefreshing.value = false
  }
}

async function setVolume(): Promise<void> {
  try {
    await xiaomiApi.setVolume(volume.value)
    saveVolume()
  } catch (err: unknown) {
    console.error('Failed to set volume:', err)
    errorMessage.value = '设置音量失败'
  }
}

function increaseVolume(): void {
  if (volume.value < 100) {
    volume.value = Math.min(100, volume.value + 10)
    setVolume()
  }
}

function decreaseVolume(): void {
  if (volume.value > 0) {
    volume.value = Math.max(0, volume.value - 10)
    setVolume()
  }
}

async function playAudio(): Promise<void> {
  try {
    await xiaomiApi.play()
  } catch (err: unknown) {
    console.error('Failed to play:', err)
    errorMessage.value = '播放失败'
  }
}

async function pauseAudio(): Promise<void> {
  try {
    await xiaomiApi.pause()
  } catch (err: unknown) {
    console.error('Failed to pause:', err)
    errorMessage.value = '暂停失败'
  }
}

async function stopAudio(): Promise<void> {
  try {
    await xiaomiApi.stop()
  } catch (err: unknown) {
    console.error('Failed to stop:', err)
    errorMessage.value = '停止失败'
  }
}

async function playTts(): Promise<void> {
  const text = ttsText.value.trim()
  if (!text) return

  isTtsLoading.value = true
  errorMessage.value = ''

  try {
    await xiaomiApi.tts({ text, volume: volume.value })
    ttsText.value = ''
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    console.error('Failed to play TTS:', err)
    errorMessage.value = e.response?.data?.message || 'TTS 播放失败'
  } finally {
    isTtsLoading.value = false
  }
}

async function sendMessage(): Promise<void> {
  const message = inputMessage.value.trim()
  if (!message || isChatLoading.value) return

  messages.value.push({
    role: 'user',
    content: message,
    timestamp: Date.now()
  })
  inputMessage.value = ''
  isChatLoading.value = true
  errorMessage.value = ''
  streamingContent.value = ''

  scrollToBottom()
  
  await xiaomiApi.chatStream(
    message,
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
        
        // 自动播放 AI 回答
        if (autoPlayResponse.value && streamingContent.value) {
          playTtsFromText(streamingContent.value)
        }
        
        streamingContent.value = ''
      }
      isChatLoading.value = false
      scrollToBottom()
    },
    (error) => {
      console.error('Chat error:', error)
      errorMessage.value = '发送消息失败: ' + error.message
      isChatLoading.value = false
      streamingContent.value = ''
    }
  )
}

async function playTtsFromText(text: string): Promise<void> {
  try {
    await xiaomiApi.tts({ text, volume: volume.value })
  } catch (err) {
    console.error('Failed to auto-play TTS:', err)
  }
}

function clearChat(): void {
  if (confirm('确定要清空所有对话记录吗？')) {
    messages.value = []
    streamingContent.value = ''
    errorMessage.value = ''
  }
}

function scrollToBottom(): void {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  loadMessages()
  loadVolume()
  checkStatus()
})
</script>