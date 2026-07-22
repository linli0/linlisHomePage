<template>
  <div>
    <!-- Hero: espresso chalkboard — signature = live AU9999 -->
    <section class="relative min-h-[92vh] flex items-center overflow-hidden bg-brand-900">
      <div
        class="absolute inset-0"
        style="background: radial-gradient(ellipse 80% 60% at 20% 30%, #4A2E18 0%, transparent 55%), radial-gradient(ellipse 70% 50% at 90% 80%, #6B4423 0%, transparent 50%), #24160F;"
      />
      <div class="absolute inset-0 hero-grain opacity-[0.07] mix-blend-overlay pointer-events-none" />

      <div class="relative z-10 w-full max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-28">
        <div class="grid lg:grid-cols-[1.1fr_0.9fr] gap-12 lg:gap-16 items-center">
          <div>
            <h1 class="font-display text-[clamp(3.25rem,8vw,5.5rem)] font-semibold leading-[0.95] tracking-tight text-brand-50 animate-fade-up">
              Coffee<br />Cookies
            </h1>
            <p class="mt-6 max-w-md text-lg text-brand-200/90 leading-relaxed animate-fade-up-delay">
              一杯咖啡的时间，看完今日金价，再打开 Wiki 或工具箱。
            </p>
            <div class="mt-10 flex flex-wrap gap-3 animate-fade-up-delay-2">
              <router-link
                to="/gold"
                class="inline-flex items-center justify-center px-6 py-3 rounded-lg text-sm font-semibold bg-brand-50 text-brand-900 hover:bg-brand-100 transition-colors"
              >
                看今日金价
              </router-link>
              <router-link
                to="/articles"
                class="inline-flex items-center justify-center px-6 py-3 rounded-lg text-sm font-semibold border border-brand-300/40 text-brand-100 hover:bg-white/5 transition-colors"
              >
                打开 Wiki
              </router-link>
            </div>
          </div>

          <!-- Signature: chalk board -->
          <router-link
            to="/gold"
            class="block animate-chalk-in group focus-visible:outline-offset-4"
          >
            <div
              class="relative rounded-sm border border-brand-300/25 bg-[#1a120b] p-6 sm:p-8 shadow-lg
                     before:absolute before:inset-0 before:bg-[linear-gradient(180deg,rgb(255_255_255/0.03),transparent_40%)] before:pointer-events-none
                     group-hover:border-brand-300/45 transition-colors"
            >
              <p class="font-mono text-[11px] uppercase tracking-[0.2em] text-brand-300/80 mb-4">
                今日板价 · 黄金9999
              </p>
              <div class="flex items-baseline gap-2 text-brand-50">
                <span class="font-display text-2xl text-brand-300">¥</span>
                <span class="font-display text-5xl sm:text-6xl font-semibold tabular-nums tracking-tight">
                  {{ goldPrice ? formatPrice(goldPrice.price) : (loading ? '—' : '—') }}
                </span>
                <span class="font-mono text-sm text-brand-300/70 ml-1">元/克</span>
              </div>
              <div class="mt-5 flex items-center justify-between gap-3 border-t border-brand-300/15 pt-4">
                <div>
                  <p class="font-mono text-xs text-brand-300/70">
                    <template v-if="goldPrice">
                      {{ goldPrice.changePercent >= 0 ? '+' : '' }}{{ goldPrice.changePercent.toFixed(2) }}%
                    </template>
                    <template v-else-if="loading">加载中…</template>
                    <template v-else-if="goldError">{{ goldError }}</template>
                    <template v-else>每日更新</template>
                  </p>
                  <button
                    v-if="goldError && !loading"
                    type="button"
                    class="mt-1 font-mono text-xs text-brand-200 hover:text-brand-50 underline underline-offset-2"
                    @click.stop.prevent="loadGold"
                  >
                    重试
                  </button>
                </div>
                <span class="text-sm text-brand-200 group-hover:text-brand-50 transition-colors">
                  进入金价页 →
                </span>
              </div>
            </div>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Quiet directory -->
    <section class="py-20 md:py-24 bg-brand-50">
      <div class="max-w-3xl mx-auto px-4 sm:px-6">
        <p class="font-mono text-[11px] uppercase tracking-[0.18em] text-brand-500 mb-3">目录</p>
        <h2 class="font-display text-3xl font-semibold text-ink-900 mb-10">常去的地方</h2>
        <ul class="divide-y divide-ink-100 border-y border-ink-100">
          <li v-for="item in features" :key="item.to">
            <router-link
              :to="item.to"
              class="group flex items-baseline justify-between gap-6 py-5 hover:bg-brand-100/50 -mx-2 px-2 rounded-sm transition-colors"
            >
              <div>
                <span class="font-display text-xl text-ink-900 group-hover:text-brand-600 transition-colors">
                  {{ item.title }}
                </span>
                <p class="mt-1 text-sm text-ink-500">{{ item.desc }}</p>
              </div>
              <span class="font-mono text-xs text-ink-400 shrink-0 group-hover:text-brand-500">{{ item.hint }}</span>
            </router-link>
          </li>
        </ul>
      </div>
    </section>

    <!-- Wiki -->
    <section class="py-20 md:py-24 bg-white dark:bg-ink-900">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-end justify-between gap-4 mb-10">
          <div>
            <p class="font-mono text-[11px] uppercase tracking-[0.18em] text-brand-500 mb-2">Wiki</p>
            <h2 class="font-display text-3xl font-semibold text-ink-900 dark:text-white">最近写下的</h2>
          </div>
          <router-link to="/articles" class="text-sm font-medium text-brand-600 hover:text-brand-500">全部</router-link>
        </div>

        <div v-if="articles.length" class="grid md:grid-cols-3 gap-8 md:gap-10">
          <article
            v-for="a in articles"
            :key="a.id"
            class="cursor-pointer group"
            @click="$router.push(`/article/${a.id}`)"
          >
            <p class="font-mono text-[11px] text-ink-400 mb-2">{{ formatDate(a.createdAt) }}</p>
            <h3 class="font-display text-xl font-semibold text-ink-900 dark:text-white group-hover:text-brand-600 transition-colors line-clamp-2 mb-2">
              {{ a.title }}
            </h3>
            <p class="text-sm text-ink-500 line-clamp-2">{{ a.summary }}</p>
          </article>
        </div>
        <p v-else class="text-sm text-ink-400 py-8">
          {{ loadingArticles ? '加载文章…' : '还没有文章。去 Wiki 写第一篇。' }}
        </p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { goldPriceApi, type GoldPrice } from '@/api/goldPrice'
