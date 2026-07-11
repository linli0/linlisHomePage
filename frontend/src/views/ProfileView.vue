<template>
  <div class="min-h-screen bg-surface-50 dark:bg-surface-950 py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-10">
        <span class="inline-block px-4 py-1.5 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 text-sm font-medium mb-4">
          个人中心
        </span>
        <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-4">
          账户<span class="gradient-text">设置</span>
        </h1>
        <p class="text-lg text-surface-600 dark:text-surface-400 max-w-2xl">
          管理您的个人资料和账户安全
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="card p-8">
          <div class="text-center">
            <div class="relative inline-block mb-6">
              <div class="absolute inset-0 bg-primary-400/20 rounded-2xl blur-xl animate-pulse-slow"></div>
              <div class="relative w-28 h-28 rounded-2xl bg-gradient-to-br from-primary-500 via-accent-500 to-gold-500 flex items-center justify-center text-white text-4xl font-bold shadow-2xl">
                {{ authStore.user?.displayName?.[0] || authStore.user?.username?.[0] || 'U' }}
              </div>
            </div>
            <h2 class="text-2xl font-bold text-surface-900 dark:text-white mb-2">
              {{ authStore.user?.displayName || authStore.user?.username }}
            </h2>
            <p class="text-surface-500 dark:text-surface-400 mb-4">{{ authStore.user?.email }}</p>
            <span 
              class="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-xl"
              :class="authStore.user?.role === 'ADMIN' 
                ? 'bg-gradient-to-r from-red-100 to-orange-100 text-red-700 dark:from-red-900/30 dark:to-orange-900/30 dark:text-red-300' 
                : 'bg-gradient-to-r from-primary-100 to-accent-100 text-primary-700 dark:from-primary-900/30 dark:to-accent-900/30 dark:text-primary-300'"
            >
              <span v-if="authStore.user?.role === 'ADMIN'">👑</span>
              <span v-else>👤</span>
              {{ authStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </span>
          </div>

          <div class="mt-8 space-y-4">
            <div class="flex justify-between items-center py-3 border-b border-surface-200 dark:border-surface-800">
              <span class="text-surface-500 dark:text-surface-400">用户名</span>
              <span class="font-medium text-surface-900 dark:text-white">{{ authStore.user?.username }}</span>
            </div>
            <div class="flex justify-between items-center py-3 border-b border-surface-200 dark:border-surface-800">
              <span class="text-surface-500 dark:text-surface-400">注册时间</span>
              <span class="font-medium text-surface-900 dark:text-white">{{ formatDate(authStore.user?.createdAt) }}</span>
            </div>
          </div>

          <button
            @click="handleLogout"
            class="mt-8 w-full py-3 px-4 border-2 border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 rounded-xl font-medium hover:bg-red-50 dark:hover:bg-red-900/20 hover:border-red-300 dark:hover:border-red-700 transition-all flex items-center justify-center gap-2"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            退出登录
          </button>
        </div>

        <div class="lg:col-span-2 space-y-8">
          <div class="card p-8">
            <div class="flex items-center gap-3 mb-6">
              <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-white">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <h3 class="text-xl font-bold text-surface-900 dark:text-white">个人资料</h3>
            </div>
            
            <div class="space-y-6">
              <div>
                <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
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
                <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                  邮箱地址
                </label>
                <input
                  v-model="profileForm.email"
                  type="email"
                  class="input"
                  placeholder="输入邮箱地址"
                />
              </div>

              <div class="flex items-center gap-4">
                <button @click="handleSaveProfile" :disabled="profileSaving" class="btn-primary">
                  <span v-if="profileSaving" class="flex items-center gap-2">
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
                    保存资料
                  </span>
                </button>
                <div 
                  v-if="profileMsg" 
                  class="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium"
                  :class="profileMsgType === 'success' 
                    ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' 
                    : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'"
                >
                  <svg v-if="profileMsgType === 'success'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                  {{ profileMsg }}
                </div>
              </div>
            </div>
          </div>

          <div class="card p-8">
            <div class="flex items-center gap-3 mb-6">
              <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-gold-400 to-gold-600 flex items-center justify-center text-white">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <h3 class="text-xl font-bold text-surface-900 dark:text-white">修改密码</h3>
            </div>
            
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
              <div 
                v-if="passwordMismatch" 
                class="flex items-center gap-2 px-4 py-2 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/50 text-red-700 dark:text-red-300 rounded-lg text-sm"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                两次输入的新密码不一致
              </div>
            </div>

            <div class="mt-6 flex items-center gap-4">
              <button @click="handleChangePassword" :disabled="passwordSaving" class="btn-gold">
                <span v-if="passwordSaving" class="flex items-center gap-2">
                  <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  修改中...
                </span>
                <span v-else class="flex items-center gap-2">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                  修改密码
                </span>
              </button>
              <div 
                v-if="passwordMsg" 
                class="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium"
                :class="passwordMsgType === 'success' 
                  ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' 
                  : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'"
              >
                <svg v-if="passwordMsgType === 'success'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                {{ passwordMsg }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { formatDate } from '@/utils/format'

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

const profileSaving = ref(false)
const profileMsg = ref('')
const profileMsgType = ref<'success' | 'error'>('success')

const passwordSaving = ref(false)
const passwordMsg = ref('')
const passwordMsgType = ref<'success' | 'error'>('success')

const passwordMismatch = computed(() =>
  passwordForm.value.newPassword !== '' &&
  passwordForm.value.confirmPassword !== '' &&
  passwordForm.value.newPassword !== passwordForm.value.confirmPassword
)

async function handleSaveProfile() {
  profileSaving.value = true
  profileMsg.value = ''
  try {
    await authApi.updateProfile({
      displayName: profileForm.value.displayName,
      email: profileForm.value.email
    })
    await authStore.fetchUser()
    profileMsg.value = '资料保存成功'
    profileMsgType.value = 'success'
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    profileMsg.value = e.response?.data?.message || '保存失败'
    profileMsgType.value = 'error'
  } finally {
    profileSaving.value = false
  }
}

async function handleChangePassword() {
  if (passwordMismatch.value) return
  if (!passwordForm.value.currentPassword || !passwordForm.value.newPassword) {
    passwordMsg.value = '请填写当前密码和新密码'
    passwordMsgType.value = 'error'
    return
  }
  passwordSaving.value = true
  passwordMsg.value = ''
  try {
    await authApi.changePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })
    passwordMsg.value = '密码修改成功'
    passwordMsgType.value = 'success'
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    passwordMsg.value = e.response?.data?.message || '密码修改失败'
    passwordMsgType.value = 'error'
  } finally {
    passwordSaving.value = false
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/')
}
</script>
