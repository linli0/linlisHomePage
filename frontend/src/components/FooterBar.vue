<template>
  <footer class="bg-surface-900 dark:bg-surface-950 relative overflow-hidden">
    <div class="absolute inset-0 hero-pattern opacity-5"></div>
    <div class="absolute top-0 left-1/4 w-96 h-96 bg-primary-500/5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-0 right-1/4 w-96 h-96 bg-accent-500/5 rounded-full blur-3xl"></div>
    
    <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12">
        <div class="lg:col-span-1">
          <router-link to="/" class="flex items-center space-x-3 mb-6 group">
            <div class="relative">
              <div class="absolute inset-0 bg-primary-500/20 rounded-xl blur-lg group-hover:bg-primary-500/30 transition-colors"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-primary-500 to-accent-500 rounded-xl flex items-center justify-center shadow-lg group-hover:scale-105 transition-transform">
                <span class="text-2xl">☕</span>
              </div>
            </div>
            <span class="font-bold text-xl text-white">CoffeeCookies</span>
          </router-link>
          <p class="text-surface-400 text-sm leading-relaxed mb-6">
            一个集金价追踪、技术文章、实用工具于一体的个人主页应用。基于 Express + Vue 3 构建的现代化全栈应用。
          </p>
          <div class="flex space-x-4">
            <a href="#" class="w-10 h-10 rounded-xl bg-surface-800 hover:bg-surface-700 flex items-center justify-center text-surface-400 hover:text-white transition-all duration-200">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
            </a>
            <a href="#" class="w-10 h-10 rounded-xl bg-surface-800 hover:bg-surface-700 flex items-center justify-center text-surface-400 hover:text-white transition-all duration-200">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z"/>
              </svg>
            </a>
          </div>
        </div>
        
        <div>
          <h3 class="text-white font-semibold mb-6">快速链接</h3>
          <ul class="space-y-4">
            <li>
              <router-link to="/gold" class="text-surface-400 hover:text-white transition-colors flex items-center gap-2 group">
                <span class="w-1.5 h-1.5 rounded-full bg-gold-500 group-hover:scale-150 transition-transform"></span>
                金价追踪
              </router-link>
            </li>
            <li>
              <router-link to="/articles" class="text-surface-400 hover:text-white transition-colors flex items-center gap-2 group">
                <span class="w-1.5 h-1.5 rounded-full bg-primary-500 group-hover:scale-150 transition-transform"></span>
                技术文章
              </router-link>
            </li>
            <li>
              <router-link to="/tools" class="text-surface-400 hover:text-white transition-colors flex items-center gap-2 group">
                <span class="w-1.5 h-1.5 rounded-full bg-green-500 group-hover:scale-150 transition-transform"></span>
                实用工具
              </router-link>
            </li>
            <li>
              <router-link to="/ai" class="text-surface-400 hover:text-white transition-colors flex items-center gap-2 group">
                <span class="w-1.5 h-1.5 rounded-full bg-accent-500 group-hover:scale-150 transition-transform"></span>
                AI 对话
              </router-link>
            </li>
          </ul>
        </div>
        
        <div>
          <h3 class="text-white font-semibold mb-6">功能特性</h3>
          <ul class="space-y-4">
            <li>
              <span class="text-surface-400">实时金价查询</span>
            </li>
            <li>
              <span class="text-surface-400">多货币支持</span>
            </li>
            <li>
              <span class="text-surface-400">价格走势图表</span>
            </li>
            <li>
              <span class="text-surface-400">AI 智能对话</span>
            </li>
            <li>
              <span class="text-surface-400">开发者工具箱</span>
            </li>
          </ul>
        </div>
        
        <div>
          <h3 class="text-white font-semibold mb-6">技术栈</h3>
          <div class="flex flex-wrap gap-2">
            <span class="px-3 py-1.5 bg-surface-800 text-surface-300 text-xs rounded-lg font-medium">
              Express 4
            </span>
            <span class="px-3 py-1.5 bg-surface-800 text-surface-300 text-xs rounded-lg font-medium">
              Vue 3
            </span>
            <span class="px-3 py-1.5 bg-surface-800 text-surface-300 text-xs rounded-lg font-medium">
              Node.js
            </span>
            <span class="px-3 py-1.5 bg-surface-800 text-surface-300 text-xs rounded-lg font-medium">
              TypeScript
            </span>
            <span class="px-3 py-1.5 bg-surface-800 text-surface-300 text-xs rounded-lg font-medium">
              Tailwind CSS
            </span>
            <span class="px-3 py-1.5 bg-surface-800 text-surface-300 text-xs rounded-lg font-medium">
              Chart.js
            </span>
            <span class="px-3 py-1.5 bg-surface-800 text-surface-300 text-xs rounded-lg font-medium">
              Pinia
            </span>
            <span class="px-3 py-1.5 bg-surface-800 text-surface-300 text-xs rounded-lg font-medium">
              Ollama
            </span>
          </div>
        </div>
      </div>
      
      <div class="mt-16 pt-8 border-t border-surface-800">
        <div class="flex flex-col md:flex-row items-center justify-between gap-4">
          <p class="text-surface-500 text-sm">
            &copy; {{ new Date().getFullYear() }} CoffeeCookies Homepage. All rights reserved.
          </p>
          <div class="flex items-center gap-6 text-sm text-surface-500">
            <span class="flex items-center gap-2">
              <span class="relative flex h-2 w-2">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
              </span>
              系统运行正常
            </span>
            <span>Built with ❤️</span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
</script>
