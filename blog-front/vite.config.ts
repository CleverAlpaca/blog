import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // SEO 文件由后端动态生成（生产由 Nginx 在根路径转发）
      '/robots.txt': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/sitemap.xml': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
