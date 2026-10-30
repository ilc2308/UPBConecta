import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthPage } from './AuthPage'
import { DashboardPage } from './DashboardPage'
import { PostJobPage } from './features/ofertas/PostJobPage'
import { MisPublicacionesPage } from './features/ofertas/MisPublicacionesPage'
import { NotFoundPage } from './NotFoundPage'
import { ProtectedRoute } from './ProtectedRoute'

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/auth" element={<AuthPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/publicar" element={<PostJobPage />} />
          <Route path="/mis-publicaciones" element={<MisPublicacionesPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  )
}