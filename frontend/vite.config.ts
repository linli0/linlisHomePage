import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    // Disable sourcemaps in production to reduce bundle size
    sourcemap: mode === 'development',
    // Enable Terser minification
    minify: 'terser',
    terserOptions: {
      compress: {
        // Remove console.log and debugger in production
        drop_console: mode === 'production',
        drop_debugger: mode === 'production',
        // Dead code elimination
        dead_code: true,
        // Optimize comparisons
        comparisons: true,
        // Optimize conditionals
        conditionals: true,
      },
      mangle: {
        // Mangle private properties
        properties: {
          regex: /^_/,
        },
      },
      format: {
        // Remove comments
        comments: false,
      },
    },
    rollupOptions: {
      output: {
        // Manual chunk splitting for better caching
        manualChunks: {
          // Vue ecosystem (core framework)
          'vendor': ['vue', 'vue-router', 'pinia'],
          // Chart libraries (heavy, lazy-loadable)
          'charts': ['chart.js', 'vue-chartjs', 'lightweight-charts', 'trading-signals'],
          // API and HTTP utilities
          'api': ['axios'],
          // Utility libraries
          'utils': ['dayjs', 'dompurify', 'marked', '@heroicons/vue'],
        },
        // Chunk file naming with content hash
        chunkFileNames: 'assets/js/[name]-[hash].js',
        // Entry file naming
        entryFileNames: 'assets/js/[name]-[hash].js',
        // Asset file naming
        assetFileNames: 'assets/[ext]/[name]-[hash][extname]',
      },
    },
    // Report compressed sizes
    reportCompressedSize: true,
    // Warn if chunk exceeds 500KB
    chunkSizeWarningLimit: 500,
    // Target modern browsers
    target: 'es2020',
  },
  // Dependency optimization
  optimizeDeps: {
    // Pre-bundle for faster dev startup
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      '@heroicons/vue',
    ],
    // Exclude heavy libraries for lazy loading
    exclude: [
      'lightweight-charts',
      'trading-signals',
      'chart.js',
    ],
  },
  // CSS optimization
  css: {
    devSourcemap: mode === 'development',
  },
}))
