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
      // 所有 `/api` 请求直接代理到真实后端（已移除本地 mock 专用映射）
      '/api': {
        target: 'http://localhost:28080',
        changeOrigin: true,
      }
    }
    // proxy: {
    //   '/api': {
    //     target: 'http://localhost:5175', // 这里要改成你 Mock 服务运行的端口
    //     changeOrigin: true,
    //     // 不要加这行，因为你的 Mock 代码里本身就包含了 /api 前缀
    //     // rewrite: (path) => path.replace(/^\/api/, '') 
    //   }
    // }
  }
})
