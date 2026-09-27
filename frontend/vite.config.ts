/// <reference types="vitest" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath, URL } from 'node:url'

const stripApi = (path: string) => path.replace(/^\/api/, '')

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    port: 5173,
    // Enquanto o API Gateway não existe (Fase 3), cada prefixo vai direto ao serviço.
    proxy: {
      '/api/auth': { target: 'http://localhost:8181', rewrite: stripApi },
      '/api/restaurants': { target: 'http://localhost:8182', rewrite: stripApi },
      '/api/cart': { target: 'http://localhost:8183', rewrite: stripApi },
      '/api/orders': { target: 'http://localhost:8183', rewrite: stripApi },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    css: false,
  },
})
