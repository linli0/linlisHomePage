<template>
  <div class="page-wrap max-w-xl">
    <h1 class="page-title">个人中心</h1>
    <p class="page-desc mb-8">查看当前登录账号信息</p>

    <div v-if="!authStore.user" class="card p-8 text-ink-400">加载中…</div>
    <div v-else class="card p-8 space-y-4">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 rounded-2xl bg-brand-500 text-white text-xl font-bold flex items-center justify-center">
          {{ authStore.user.displayName?.[0] || authStore.user.username[0] }}
        </div>
        <div>
          <p class="text-lg font-bold text-ink-900 dark:text-white">
            {{ authStore.user.displayName || authStore.user.username }}
          </p>
          <p class="text-sm text-ink-500">{{ authStore.user.role }}</p>
        </div>
      </div>
      <dl class="space-y-3 text-sm">
        <div class="flex justify-between border-b border-ink-100 dark:border-ink-800 py-2">
          <dt class="text-ink-400">用户名</dt>
          <dd class="font-medium">{{ authStore.user.username }}</dd>
        </div>
        <div class="flex justify-between border-b border-ink-100 dark:border-ink-800 py-2">
          <dt class="text-ink-400">邮箱</dt>
          <dd class="font-medium">{{ authStore.user.email || '--' }}</dd>
        </div>
      </dl>
      <router-link to="/settings" class="btn-outline">前往设置</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  if (!authStore.user) authStore.fetchUser()
})
</script>
