<template>
  <div
    class="xiaomi-page"
    :class="{
      'xiaomi-page--tts': speaking,
      'xiaomi-page--user': userListening && !speaking,
    }"
  >
    <div class="wave-overlay" aria-hidden="true">
      <div class="wave-layer wave-layer--back" />
      <div class="wave-layer wave-layer--front" />
    </div>

    <header class="xiaomi-hero">
      <div class="xiaomi-hero-inner">
        <p class="xiaomi-kicker">CoffeeCookies · 小爱</p>
        <h1 class="page-title !text-brand-50">小爱对话台</h1>
        <p class="mt-2 text-brand-100/80 max-w-xl text-sm md:text-base leading-relaxed">
          网页切模式、打字对话；音箱负责播报。DeepSeek 作答，完成提醒可关。
        </p>
        <div class="mt-5 flex flex-wrap items-center gap-3 text-sm">
          <span
            class="inline-flex items-center gap-2 rounded-full px-3 py-1 bg-black/20 text-brand-50"
          >
            <span
              class="w-2 h-2 rounded-full"
              :class="status?.connected ? 'bg-emerald-400' : 'bg-ink-400'"
            />
            {{ status?.connected ? (status.device?.name || '音箱在线') : (status?.error || '未连接') }}
          </span>
          <span v-if="speaking" class="text-amber-200/90 text-xs tracking-wide">播报中 · 麦克暂停</span>
          <span v-else-if="userListening" class="text-emerald-200/90 text-xs tracking-wide">聆听中 · 请说话</span>
          <span class="text-brand-100/50 font-mono text-xs">{{ status?.device?.ip || '—' }}</span>
        </div>
      </div>
    </header>

    <div class="page-wrap max-w-3xl !pt-8 !pb-16 -mt-8 relative z-10">
      <!-- Mode switch -->
      <section class="card p-3 mb-5 shadow-sm">
        <div class="flex items-center justify-between gap-3 px-1 mb-2">
          <h2 class="font-display text-lg text-ink-900 dark:text-white">对话模式</h2>
          <button type="button" class="btn-ghost btn-sm" :disabled="busy" @click="refreshAll">刷新状态</button>
        </div>
        <div class="mode-seg" role="tablist" aria-label="对话模式">
          <button
            v-for="m in modes"
            :key="m.id"
            type="button"
            role="tab"
            class="mode-seg__btn"
            :class="{ 'mode-seg__btn--on': activeMode === m.id }"
            :aria-selected="activeMode === m.id"
            :disabled="busy"
            @click="switchMode(m.id)"
          >
            {{ m.label }}
          </button>
        </div>
        <p class="px-1 pt-2 text-xs text-ink-400">{{ modeHint }}</p>
      </section>

      <!-- Chat -->
      <section class="card overflow-hidden mb-5 shadow-sm">
        <div class="px-5 pt-4 pb-3 border-b border-ink-100 dark:border-ink-800 flex items-center justify-between gap-3">
          <div class="min-w-0">
            <h2 class="font-display text-lg text-ink-900 dark:text-white">对话</h2>
            <span class="text-xs text-ink-400">当前 · {{ routeLabel }}</span>
          </div>
          <button
            type="button"
            class="btn-ghost btn-sm shrink-0"
            aria-label="打开设置"
            @click="showSettings = true"
          >
            设置
          </button>
        </div>
        <div ref="logEl" class="chat-log px-5 py-4 space-y-3">
          <p v-if="!messages.length" class="text-sm text-ink-400 text-center py-10">
            选好模式后，在下方输入即可。也可说「小爱小爱」用语音进入多轮。
          </p>
          <div
            v-for="(m, i) in messages"
            :key="m.id ?? i"
            class="flex"
            :class="m.role === 'user' ? 'justify-end' : 'justify-start'"
          >
            <div
              class="chat-bubble"
              :class="{
                'chat-bubble--user': m.role === 'user',
                'chat-bubble--bot': m.role === 'assistant' || m.role === 'system',
              }"
            >
              <p class="whitespace-pre-wrap break-words">{{ m.content }}</p>
              <p v-if="m.route || m.provider" class="chat-meta">
                {{ m.route }}<template v-if="m.provider"> · {{ m.provider }}</template>
              </p>
            </div>
          </div>
        </div>
        <form class="chat-composer" @submit.prevent="sendChat">
          <input
            v-model="draft"
            class="input !rounded-none !border-0 !shadow-none flex-1 !py-4"
            placeholder="输入消息，Enter 发送…"
            :disabled="busy"
          />
          <button type="submit" class="btn-primary !rounded-none px-6" :disabled="busy || !draft.trim()">
            发送
          </button>
        </form>
      </section>

      <p v-if="msg" class="text-sm" :class="msgError ? 'text-red-600' : 'text-ink-500'">{{ msg }}</p>
    </div>

    <Teleport to="body">
      <div
        v-if="showSettings"
        class="settings-backdrop"
        role="dialog"
        aria-modal="true"
        aria-label="小爱设置"
        @click.self="showSettings = false"
      >
        <aside class="settings-panel">
          <div class="settings-panel__head">
            <h2 class="font-display text-lg text-ink-900 dark:text-white">设置与控制</h2>
            <button type="button" class="btn-ghost btn-sm" @click="showSettings = false">关闭</button>
          </div>

          <div class="settings-panel__body">
            <section class="settings-block">
              <h3 class="settings-block__title">音箱控制</h3>
              <div class="flex flex-wrap gap-2 mb-3">
                <button type="button" class="btn-outline btn-sm" :disabled="busy || !canControl" @click="doWake">唤醒</button>
                <button type="button" class="btn-outline btn-sm" :disabled="busy || !canControl" @click="run('stop')">停止</button>
              </div>
              <div class="flex gap-2 mb-4">
                <input v-model="ttsText" class="input flex-1" placeholder="直接让音箱说…" :disabled="!canControl" />
                <button type="button" class="btn-primary btn-sm" :disabled="busy || !canControl || !ttsText.trim()" @click="doTts">
                  播报
                </button>
              </div>
              <div class="flex items-center gap-3">
                <span class="text-xs text-ink-400 w-10">音量</span>
                <input
                  v-model.number="volume"
                  type="range"
                  min="0"
                  max="100"
                  class="flex-1 accent-brand-500"
                  :disabled="!canControl || volBusy"
                  @input="onVolumeInput"
                />
                <span class="text-xs font-mono text-ink-500 w-8">{{ volume }}</span>
              </div>
              <p v-if="volMsg" class="mt-2 text-xs" :class="volError ? 'text-red-600' : 'text-ink-400'">{{ volMsg }}</p>
            </section>

            <section v-if="auth.isAdmin" class="settings-block">
              <h3 class="settings-block__title">对话选项</h3>
              <label class="flex items-center gap-2 text-sm text-ink-600 dark:text-ink-300">
                <input v-model="dlgSettings.announceEnabled" type="checkbox" class="accent-brand-500" @change="saveSettings" />
                完成播报总开关
              </label>
              <div v-if="dlgSettings.announceEnabled" class="mt-2 space-y-2 pl-1">
                <label class="flex items-center gap-2 text-sm text-ink-600 dark:text-ink-300">
                  <input v-model="dlgSettings.announceCursor" type="checkbox" class="accent-brand-500" @change="saveSettings" />
                  Cursor 主会话
                </label>
                <label class="flex items-center gap-2 text-sm text-ink-600 dark:text-ink-300">
                  <input v-model="dlgSettings.announceCodex" type="checkbox" class="accent-brand-500" @change="saveSettings" />
                  Codex
                </label>
                <label class="flex items-center gap-2 text-sm text-ink-600 dark:text-ink-300">
                  <input v-model="dlgSettings.announceSubagent" type="checkbox" class="accent-brand-500" @change="saveSettings" />
                  Cursor 子 agent
                </label>
                <div class="flex flex-wrap items-center gap-2 text-sm pt-1">
                  <span class="text-ink-500">播报力度</span>
                  <select v-model="dlgSettings.announceDetail" class="input !py-2 !w-auto" @change="saveSettings">
                    <option value="brief">简短（固定句）</option>
                    <option value="detailed">详细（摘要/末段，截断）</option>
                  </select>
                </div>
              </div>
              <label class="flex items-center gap-2 text-sm text-ink-600 dark:text-ink-300 mt-3">
                <input v-model="dlgSettings.voiceInputEnabled" type="checkbox" class="accent-brand-500" @change="saveSettings" />
                本地麦克风唤醒
              </label>
              <div class="flex flex-wrap items-center gap-2 text-sm mt-3">
                <span class="text-ink-500">模型通道</span>
                <select v-model="dlgSettings.provider" class="input !py-2 !w-auto" @change="saveSettings">
                  <option value="ollama">Ollama（默认）</option>
                  <option value="deepseek">DeepSeek（备用）</option>
                </select>
                <span class="text-xs text-ink-400">{{ voiceInfo }}</span>
              </div>
            </section>

            <section v-if="auth.isAdmin" class="settings-block">
              <h3 class="settings-block__title">Panel 关键词</h3>
              <ul class="text-sm space-y-2 mb-3">
                <li
                  v-for="k in panelKeywords"
                  :key="k.id"
                  class="flex justify-between gap-2 py-1.5 border-b border-ink-50 dark:border-ink-800"
                >
                  <span><span class="font-mono text-xs text-brand-700">{{ k.keyword }}</span> → {{ k.actionType }}</span>
                  <button type="button" class="text-xs text-ink-400 hover:text-red-600" @click="removeKeyword(k.id)">删除</button>
                </li>
              </ul>
              <div class="flex flex-wrap gap-2">
                <input v-model="newKw" class="input !py-2 !w-36" placeholder="关键词" />
                <select v-model="newAction" class="input !py-2 !w-auto">
                  <option value="gold_price">今日金价</option>
                  <option value="gold_refresh">刷新金价</option>
                  <option value="speaker_status">音箱状态</option>
                  <option value="say">自定义播报</option>
                  <option value="help">帮助</option>
                </select>
                <input v-if="newAction === 'say'" v-model="newSay" class="input !py-2 !w-36" placeholder="播报文本" />
                <button type="button" class="btn-outline btn-sm" @click="addKeyword">添加</button>
              </div>
            </section>

            <section v-if="auth.isAdmin" class="settings-block">
              <h3 class="settings-block__title">账号绑定</h3>
              <div v-if="account?.bound" class="text-sm text-ink-500">
                {{ account.device?.name }} · {{ account.device?.ip }} · {{ account.device?.model }}
                <button type="button" class="btn-outline btn-sm ml-2" :disabled="busy" @click="doUnbind">解绑</button>
              </div>
              <template v-else>
                <div v-if="bindStep === 'login'" class="space-y-2 max-w-sm">
                  <input v-model="miUser" class="input !py-2" placeholder="小米账号" />
                  <input v-model="miPass" class="input !py-2" type="password" placeholder="密码" />
                  <button type="button" class="btn-primary btn-sm" :disabled="busy" @click="doLogin">登录</button>
                </div>
                <div v-else-if="bindStep === 'sms'" class="space-y-2 max-w-sm">
                  <input v-model="smsCode" class="input !py-2" placeholder="短信验证码" />
                  <button type="button" class="btn-primary btn-sm" :disabled="busy" @click="doVerify">验证</button>
                </div>
                <ul v-else class="space-y-2 text-sm">
                  <li v-for="d in cloudDevices" :key="d.did" class="flex justify-between gap-2">
                    <span>{{ d.name }} · {{ d.ip }}</span>
                    <button type="button" class="btn-primary btn-sm" :disabled="!d.hasToken || !d.ip" @click="doBind(d.did)">绑定</button>
                  </li>
                </ul>
              </template>
            </section>
          </div>
        </aside>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  xiaomiApi,
  type XiaomiStatus,
  type AccountStatus,
  type CloudDevice,
  type ChatMessage,
  type DialogueSettings,
  type PanelKeyword,
} from '@/api/xiaomi'

