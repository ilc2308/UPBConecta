// Llamadas de autenticacion. La sesion viaja en una cookie HttpOnly,
// por eso todas usan credentials: 'include' y el token nunca pasa por el navegador.
const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export type Usuario = { correo: string; nombre: string };

async function llamar<T>(ruta: string, metodo: 'GET' | 'POST'): Promise<T> {
  const res = await fetch(`${API_URL}/api/v1/auth${ruta}`, {
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
