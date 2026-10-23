import axios from 'axios';

/**
 * Instancia única de Axios para hablar con el backend.
 * La URL base sale de VITE_API_URL (ver .env.example).
 */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

export const TOKEN_KEY = 'upbconecta_token';

// Adjunta el token (cuando exista el login con SSO) a cada petición.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
