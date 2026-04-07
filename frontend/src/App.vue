<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <NavigationBar />
    <main class="pt-16">
      <router-view />
    </main>
    <FooterBar />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import NavigationBar from './components/NavigationBar.vue'
import FooterBar from './components/FooterBar.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  if (authStore.token && !authStore.user) {
    authStore.fetchUser()
  }
})
</script>
