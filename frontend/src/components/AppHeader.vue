<template>
  <nav
    :class="[
      'fixed top-0 left-0 right-0 z-50 transition-all duration-300',
      navSolid
        ? 'bg-brand-50/95 dark:bg-ink-900/95 backdrop-blur-md border-b border-ink-100 dark:border-ink-800 shadow-xs'
        : 'bg-transparent border-b border-transparent'
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16 items-center">
        <div class="flex items-center min-w-0 gap-8">
          <router-link to="/" class="flex items-center gap-2 shrink-0">
            <span
              :class="[
                'font-display text-lg font-semibold tracking-tight',
                navSolid ? 'text-ink-900 dark:text-white' : 'text-brand-50'
              ]"
            >
              CoffeeCookies
            </span>
          </router-link>

          <div class="hidden lg:flex items-center gap-0.5">
            <router-link
              v-for="item in menuItems"
              :key="item.path"
              :to="item.path"
              :class="linkClass(isActive(item))"
            >
              {{ item.name }}
            </router-link>
          </div>
        </div>

        <div class="flex items-center gap-2">
          <router-link
            v-if="!authStore.isAuthenticated"
            to="/login"
            :class="[
              'px-4 py-2 rounded-xl text-sm font-semibold transition-colors',
              navSolid ? 'bg-brand-500 text-brand-50 hover:bg-brand-600' : 'bg-brand-50 text-brand-900 hover:bg-brand-100'
            ]"
          >
            登录
          </router-link>

          <div v-else class="relative" ref="userMenuRef">
            <button
              type="button"
              class="flex items-center gap-2 px-2.5 py-1.5 rounded-xl transition-colors"
              :class="navSolid ? 'hover:bg-ink-100 dark:hover:bg-ink-800' : 'hover:bg-white/10'"
              @click="showUserMenu = !showUserMenu"
            >
                <div class="w-8 h-8 rounded-lg bg-brand-500 flex items-center justify-center text-brand-50 text-sm font-semibold">
                  {{ initial }}
                </div>
                <span :class="['hidden sm:block text-sm font-medium', navSolid ? 'text-ink-700 dark:text-ink-200' : 'text-brand-100']">
                  {{ authStore.user?.displayName || authStore.user?.username }}
                </span>
            </button>
            <div
              v-if="showUserMenu"
              class="absolute right-0 mt-2 w-48 rounded-xl bg-white dark:bg-ink-800 shadow-lg border border-ink-100 dark:border-ink-700 py-1"
            >
              <router-link to="/profile" class="block px-4 py-2.5 text-sm text-ink-700 dark:text-ink-200 hover:bg-ink-50 dark:hover:bg-ink-700" @click="showUserMenu = false">个人中心</router-link>
              <router-link to="/settings" class="block px-4 py-2.5 text-sm text-ink-700 dark:text-ink-200 hover:bg-ink-50 dark:hover:bg-ink-700" @click="showUserMenu = false">设置</router-link>
              <button type="button" class="w-full text-left px-4 py-2.5 text-sm text-red-600 hover:bg-red-50" @click="handleLogout">退出登录</button>
            </div>
          </div>

          <button
            type="button"
            class="lg:hidden p-2 rounded-xl"
            :class="navSolid ? 'text-ink-700 hover:bg-ink-100' : 'text-brand-50 hover:bg-white/10'"
            @click="showMobile = !showMobile"
          >
            <span class="text-xl">{{ showMobile ? '✕' : '☰' }}</span>
          </button>
        </div>
      </div>

      <div v-if="showMobile" class="lg:hidden pb-3 border-t" :class="navSolid ? 'border-ink-100' : 'border-white/15'">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="block px-3 py-2.5 rounded-lg text-sm font-medium mt-1"
          :class="navSolid
            ? (isActive(item) ? 'bg-brand-100 text-brand-700' : 'text-ink-700 hover:bg-ink-50')
            : (isActive(item) ? 'bg-white/10 text-brand-50' : 'text-brand-100/90 hover:bg-white/10')"
          @click="showMobile = false"
        >
          {{ item.name }}
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isScrolled = ref(false)
const showMobile = ref(false)
const showUserMenu = ref(false)
const userMenuRef = ref<HTMLElement>()

const onHeroPage = computed(() => route.path === '/')
const navSolid = computed(() => isScrolled.value || !onHeroPage.value)
const initial = computed(() =>
  authStore.user?.displayName?.[0] || authStore.user?.username?.[0] || 'U',
)

const menuItems = [
  { name: '首页', path: '/' },
  { name: '金价', path: '/gold' },
  { name: 'Wiki', path: '/articles', match: '/article' },
  { name: '工具箱', path: '/tools' },
  { name: '推特', path: '/tweets' },
  { name: '小爱', path: '/xiaomi' },
  { name: '量化', path: '/quant' },
]

function isActive(item: { path: string; match?: string }) {
  if (item.path === '/') return route.path === '/'
  const base = item.match || item.path
  return route.path === item.path || route.path.startsWith(base)
}

function linkClass(active: boolean) {
  if (navSolid.value) {
    return [
      'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
      active
        ? 'bg-brand-100 text-brand-700 dark:bg-brand-900/40 dark:text-brand-300'
        : 'text-ink-600 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-ink-800',
    ]
  }
  return [
    'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
    active ? 'bg-white/10 text-brand-50' : 'text-brand-100/85 hover:bg-white/10 hover:text-brand-50',
  ]
}

function handleLogout() {
  authStore.logout()
  showUserMenu.value = false
  router.push('/')
}

function onScroll() {
  isScrolled.value = window.scrollY > 20
}

function onClickOutside(e: MouseEvent) {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target as Node)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  window.addEventListener('scroll', onScroll)
  document.addEventListener('click', onClickOutside)
  onScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  document.removeEventListener('click', onClickOutside)
})
</script>
