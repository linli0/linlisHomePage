<template>
  <nav
    :class="[
      'fixed top-0 left-0 right-0 z-50 transition-all duration-300',
      isScrolled || !onHeroPage
        ? 'bg-white/95 dark:bg-surface-900/95 backdrop-blur-xl shadow-soft'
        : 'bg-transparent'
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <div class="flex items-center">
          <router-link to="/" class="flex items-center space-x-3 group">
            <div class="relative">
              <div class="absolute inset-0 bg-primary-500/20 rounded-xl blur-lg group-hover:bg-primary-500/30 transition-colors"></div>
              <div class="relative w-10 h-10 bg-gradient-to-br from-primary-500 to-accent-500 rounded-xl flex items-center justify-center shadow-lg group-hover:scale-105 transition-transform">
                <span class="text-xl">☕</span>
              </div>
            </div>
            <div class="flex flex-col">
              <span :class="[
                'font-bold text-lg transition-colors',
                (isScrolled || !onHeroPage) ? 'text-surface-900 dark:text-white' : 'text-white'
              ]">
                CoffeeCookies
              </span>
            </div>
          </router-link>
          
          <div class="hidden md:flex items-center ml-10 space-x-1">
            <router-link 
              to="/" 
              :class="[
                'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200',
                $route.path === '/' 
                  ? ((isScrolled || !onHeroPage) ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400' : 'bg-white/20 text-white')
                  : ((isScrolled || !onHeroPage) ? 'text-surface-600 dark:text-surface-300 hover:bg-surface-100 dark:hover:bg-surface-800' : 'text-white/80 hover:text-white hover:bg-white/10')
              ]"
            >
              首页
            </router-link>
            <router-link 
              to="/gold" 
              :class="[
                'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200 flex items-center gap-1.5',
                $route.path === '/gold' 
                  ? ((isScrolled || !onHeroPage) ? 'bg-gold-100 dark:bg-gold-900/30 text-gold-600 dark:text-gold-400' : 'bg-white/20 text-white')
                  : ((isScrolled || !onHeroPage) ? 'text-surface-600 dark:text-surface-300 hover:bg-surface-100 dark:hover:bg-surface-800' : 'text-white/80 hover:text-white hover:bg-white/10')
              ]"
            >
              <span>💰</span>
              <span>金价</span>
            </router-link>
            <router-link
              to="/articles"
              :class="[
                'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200',
                $route.path.startsWith('/article')
                  ? (isScrolled || !onHeroPage ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400' : 'bg-white/20 text-white')
                  : (isScrolled || !onHeroPage ? 'text-surface-600 dark:text-surface-300 hover:bg-surface-100 dark:hover:bg-surface-800' : 'text-white/80 hover:text-white hover:bg-white/10')
              ]"
            >
              Wiki
            </router-link>
            <router-link 
              to="/tools" 
              :class="[
                'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200',
                $route.path === '/tools' 
                  ? ((isScrolled || !onHeroPage) ? 'bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400' : 'bg-white/20 text-white')
                  : ((isScrolled || !onHeroPage) ? 'text-surface-600 dark:text-surface-300 hover:bg-surface-100 dark:hover:bg-surface-800' : 'text-white/80 hover:text-white hover:bg-white/10')
              ]"
            >
              工具箱
            </router-link>
            <router-link 
              to="/ai" 
              :class="[
                'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200 flex items-center gap-1.5',
                $route.path === '/ai' 
                  ? ((isScrolled || !onHeroPage) ? 'bg-accent-100 dark:bg-accent-900/30 text-accent-600 dark:text-accent-400' : 'bg-white/20 text-white')
                  : ((isScrolled || !onHeroPage) ? 'text-surface-600 dark:text-surface-300 hover:bg-surface-100 dark:hover:bg-surface-800' : 'text-white/80 hover:text-white hover:bg-white/10')
              ]"
            >
              <span>🤖</span>
              <span>AI</span>
            </router-link>
          </div>
        </div>
        
        <div class="flex items-center space-x-3">
          <template v-if="!authStore.isAuthenticated">
            <router-link 
              to="/login" 
              :class="[
                'px-5 py-2 rounded-xl font-medium text-sm transition-all duration-200',
                (isScrolled || !onHeroPage)
                  ? 'bg-primary-500 text-white hover:bg-primary-600 shadow-soft hover:shadow-soft-lg'
                  : 'bg-white text-surface-900 hover:bg-surface-100'
              ]"
            >
              登录
            </router-link>
          </template>
          
          <template v-else>
            <div class="relative" ref="userMenuRef">
              <button 
                @click="showUserMenu = !showUserMenu"
                :class="[
                  'flex items-center space-x-2 px-3 py-1.5 rounded-xl transition-all duration-200',
                  isScrolled 
                    ? 'hover:bg-surface-100 dark:hover:bg-surface-800' 
                    : 'hover:bg-white/10'
                ]"
              >
                <div class="relative">
                  <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center text-white text-sm font-medium shadow-lg">
                    {{ authStore.user?.displayName?.[0] || authStore.user?.username?.[0] || 'U' }}
                  </div>
                  <div class="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 rounded-full border-2 border-white dark:border-surface-900"></div>
                </div>
                <span :class="['hidden sm:block text-sm font-medium', (isScrolled || !onHeroPage) ? 'text-surface-700 dark:text-surface-200' : 'text-white']">
                  {{ authStore.user?.displayName || authStore.user?.username }}
                </span>
                <svg :class="['w-4 h-4 transition-transform', showUserMenu ? 'rotate-180' : '', (isScrolled || !onHeroPage) ? 'text-surface-500' : 'text-white/60']" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              <Transition
                enter-active-class="transition ease-out duration-200"
                enter-from-class="opacity-0 translate-y-1"
                enter-to-class="opacity-100 translate-y-0"
                leave-active-class="transition ease-in duration-150"
                leave-from-class="opacity-100 translate-y-0"
                leave-to-class="opacity-0 translate-y-1"
              >
                <div v-if="showUserMenu" class="absolute right-0 mt-2 w-56 bg-white dark:bg-surface-800 rounded-2xl shadow-soft-lg border border-surface-200/50 dark:border-surface-700/50 py-2 overflow-hidden">
                  <div class="px-4 py-3 border-b border-surface-100 dark:border-surface-700">
                    <p class="text-sm font-medium text-surface-900 dark:text-white">
                      {{ authStore.user?.displayName || authStore.user?.username }}
                    </p>
                    <p class="text-xs text-surface-500 dark:text-surface-400">
                      {{ authStore.user?.email || '已登录' }}
                    </p>
                  </div>
                  <div class="py-1">
                    <router-link 
                      to="/profile" 
                      @click="showUserMenu = false"
                      class="flex items-center gap-3 px-4 py-2.5 text-sm text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-700 transition-colors"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      个人中心
                    </router-link>
                  </div>
                  <div class="border-t border-surface-100 dark:border-surface-700 pt-1">
                    <button 
                      @click="handleLogout" 
                      class="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                      </svg>
                      退出登录
                    </button>
                  </div>
                </div>
              </Transition>
            </div>
          </template>
          
          <button 
            @click="showMobileMenu = !showMobileMenu"
            :class="[
              'md:hidden p-2 rounded-xl transition-colors',
              (isScrolled || !onHeroPage)
                ? 'text-surface-600 dark:text-surface-300 hover:bg-surface-100 dark:hover:bg-surface-800' 
                : 'text-white hover:bg-white/10'
            ]"
          >
            <svg v-if="!showMobileMenu" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
            <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
      
      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0 -translate-y-2"
        enter-to-class="opacity-100 translate-y-0"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="opacity-100 translate-y-0"
        leave-to-class="opacity-0 -translate-y-2"
      >
        <div v-if="showMobileMenu" class="md:hidden py-4 border-t border-surface-200/50 dark:border-surface-700/50">
          <div class="space-y-1">
            <router-link 
              v-for="item in menuItems" 
              :key="item.path"
              :to="item.path"
              @click="showMobileMenu = false"
              :class="[
                'flex items-center gap-3 px-4 py-3 rounded-xl text-base font-medium transition-colors',
                $route.path === item.path || (item.path !== '/' && $route.path.startsWith(item.path))
                  ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400'
                  : 'text-surface-600 dark:text-surface-300 hover:bg-surface-100 dark:hover:bg-surface-800'
              ]"
            >
              <span v-if="item.icon">{{ item.icon }}</span>
              <span>{{ item.name }}</span>
            </router-link>
          </div>
        </div>
      </Transition>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const showMobileMenu = ref(false)
const showUserMenu = ref(false)
const userMenuRef = ref<HTMLElement>()
const isScrolled = ref(false)

// 判断是否在首页（仅首页使用透明背景）
const onHeroPage = computed(() => route.path === '/')

const menuItems = [
  { name: '首页', path: '/' },
  { name: '金价', path: '/gold', icon: '💰' },
  { name: 'Wiki', path: '/articles' },
  { name: '工具箱', path: '/tools' },
  { name: 'AI', path: '/ai', icon: '🤖' }
]

function handleLogout() {
  authStore.logout()
  showUserMenu.value = false
  router.push('/')
}

function handleClickOutside(event: MouseEvent) {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false
  }
}

function handleScroll() {
  isScrolled.value = window.scrollY > 20
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('scroll', handleScroll)
  handleScroll()
  
  if (authStore.token && !authStore.user) {
    authStore.fetchUser()
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('scroll', handleScroll)
})
</script>