const auth = useAuthStore()
const status = ref<XiaomiStatus | null>(null)
const account = ref<AccountStatus | null>(null)
const canControl = ref(false)
const route = ref('idle')
const speaking = ref(false)
const userListening = ref(false)
const showSettings = ref(false)
const messages = ref<ChatMessage[]>([])
const draft = ref('')
const dlgSettings = reactive<DialogueSettings>({
  announceEnabled: true,
  voiceInputEnabled: true,
  provider: 'ollama',
  announceCursor: true,
  announceCodex: true,
  announceSubagent: false,
  announceDetail: 'brief',
})
const panelKeywords = ref<PanelKeyword[]>([])
const newKw = ref('')
const newAction = ref('gold_price')
const newSay = ref('')
const voiceInfo = ref('')

const bindStep = ref<'login' | 'sms' | 'pick'>('login')
const sessionId = ref('')
const miUser = ref('')
const miPass = ref('')
const smsCode = ref('')
const cloudDevices = ref<CloudDevice[]>([])

const ttsText = ref('你好，我是 CoffeeCookies')
const volume = ref(50)
const volBusy = ref(false)
const volMsg = ref('')
const volError = ref(false)
const busy = ref(false)
const msg = ref('')
const msgError = ref(false)
const logEl = ref<HTMLElement | null>(null)

