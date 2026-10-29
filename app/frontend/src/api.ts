import axios from 'axios'

let accessToken: string | null = null

export function configurarToken(token: string | null) {
  accessToken = token
}

/**
 * Instancia unica de Axios para hablar con el backend.
 * La URL base sale de VITE_API_URL (ver .env.example).
 * withCredentials envia las cookies de sesion HttpOnly automaticamente.
 */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
  withCredentials: true,
})

api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})
