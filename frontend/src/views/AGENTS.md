# Views Directory

> 12 page-level Vue 3 components using `<script setup lang="ts">` Composition API.

**Generated:** 2026-04-17

---

## WHERE TO LOOK

| View | Lines | Purpose |
|------|-------|---------|
| `HomeView.vue` | 428 | Landing page with hero animations, gold price preview, article cards |
| `GoldPriceView.vue` | ~300 | Price tracking with charts, multi-currency support |
| `ArticlesView.vue` | 341 | Article CRUD, categories, admin-only create modal |
| `ArticleDetailView.vue` | ~200 | Single article display with markdown rendering |
| `ToolsView.vue` | 484 | 6 developer tools (JSON, Base64, URL, Hash, Timestamp, QR) |
| `AIView.vue` | 25 | AI chat wrapper (uses AIChat component) |
| `XiaomiView.vue` | 569 | Speaker control, TTS, AI streaming chat, volume |
| `TweetsView.vue` | 344 | Tweet monitoring with search, filters, WebSocket stats |
| `QuantView.vue` | ~150 | Quantitative trading interface |
| `LoginView.vue` | ~120 | Password-only auth form |
| `RegisterView.vue` | ~100 | Registration form (currently disabled) |
| `ProfileView.vue` | ~200 | User profile management |

---

## COMPLEXITY HOTSPOTS

**XiaomiView.vue** (569 lines)
- Device status + TTS + AI chat + volume control in one file
- Streaming response handling with `xiaomiApi.chatStream()`
- LocalStorage persistence for messages and volume
- Refactor candidate: split into DevicePanel, TTSPanel, ChatPanel components

**ToolsView.vue** (484 lines)
- 6 independent tools sharing similar UI patterns
- Each tool: input → button → output with copy
- Refactor candidate: extract ToolCard component, move logic to composables

**HomeView.vue** (428 lines)
- Rich animations (float, pulse, fade)
- Multiple data sources (gold price + articles)
- Hero section with gradient overlays

---

## CONVENTIONS

### View-Specific Patterns

**Data Fetching in onMounted:**
```typescript
const data = ref<DataType | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.getData()
    data.value = res.data.data
  } catch (error) {
    console.error('Failed:', error)
  } finally {
    loading.value = false
  }
})
```

**Modal Pattern (ArticlesView):**
```typescript
const showModal = ref(false)
const formData = reactive({ title: '', content: '' })
const submitting = ref(false)
const error = ref('')
```

**WebSocket Cleanup (TweetsView):**
```typescript
import { useTweetWebSocket } from '@/composables/useTweetWebSocket'

const { messages, isConnected, connect, disconnect } = useTweetWebSocket()

onUnmounted(() => {
  disconnect()
})
```

**Streaming AI Pattern (XiaomiView):**
```typescript
const streamingContent = ref('')
const isChatLoading = ref(false)

await xiaomiApi.chatStream(
  message,
  (chunk) => { streamingContent.value += chunk },
  () => { /* complete */ },
  (error) => { /* error */ }
)
```

---

## NOTES

- All views use Tailwind `card-hover` class for consistent card styling
- Dark mode support via `dark:` variants
- Admin-only features check `authStore.isAdmin`
- Views don't import from each other, only from components/api/stores
- Complex views (Xiaomi, Tools) should be refactored into smaller components