let ws: WebSocket | null = null
let volTimer: ReturnType<typeof setTimeout> | null = null

function applyStatus(data: { route?: string; speaking?: boolean; listening?: boolean }) {
  if (data.route) route.value = data.route
  speaking.value = !!data.speaking
  userListening.value = !!data.listening
}

const modes = [
  { id: 'idle', label: '空闲' },
  { id: 'multi', label: '多轮' },
  { id: 'panel', label: 'Panel' },
  { id: 'codex', label: 'Codex' },
  { id: 'debug', label: '调试' },
] as const

const MODE_IDS = new Set(['idle', 'multi', 'panel', 'codex', 'debug'])

const activeMode = computed(() =>
  MODE_IDS.has(route.value) ? route.value : 'idle',
)

const routeLabel = computed(() => {
  const m: Record<string, string> = {
    idle: '空闲',
    multi: '多轮',
    codex: 'Codex',
    panel: 'Panel',
    debug: '调试',
    speaking: '播报中',
  }
  return m[route.value] || route.value
})

const modeHint = computed(() => {
  const h: Record<string, string> = {
    idle: '不处理开放问答；点「多轮」或说小爱小爱开始。',
    multi: '开放问答走 DeepSeek；也可再切 Panel / Codex。',
    panel: '按关键词执行网站动作，如「今日金价」。',
    codex: '后续消息发给本机 Codex（最近 session）。',
    debug: 'Panel 关键词与 Codex 入口同时可用。',
  }
  return h[activeMode.value] || ''
})

