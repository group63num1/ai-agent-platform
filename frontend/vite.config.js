import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      // AI 服务（FastAPI）接口：仅聊天走 Python
      '/api/chat': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        ws: true,
      },
      // 业务后端（Java）接口：其余 /api/**、/v1/** 走 28080
      '/api': {
        target: 'http://localhost:28080',
        changeOrigin: true,
      },
      '/v1': {
        target: 'http://localhost:28080',
        changeOrigin: true,
      }
    }
  }
})
