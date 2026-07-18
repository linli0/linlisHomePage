<template>
  <div class="min-h-screen bg-brand-50 dark:bg-ink-950 text-ink-800 dark:text-ink-200">
    <AppHeader />
    <main class="pt-16 min-h-[60vh]">
      <slot />
    </main>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>
