import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ApiStatus } from '../components/ApiStatus';
import './AuthPage.css';

export function AuthPage() {
  const { estaAutenticado, iniciarSesion } = useAuth();
  const navigate = useNavigate();

  if (estaAutenticado) {
    return <Navigate to="/dashboard" replace />;
  }

  // Provisional: se reemplaza por el inicio de sesión con Microsoft (UPBCONECT-6).
  const entrarModoDemo = () => {
    iniciarSesion({ nombre: 'Usuario demo', correo: 'demo@upb.edu.co' }, 'token-demo');
    navigate('/dashboard');
  };

  return (
    <main className="auth">
      <section className="tarjeta auth__tarjeta">
        <h1>
          UPB <span className="auth__acento">Conecta</span>
        </h1>
        <p>Conectamos a estudiantes de la UPB con oportunidades de trabajo.</p>
        <button className="boton" onClick={entrarModoDemo}>
          Ingresar con cuenta institucional
        </button>
        <ApiStatus />
      </section>
    </main>
  );
}