function errDetail(e: unknown): string {
  const any = e as { response?: { data?: { detail?: string; message?: string } }; message?: string }
  const d = any?.response?.data?.detail
  if (typeof d === 'string') return d
  return any?.response?.data?.message || any?.message || '请求失败'
}

async function scrollLog() {
  await nextTick()
  if (logEl.value) logEl.value.scrollTop = logEl.value.scrollHeight
}

function connectWs() {
  const token = localStorage.getItem('token')
  if (!token) return
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  const url = `${proto}://${location.host}/api/xiaomi/dialogue/ws?token=${encodeURIComponent(token)}`
  ws = new WebSocket(url)
  ws.onmessage = (ev) => {
    try {
      const data = JSON.parse(ev.data)
      if (data.type === 'hello') {
        applyStatus(data.status || {})
        if (Array.isArray(data.messages)) messages.value = data.messages
        scrollLog()
      } else if (data.type === 'message') {
        messages.value.push({
          id: data.id,
          role: data.role,
          content: data.content,
          route: data.route,
          provider: data.provider,
          createdAt: data.createdAt,
        })
        scrollLog()
      } else if (data.type === 'status') {
        applyStatus(data)
      }
    } catch {
      /* ignore */
    }
  }
  ws.onclose = () => {
    setTimeout(connectWs, 3000)
  }
}

