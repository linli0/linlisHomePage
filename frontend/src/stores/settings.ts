import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import type { SettingsConfig } from '@/types/settings'
import { DEFAULT_SETTINGS } from '@/types/settings'

const STORAGE_KEY = 'settings-config'

function loadFromStorage(): SettingsConfig {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      return { ...DEFAULT_SETTINGS, ...JSON.parse(stored) }
    }
  } catch (e) {
    console.error('Failed to load settings from storage:', e)
  }
  return { ...DEFAULT_SETTINGS }
}

function saveToStorage(settings: SettingsConfig) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
  } catch (e) {
    console.error('Failed to save settings to storage:', e)
  }
}

export const useSettingsStore = defineStore('settings', () => {
  const settings = ref<SettingsConfig>(loadFromStorage())
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Persist to localStorage on change
  watch(
    settings,
    (newSettings) => {
      saveToStorage(newSettings)
    },
    { deep: true }
  )

  function updateXiaomiConfig(config: Partial<SettingsConfig['xiaomi']>) {
    settings.value.xiaomi = { ...settings.value.xiaomi, ...config }
  }

  function updateTwitterConfig(config: Partial<SettingsConfig['twitter']>) {
    settings.value.twitter = { ...settings.value.twitter, ...config }
  }

  function updateAIConfig(config: Partial<SettingsConfig['ai']>) {
    settings.value.ai = { ...settings.value.ai, ...config }
  }

  function resetToDefaults() {
    settings.value = { ...DEFAULT_SETTINGS }
  }

  return {
    settings,
    loading,
    error,
    updateXiaomiConfig,
    updateTwitterConfig,
    updateAIConfig,
    resetToDefaults
  }
})
