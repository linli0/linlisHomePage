import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)
  
  const theme = computed(() => isDark.value ? 'dark' : 'light')
  
  function toggleTheme() {
    isDark.value = !isDark.value
    updateDocumentClass()
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  }
  
  function initTheme() {
    const saved = localStorage.getItem('theme')
    if (saved) {
      isDark.value = saved === 'dark'
    } else {
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    updateDocumentClass()
  }
  
  function updateDocumentClass() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
  
  return {
    isDark,
    theme,
    toggleTheme,
    initTheme
  }
})
