// ============================================================
// auth.api.js
// ============================================================
import client from './client'

export const authApi = {
  login:    (data) => client.post('/v1/auth/login', data),
  register: (data) => client.post('/v1/auth/register', data),
}