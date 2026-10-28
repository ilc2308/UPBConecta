import axios from 'axios'

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

/** @deprecated Usar cookies HttpOnly. Conservado solo para compatibilidad temporal. */
export const TOKEN_KEY = 'upbconecta_token'