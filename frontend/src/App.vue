<template>
  <div class="min-h-screen bg-surface-50 dark:bg-surface-950">
    <NavigationBar />
    <main>
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
