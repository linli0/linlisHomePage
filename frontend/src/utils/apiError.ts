import axios from 'axios'

/** Prefer FastAPI / Result envelope Chinese messages over axios English defaults. */
export function extractApiError(e: unknown, fallback = '请求失败'): string {
  if (axios.isAxiosError(e)) {
    const data = e.response?.data as { message?: string; detail?: string | { msg?: string }[] } | undefined
    if (data?.message && typeof data.message === 'string') return data.message
    if (typeof data?.detail === 'string') return data.detail
    if (Array.isArray(data?.detail) && data.detail[0]?.msg) return String(data.detail[0].msg)
  }
  if (e instanceof Error && e.message && !e.message.startsWith('Request failed')) {
    return e.message
  }
  return fallback
}
