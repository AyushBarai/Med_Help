import axios from 'axios'
import { useAuthStore } from './auth.store'

/**
 * Single Axios instance used by every API function.
 *
 * REQUEST interceptor: automatically adds "Authorization: Bearer <token>"
 * to every outgoing request — you never do this manually.
 *
 * RESPONSE interceptor:
 *  - Unwraps .data so callers get the payload directly (not axios wrapper)
 *  - On 401 → clears auth state and redirects to login
 *  - On error → throws the server's error message string
 */
const client = axios.create({
  baseURL: '/api',          // proxied to http://localhost:8080/api by vite.config.js
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' }
})

// Attach token before every request
client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Unwrap response or throw a clean error message
client.interceptors.response.use(
  (res) => res.data,
  (err) => {
    if (err.response?.status === 401) {
      useAuthStore.getState().clearAuth()
      window.location.href = '/login'
    }
    const msg = err.response?.data?.message || err.message || 'Something went wrong'
    return Promise.reject(new Error(msg))
  }
)

export default client