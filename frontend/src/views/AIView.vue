<template>
  <div class="page-wrap max-w-3xl">
    <h1 class="page-title">AI 对话</h1>
    <p class="page-desc mb-8">连接本地 Ollama 模型进行对话</p>
    <AIChat />
  </div>
</template>

<script setup lang="ts">
import AIChat from '@/components/AIChat.vue'
</script>
