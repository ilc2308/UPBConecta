import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from './authApi'
import { configurarToken } from './api'

export interface Usuario {
  id?: string
  nombre: string
  correo: string
  rol?: 'ESTUDIANTE' | 'EMPLEADOR'
  carrera?: string
}

interface AuthContextValue {
  usuario: Usuario | null
  estaAutenticado: boolean
  cargando: boolean
  accessToken: string | null
  iniciarSesionDemo: () => Promise<void>
  completarAutenticacion: (response: { accessToken: string; usuario: Usuario }, recordar?: boolean) => void
  cerrarSesion: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

/**
 * Estado de autenticacion de la app.
 * - SSO conserva el JWT durante la pestaña o en localStorage si se elige recordarlo.
 * - La sesión demo heredada sigue viajando en la cookie HttpOnly UPBC_SESSION.
 * - Al cargar, se consulta GET /auth/me para validar la sesión existente.
 * - iniciarSesionDemo: modo temporal protegido por VITE_DEMO_MODE (frontend) y DEMO_ENABLED (backend).
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [accessToken, setAccessToken] = useState<string | null>(
    () => localStorage.getItem('upbconecta_access_token') ?? sessionStorage.getItem('upbconecta_access_token'),
  )
  const [cargando, setCargando] = useState(true)

  const limpiarTokens = useCallback(() => {
    localStorage.removeItem('upbconecta_access_token')
    sessionStorage.removeItem('upbconecta_access_token')
  }, [])

  useEffect(() => {
    configurarToken(accessToken)
    authApi
      .me()
      .then(setUsuario)
      .catch(() => {
        limpiarTokens()
        configurarToken(null)
        setAccessToken(null)
        setUsuario(null)
      })
      .finally(() => setCargando(false))
  }, [accessToken, limpiarTokens])

  const iniciarSesionDemo = useCallback(async () => {
    limpiarTokens()
    configurarToken(null)
    setAccessToken(null)
    setUsuario(await authApi.loginDemo())
  }, [limpiarTokens])

  const completarAutenticacion = useCallback((
    response: { accessToken: string; usuario: Usuario },
    recordar = false,
  ) => {
    limpiarTokens()
    const storage = recordar ? localStorage : sessionStorage
    storage.setItem('upbconecta_access_token', response.accessToken)
    configurarToken(response.accessToken)
    setAccessToken(response.accessToken)
    setUsuario(response.usuario)
  }, [limpiarTokens])

  const cerrarSesion = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      limpiarTokens()
      configurarToken(null)
      setAccessToken(null)
      setUsuario(null)
    }
  }, [limpiarTokens])

  const value = useMemo(
    () => ({ usuario, estaAutenticado: usuario !== null, cargando, accessToken, iniciarSesionDemo, completarAutenticacion, cerrarSesion }),
    [usuario, cargando, accessToken, iniciarSesionDemo, completarAutenticacion, cerrarSesion],
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
