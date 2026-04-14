<template>
  <div class="min-h-screen bg-surface-50 dark:bg-surface-950 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12">
        <span class="inline-block px-4 py-1.5 rounded-full bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 text-sm font-medium mb-4">
          开发者工具
        </span>
        <h1 class="text-4xl md:text-5xl font-bold text-surface-900 dark:text-white mb-4">
          实用<span class="gradient-text">工具箱</span>
        </h1>
        <p class="text-lg text-surface-600 dark:text-surface-400 max-w-2xl">
          开发者常用工具集合，提供 JSON 格式化、Base64 编解码、哈希计算、二维码生成等实用功能，以及 AI 智能对话
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div class="card-hover p-8">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-blue-400/20 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-blue-400 to-blue-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">📋</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">JSON 工具</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">格式化与压缩</p>
            </div>
          </div>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输入 JSON</label>
              <textarea
                v-model="jsonInput"
                placeholder='{"key": "value"}'
                class="input h-32 font-mono text-sm resize-none"
              ></textarea>
            </div>
            <div class="flex gap-3">
              <button @click="formatJson" class="btn-primary flex-1">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16m-7 6h7" />
                </svg>
                格式化
              </button>
              <button @click="minifyJson" class="btn-secondary flex-1">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
                压缩
              </button>
            </div>
            <div v-if="jsonOutput" class="relative">
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输出结果</label>
              <textarea
                v-model="jsonOutput"
                readonly
                class="input h-32 font-mono text-sm bg-surface-50 dark:bg-surface-900 resize-none"
              ></textarea>
              <button
                @click="copyToClipboard(jsonOutput)"
                class="absolute top-10 right-3 p-2 rounded-lg bg-surface-200 dark:bg-surface-700 text-surface-600 dark:text-surface-300 hover:bg-surface-300 dark:hover:bg-surface-600 transition-colors"
                title="复制"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="card-hover p-8">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-green-400/20 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-green-400 to-green-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">🔐</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">Base64 工具</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">编码与解码</p>
            </div>
          </div>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输入文本</label>
              <textarea
                v-model="base64Input"
                placeholder="输入要编码或解码的文本..."
                class="input h-24 font-mono text-sm resize-none"
              ></textarea>
            </div>
            <div class="flex gap-3">
              <button @click="base64Encode" class="btn-primary flex-1">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
                编码
              </button>
              <button @click="base64Decode" class="btn-secondary flex-1">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z" />
                </svg>
                解码
              </button>
            </div>
            <div v-if="base64Output" class="relative">
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输出结果</label>
              <textarea
                v-model="base64Output"
                readonly
                class="input h-24 font-mono text-sm bg-surface-50 dark:bg-surface-900 resize-none"
              ></textarea>
              <button
                @click="copyToClipboard(base64Output)"
                class="absolute top-10 right-3 p-2 rounded-lg bg-surface-200 dark:bg-surface-700 text-surface-600 dark:text-surface-300 hover:bg-surface-300 dark:hover:bg-surface-600 transition-colors"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="card-hover p-8">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-purple-400/20 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-purple-400 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">🔗</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">URL 工具</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">编码与解码</p>
            </div>
          </div>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输入文本</label>
              <textarea
                v-model="urlInput"
                placeholder="输入要编码或解码的 URL..."
                class="input h-24 font-mono text-sm resize-none"
              ></textarea>
            </div>
            <div class="flex gap-3">
              <button @click="urlEncode" class="btn-primary flex-1">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                </svg>
                编码
              </button>
              <button @click="urlDecode" class="btn-secondary flex-1">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                </svg>
                解码
              </button>
            </div>
            <div v-if="urlOutput" class="relative">
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输出结果</label>
              <textarea
                v-model="urlOutput"
                readonly
                class="input h-24 font-mono text-sm bg-surface-50 dark:bg-surface-900 resize-none"
              ></textarea>
              <button
                @click="copyToClipboard(urlOutput)"
                class="absolute top-10 right-3 p-2 rounded-lg bg-surface-200 dark:bg-surface-700 text-surface-600 dark:text-surface-300 hover:bg-surface-300 dark:hover:bg-surface-600 transition-colors"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="card-hover p-8">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-red-400/20 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-red-400 to-red-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">#️⃣</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">哈希工具</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">多种哈希算法</p>
            </div>
          </div>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输入文本</label>
              <input
                v-model="hashInput"
                placeholder="输入要计算哈希的文本..."
                class="input font-mono"
              />
            </div>
            <div class="flex flex-wrap gap-2">
              <button @click="calculateHash('md5')" class="btn-secondary text-sm">
                MD5
              </button>
              <button @click="calculateHash('sha1')" class="btn-secondary text-sm">
                SHA1
              </button>
              <button @click="calculateHash('sha256')" class="btn-secondary text-sm">
                SHA256
              </button>
              <button @click="calculateHash('sha512')" class="btn-secondary text-sm">
                SHA512
              </button>
            </div>
            <div v-if="hashOutput" class="relative">
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输出结果</label>
              <input
                v-model="hashOutput"
                readonly
                class="input font-mono text-sm bg-surface-50 dark:bg-surface-900 pr-12"
              />
              <button
                @click="copyToClipboard(hashOutput)"
                class="absolute right-3 top-9 p-2 rounded-lg bg-surface-200 dark:bg-surface-700 text-surface-600 dark:text-surface-300 hover:bg-surface-300 dark:hover:bg-surface-600 transition-colors"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="card-hover p-8">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-yellow-400/20 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-yellow-400 to-yellow-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">🕐</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">时间戳转换</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">多种格式互转</p>
            </div>
          </div>
          
          <div class="space-y-4">
            <div class="flex gap-3">
              <select v-model="timestampFormat" class="input w-auto min-w-[100px]">
                <option value="timestamp_ms">毫秒</option>
                <option value="timestamp_s">秒</option>
                <option value="iso">ISO</option>
              </select>
              <input
                v-model="timestampInput"
                placeholder="输入时间戳..."
                class="input flex-1 font-mono"
              />
              <button @click="convertTimestamp" class="btn-primary">转换</button>
            </div>
            <div v-if="timestampOutput" class="p-5 bg-surface-50 dark:bg-surface-900 rounded-xl space-y-3">
              <div class="flex justify-between items-center">
                <span class="text-sm text-surface-500 dark:text-surface-400">毫秒时间戳</span>
                <code class="font-mono text-sm bg-surface-200 dark:bg-surface-800 px-2 py-1 rounded">{{ timestampOutput.timestampMs }}</code>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-surface-500 dark:text-surface-400">秒时间戳</span>
                <code class="font-mono text-sm bg-surface-200 dark:bg-surface-800 px-2 py-1 rounded">{{ timestampOutput.timestampSec }}</code>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-surface-500 dark:text-surface-400">ISO 格式</span>
                <code class="font-mono text-sm bg-surface-200 dark:bg-surface-800 px-2 py-1 rounded">{{ timestampOutput.iso }}</code>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-surface-500 dark:text-surface-400">本地格式</span>
                <code class="font-mono text-sm bg-surface-200 dark:bg-surface-800 px-2 py-1 rounded">{{ timestampOutput.formatted }}</code>
              </div>
            </div>
          </div>
        </div>

        <div class="card-hover p-8">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-indigo-400/20 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-indigo-400 to-indigo-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">📱</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">二维码生成</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">自定义尺寸</p>
            </div>
          </div>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">输入内容</label>
              <textarea
                v-model="qrContent"
                placeholder="输入要生成二维码的内容..."
                class="input h-20 resize-none"
              ></textarea>
            </div>
            <div class="flex items-end gap-4">
              <div>
                <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">宽度</label>
                <input v-model.number="qrWidth" type="number" min="100" max="500" class="input w-24" />
              </div>
              <div>
                <label class="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">高度</label>
                <input v-model.number="qrHeight" type="number" min="100" max="500" class="input w-24" />
              </div>
              <button @click="generateQRCode" class="btn-primary mb-0.5">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
                </svg>
                生成
              </button>
            </div>
            <div v-if="qrImageUrl" class="flex justify-center p-6 bg-surface-50 dark:bg-surface-900 rounded-xl">
              <img :src="qrImageUrl" alt="QR Code" class="max-w-full h-auto rounded-lg shadow-soft" />
            </div>
          </div>
        </div>

        <!-- AI 对话工具 -->
        <div class="card-hover p-8 lg:col-span-2">
          <div class="flex items-center gap-4 mb-6">
            <div class="relative">
              <div class="absolute inset-0 bg-accent-400/20 rounded-xl blur-lg"></div>
              <div class="relative w-12 h-12 bg-gradient-to-br from-accent-400 to-accent-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-2xl">🤖</span>
              </div>
            </div>
            <div>
              <h2 class="text-xl font-bold text-surface-900 dark:text-white">AI 智能对话</h2>
              <p class="text-sm text-surface-500 dark:text-surface-400">基于本地/远程 Ollama 模型</p>
            </div>
          </div>
          <AIChat />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { toolsApi, type TimestampResult } from '@/api/tools'
