<template>
  <div class="page-wrap max-w-md mx-auto">
    <div class="card p-8">
      <h1 class="page-title text-2xl mb-1">登录</h1>
      <p class="page-desc mb-2">私人站点：只需密码，不开放注册</p>
      <p class="text-xs text-ink-400 mb-8">默认管理员密码见 README（本地开发可改环境变量）</p>

      <form class="space-y-4" @submit.prevent="onSubmit">
        <div>
          <label class="block text-sm font-medium text-ink-600 mb-1.5">密码</label>
          <input
            v-model="password"
            type="password"
            class="input"
            placeholder="请输入密码"
            autocomplete="current-password"
            autofocus
            required
          />
        </div>
        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
        <button type="submit" class="btn-primary w-full" :disabled="authStore.loading || !password.trim()">
          {{ authStore.loading ? '登录中…' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const password = ref('')
const error = ref('')

async function onSubmit() {
  error.value = ''
  const ok = await authStore.login(password.value)
  if (!ok) {
    error.value = authStore.error || '密码错误'
    return
  }
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
  router.push(redirect)
}
</script>
