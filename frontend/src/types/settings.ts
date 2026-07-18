export interface XiaomiConfig {
  deviceId: string
  apiUrl: string
}

export interface TwitterConfig {
  keywords: string[]
  usernames: string[]
}

export interface AIConfig {
  defaultModel: string
}

export interface SettingsConfig {
  xiaomi: XiaomiConfig
  twitter: TwitterConfig
  ai: AIConfig
}

export const DEFAULT_SETTINGS: SettingsConfig = {
  xiaomi: { deviceId: '', apiUrl: 'http://localhost:8000/api/xiaomi' },
  twitter: { keywords: [], usernames: [] },
  ai: { defaultModel: '' },
}
