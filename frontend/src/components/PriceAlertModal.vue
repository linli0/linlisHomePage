<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <!-- Backdrop -->
    <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="close"></div>

    <!-- Modal Content -->
    <div class="relative w-full max-w-md card-cyber-glow border-cyber-yellow-500 shadow-neon-yellow p-6">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-bold text-surface-900 dark:text-white">价格提醒</h2>
        <button @click="close" class="text-surface-500 hover:text-surface-700 dark:hover:text-surface-300">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
            目标价格 ({{ currencySymbol }})
          </label>
          <input
            v-model.number="targetPrice"
            type="number"
            step="0.01"
            class="input w-full"
            placeholder="输入目标价格"
            required
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
            提醒类型
          </label>
          <div class="flex gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                v-model="alertType"
                type="radio"
                value="above"
                class="w-4 h-4 text-gold-500"
              />
              <span class="text-surface-700 dark:text-surface-300">高于</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                v-model="alertType"
                type="radio"
                value="below"
                class="w-4 h-4 text-gold-500"
              />
              <span class="text-surface-700 dark:text-surface-300">低于</span>
            </label>
          </div>
        </div>

        <div>
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              v-model="soundEnabled"
              type="checkbox"
              class="w-4 h-4 text-gold-500"
            />
            <span class="text-surface-700 dark:text-surface-300">启用声音提醒</span>
          </label>
        </div>

        <button type="submit" class="btn-gold w-full">
          添加提醒
        </button>
      </form>

      <!-- Existing Alerts -->
      <div v-if="alerts.length > 0" class="mt-8">
        <h3 class="text-lg font-semibold text-surface-900 dark:text-white mb-4">现有提醒</h3>
        <div class="space-y-3">
          <div
            v-for="alert in alerts"
            :key="alert.id"
            class="flex items-center justify-between p-3 bg-surface-100 dark:bg-surface-800 rounded-lg"
          >
            <div>
              <p class="text-sm font-medium text-surface-900 dark:text-white">
                {{ alert.type === 'above' ? '高于' : '低于' }} {{ currencySymbol }}{{ alert.targetPrice.toLocaleString() }}
              </p>
              <p class="text-xs text-surface-500 dark:text-surface-400">
                {{ new Date(alert.createdAt).toLocaleString('zh-CN') }}
              </p>
            </div>
            <button
              @click="removeAlert(alert.id)"
              class="text-red-500 hover:text-red-700"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { usePriceAlerts } from '@/composables/usePriceAlerts'

const props = defineProps<{
  isOpen: boolean
  currentPrice: number
  currency: string
  currencySymbol: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const { alerts, addAlert, removeAlert } = usePriceAlerts()

const targetPrice = ref(props.currentPrice)
const alertType = ref<'above' | 'below'>('above')
const soundEnabled = ref(true)

function close() {
  emit('close')
}

function handleSubmit() {
  addAlert({
    targetPrice: targetPrice.value,
    type: alertType.value,
    currency: props.currency,
    sound: soundEnabled.value
  })
  close()
}
</script>
