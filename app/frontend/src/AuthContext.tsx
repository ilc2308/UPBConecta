import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import type { ReactNode } from 'react'

export interface Usuario {
  nombre: string
  correo: string
}

interface AuthContextValue {
  usuario: Usuario | null
  estaAutenticado: boolean
  iniciarSesionDemo: (usuario: Usuario) => void
  cerrarSesion: () => void
}

const USER_KEY = 'upbconecta_usuario'

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function leerUsuarioGuardado(): Usuario | null {
  try {
    const guardado = sessionStorage.getItem(USER_KEY)
    return guardado ? (JSON.parse(guardado) as Usuario) : null
  } catch {
    return null
  }
}

/**
 * Estado de autenticacion de la app.
 * - iniciarSesionDemo: modo temporal protegido por VITE_DEMO_MODE=true.
 * - El inicio de sesion real con Microsoft (SSO) se conecta aqui en UPBCONECT-5/6.
 * - El token real viajara en cookies HttpOnly (no localStorage).
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(leerUsuarioGuardado)

  const iniciarSesionDemo = useCallback((nuevoUsuario: Usuario) => {
    sessionStorage.setItem(USER_KEY, JSON.stringify(nuevoUsuario))
    setUsuario(nuevoUsuario)
  }, [])

  const cerrarSesion = useCallback(() => {
    sessionStorage.removeItem(USER_KEY)
    setUsuario(null)
  }, [])

  const value = useMemo(
    () => ({ usuario, estaAutenticado: usuario !== null, iniciarSesionDemo, cerrarSesion }),
    [usuario, iniciarSesionDemo, cerrarSesion],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return ctx
}