import AIChat from '@/components/AIChat.vue'

const jsonInput = ref('')
const jsonOutput = ref('')

async function formatJson() {
  try {
    const res = await toolsApi.formatJson(jsonInput.value)
    jsonOutput.value = res.data.data
  } catch (error) {
    jsonOutput.value = 'Error: Invalid JSON'
  }
}

async function minifyJson() {
  try {
    const res = await toolsApi.minifyJson(jsonInput.value)
    jsonOutput.value = res.data.data
  } catch (error) {
    jsonOutput.value = 'Error: Invalid JSON'
  }
}

const base64Input = ref('')
const base64Output = ref('')

async function base64Encode() {
  try {
    const res = await toolsApi.base64Encode(base64Input.value)
    base64Output.value = res.data.data
  } catch (error) {
    base64Output.value = 'Error'
  }
}

async function base64Decode() {
  try {
    const res = await toolsApi.base64Decode(base64Input.value)
    base64Output.value = res.data.data
  } catch (error) {
    base64Output.value = 'Error: Invalid Base64'
  }
}

const urlInput = ref('')
const urlOutput = ref('')

async function urlEncode() {
  try {
    const res = await toolsApi.urlEncode(urlInput.value)
    urlOutput.value = res.data.data
  } catch (error) {
    urlOutput.value = 'Error'
  }
}

