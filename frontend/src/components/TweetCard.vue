<template>
  <article class="card p-5">
    <div class="flex items-start justify-between gap-3 mb-2">
      <div>
        <p class="font-semibold text-ink-900 dark:text-white">
          {{ tweet.displayName || tweet.username }}
        </p>
        <p class="text-xs text-ink-400">@{{ tweet.username }} · {{ tweet.platform }}</p>
      </div>
      <p class="text-xs text-ink-400 shrink-0">{{ formatDate(tweet.createdAt) }}</p>
    </div>
    <p class="text-sm text-ink-700 dark:text-ink-200 whitespace-pre-wrap leading-relaxed">{{ tweet.content }}</p>
    <div class="flex gap-4 mt-3 text-xs text-ink-400">
      <span>♥ {{ tweet.likesCount }}</span>
      <span>↻ {{ tweet.retweetsCount }}</span>
      <span>💬 {{ tweet.repliesCount }}</span>
    </div>
  </article>
</template>

<script setup lang="ts">
import type { TweetDTO } from '@/types/tweet'
import { formatDate } from '@/utils/format'

defineProps<{ tweet: TweetDTO }>()
</script>
