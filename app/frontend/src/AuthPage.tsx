import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from './AuthContext'
import { ApiStatus } from './ApiStatus'
import './AuthPage.css'

/** Solo true si VITE_DEMO_MODE=true en .env.development (nunca en produccion). */
const DEMO_HABILITADO = import.meta.env.VITE_DEMO_MODE === 'true'

export function AuthPage() {
  const { estaAutenticado, iniciarSesionDemo } = useAuth()
  const navigate = useNavigate()

  if (estaAutenticado) {
    return <Navigate to="/dashboard" replace />
  }

  const entrarModoDemo = () => {
    iniciarSesionDemo({ nombre: 'Usuario demo', correo: 'demo@upb.edu.co' })
    navigate('/dashboard')
  }

  return (
    <main className="auth">
      <section className="tarjeta auth__tarjeta">
        <h1>
          UPB <span className="auth__acento">Conecta</span>
        </h1>
        <p>Conectamos a estudiantes de la UPB con oportunidades de trabajo.</p>

        {/* Boton real de SSO - se habilita en UPBCONECT-6 */}
        <button className="boton" disabled>
          Ingresar con cuenta institucional (proximamente)
        </button>

        {/* Modo demo: solo visible cuando VITE_DEMO_MODE=true */}
        {DEMO_HABILITADO && (
          <button className="boton btn-demo" onClick={entrarModoDemo}>
            Entrar en modo demo
          </button>
        )}

        <ApiStatus />
      </section>
    </main>
  )
}