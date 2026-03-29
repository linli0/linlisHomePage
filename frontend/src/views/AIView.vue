<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-6">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">AI 对话</h1>
        <p class="mt-2 text-gray-600 dark:text-gray-400">
          与本地 Ollama 模型对话
        </p>
      </div>
      
      <div class="card p-6">
        <AIChat />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AIChat from '@/components/AIChat.vue'
</script>
