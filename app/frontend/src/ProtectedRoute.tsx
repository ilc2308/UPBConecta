import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

/** Deja pasar solo a usuarios autenticados; si no, redirige a /auth. */
export function ProtectedRoute() {
  const { estaAutenticado } = useAuth()
  return estaAutenticado ? <Outlet /> : <Navigate to="/auth" replace />
}