# Frontend - Vue 3 + TypeScript

> Vue 3 Composition API + Pinia + Vue Router + Tailwind CSS

**Generated:** 2026-04-08

---

## OVERVIEW

Vue 3 前端应用。使用 Composition API (`<script setup>`)、Pinia 状态管理、Vue Router 路由。TypeScript 严格模式。

---

## WHERE TO LOOK

| Task | Location |
|------|----------|
| 页面组件 | `src/views/*.vue` (9 个) |
| 可复用组件 | `src/components/*.vue` (4 个) |
| API 接口 | `src/api/*.ts` (auth, goldPrice, article, tools, ai) |
| 状态管理 | `src/stores/auth.ts` |
| 路由配置 | `src/router/index.ts` |
| HTTP 客户端 | `src/utils/request.ts` |
| 工具函数 | `src/utils/format.ts` |
| 样式 | `src/assets/main.css` |
| Tailwind 配置 | `tailwind.config.js` |

---

## CONVENTIONS

### 组件风格

**所有组件使用 `<script setup lang="ts">`:**
```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const data = ref<string | null>(null)
const computedValue = computed(() => data.value?.toUpperCase())

onMounted(() => {
  fetchData()
})
</script>
```

### Props 定义

```typescript
// 使用 defineProps + TypeScript 接口
const props = defineProps<{
  data: PricePoint[]
  currency: string
}>()
```

### Pinia Store

**Composition API 风格:**
```typescript
export const useAuthStore = defineStore('auth', () => {
  // State: ref()
  const token = ref<string | null>(null)
  // Getters: computed()
  const isAuthenticated = computed(() => !!token.value)
  // Actions: 函数
  async function login(password: string) { ... }
  
  return { token, isAuthenticated, login }
})
```

### API 模块

**按功能分文件:**
```typescript
// src/api/goldPrice.ts
export const goldPriceApi = {
  getCurrentPrice: (currency: string = 'USD') => 
    request.get(`/gold-price/current?currency=${currency}`),
  getPriceHistory: (currency: string, days: number) => 
    request.get(`/gold-price/history?currency=${currency}&days=${days}`)
}

export interface GoldPrice { ... }
```

### 导入顺序

```typescript
// 1. Vue/框架
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. 第三方库
import { useAuthStore } from '@/stores/auth'

// 3. 本地组件
import PriceChart from '@/components/PriceChart.vue'

// 4. 工具函数
import { formatPrice } from '@/utils/format'

// 5. 类型
import type { GoldPrice } from '@/api/goldPrice'
```

---

## ANTI-PATTERNS

1. **不要使用 Options API** — 全部使用 `<script setup>`
2. **不要使用 `any`** — 使用具体类型或 `unknown`
3. **不要在模板中写复杂逻辑** — 提取到 computed 或方法
4. **不要忘记清理副作用** — `onUnmounted` 中清理 interval/eventListener
5. **不要直接使用 fetch** — 使用 `request.ts` 中的 Axios 实例（自动注入 JWT）

---

## ROUTING

```typescript
// 路由守卫模式
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.guest && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})
```

**Meta 字段:**
- `requiresAuth`: 需要登录
- `guest`: 仅游客可访问（已登录用户跳转）

---

## HTTP CLIENT

```typescript
// src/utils/request.ts
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 自动注入 JWT
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 401 自动登出
request.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      authStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

---

## TAILWIND

**自定义色板:**
- `primary`: 天蓝 (#0ea5e9)
- `gold`: 琥珀 (#f59e0b)
- `accent`: 紫红 (#d946ef)
- `surface`: 中性灰

**自定义组件类 (main.css):**
- `.btn`, `.btn-primary`, `.btn-gold`
- `.card`, `.card-hover`, `.card-glass`
- `.input`, `.input-glass`
- `.badge`, `.badge-primary`

**暗色模式:** `darkMode: 'class'` — 需手动切换 `dark` 类

---

## COMMANDS

```bash
npm run dev      # Vite 开发服务器 (端口 3000)
npm run build    # 构建到 dist/
npm run preview  # 预览构建结果
```

---

## NOTES

- **路径别名**: `@` → `src/`
- **严格模式**: `strict: true` in tsconfig.json
- **懒加载**: 所有路由使用 `() => import('@/views/...')`
- **Streaming AI**: `ai.ts` 使用原生 fetch 处理流式响应