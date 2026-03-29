<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">实用工具箱</h1>
        <p class="text-gray-600 dark:text-gray-400">开发者常用工具集合</p>
      </div>

      <!-- AI Chat Tool (Full Width) -->
      <div class="card p-6 mb-6">
        <AIChat />
      </div>

      <!-- Tool Categories -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- JSON Tools -->
        <div class="card p-6">
          <div class="flex items-center space-x-3 mb-4">
            <div class="w-10 h-10 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center">
              <span class="text-xl">📋</span>
            </div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">JSON 工具</h2>
          </div>
          
          <div class="space-y-4">
            <div>
              <textarea
                v-model="jsonInput"
                placeholder="输入 JSON..."
                class="input h-32 font-mono text-sm"
              ></textarea>
            </div>
            <div class="flex space-x-2">
              <button @click="formatJson" class="btn-primary flex-1">格式化</button>
              <button @click="minifyJson" class="btn-secondary flex-1">压缩</button>
            </div>
            <div v-if="jsonOutput" class="relative">
              <textarea
                v-model="jsonOutput"
                readonly
                class="input h-32 font-mono text-sm bg-gray-50 dark:bg-gray-900"
              ></textarea>
              <button
                @click="copyToClipboard(jsonOutput)"
                class="absolute top-2 right-2 p-1.5 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300"
                title="复制"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Base64 Tools -->
        <div class="card p-6">
          <div class="flex items-center space-x-3 mb-4">
            <div class="w-10 h-10 bg-green-100 dark:bg-green-900/30 rounded-lg flex items-center justify-center">
              <span class="text-xl">🔐</span>
            </div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">Base64 工具</h2>
          </div>
          
          <div class="space-y-4">
            <div>
              <textarea
                v-model="base64Input"
                placeholder="输入文本..."
                class="input h-24 font-mono text-sm"
              ></textarea>
            </div>
            <div class="flex space-x-2">
              <button @click="base64Encode" class="btn-primary flex-1">编码</button>
              <button @click="base64Decode" class="btn-secondary flex-1">解码</button>
            </div>
            <div v-if="base64Output" class="relative">
              <textarea
                v-model="base64Output"
                readonly
                class="input h-24 font-mono text-sm bg-gray-50 dark:bg-gray-900"
              ></textarea>
              <button
                @click="copyToClipboard(base64Output)"
                class="absolute top-2 right-2 p-1.5 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- URL Tools -->
        <div class="card p-6">
          <div class="flex items-center space-x-3 mb-4">
            <div class="w-10 h-10 bg-purple-100 dark:bg-purple-900/30 rounded-lg flex items-center justify-center">
              <span class="text-xl">🔗</span>
            </div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">URL 工具</h2>
          </div>
          
          <div class="space-y-4">
            <div>
              <textarea
                v-model="urlInput"
                placeholder="输入文本..."
                class="input h-24 font-mono text-sm"
              ></textarea>
            </div>
            <div class="flex space-x-2">
              <button @click="urlEncode" class="btn-primary flex-1">编码</button>
              <button @click="urlDecode" class="btn-secondary flex-1">解码</button>
            </div>
            <div v-if="urlOutput" class="relative">
              <textarea
                v-model="urlOutput"
                readonly
                class="input h-24 font-mono text-sm bg-gray-50 dark:bg-gray-900"
              ></textarea>
              <button
                @click="copyToClipboard(urlOutput)"
                class="absolute top-2 right-2 p-1.5 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Hash Tools -->
        <div class="card p-6">
          <div class="flex items-center space-x-3 mb-4">
            <div class="w-10 h-10 bg-red-100 dark:bg-red-900/30 rounded-lg flex items-center justify-center">
              <span class="text-xl">#️⃣</span>
            </div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">哈希工具</h2>
          </div>
          
          <div class="space-y-4">
            <div>
              <input
                v-model="hashInput"
                placeholder="输入文本..."
                class="input font-mono"
              />
            </div>
            <div class="flex flex-wrap gap-2">
              <button @click="calculateHash('md5')" class="btn-secondary text-sm">MD5</button>
              <button @click="calculateHash('sha1')" class="btn-secondary text-sm">SHA1</button>
              <button @click="calculateHash('sha256')" class="btn-secondary text-sm">SHA256</button>
              <button @click="calculateHash('sha512')" class="btn-secondary text-sm">SHA512</button>
            </div>
            <div v-if="hashOutput" class="relative">
              <input
                v-model="hashOutput"
                readonly
                class="input font-mono text-sm bg-gray-50 dark:bg-gray-900"
              />
              <button
                @click="copyToClipboard(hashOutput)"
                class="absolute top-2 right-2 p-1.5 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Timestamp Tools -->
        <div class="card p-6">
          <div class="flex items-center space-x-3 mb-4">
            <div class="w-10 h-10 bg-yellow-100 dark:bg-yellow-900/30 rounded-lg flex items-center justify-center">
              <span class="text-xl">🕐</span>
            </div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">时间戳转换</h2>
          </div>
          
          <div class="space-y-4">
            <div class="flex space-x-2">
              <select v-model="timestampFormat" class="input w-auto">
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
            <div v-if="timestampOutput" class="space-y-2 p-4 bg-gray-50 dark:bg-gray-900 rounded-lg">
              <div class="flex justify-between">
                <span class="text-sm text-gray-500 dark:text-gray-400">毫秒:</span>
                <span class="font-mono text-sm">{{ timestampOutput.timestampMs }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-500 dark:text-gray-400">秒:</span>
                <span class="font-mono text-sm">{{ timestampOutput.timestampSec }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-500 dark:text-gray-400">ISO:</span>
                <span class="font-mono text-sm">{{ timestampOutput.iso }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-500 dark:text-gray-400">格式化:</span>
                <span class="font-mono text-sm">{{ timestampOutput.formatted }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- QR Code Generator -->
        <div class="card p-6">
          <div class="flex items-center space-x-3 mb-4">
            <div class="w-10 h-10 bg-indigo-100 dark:bg-indigo-900/30 rounded-lg flex items-center justify-center">
              <span class="text-xl">📱</span>
            </div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">二维码生成</h2>
          </div>
          
          <div class="space-y-4">
            <textarea
              v-model="qrContent"
              placeholder="输入内容..."
              class="input h-24"
            ></textarea>
            <div class="flex items-center space-x-4">
              <div>
                <label class="text-sm text-gray-600 dark:text-gray-400">宽度</label>
                <input v-model.number="qrWidth" type="number" min="100" max="500" class="input w-24" />
              </div>
              <div>
                <label class="text-sm text-gray-600 dark:text-gray-400">高度</label>
                <input v-model.number="qrHeight" type="number" min="100" max="500" class="input w-24" />
              </div>
              <button @click="generateQRCode" class="btn-primary mt-6">生成</button>
            </div>
            <div v-if="qrImageUrl" class="flex justify-center p-4 bg-gray-50 dark:bg-gray-900 rounded-lg">
              <img :src="qrImageUrl" alt="QR Code" class="max-w-full h-auto" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { toolsApi, type TimestampResult } from '@/api/tools'
import AIChat from '@/components/AIChat.vue'

// JSON Tools
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

// Base64 Tools
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

// URL Tools
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

// Hash Tools
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

// Timestamp Tools
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

// QR Code
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

// Copy to clipboard
function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text)
}
</script>
