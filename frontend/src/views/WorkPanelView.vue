<template>
  <div class="page-wrap h-[calc(100vh-4rem)] flex flex-col">
    <h1 class="page-title text-lg font-semibold text-ink-800 dark:text-ink-200 mb-4">AI 工作台</h1>
    <div class="flex-1 rounded-2xl overflow-hidden border border-ink-200 dark:border-ink-700 shadow-sm bg-white dark:bg-ink-900">
      <iframe
        :src="workpanelUrl"
        class="w-full h-full"
        frameborder="0"
        allow="clipboard-read; clipboard-write"
        @load="onLoad"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const workpanelUrl = ref('http://101.132.60.79:8080')

function onLoad() {
  console.log('WorkPanel loaded')
}
</script>
