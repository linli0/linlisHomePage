<template>
  <div class="min-h-screen bg-surface-50 dark:bg-cyber-void-900 py-8">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-10">
        <span class="inline-block px-4 py-1.5 rounded-full bg-cyber-cyan-500/20 text-cyber-cyan-400 text-sm font-medium mb-4 border border-cyber-cyan-500/30">
          系统配置
        </span>
        <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-4">
          模块<span class="text-glow-cyan">配置中心</span>
        </h1>
        <p class="text-lg text-surface-600 dark:text-surface-400 max-w-2xl">
          集中管理小爱音箱、推特监控、AI对话等模块的配置项
        </p>
      </div>

      <form @submit.prevent="handleSave" class="space-y-8">
        <!-- 小爱音箱配置 -->
        <div class="card border border-cyber-orange-500/30 bg-cyber-void-800/50 backdrop-blur-xl shadow-neon-orange/20">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-cyber-orange-500/30 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-cyber-orange-400 to-cyber-orange-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">🔊</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">小爱音箱配置</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">配置小爱音箱设备连接信息</p>
            </div>
          </div>

          <div class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                设备 ID
              </label>
              <input
                v-model="form.xiaomi.deviceId"
                type="text"
                class="input bg-cyber-void-700/50 border-cyber-orange-500/30 focus:border-cyber-orange-400 focus:ring-cyber-orange-500/20"
                placeholder="例如: LX06-xxxxxxxx"
              />
              <p v-if="errors.xiaomi.deviceId" class="mt-1 text-sm text-red-500">
                {{ errors.xiaomi.deviceId }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                API 地址
              </label>
              <input
                v-model="form.xiaomi.apiUrl"
                type="url"
                class="input bg-cyber-void-700/50 border-cyber-orange-500/30 focus:border-cyber-orange-400 focus:ring-cyber-orange-500/20"
                placeholder="http://localhost:8080/api/xiaomi"
              />
              <p v-if="errors.xiaomi.apiUrl" class="mt-1 text-sm text-red-500">
                {{ errors.xiaomi.apiUrl }}
              </p>
            </div>
          </div>
        </div>

        <!-- 推特监控配置 -->
        <div class="card border border-cyber-blue-500/30 bg-cyber-void-800/50 backdrop-blur-xl shadow-neon-blue/20">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-cyber-blue-500/30 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-cyber-blue-400 to-cyber-blue-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">🐦</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">推特监控配置</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">配置推特监控的关键词和用户名过滤</p>
            </div>
          </div>

          <div class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                监控关键词 (用逗号分隔)
              </label>
              <input
                v-model="twitterKeywordsInput"
                type="text"
                class="input bg-cyber-void-700/50 border-cyber-blue-500/30 focus:border-cyber-blue-400 focus:ring-cyber-blue-500/20"
                placeholder="例如: gold, bitcoin, ai"
              />
              <div class="mt-2 flex flex-wrap gap-2">
                <span
                  v-for="(keyword, index) in form.twitter.keywords"
                  :key="index"
                  class="inline-flex items-center gap-1 px-3 py-1 bg-cyber-blue-500/20 text-cyber-blue-300 text-sm rounded-lg border border-cyber-blue-500/30"
                >
                  {{ keyword }}
                  <button
                    type="button"
                    @click="removeTwitterKeyword(index)"
                    class="hover:text-cyber-blue-100 transition-colors"
                  >
                    &times;
                  </button>
                </span>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                用户名过滤 (用逗号分隔)
              </label>
              <input
                v-model="twitterUsernamesInput"
                type="text"
                class="input bg-cyber-void-700/50 border-cyber-blue-500/30 focus:border-cyber-blue-400 focus:ring-cyber-blue-500/20"
                placeholder="例如: elonmusk, VitalikButerin"
              />
              <div class="mt-2 flex flex-wrap gap-2">
                <span
                  v-for="(username, index) in form.twitter.usernames"
                  :key="index"
                  class="inline-flex items-center gap-1 px-3 py-1 bg-cyber-blue-500/20 text-cyber-blue-300 text-sm rounded-lg border border-cyber-blue-500/30"
                >
                  @{{ username }}
                  <button
                    type="button"
                    @click="removeTwitterUsername(index)"
                    class="hover:text-cyber-blue-100 transition-colors"
                  >
                    &times;
                  </button>
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- AI对话配置 -->
        <div class="card border border-cyber-magenta-500/30 bg-cyber-void-800/50 backdrop-blur-xl shadow-neon-magenta/20">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-cyber-magenta-500/30 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-cyber-magenta-400 to-cyber-magenta-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">🤖</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">AI对话配置</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">配置AI对话的默认模型</p>
            </div>
          </div>

          <div class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                默认模型
              </label>
              <select
                v-model="form.ai.defaultModel"
                class="input bg-cyber-void-700/50 border-cyber-magenta-500/30 focus:border-cyber-magenta-400 focus:ring-cyber-magenta-500/20"
              >
                <option value="llama3">Llama 3</option>
                <option value="llama2">Llama 2</option>
                <option value="mistral">Mistral</option>
                <option value="gemma">Gemma</option>
                <option value="phi3">Phi-3</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex items-center gap-4 pt-4">
          <button
            type="submit"
            :disabled="saving"
            class="btn-primary bg-gradient-to-r from-cyber-cyan-500 to-cyber-magenta-500 hover:from-cyber-cyan-600 hover:to-cyber-magenta-600 shadow-neon-cyan"
          >
            <span v-if="saving" class="flex items-center gap-2">
              <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              保存中...
            </span>
            <span v-else class="flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              保存配置
            </span>
          </button>

          <button
            type="button"
            @click="handleReset"
            class="btn-secondary"
          >
            重置默认
          </button>

          <div
            v-if="saveMessage"
            class="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium"
            :class="saveMessageType === 'success'
              ? 'bg-green-500/20 text-green-400 border border-green-500/30'
              : 'bg-red-500/20 text-red-400 border border-red-500/30'"
          >
            <svg v-if="saveMessageType === 'success'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
            {{ saveMessage }}
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import type { SettingsConfig } from '@/types/settings'

const settingsStore = useSettingsStore()

const form = reactive<SettingsConfig>({ ...settingsStore.settings })

const saving = ref(false)
const saveMessage = ref('')
const saveMessageType = ref<'success' | 'error'>('success')

const errors = reactive({
  xiaomi: {
    deviceId: '',
    apiUrl: ''
  }
})

// Twitter 输入处理
const twitterKeywordsInput = ref('')
const twitterUsernamesInput = ref('')

watch(twitterKeywordsInput, (value) => {
  if (value.endsWith(',')) {
    const keyword = value.slice(0, -1).trim()
    if (keyword && !form.twitter.keywords.includes(keyword)) {
      form.twitter.keywords.push(keyword)
    }
    twitterKeywordsInput.value = ''
  }
})

watch(twitterUsernamesInput, (value) => {
  if (value.endsWith(',')) {
    const username = value.slice(0, -1).trim().replace(/^@/, '')
    if (username && !form.twitter.usernames.includes(username)) {
      form.twitter.usernames.push(username)
    }
    twitterUsernamesInput.value = ''
  }
})

function removeTwitterKeyword(index: number) {
  form.twitter.keywords.splice(index, 1)
}

function removeTwitterUsername(index: number) {
  form.twitter.usernames.splice(index, 1)
}

function validateForm(): boolean {
  let valid = true
  errors.xiaomi.deviceId = ''
  errors.xiaomi.apiUrl = ''

  if (!form.xiaomi.deviceId.trim()) {
    errors.xiaomi.deviceId = '请输入设备ID'
    valid = false
  }

  if (!form.xiaomi.apiUrl.trim()) {
    errors.xiaomi.apiUrl = '请输入API地址'
    valid = false
  } else {
    try {
      new URL(form.xiaomi.apiUrl)
    } catch {
      errors.xiaomi.apiUrl = '请输入有效的URL'
      valid = false
    }
  }

  return valid
}

async function handleSave() {
  if (!validateForm()) return

  saving.value = true
  saveMessage.value = ''

  try {
    // Update store
    settingsStore.updateXiaomiConfig(form.xiaomi)
    settingsStore.updateTwitterConfig(form.twitter)
    settingsStore.updateAIConfig(form.ai)

    saveMessage.value = '配置保存成功！'
    saveMessageType.value = 'success'

    setTimeout(() => {
      saveMessage.value = ''
    }, 3000)
  } catch (error) {
    saveMessage.value = '保存失败，请重试'
    saveMessageType.value = 'error'
  } finally {
    saving.value = false
  }
}

function handleReset() {
  settingsStore.resetToDefaults()
  Object.assign(form, settingsStore.settings)
  saveMessage.value = '已重置为默认配置'
  saveMessageType.value = 'success'
  setTimeout(() => {
    saveMessage.value = ''
  }, 3000)
}
</script>
