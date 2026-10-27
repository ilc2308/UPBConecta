// Llamadas de autenticacion. La sesion viaja en una cookie HttpOnly,
// por eso todas usan credentials: 'include' y el token nunca pasa por el navegador.
const BASE = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1').replace(/\/+$/, '');
const API_BASE = BASE.endsWith('/api/v1') ? BASE : `${BASE}/api/v1`;

export type Usuario = { correo: string; nombre: string };

async function llamar<T>(ruta: string, metodo: 'GET' | 'POST'): Promise<T> {
  const res = await fetch(`${API_BASE}/auth${ruta}`, {
    method: metodo,
    credentials: 'include',
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.status === 204 ? (undefined as T) : res.json();
}

export const authApi = {
  me: () => llamar<Usuario>('/me', 'GET'),
  loginDemo: () => llamar<Usuario>('/demo', 'POST'),
  logout: () => llamar<void>('/logout', 'POST'),
};
