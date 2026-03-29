<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-8">个人中心</h1>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- User Info Card -->
        <div class="card p-6">
          <div class="text-center">
            <div class="w-24 h-24 mx-auto mb-4 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white text-3xl font-bold">
              {{ authStore.user?.displayName?.[0] || authStore.user?.username?.[0] || 'U' }}
            </div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">
              {{ authStore.user?.displayName || authStore.user?.username }}
            </h2>
            <p class="text-gray-500 dark:text-gray-400">{{ authStore.user?.email }}</p>
            <span class="inline-block mt-2 px-3 py-1 text-xs font-medium rounded-full"
              :class="authStore.user?.role === 'ADMIN' ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300' : 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300'"
            >
              {{ authStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </span>
          </div>

          <div class="mt-6 space-y-3">
            <div class="flex justify-between text-sm">
              <span class="text-gray-500 dark:text-gray-400">用户名</span>
              <span class="text-gray-900 dark:text-white">{{ authStore.user?.username }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-500 dark:text-gray-400">注册时间</span>
              <span class="text-gray-900 dark:text-white">{{ formatDate(authStore.user?.createdAt) }}</span>
            </div>
          </div>

          <button
            @click="handleLogout"
            class="mt-6 w-full py-2 px-4 border border-red-300 text-red-600 rounded-lg hover:bg-red-50 dark:border-red-700 dark:hover:bg-red-900/20 transition-colors"
          >
            退出登录
          </button>
        </div>

        <!-- Settings -->
        <div class="md:col-span-2 card p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">账户设置</h3>
          
          <div class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                显示名称
              </label>
              <input
                v-model="profileForm.displayName"
                type="text"
                class="input"
                placeholder="输入显示名称"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                邮箱
              </label>
              <input
                v-model="profileForm.email"
                type="email"
                class="input"
                placeholder="输入邮箱地址"
              />
            </div>

            <div class="border-t border-gray-200 dark:border-gray-700 pt-6">
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">修改密码</h4>
              
              <div class="space-y-4">
                <input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  class="input"
                  placeholder="当前密码"
                />
                <input
                  v-model="passwordForm.newPassword"
                  type="password"
                  class="input"
                  placeholder="新密码"
                />
                <input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  class="input"
                  placeholder="确认新密码"
                />
              </div>
            </div>

            <div class="flex justify-end">
              <button class="btn-primary">
                保存修改
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const profileForm = ref({
  displayName: authStore.user?.displayName || '',
  email: authStore.user?.email || ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '--'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function handleLogout() {
  authStore.logout()
  router.push('/')
}
</script>
