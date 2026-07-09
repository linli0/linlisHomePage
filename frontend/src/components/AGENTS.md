# Components

> 8 reusable Vue 3 components. All use `<script setup lang="ts">`.

**Generated:** 2026-04-17

---

## WHERE TO LOOK

| Component | Lines | Purpose |
|-----------|-------|---------|
| AIChat.vue | 425 | Streaming AI chat with Ollama |
| PriceChart.vue | 128 | Chart.js line chart for gold prices |
| KLineChart.vue | 205 | lightweight-charts candlestick |
| NavigationBar.vue | 308 | Responsive nav with auth state |
| FooterBar.vue | 140 | Site footer |
| TweetCard.vue | 144 | Tweet display with engagement metrics |
| TweetMetricsChart.vue | 98 | Bar chart for tweet statistics |
| IndicatorOverlay.vue | 117 | Trading indicator overlay for charts |

---

## COMPLEXITY HOTSPOTS

**AIChat.vue** - Extract streaming logic:
- Chunk-based streaming via `aiApi.chatStream()`
- Markdown rendering with `marked.js` + `DOMPurify`
- Conversation history persisted to `localStorage`
- Context management for multi-turn dialogue

---

## CONVENTIONS

**Props with TypeScript:**
```typescript
const props = defineProps<{
  data: PricePoint[]
  currency: string
}>()
```

**With defaults:**
```typescript
const props = withDefaults(defineProps<{
  height?: number
}>(), {
  height: 400
})
```

**Chart cleanup:**
```typescript
onUnmounted(() => {
  chart?.remove()
})
```

---

## NOTES

- AIChat uses `localStorage` keys: `ai-chat-messages`, `ai-chat-selected-model`
- KLineChart exposes `CandleData` interface for parent components
- IndicatorOverlay attaches to existing chart instance via props
- NavigationBar detects hero page for transparent styling
