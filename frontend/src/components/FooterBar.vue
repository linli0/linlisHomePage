<template>
  <footer class="bg-white dark:bg-gray-900 border-t border-gray-200 dark:border-gray-800">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <!-- Brand -->
        <div>
          <div class="flex items-center space-x-2 mb-4">
            <span class="text-2xl">☕</span>
            <span class="font-bold text-lg text-gray-900 dark:text-white">CoffeeCookies</span>
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">
            一个集金价追踪、技术文章、实用工具于一体的个人主页应用。
          </p>
        </div>
        
        <!-- Links -->
        <div>
          <h3 class="font-semibold text-gray-900 dark:text-white mb-4">快速链接</h3>
          <ul class="space-y-2">
            <li>
              <router-link to="/gold" class="text-sm text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400">
                金价追踪
              </router-link>
            </li>
            <li>
              <router-link to="/articles" class="text-sm text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400">
                技术文章
              </router-link>
            </li>
            <li>
              <router-link to="/tools" class="text-sm text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400">
                实用工具
              </router-link>
            </li>
          </ul>
        </div>
        
        <!-- Tech Stack -->
        <div>
          <h3 class="font-semibold text-gray-900 dark:text-white mb-4">技术栈</h3>
          <div class="flex flex-wrap gap-2">
            <span class="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 text-xs rounded">
              Spring Boot 3
            </span>
            <span class="px-2 py-1 bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300 text-xs rounded">
              Vue 3
            </span>
            <span class="px-2 py-1 bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-300 text-xs rounded">
              Java 17
            </span>
            <span class="px-2 py-1 bg-cyan-100 dark:bg-cyan-900/30 text-cyan-700 dark:text-cyan-300 text-xs rounded">
              TypeScript
            </span>
          </div>
        </div>
      </div>
      
      <div class="mt-8 pt-8 border-t border-gray-200 dark:border-gray-800 text-center">
        <p class="text-sm text-gray-500 dark:text-gray-400">
          &copy; {{ new Date().getFullYear() }} CoffeeCookies Homepage. All rights reserved.
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
// No script needed
</script>
