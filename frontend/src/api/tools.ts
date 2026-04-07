import request from '@/utils/request'

export interface TimestampResult {
  timestampMs: number
  timestampSec: number
  iso: string
  formatted: string
}

export const toolsApi = {
  // JSON Tools
  formatJson: (json: string) => request.post('/tools/json/format', { json }),
  minifyJson: (json: string) => request.post('/tools/json/minify', { json }),
  
  // Base64 Tools
  base64Encode: (text: string) => request.post('/tools/base64/encode', { text }),
  base64Decode: (encoded: string) => request.post('/tools/base64/decode', { encoded }),
  
  // URL Tools
  urlEncode: (text: string) => request.post('/tools/url/encode', { text }),
  urlDecode: (encoded: string) => request.post('/tools/url/decode', { encoded }),
  
  // Hash Tools
  md5: (text: string) => request.post('/tools/hash/md5', { text }),
  sha1: (text: string) => request.post('/tools/hash/sha1', { text }),
  sha256: (text: string) => request.post('/tools/hash/sha256', { text }),
  sha512: (text: string) => request.post('/tools/hash/sha512', { text }),
  
  // Timestamp Tools
  timestampConvert: (input: string, fromFormat: string = 'timestamp_ms') => 
    request.post('/tools/timestamp/convert', { input, fromFormat }),
  
  // QR Code
  generateQRCode: (content: string, width: number = 200, height: number = 200) => 
    request.post('/tools/qrcode/generate', { text: content, width, height }, { responseType: 'blob' }),
  
  // Health Check
  healthCheck: () => request.get('/tools/health')
}
