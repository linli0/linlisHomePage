<template>
  <div class="card flex flex-col h-[min(70vh,640px)]">
    <div class="flex items-center justify-between gap-3 px-4 py-3 border-b border-ink-100 dark:border-ink-800">
      <p class="font-semibold text-ink-900 dark:text-white">AI 对话</p>
      <select v-model="model" class="input !py-1.5 !px-3 text-sm w-auto min-w-[10rem]">
        <option v-for="m in models" :key="m.name" :value="m.name">{{ m.name }}</option>
      </select>
    </div>

    <div ref="listRef" class="flex-1 overflow-y-auto p-4 space-y-3">
      <div
        v-for="(msg, i) in messages"
        :key="i"
        :class="[
          'max-w-[85%] rounded-2xl px-4 py-2.5 text-sm leading-relaxed',
          msg.role === 'user'
            ? 'ml-auto bg-brand-500 text-white'
            : 'bg-ink-50 dark:bg-ink-950 text-ink-800 dark:text-ink-200 border border-ink-100 dark:border-ink-800'
        ]"
      >
        <div v-if="msg.role === 'assistant'" v-html="renderMd(msg.content)" />
        <span v-else>{{ msg.content }}</span>
      </div>
      <p v-if="streaming" class="text-xs text-ink-400">生成中…</p>
    </div>

    <form class="p-4 border-t border-ink-100 dark:border-ink-800 flex gap-2" @submit.prevent="send">
      <input v-model="prompt" class="input flex-1" placeholder="输入消息…" :disabled="streaming" />
      <button type="submit" class="btn-primary shrink-0" :disabled="streaming || !prompt.trim()">发送</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { aiApi, type AIModel } from '@/api/ai'
import { useSettingsStore } from '@/stores/settings'

interface Msg {
  role: 'user' | 'assistant'
  content: string
}

const settings = useSettingsStore()
const models = ref<AIModel[]>([])
const model = ref(settings.settings.ai.defaultModel || '')
const messages = ref<Msg[]>([])
const prompt = ref('')
const streaming = ref(false)
const listRef = ref<HTMLElement>()

function renderMd(text: string) {
  return DOMPurify.sanitize(marked.parse(text, { async: false }) as string)
}

async function scrollBottom() {
  await nextTick()
  if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
}

async function send() {
  const text = prompt.value.trim()
  if (!text || streaming.value) return
  messages.value.push({ role: 'user', content: text })
  prompt.value = ''
  messages.value.push({ role: 'assistant', content: '' })
  const idx = messages.value.length - 1
  streaming.value = true
  await scrollBottom()

  await aiApi.chatStream(
    { model: model.value, prompt: text, stream: true },
    (chunk) => {
      messages.value[idx].content += chunk
      scrollBottom()
    },
    () => { streaming.value = false },
    (err) => {
      messages.value[idx].content += `\n[错误] ${err.message}`
      streaming.value = false
    },
  )
}

watch(model, (v) => settings.updateAIConfig({ defaultModel: v }))

onMounted(async () => {
  try {
    const res = await aiApi.getModels()
    const data = res.data.data
    models.value = Array.isArray(data) ? data : (data?.models ?? [])
    if (models.value.length) {
      if (!model.value || !models.value.find((m) => m.name === model.value)) {
        model.value = models.value[0].name
        settings.updateAIConfig({ defaultModel: model.value })
      }
    } else if (!model.value) {
      model.value = 'qwen3.5-4b:latest'
    }
  } catch {
    if (!model.value) model.value = 'qwen3.5-4b:latest'
    models.value = [{ name: model.value }]
  }
})
</script>
