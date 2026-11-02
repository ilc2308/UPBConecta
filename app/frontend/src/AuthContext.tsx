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
  completarAutenticacion: (response: { accessToken: string; usuario: Usuario }) => void
  cerrarSesion: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

/**
 * Estado de autenticacion de la app.
 * - SSO conserva el JWT en memoria y sessionStorage durante la pestaña.
 * - La sesión demo heredada sigue viajando en la cookie HttpOnly UPBC_SESSION.
 * - Al cargar, se consulta GET /auth/me para validar la sesión existente.
 * - iniciarSesionDemo: modo temporal protegido por VITE_DEMO_MODE (frontend) y DEMO_ENABLED (backend).
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [accessToken, setAccessToken] = useState<string | null>(() => sessionStorage.getItem('upbconecta_access_token'))
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    configurarToken(accessToken)
    authApi
      .me()
      .then(setUsuario)
      .catch(() => {
        sessionStorage.removeItem('upbconecta_access_token')
        configurarToken(null)
        setAccessToken(null)
        setUsuario(null)
      })
      .finally(() => setCargando(false))
  }, [accessToken])

  const iniciarSesionDemo = useCallback(async () => {
    sessionStorage.removeItem('upbconecta_access_token')
    configurarToken(null)
    setAccessToken(null)
    setUsuario(await authApi.loginDemo())
  }, [])

  const completarAutenticacion = useCallback((response: { accessToken: string; usuario: Usuario }) => {
    sessionStorage.setItem('upbconecta_access_token', response.accessToken)
    configurarToken(response.accessToken)
    setAccessToken(response.accessToken)
    setUsuario(response.usuario)
  }, [])

  const cerrarSesion = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      sessionStorage.removeItem('upbconecta_access_token')
      configurarToken(null)
      setAccessToken(null)
      setUsuario(null)
    }
  }, [])

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
