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
import NavigationBar from './components/NavigationBar.vue'
import FooterBar from './components/FooterBar.vue'
</script>
