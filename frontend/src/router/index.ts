import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: () => import('@/views/HomeView.vue') },
    { path: '/gold', name: 'GoldPrice', component: () => import('@/views/GoldPriceView.vue') },
    { path: '/articles', name: 'Articles', component: () => import('@/views/ArticlesView.vue') },
    { path: '/article/:id', name: 'ArticleDetail', component: () => import('@/views/ArticleDetailView.vue') },
    { path: '/tools', name: 'Tools', component: () => import('@/views/ToolsView.vue') },
    { path: '/ai', name: 'AI', component: () => import('@/views/AIView.vue') },
    { path: '/tweets', name: 'TweetsMonitor', component: () => import('@/views/TweetsView.vue'), meta: { requiresAuth: true } },
    { path: '/xiaomi', name: 'Xiaomi', component: () => import('@/views/XiaomiView.vue'), meta: { requiresAuth: true } },
    { path: '/quant', name: 'QuantTrading', component: () => import('@/views/QuantView.vue'), meta: { requiresAuth: true } },
    { path: '/settings', name: 'Settings', component: () => import('@/views/SettingsView.vue'), meta: { requiresAuth: true } },
    { path: '/profile', name: 'Profile', component: () => import('@/views/ProfileView.vue'), meta: { requiresAuth: true } },
    { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { guest: true } },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (to.meta.guest && authStore.isAuthenticated) {
    next('/')
    return
  }
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

export default router

