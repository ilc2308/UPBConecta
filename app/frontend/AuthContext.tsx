import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { TOKEN_KEY } from '../services/api';

export interface Usuario {
  nombre: string;
  correo: string;
}

interface AuthContextValue {
  usuario: Usuario | null;
  estaAutenticado: boolean;
  iniciarSesion: (usuario: Usuario, token: string) => void;
  cerrarSesion: () => void;
}

const USER_KEY = 'upbconecta_usuario';

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function leerUsuarioGuardado(): Usuario | null {
  try {
    const guardado = localStorage.getItem(USER_KEY);
    return guardado ? (JSON.parse(guardado) as Usuario) : null;
  } catch {
    return null;
  }
}

/**
 * Estado de autenticación de la app.
 * El inicio de sesión real con Microsoft (SSO) se conecta aquí en
 * UPBCONECT-5 y UPBCONECT-6.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(leerUsuarioGuardado);

  const iniciarSesion = useCallback((nuevoUsuario: Usuario, token: string) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(nuevoUsuario));
    setUsuario(nuevoUsuario);
  }, []);

  const cerrarSesion = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUsuario(null);
  }, []);

  const value = useMemo(
    () => ({ usuario, estaAutenticado: usuario !== null, iniciarSesion, cerrarSesion }),
    [usuario, iniciarSesion, cerrarSesion],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  }
  return ctx;
}
