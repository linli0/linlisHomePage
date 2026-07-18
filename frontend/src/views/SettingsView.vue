<template>
  <div class="page-wrap max-w-2xl">
    <h1 class="page-title">设置</h1>
    <p class="page-desc mb-8">本地偏好（保存在浏览器）</p>

    <div class="space-y-6">
      <section class="card p-6 space-y-4">
        <h2 class="font-semibold text-ink-900 dark:text-white">AI</h2>
        <p class="text-sm text-ink-500 leading-relaxed">
          对话页默认走本地 Ollama。这里的「默认模型」只保存在本浏览器，需与 <code class="font-mono text-xs">ollama list</code> 中的名称一致。
        </p>
        <div>
          <label class="text-sm text-ink-500 mb-1.5 block">默认模型</label>
          <input v-model="aiModel" class="input" placeholder="例如 qwen2.5:7b" @change="saveAi" />
        </div>
        <router-link to="/ai" class="text-sm font-medium text-brand-600 hover:text-brand-500">打开 AI 对话页 →</router-link>
      </section>

      <section class="card p-6 space-y-3">
        <h2 class="font-semibold text-ink-900 dark:text-white">小爱音箱</h2>
        <p class="text-sm text-ink-500 leading-relaxed">
          管理员在「小爱」页用小米账号绑定音箱；凭证加密保存在服务端数据库，清浏览器缓存不会丢失。
        </p>
        <router-link to="/xiaomi" class="text-sm font-medium text-brand-600 hover:text-brand-500">打开小爱绑定/控制页 →</router-link>
      </section>

      <section class="card p-6 space-y-4">
        <h2 class="font-semibold text-ink-900 dark:text-white">推特监控</h2>
        <div>
          <label class="text-sm text-ink-500 mb-1.5 block">关键词（逗号分隔）</label>
          <input v-model="twitterKeywords" class="input" @change="saveTwitter" />
        </div>
        <div>
          <label class="text-sm text-ink-500 mb-1.5 block">用户名（逗号分隔）</label>
          <input v-model="twitterUsers" class="input" @change="saveTwitter" />
        </div>
      </section>

      <button type="button" class="btn-outline" @click="reset">恢复默认</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useSettingsStore } from '@/stores/settings'

const store = useSettingsStore()

const aiModel = ref(store.settings.ai.defaultModel)
const twitterKeywords = ref(store.settings.twitter.keywords.join(', '))
const twitterUsers = ref(store.settings.twitter.usernames.join(', '))

function saveAi() {
  store.updateAIConfig({ defaultModel: aiModel.value.trim() })
}

function saveTwitter() {
  store.updateTwitterConfig({
    keywords: twitterKeywords.value.split(',').map((s) => s.trim()).filter(Boolean),
    usernames: twitterUsers.value.split(',').map((s) => s.trim()).filter(Boolean),
  })
}

function reset() {
  store.resetToDefaults()
  aiModel.value = store.settings.ai.defaultModel
  twitterKeywords.value = ''
  twitterUsers.value = ''
}
</script>
