import request from '@/utils/request'

export interface XiaomiDevice {
  deviceId: string
  name: string
  model?: string
  online: boolean
  volume?: number | null
  ip?: string
  lastSeen?: string
  did?: string
}

export interface XiaomiStatus {
  connected: boolean
  configured?: boolean
  device?: XiaomiDevice
  error?: string
  source?: string
}

export interface CloudDevice {
  did: string
  name: string
  model: string
  ip: string
  isOnline: boolean
  hasToken: boolean
  isSpeaker: boolean
}

export interface AccountStatus {
  bound: boolean
  miUsername?: string | null
  miUserId?: string | null
  hasPassToken?: boolean
  device?: {
    did: string
    name: string
    model: string
    ip: string
    hasToken: boolean
  } | null
  updatedAt?: string | null
}

export interface ChatMessage {
  id?: number
  role: string
  content: string
  route?: string | null
  provider?: string | null
  createdAt?: string | null
}

export interface DialogueSettings {
  announceEnabled: boolean
  voiceInputEnabled: boolean
  provider: string
  idleSec?: number
  ttsCooldownMs?: number
}

export interface PanelKeyword {
  id: number
  keyword: string
  actionType: string
  payload: Record<string, unknown>
  enabled: boolean
  sortOrder: number
}

export const xiaomiApi = {
  getStatus: () => request.get('/xiaomi/status'),
  getConfig: () => request.get('/xiaomi/config'),
  getAccountStatus: () => request.get('/xiaomi/account/status'),
  accountLogin: (username: string, password: string) =>
    request.post('/xiaomi/account/login', { username, password }),
  accountVerify: (sessionId: string, code: string) =>
    request.post('/xiaomi/account/verify', { sessionId, code }),
  accountBind: (sessionId: string, did: string) =>
    request.post('/xiaomi/account/bind', { sessionId, did }),
  refreshDevices: () => request.post('/xiaomi/account/refresh-devices'),
  rebindDevice: (did: string) => request.post('/xiaomi/account/rebind-device', { did }),
  unbind: () => request.delete('/xiaomi/account'),
  tts: (data: { text: string; volume?: number }) => request.post('/xiaomi/tts', data),
  setVolume: (volume: number) => request.post('/xiaomi/volume', { volume }),
  play: () => request.post('/xiaomi/play'),
  pause: () => request.post('/xiaomi/pause'),
  stop: () => request.post('/xiaomi/stop'),
  wake: () => request.post('/xiaomi/wake'),

  getDialogueStatus: () => request.get('/xiaomi/dialogue/status'),
  getMessages: (limit = 50) => request.get(`/xiaomi/dialogue/messages?limit=${limit}`),
  utterance: (text: string, source = 'web') =>
    request.post('/xiaomi/dialogue/utterance', { text, source }),
  setMode: (mode: string, speak = false) =>
    request.post('/xiaomi/dialogue/mode', { mode, speak }),
  getDialogueSettings: () => request.get('/xiaomi/dialogue/settings'),
  updateDialogueSettings: (body: Partial<DialogueSettings>) =>
    request.put('/xiaomi/dialogue/settings', body),
  listPanelKeywords: () => request.get('/xiaomi/panel/keywords'),
  createPanelKeyword: (body: {
    keyword: string
    actionType: string
    payload?: Record<string, unknown>
    enabled?: boolean
    sortOrder?: number
  }) => request.post('/xiaomi/panel/keywords', body),
  updatePanelKeyword: (
    id: number,
    body: {
      keyword: string
      actionType: string
      payload?: Record<string, unknown>
      enabled?: boolean
      sortOrder?: number
    },
  ) => request.put(`/xiaomi/panel/keywords/${id}`, body),
  deletePanelKeyword: (id: number) => request.delete(`/xiaomi/panel/keywords/${id}`),
  getVoiceStatus: () => request.get('/xiaomi/voice/status'),
}
