<template>
  <footer class="bg-brand-50 border-t border-ink-100 dark:bg-ink-950 dark:border-ink-800">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-10">
        <div>
          <p class="font-display text-xl font-semibold text-ink-900 dark:text-white">CoffeeCookies</p>
          <p class="mt-2 text-sm text-ink-500 max-w-xs leading-relaxed">
            个人主页：今日金价、Wiki、工具与本地 AI。
          </p>
        </div>
        <nav class="flex flex-wrap gap-x-6 gap-y-2 text-sm">
          <router-link to="/gold" class="text-ink-500 hover:text-brand-600">金价</router-link>
          <router-link to="/articles" class="text-ink-500 hover:text-brand-600">Wiki</router-link>
          <router-link to="/tools" class="text-ink-500 hover:text-brand-600">工具</router-link>
          <router-link to="/ai" class="text-ink-500 hover:text-brand-600">AI</router-link>
        </nav>
      </div>
      <p class="mt-10 pt-6 border-t border-ink-100 dark:border-ink-800 text-xs font-mono text-ink-400">
        &copy; {{ year }} CoffeeCookies
      </p>
    </div>
  </footer>
</template>

<script setup lang="ts">
const year = new Date().getFullYear()
</script>
