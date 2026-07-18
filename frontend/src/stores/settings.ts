import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { DEFAULT_SETTINGS, type SettingsConfig, type XiaomiConfig, type TwitterConfig, type AIConfig } from '@/types/settings'

const STORAGE_KEY = 'settings-config'

function loadSettings(): SettingsConfig {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return { ...DEFAULT_SETTINGS, xiaomi: { ...DEFAULT_SETTINGS.xiaomi }, twitter: { ...DEFAULT_SETTINGS.twitter }, ai: { ...DEFAULT_SETTINGS.ai } }
    const parsed = JSON.parse(raw) as Partial<SettingsConfig>
    return {
      xiaomi: { ...DEFAULT_SETTINGS.xiaomi, ...parsed.xiaomi },
      twitter: { ...DEFAULT_SETTINGS.twitter, ...parsed.twitter },
      ai: { ...DEFAULT_SETTINGS.ai, ...parsed.ai },
    }
  } catch {
    return { ...DEFAULT_SETTINGS, xiaomi: { ...DEFAULT_SETTINGS.xiaomi }, twitter: { ...DEFAULT_SETTINGS.twitter }, ai: { ...DEFAULT_SETTINGS.ai } }
  }
}

export const useSettingsStore = defineStore('settings', () => {
  const settings = ref<SettingsConfig>(loadSettings())

  watch(settings, (v) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(v))
  }, { deep: true })

  function updateXiaomiConfig(partial: Partial<XiaomiConfig>) {
    settings.value.xiaomi = { ...settings.value.xiaomi, ...partial }
  }

  function updateTwitterConfig(partial: Partial<TwitterConfig>) {
    settings.value.twitter = { ...settings.value.twitter, ...partial }
  }

  function updateAIConfig(partial: Partial<AIConfig>) {
    settings.value.ai = { ...settings.value.ai, ...partial }
  }

  function resetToDefaults() {
    settings.value = {
      xiaomi: { ...DEFAULT_SETTINGS.xiaomi },
      twitter: { ...DEFAULT_SETTINGS.twitter },
      ai: { ...DEFAULT_SETTINGS.ai },
    }
  }

  return { settings, updateXiaomiConfig, updateTwitterConfig, updateAIConfig, resetToDefaults }
})
