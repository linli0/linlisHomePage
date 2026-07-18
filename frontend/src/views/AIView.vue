<template>
  <div class="page-wrap max-w-3xl">
    <h1 class="page-title">AI 对话</h1>
    <p class="page-desc mb-4">默认连接本地 Ollama；离线时请先启动模型，站点不会默认调用 DeepSeek</p>

    <div
      v-if="statusLoaded"
      class="card p-4 mb-6 text-sm"
      :class="statusBannerClass"
    >
      <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
        <span class="font-semibold">{{ statusLabel }}</span>
        <span v-if="selectedModel" class="text-ink-500 dark:text-ink-400">· 模型：{{ selectedModel }}</span>
      </div>
      <p v-if="statusHint" class="mt-1.5 text-ink-600 dark:text-ink-300">{{ statusHint }}</p>
    </div>

    <AIChat @model-change="selectedModel = $event" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AIChat from '@/components/AIChat.vue'
import { aiApi, type AIStatus } from '@/api/ai'

const status = ref<AIStatus | null>(null)
const statusLoaded = ref(false)
const selectedModel = ref('')

const isOnline = computed(() => status.value?.status === 'connected')

const statusLabel = computed(() => {
  if (!status.value) return ''
  if (isOnline.value) {
    return status.value.localOnline ? 'Ollama 在线（本地）' : 'Ollama 在线（远程）'
  }
  return 'Ollama 离线'
})

const statusHint = computed(() => {
  if (!status.value) return ''
  if (isOnline.value) return status.value.message
  return (
    status.value.hint ||
    '请在本机安装并启动 Ollama（ollama serve），然后刷新页面。不要依赖 DeepSeek 作为默认通道。'
  )
})

const statusBannerClass = computed(() =>
  isOnline.value
    ? 'border border-green-200 bg-green-50 dark:bg-green-950/30 dark:border-green-800 text-green-800 dark:text-green-200'
    : 'border border-amber-200 bg-amber-50 dark:bg-amber-950/30 dark:border-amber-800 text-amber-800 dark:text-amber-200',
)

onMounted(async () => {
  try {
    const res = await aiApi.getStatus()
    status.value = res.data.data
  } catch {
    status.value = {
      status: 'disconnected',
      message: '无法获取 AI 服务状态',
      hint: '请确认后端 API 已启动，并在本机运行 Ollama（ollama serve）。',
      localOnline: false,
      remoteOnline: false,
      remoteEnabled: false,
    }
  } finally {
    statusLoaded.value = true
  }
})
</script>