async function urlDecode() {
  try {
    const res = await toolsApi.urlDecode(urlInput.value)
    urlOutput.value = res.data.data
  } catch (error) {
    urlOutput.value = 'Error: Invalid URL encoding'
  }
}

const hashInput = ref('')
const hashOutput = ref('')

async function calculateHash(algorithm: string) {
  try {
    let res
    switch (algorithm) {
      case 'md5':
        res = await toolsApi.md5(hashInput.value)
        break
      case 'sha1':
        res = await toolsApi.sha1(hashInput.value)
        break
      case 'sha256':
        res = await toolsApi.sha256(hashInput.value)
        break
      case 'sha512':
        res = await toolsApi.sha512(hashInput.value)
        break
      default:
        return
    }
    hashOutput.value = res.data.data
  } catch (error) {
    hashOutput.value = 'Error'
  }
}

const timestampInput = ref('')
const timestampFormat = ref('timestamp_ms')
const timestampOutput = ref<TimestampResult | null>(null)

async function convertTimestamp() {
  try {
    const res = await toolsApi.timestampConvert(timestampInput.value, timestampFormat.value)
    timestampOutput.value = res.data.data
  } catch (error) {
    timestampOutput.value = null
  }
}

const qrContent = ref('')
const qrWidth = ref(200)
const qrHeight = ref(200)
const qrImageUrl = ref('')

async function generateQRCode() {
  try {
    const res = await toolsApi.generateQRCode(qrContent.value, qrWidth.value, qrHeight.value)
    const blob = new Blob([res.data], { type: 'image/png' })
    qrImageUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('Failed to generate QR code:', error)
  }
}

function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text)
}
</script>
