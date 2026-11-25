import { defineConfig } from 'vite'

export default defineConfig({
  // Server configuration for the Vite development server
  server: {
    // Proxy configuration - redirects API requests during development
    // This solves CORS issues by making the frontend and backend appear to be on the same origin
    proxy: {
      // Any request starting with '/api' will be proxied
      '/api': {
        target: 'http://localhost:8180',
        changeOrigin: true,
      },
    },
  },
})
