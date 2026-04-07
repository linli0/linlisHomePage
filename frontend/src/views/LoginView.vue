<template>
  <div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
    <div class="absolute inset-0 hero-gradient-animated"></div>
    <div class="absolute inset-0 bg-hero-pattern opacity-30"></div>
    
    <div class="absolute top-20 left-10 w-72 h-72 bg-white/10 rounded-full blur-3xl animate-float"></div>
    <div class="absolute bottom-20 right-10 w-96 h-96 bg-accent-500/20 rounded-full blur-3xl animate-float-slow"></div>
    
    <div class="relative z-10 max-w-md w-full">
      <div class="text-center mb-10">
        <div class="relative inline-block mb-6">
          <div class="absolute inset-0 bg-primary-500/30 rounded-2xl blur-2xl animate-pulse-slow"></div>
          <div class="relative w-24 h-24 bg-gradient-to-br from-primary-500 via-accent-500 to-gold-500 rounded-2xl flex items-center justify-center shadow-2xl mx-auto">
            <span class="text-5xl">☕</span>
          </div>
        </div>
        <h1 class="text-4xl font-bold text-white mb-3">CoffeeCookies</h1>
        <p class="text-white/70 text-lg">请输入访问密码以继续</p>
      </div>

      <div class="card p-8 backdrop-blur-xl bg-white/90 dark:bg-surface-900/90 border-white/20 dark:border-surface-700/50">
        <form @submit.prevent="handleLogin" class="space-y-6">
          <div>
            <label for="password" class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
              访问密码
            </label>
            <div class="relative">
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="input pr-12"
                placeholder="请输入访问密码"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-4 flex items-center text-surface-400 hover:text-surface-600 dark:hover:text-surface-300 transition-colors"
              >
                <svg v-if="showPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              </button>
            </div>
          </div>

          <div v-if="authStore.error" class="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/50 text-red-700 dark:text-red-300 rounded-xl text-sm flex items-start gap-3">
            <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ authStore.error }}</span>
          </div>

          <button
            type="submit"
            :disabled="authStore.loading"
            class="btn-primary w-full flex justify-center items-center gap-2 py-3.5 text-base"
          >
            <svg v-if="authStore.loading" class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>{{ authStore.loading ? '验证中...' : '进入网站' }}</span>
          </button>
        </form>
      </div>

      <div class="mt-8 text-center">
        <router-link to="/" class="inline-flex items-center gap-2 text-sm text-white/60 hover:text-white transition-colors">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const password = ref('')
const showPassword = ref(false)

async function handleLogin() {
  if (!password.value.trim()) return
  
  const success = await authStore.login(password.value)
  if (success) {
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  }
}
</script>
