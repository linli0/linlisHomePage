<template>
  <div class="page-wrap">
    <h1 class="page-title">工具箱</h1>
    <p class="page-desc mb-8">常用开发小工具</p>

    <div class="flex flex-wrap gap-2 mb-6">
      <button
        v-for="t in tabs"
        :key="t.id"
        type="button"
        class="px-4 py-2 rounded-xl text-sm font-medium"
        :class="active === t.id ? 'bg-brand-500 text-white' : 'bg-white dark:bg-ink-900 border border-ink-200 text-ink-600'"
        @click="active = t.id"
      >
        {{ t.label }}
      </button>
    </div>

    <div class="card p-6 space-y-4">
      <textarea v-model="input" class="input min-h-[140px] font-mono text-sm" :placeholder="placeholder" />
      <div class="flex flex-wrap gap-2">
        <button
          v-for="a in actions"
          :key="a.label"
          type="button"
          class="btn-primary btn-sm"
          :disabled="busy"
          @click="run(a.fn)"
        >
          {{ a.label }}
        </button>
      </div>
      <div v-if="output">
        <div class="flex justify-between items-center mb-2">
          <p class="text-sm font-medium text-ink-600">结果</p>
          <button type="button" class="btn-ghost btn-sm" @click="copy">复制</button>
        </div>
        <pre class="input min-h-[100px] whitespace-pre-wrap font-mono text-sm overflow-auto">{{ output }}</pre>
      </div>
      <div v-if="qrUrl" class="pt-2">
        <img :src="qrUrl" alt="QR" class="w-48 h-48 rounded-xl border border-ink-100" />
      </div>
      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { toolsApi } from '@/api/tools'

type TabId = 'json' | 'base64' | 'url' | 'hash' | 'timestamp' | 'qr'

const tabs: { id: TabId; label: string }[] = [
  { id: 'json', label: 'JSON' },
  { id: 'base64', label: 'Base64' },
  { id: 'url', label: 'URL' },
  { id: 'hash', label: 'Hash' },
  { id: 'timestamp', label: '时间戳' },
  { id: 'qr', label: '二维码' },
]

const active = ref<TabId>('json')
const input = ref('')
const output = ref('')
const error = ref('')
const busy = ref(false)
const qrUrl = ref('')

const placeholder = computed(() => {
  const map: Record<TabId, string> = {
    json: '粘贴 JSON…',
    base64: '输入文本…',
    url: '输入 URL 或文本…',
    hash: '输入待哈希文本…',
    timestamp: '毫秒时间戳或日期字符串…',
    qr: '输入二维码内容…',
  }
  return map[active.value]
})

const actions = computed(() => {
  const map: Record<TabId, { label: string; fn: () => Promise<void> }[]> = {
    json: [
      { label: '格式化', fn: async () => { output.value = pick(await toolsApi.formatJson(input.value)) } },
      { label: '压缩', fn: async () => { output.value = pick(await toolsApi.minifyJson(input.value)) } },
    ],
    base64: [
      { label: '编码', fn: async () => { output.value = pick(await toolsApi.base64Encode(input.value)) } },
      { label: '解码', fn: async () => { output.value = pick(await toolsApi.base64Decode(input.value)) } },
    ],
    url: [
      { label: '编码', fn: async () => { output.value = pick(await toolsApi.urlEncode(input.value)) } },
      { label: '解码', fn: async () => { output.value = pick(await toolsApi.urlDecode(input.value)) } },
    ],
    hash: [
      { label: 'MD5', fn: async () => { output.value = pick(await toolsApi.md5(input.value)) } },
      { label: 'SHA256', fn: async () => { output.value = pick(await toolsApi.sha256(input.value)) } },
    ],
    timestamp: [
      {
        label: '转换',
        fn: async () => {
          const d = (await toolsApi.timestampConvert(input.value)).data.data
          output.value = typeof d === 'string' ? d : JSON.stringify(d, null, 2)
        },
      },
    ],
    qr: [
      {
        label: '生成',
        fn: async () => {
          const r = await toolsApi.generateQRCode(input.value)
          if (qrUrl.value) URL.revokeObjectURL(qrUrl.value)
          qrUrl.value = URL.createObjectURL(r.data as Blob)
          output.value = '二维码已生成'
        },
      },
    ],
  }
  return map[active.value]
})

function pick(res: { data: { data: unknown } }): string {
  const d = res.data.data
  if (typeof d === 'string') return d
  if (d && typeof d === 'object') {
    const o = d as Record<string, unknown>
    return String(o.result ?? o.encoded ?? o.decoded ?? o.hash ?? o.text ?? JSON.stringify(d, null, 2))
  }
  return String(d ?? '')
}

async function run(fn: () => Promise<void>) {
  error.value = ''
  busy.value = true
  try {
    await fn()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  } finally {
    busy.value = false
  }
}

async function copy() {
  if (output.value) await navigator.clipboard.writeText(output.value)
}
</script>