async function switchMode(mode: string) {
  if (mode === route.value) return
  busy.value = true
  msgError.value = false
  try {
    const res = await xiaomiApi.setMode(mode, false)
    route.value = res.data.data?.route || mode
    msg.value = res.data.data?.reply || `已切换到 ${mode}`
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  } finally {
    busy.value = false
  }
}

async function refreshAll() {
  try {
    const [s, a, d, set, pk, v] = await Promise.all([
      xiaomiApi.getStatus(),
      xiaomiApi.getAccountStatus(),
      xiaomiApi.getDialogueStatus(),
      xiaomiApi.getDialogueSettings(),
      auth.isAdmin ? xiaomiApi.listPanelKeywords() : Promise.resolve(null),
      xiaomiApi.getVoiceStatus(),
    ])
    status.value = s.data.data
    account.value = a.data.data
    canControl.value = !!s.data.data?.configured
    const st = d.data.data
    applyStatus(st || {})
    Object.assign(dlgSettings, set.data.data)
    if (pk) panelKeywords.value = pk.data.data || []
    const vs = v.data.data
    voiceInfo.value = vs?.degraded
      ? `语音降级: ${vs.error || '无麦'}`
      : vs?.running
        ? `语音: ${vs.engine}`
        : '语音未运行'
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  }
}

async function sendChat() {
  const text = draft.value.trim()
  if (!text) return
  busy.value = true
  msg.value = ''
  msgError.value = false
  draft.value = ''
  try {
    await xiaomiApi.utterance(text, 'web')
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  } finally {
    busy.value = false
  }
}

async function saveSettings() {
  if (!auth.isAdmin) return
  try {
    await xiaomiApi.updateDialogueSettings({
      announceEnabled: dlgSettings.announceEnabled,
      voiceInputEnabled: dlgSettings.voiceInputEnabled,
      provider: dlgSettings.provider,
      announceCursor: dlgSettings.announceCursor,
      announceCodex: dlgSettings.announceCodex,
      announceSubagent: dlgSettings.announceSubagent,
      announceDetail: dlgSettings.announceDetail,
    })
    msg.value = '设置已保存'
    msgError.value = false
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  }
}

async function addKeyword() {
  if (!newKw.value.trim()) return
  try {
    await xiaomiApi.createPanelKeyword({
      keyword: newKw.value.trim(),
      actionType: newAction.value,
      payload: newAction.value === 'say' ? { text: newSay.value || '好的' } : {},
      enabled: true,
    })
    newKw.value = ''
    const pk = await xiaomiApi.listPanelKeywords()
    panelKeywords.value = pk.data.data || []
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  }
}

async function removeKeyword(id: number) {
  await xiaomiApi.deletePanelKeyword(id)
  panelKeywords.value = panelKeywords.value.filter((k) => k.id !== id)
}

async function doLogin() {
  busy.value = true
  try {
    const res = await xiaomiApi.accountLogin(miUser.value.trim(), miPass.value)
    const data = res.data.data
    sessionId.value = data.sessionId
    if (data.needSms) bindStep.value = 'sms'
    else {
      cloudDevices.value = data.devices || []
      bindStep.value = 'pick'
    }
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  } finally {
    busy.value = false
  }
}

async function doVerify() {
  busy.value = true
  try {
    const res = await xiaomiApi.accountVerify(sessionId.value, smsCode.value.trim())
    cloudDevices.value = res.data.data.devices || []
    bindStep.value = 'pick'
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  } finally {
    busy.value = false
  }
}

async function doBind(did: string) {
  busy.value = true
  try {
    await xiaomiApi.accountBind(sessionId.value, did)
    await refreshAll()
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  } finally {
    busy.value = false
  }
}

async function doUnbind() {
  if (!confirm('确定解绑？')) return
  await xiaomiApi.unbind()
  await refreshAll()
}

async function doWake() {
  busy.value = true
  try {
    await xiaomiApi.wake()
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  } finally {
    busy.value = false
  }
}

async function doTts() {
  busy.value = true
  try {
    await xiaomiApi.tts({ text: ttsText.value })
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  } finally {
    busy.value = false
  }
}

