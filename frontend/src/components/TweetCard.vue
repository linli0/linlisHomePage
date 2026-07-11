<template>
  <div 
    class="card-hover p-5 cursor-pointer"
    @click="handleClick"
  >
    <div class="flex items-start gap-4">
      <!-- Platform Indicator -->
      <div 
        :class="[
          'w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg',
          platformBgClass
        ]"
      >
        <span class="text-lg">{{ platformIcon }}</span>
      </div>
      
      <div class="flex-1 min-w-0">
        <!-- Header: Username and Timestamp -->
        <div class="flex items-center justify-between gap-2 mb-2">
          <div class="flex items-center gap-2">
            <span class="font-semibold text-surface-900 dark:text-white">
              {{ tweet.username }}
            </span>
            <span 
              :class="[
                'px-2 py-0.5 rounded text-xs font-medium',
                platformBadgeClass
              ]"
            >
              {{ platform }}
            </span>
          </div>
          <span class="text-sm text-surface-500 dark:text-surface-400">
            {{ formattedTime }}
          </span>
        </div>
        
        <!-- Tweet Content -->
        <p class="text-surface-700 dark:text-surface-300 leading-relaxed mb-3">
          {{ tweet.content }}
        </p>
        
        <!-- Engagement Metrics -->
        <div class="flex items-center gap-6 text-sm text-surface-500 dark:text-surface-400">
          <div class="flex items-center gap-1.5">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" />
            </svg>
            <span>{{ tweet.likes }}</span>
          </div>
          <div class="flex items-center gap-1.5">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" />
            </svg>
            <span>{{ tweet.retweets }}</span>
          </div>
          <div class="flex items-center gap-1.5">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clip-rule="evenodd" />
            </svg>
            <span>{{ tweet.replies }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Tweet {
  id: string
  username: string
  content: string
  likes: number
  retweets: number
  replies: number
  timestamp: string
}

const props = defineProps<{
  tweet: Tweet
  platform: string
}>()

const emit = defineEmits<{
  click: [tweet: Tweet]
}>()

const platformIcon = computed(() => {
  const icons: Record<string, string> = {
    twitter: '🐦',
    x: '❌',
    bluesky: '💙',
    threads: '🧵',
    mastodon: '🐘'
  }
  return icons[props.platform.toLowerCase()] || '📱'
})

const platformBgClass = computed(() => {
  const classes: Record<string, string> = {
    twitter: 'bg-gradient-to-br from-blue-400 to-blue-600',
    x: 'bg-gradient-to-br from-gray-700 to-gray-900',
    bluesky: 'bg-gradient-to-br from-sky-400 to-sky-600',
    threads: 'bg-gradient-to-br from-purple-400 to-purple-600',
    mastodon: 'bg-gradient-to-br from-purple-500 to-indigo-600'
  }
  return classes[props.platform.toLowerCase()] || 'bg-gradient-to-br from-surface-400 to-surface-600'
})

const platformBadgeClass = computed(() => {
  const classes: Record<string, string> = {
    twitter: 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300',
    x: 'bg-gray-100 dark:bg-gray-900/30 text-gray-700 dark:text-gray-300',
    bluesky: 'bg-sky-100 dark:bg-sky-900/30 text-sky-700 dark:text-sky-300',
    threads: 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300',
    mastodon: 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300'
  }
  return classes[props.platform.toLowerCase()] || 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300'
})

const formattedTime = computed(() => {
  const date = new Date(props.tweet.timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
})

function handleClick() {
  emit('click', props.tweet)
}
</script>