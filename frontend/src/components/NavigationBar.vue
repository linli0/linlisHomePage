<template>
  <nav class="fixed top-0 left-0 right-0 z-50 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md border-b border-gray-200 dark:border-gray-800">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <!-- Logo & Desktop Navigation -->
        <div class="flex items-center">
          <router-link to="/" class="flex items-center space-x-2">
            <span class="text-2xl">☕</span>
            <span class="font-bold text-xl text-gray-900 dark:text-white">CoffeeCookies</span>
          </router-link>
          
          <div class="hidden md:flex items-center ml-10 space-x-4">
            <router-link 
              to="/" 
              :class="[$route.path === '/' ? 'text-blue-600 dark:text-blue-400' : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white']"
              class="px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              首页
            </router-link>
            <router-link 
              to="/gold" 
              :class="[$route.path === '/gold' ? 'text-gold-600 dark:text-gold-400' : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white']"
              class="px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              <span class="mr-1">💰</span>金价
            </router-link>
            <router-link 
              to="/articles" 
              :class="[$route.path.startsWith('/article') ? 'text-blue-600 dark:text-blue-400' : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white']"
              class="px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              文章
            </router-link>
            <router-link 
              to="/tools" 
              :class="[$route.path === '/tools' ? 'text-blue-600 dark:text-blue-400' : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white']"
              class="px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              工具箱
            </router-link>
          </div>
        </div>
        
        <!-- Right Side -->
        <div class="flex items-center space-x-4">
          <!-- Auth Buttons -->
          <template v-if="!authStore.isAuthenticated">
            <router-link to="/login" class="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white px-3 py-2 text-sm font-medium">
              登录
            </router-link>
            <router-link to="/register" class="btn-primary text-sm">
              注册
            </router-link>
          </template>
          
          <template v-else>
            <div class="relative" ref="userMenuRef">
              <button 
                @click="showUserMenu = !showUserMenu"
                class="flex items-center space-x-2 text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white"
              >
                <div class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white text-sm font-medium">
                  {{ authStore.user?.displayName?.[0] || authStore.user?.username?.[0] || 'U' }}
                </div>
                <span class="hidden sm:block text-sm">{{ authStore.user?.displayName || authStore.user?.username }}</span>
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              <!-- Dropdown Menu -->
              <div v-if="showUserMenu" class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 py-1 animate-fade-in">
                <router-link to="/profile" class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                  个人中心
                </router-link>
                <button @click="handleLogout" class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700">
                  退出登录
                </button>
              </div>
            </div>
          </template>
          
          <!-- Mobile Menu Button -->
          <button 
            @click="showMobileMenu = !showMobileMenu"
            class="md:hidden p-2 rounded-md text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white"
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
      
      <!-- Mobile Menu -->
      <div v-if="showMobileMenu" class="md:hidden py-2 border-t border-gray-200 dark:border-gray-700">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path"
          :to="item.path"
          @click="showMobileMenu = false"
          class="block px-3 py-2 rounded-md text-base font-medium text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-gray-50 dark:hover:bg-gray-800"
        >
          {{ item.name }}
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const showMobileMenu = ref(false)
const showUserMenu = ref(false)
const userMenuRef = ref<HTMLElement>()

const menuItems = [
  { name: '首页', path: '/' },
  { name: '金价', path: '/gold' },
  { name: '文章', path: '/articles' },
  { name: '工具箱', path: '/tools' }
]

function handleLogout() {
  authStore.logout()
  showUserMenu.value = false
  router.push('/')
}

// Close dropdown when clicking outside
function handleClickOutside(event: MouseEvent) {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  // Try to fetch user info if token exists
  if (authStore.token && !authStore.user) {
    authStore.fetchUser()
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
