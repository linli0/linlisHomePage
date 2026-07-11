<template>
  <div class="min-h-screen bg-cyber-void-950 py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-10">
        <span class="inline-block px-4 py-1.5 rounded-full bg-accent-100 dark:bg-accent-900/30 text-accent-600 dark:text-accent-400 text-sm font-medium mb-4">
          智能助手
        </span>
        <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-4">
          AI <span class="gradient-text">智能对话</span>
        </h1>
        <p class="text-lg text-surface-600 dark:text-surface-400 max-w-2xl">
          基于本地 Ollama 模型的智能对话助手，支持多种大语言模型
        </p>
      </div>
      
      <div class="card-cyber-glow p-8 border-cyber-purple-500 shadow-neon-purple">
        <AIChat />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AIChat from '@/components/AIChat.vue'
</script>
