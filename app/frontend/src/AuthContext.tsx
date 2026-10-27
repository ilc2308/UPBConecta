import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from './authApi'

export interface Usuario {
  nombre: string
  correo: string
}

interface AuthContextValue {
  usuario: Usuario | null
  estaAutenticado: boolean
  cargando: boolean
  iniciarSesionDemo: () => Promise<void>
  cerrarSesion: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

/**
 * Estado de autenticacion de la app.
 * - La sesion la crea el backend y viaja en una cookie HttpOnly (UPBC_SESSION):
 *   el frontend nunca ve ni guarda el token (ni localStorage ni sessionStorage).
 * - Al cargar, se consulta GET /auth/me para saber si hay sesion activa.
 * - iniciarSesionDemo: modo temporal protegido por VITE_DEMO_MODE (frontend) y DEMO_ENABLED (backend).
 * - El inicio de sesion real con Microsoft (SSO) reemplaza el modo demo en UPBCONECT-5/6.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    authApi
      .me()
      .then(setUsuario)
      .catch(() => setUsuario(null))
      .finally(() => setCargando(false))
  }, [])

  const iniciarSesionDemo = useCallback(async () => {
    setUsuario(await authApi.loginDemo())
  }, [])

  const cerrarSesion = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      setUsuario(null)
    }
  }, [])

  const value = useMemo(
    () => ({ usuario, estaAutenticado: usuario !== null, cargando, iniciarSesionDemo, cerrarSesion }),
    [usuario, cargando, iniciarSesionDemo, cerrarSesion],
  )

  // Mientras se consulta la sesion no se muestran las rutas, para que una recarga
  // en /dashboard no redirija al login por un instante.
  return (
    <AuthContext.Provider value={value}>
      {cargando ? <p className="cargando-sesion">Cargando...</p> : children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return ctx
}