import { articleApi, type Article } from '@/api/article'
import { formatPrice, formatDate } from '@/utils/format'

const goldPrice = ref<GoldPrice | null>(null)
const articles = ref<Article[]>([])
const loading = ref(true)
const loadingArticles = ref(true)
const goldError = ref('')

const features = [
  { to: '/gold', title: '国内金价', desc: '黄金9999 参考价，元/克，可手动刷新', hint: 'CNY' },
  { to: '/articles', title: 'Wiki', desc: '笔记与文档', hint: '读' },
  { to: '/tools', title: '工具箱', desc: 'JSON、Base64、哈希、二维码', hint: '用' },
  { to: '/ai', title: 'AI 对话', desc: '本地 Ollama', hint: '聊' },
]

function extractGoldError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const d = e.response?.data as { message?: string; detail?: string } | undefined
    if (d?.message) return d.message
    if (typeof d?.detail === 'string') return d.detail
  }
  return '金价加载失败'
}

async function loadGold() {
  loading.value = true
  goldError.value = ''
  try {
    const p = await goldPriceApi.getCurrentPrice('CNY')
    goldPrice.value = p.data.data
  } catch (e) {
    console.error(e)
    goldError.value = extractGoldError(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadGold()
  loadingArticles.value = true
  try {
    const a = await articleApi.getRecentArticles()
    articles.value = a.data.data
  } catch (e) {
    console.error(e)
  } finally {
    loadingArticles.value = false
  }
})
</script>