async function run(action: 'play' | 'pause' | 'stop') {
  busy.value = true
  try {
    await xiaomiApi[action]()
  } catch (e) {
    msgError.value = true
    msg.value = errDetail(e)
  } finally {
    busy.value = false
  }
}

async function setVol() {
  if (!canControl.value) return
  volBusy.value = true
  volError.value = false
  try {
    await xiaomiApi.setVolume(volume.value)
    volMsg.value = `音量已设为 ${volume.value}`
  } catch (e) {
    volError.value = true
    volMsg.value = errDetail(e)
  } finally {
    volBusy.value = false
  }
}

function onVolumeInput() {
  if (volTimer) clearTimeout(volTimer)
  volTimer = setTimeout(() => {
    void setVol()
  }, 350)
}

onMounted(async () => {
  await refreshAll()
  const hist = await xiaomiApi.getMessages(50)
  messages.value = hist.data.data || []
  connectWs()
  scrollLog()
})

onUnmounted(() => {
  if (volTimer) clearTimeout(volTimer)
  ws?.close()
  ws = null
})
</script>

<style scoped>
.xiaomi-page {
  position: relative;
  min-height: 100vh;
}
.wave-overlay {
  position: fixed;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.45s ease;
  overflow: hidden;
}
.xiaomi-page--tts .wave-overlay,
.xiaomi-page--user .wave-overlay {
  opacity: 1;
}
.wave-layer {
  position: absolute;
  left: -40%;
  width: 180%;
  height: 38vh;
  background-repeat: repeat-x;
  background-size: 50% 100%;
  opacity: 0.35;
}
.wave-layer--back {
  bottom: 8vh;
  animation: wave-drift 14s linear infinite;
}
.wave-layer--front {
  bottom: 0;
  height: 28vh;
  opacity: 0.5;
  animation: wave-drift 9s linear infinite reverse;
}
.xiaomi-page--tts .wave-layer--back {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1200 120' preserveAspectRatio='none'%3E%3Cpath fill='%23c4873a' fill-opacity='0.55' d='M0,64 C200,96 400,32 600,64 C800,96 1000,32 1200,64 L1200,120 L0,120 Z'/%3E%3C/svg%3E");
}
.xiaomi-page--tts .wave-layer--front {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1200 120' preserveAspectRatio='none'%3E%3Cpath fill='%23e8b56a' fill-opacity='0.45' d='M0,72 C180,40 360,88 540,56 C720,24 900,80 1200,48 L1200,120 L0,120 Z'/%3E%3C/svg%3E");
}
.xiaomi-page--user .wave-layer--back {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1200 120' preserveAspectRatio='none'%3E%3Cpath fill='%233d8b72' fill-opacity='0.5' d='M0,48 C220,80 440,24 660,56 C880,88 1100,32 1200,64 L1200,120 L0,120 Z'/%3E%3C/svg%3E");
  animation-direction: reverse;
}
.xiaomi-page--user .wave-layer--front {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1200 120' preserveAspectRatio='none'%3E%3Cpath fill='%235fbfb0' fill-opacity='0.35' d='M0,80 C200,48 400,96 600,64 C800,32 1000,72 1200,40 L1200,120 L0,120 Z'/%3E%3C/svg%3E");
  animation-direction: normal;
}
@keyframes wave-drift {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-25%);
  }
}
@media (prefers-reduced-motion: reduce) {
  .wave-layer {
    animation: none;
  }
  .xiaomi-page--tts .wave-overlay,
  .xiaomi-page--user .wave-overlay {
    opacity: 0.25;
  }
}
.settings-backdrop {
  position: fixed;
  inset: 0;
  z-index: 60;
  background: rgb(20 14 10 / 0.45);
  display: flex;
  justify-content: flex-end;
}
.settings-panel {
  width: min(100%, 26rem);
  height: 100%;
  background: #fffaf3;
  border-left: 1px solid rgb(42 28 18 / 0.08);
  display: flex;
  flex-direction: column;
  box-shadow: -8px 0 32px rgb(42 28 18 / 0.12);
  animation: settings-slide 0.22s ease-out;
}
:global(.dark) .settings-panel {
  background: #1c1917;
  border-color: rgb(255 255 255 / 0.06);
}
.settings-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid rgb(42 28 18 / 0.08);
}
:global(.dark) .settings-panel__head {
  border-color: rgb(255 255 255 / 0.06);
}
.settings-panel__body {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.25rem 1.5rem;
}
.settings-block + .settings-block {
  margin-top: 1.5rem;
  padding-top: 1.25rem;
  border-top: 1px solid rgb(42 28 18 / 0.06);
}
:global(.dark) .settings-block + .settings-block {
  border-color: rgb(255 255 255 / 0.06);
}
.settings-block__title {
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #8a7f72;
  margin-bottom: 0.75rem;
}
@keyframes settings-slide {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}
@media (prefers-reduced-motion: reduce) {
  .settings-panel {
    animation: none;
  }
}
.xiaomi-hero {
  background:
    radial-gradient(120% 80% at 10% 0%, rgb(196 135 58 / 0.45), transparent 55%),
    linear-gradient(165deg, #2a1c12 0%, #4a2f1a 42%, #1a120c 100%);
  position: relative;
  overflow: hidden;
}
.xiaomi-hero::after {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.12;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  pointer-events: none;
}
.xiaomi-hero-inner {
  position: relative;
  z-index: 1;
  max-width: 48rem;
  margin: 0 auto;
  padding: 2.75rem 1.25rem 3.5rem;
}
.xiaomi-kicker {
  font-size: 0.75rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgb(245 230 211 / 0.65);
  margin-bottom: 0.5rem;
}
.mode-seg {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 0.35rem;
  padding: 0.35rem;
  border-radius: 0.75rem;
  background: rgb(244 236 224 / 0.85);
}
:global(.dark) .mode-seg {
  background: rgb(30 27 24 / 0.9);
}
.mode-seg__btn {
  border: 0;
  border-radius: 0.55rem;
  padding: 0.55rem 0.25rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: #5c534a;
  background: transparent;
  transition: background 0.15s ease, color 0.15s ease, transform 0.15s ease;
}
.mode-seg__btn:hover:not(:disabled) {
  background: rgb(255 255 255 / 0.65);
}
.mode-seg__btn--on {
  background: #c4873a;
  color: #fffaf3;
  box-shadow: 0 1px 2px rgb(42 28 18 / 0.2);
}
.mode-seg__btn:disabled {
  opacity: 0.45;
}
.chat-log {
  min-height: 280px;
  max-height: 420px;
  overflow-y: auto;
  background:
    linear-gradient(180deg, rgb(250 246 240 / 0.9), rgb(255 255 255 / 0.4));
}
:global(.dark) .chat-log {
  background: linear-gradient(180deg, rgb(28 25 23 / 0.9), transparent);
}
.chat-bubble {
  max-width: 85%;
  border-radius: 1.1rem;
  padding: 0.65rem 0.9rem;
  font-size: 0.925rem;
  line-height: 1.45;
}
.chat-bubble--user {
  background: #c4873a;
  color: #fffaf3;
  border-bottom-right-radius: 0.35rem;
}
.chat-bubble--bot {
  background: #fff;
  color: #2a241c;
  border: 1px solid rgb(42 28 18 / 0.06);
  border-bottom-left-radius: 0.35rem;
}
:global(.dark) .chat-bubble--bot {
  background: #1c1917;
  color: #e7e5e4;
  border-color: rgb(255 255 255 / 0.06);
}
.chat-meta {
  margin-top: 0.35rem;
  font-size: 0.65rem;
  opacity: 0.65;
}
.chat-composer {
  display: flex;
  border-top: 1px solid rgb(42 28 18 / 0.08);
  background: #fff;
}
:global(.dark) .chat-composer {
  border-color: rgb(255 255 255 / 0.06);
  background: #0c0a09;
}
@media (max-width: 640px) {
  .mode-seg {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .mode-seg__btn:nth-child(4),
  .mode-seg__btn:nth-child(5) {
    grid-column: span 1;
  }
}
</style>
