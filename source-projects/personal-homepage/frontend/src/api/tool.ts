import { get, post } from '@/utils/request'
import type { CommandRequest, CommandResponse, CommandHistory, TimestampResult } from '@/types'

export function formatJson(json: string): Promise<string> {
  return post('/tools/json/format', { json })
}

export function minifyJson(json: string): Promise<string> {
  return post('/tools/json/minify', { json })
}

export function base64Encode(text: string): Promise<string> {
  return post('/tools/base64/encode', { text })
}

export function base64Decode(encoded: string): Promise<string> {
  return post('/tools/base64/decode', { encoded })
}

export function urlEncode(text: string): Promise<string> {
  return post('/tools/url/encode', { text })
}

export function urlDecode(encoded: string): Promise<string> {
  return post('/tools/url/decode', { encoded })
}

export function md5(text: string): Promise<string> {
  return post('/tools/hash/md5', { text })
}

export function sha1(text: string): Promise<string> {
  return post('/tools/hash/sha1', { text })
}

export function sha256(text: string): Promise<string> {
  return post('/tools/hash/sha256', { text })
}

export function sha512(text: string): Promise<string> {
  return post('/tools/hash/sha512', { text })
}

export function convertTimestamp(input: string, fromFormat: string): Promise<TimestampResult> {
  return post('/tools/timestamp/convert', { input, fromFormat })
}

export function generateQRCode(content: string, width: number = 200, height: number = 200): Promise<Blob> {
  return post('/tools/qrcode/generate', { content, width, height })
}

export function executeKimiCommand(request: CommandRequest): Promise<CommandResponse> {
  return post('/tools/kimi', request)
}

export function getKimiHistory(limit: number = 20): Promise<CommandHistory[]> {
  return get('/tools/kimi/history', { limit })
}
