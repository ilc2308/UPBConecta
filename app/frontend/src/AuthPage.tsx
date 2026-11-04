import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { PublicClientApplication } from '@azure/msal-browser'
import { useAuth } from './AuthContext'
import { authApi, type RolUsuario } from './authApi'
import { ApiStatus } from './ApiStatus'
import './AuthPage.css'

/** Solo true si VITE_DEMO_MODE=true en .env.development (nunca en produccion). */
const DEMO_HABILITADO = import.meta.env.VITE_DEMO_MODE === 'true'
const TENANT_ID = import.meta.env.VITE_AZURE_TENANT_ID
const CLIENT_ID = import.meta.env.VITE_AZURE_CLIENT_ID

function MicrosoftMark() {
  return (
    <span className="auth__microsoft-mark" aria-hidden="true">
      <i />
      <i />
      <i />
      <i />
    </span>
  )
}

export function AuthPage() {
  const { estaAutenticado, iniciarSesionDemo, completarAutenticacion } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [idToken, setIdToken] = useState<string | null>(null)
  const [rol, setRol] = useState<RolUsuario | ''>('')
  const [carrera, setCarrera] = useState('')
  const [cargando, setCargando] = useState(false)
  const [recordarme, setRecordarme] = useState(false)

  if (estaAutenticado) {
    return <Navigate to="/dashboard" replace />
  }

  // La sesion la crea el backend (POST /auth/demo) y llega en una cookie HttpOnly.
  const entrarModoDemo = async () => {
    setError(null)
    try {
      await iniciarSesionDemo()
      navigate('/dashboard')
    } catch {
      setError('No se pudo iniciar el modo demo. Verifica que el backend tenga DEMO_ENABLED=true.')
    }
  }

  const continuarConMicrosoft = async () => {
    if (!TENANT_ID || !CLIENT_ID) {
      setError('El inicio de sesión institucional no está configurado. Contacta al administrador.')
      return
    }
    setError(null)
    setCargando(true)
    try {
      const msal = new PublicClientApplication({
        auth: {
          clientId: CLIENT_ID,
          authority: `https://login.microsoftonline.com/${TENANT_ID}`,
          redirectUri: `${window.location.origin}/auth`,
        },
      })
      await msal.initialize()
      const result = await msal.loginPopup({ scopes: ['openid', 'profile', 'email'] })
      if (!result.idToken) throw new Error('Microsoft no devolvió un id_token.')
      try {
        completarAutenticacion(await authApi.loginMicrosoft(result.idToken), recordarme)
        navigate('/dashboard')
      } catch (loginError) {
        const code = isAxiosError(loginError) ? loginError.response?.data?.code : undefined
        if (isAxiosError(loginError) && (loginError.response?.status === 404 || code === 'USER_NOT_REGISTERED')) {
          setIdToken(result.idToken)
        } else if (isAxiosError(loginError) && loginError.response?.status === 403) {
          setError('Solo se permiten cuentas con dominio institucional @upb.edu.co.')
        } else if (isAxiosError(loginError) && typeof loginError.response?.data?.detail === 'string') {
          setError(loginError.response.data.detail)
        } else {
          throw loginError
        }
      }
    } catch (loginError) {
      setError(loginError instanceof Error ? loginError.message : 'No se pudo completar el inicio de sesión con Microsoft.')
    } finally {
      setCargando(false)
    }
  }

  const registrar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!idToken || !rol || !carrera.trim()) return
    setError(null)
    setCargando(true)
    try {
      completarAutenticacion(await authApi.registrarMicrosoft(idToken, rol, carrera.trim()), recordarme)
      navigate('/dashboard')
    } catch (registerError) {
      const status = isAxiosError(registerError) ? registerError.response?.status : undefined
      if (status === 409) {
        setIdToken(null)
        setError('Esta cuenta ya está registrada. Pulsa continuar con Microsoft para iniciar sesión.')
      } else {
        setError(status === 403
          ? 'Solo se permiten cuentas con dominio institucional @upb.edu.co.'
          : 'No se pudo completar el registro. Inténtalo nuevamente.')
      }
    } finally {
      setCargando(false)
    }
  }

  return (
    <main className="auth">
      <div className="auth__browser" aria-hidden="true">
        <span className="auth__browser-dots"><i /><i /><i /></span>
        <span className="auth__browser-address">upbconecta.edu.co</span>
      </div>
      <section className="auth__tarjeta" aria-labelledby="auth-title">
        <div className="auth__logo" aria-hidden="true">U</div>
        <p className="auth__eyebrow">PLATAFORMA UNIVERSITARIA</p>
        <h1 id="auth-title">{idToken ? 'Completa tu registro' : <>Bienvenido a<br />UPB Conecta</>}</h1>
        <p className="auth__intro">
          Conecta, colabora y encuentra oportunidades en la comunidad UPB.
        </p>

        {!idToken ? (
          <>
            <div className="auth__divider"><span>ACCESO INSTITUCIONAL</span></div>
            <p className="auth__copy">
              Ingresa con tu cuenta institucional. Serás redirigido al inicio de sesión seguro de Microsoft UPB.
            </p>
            <button
              className="boton auth__microsoft"
              onClick={continuarConMicrosoft}
              disabled={cargando || !TENANT_ID || !CLIENT_ID}
            >
              {cargando
                ? <><span className="auth__spinner" aria-hidden="true" />Conectando con Microsoft...</>
                : <><MicrosoftMark /><span>Continuar con Microsoft UPB</span><b aria-hidden="true">→</b></>}
            </button>
            <label className="auth__remember">
              <input
                type="checkbox"
                checked={recordarme}
                onChange={(event) => setRecordarme(event.target.checked)}
              />
              Recordarme en este dispositivo
            </label>
            <small className="auth__help">Usa tu correo institucional terminado en @upb.edu.co</small>
          </>
        ) : (
          <form className="auth__formulario" onSubmit={registrar}>
            <p>Regístrate para empezar a colaborar con la comunidad UPB.</p>
            <label>
              Rol
              <select
                value={rol}
                onChange={(event) => {
                  const selected = event.target.value
                  setRol(selected === 'ESTUDIANTE' || selected === 'EMPLEADOR' ? selected : '')
                }}
                required
              >
                <option value="">Selecciona un rol</option>
                <option value="ESTUDIANTE">Estudiante</option>
                <option value="EMPLEADOR">Empleador</option>
              </select>
            </label>
            <label>
              Carrera
              <input value={carrera} onChange={(event) => setCarrera(event.target.value)} maxLength={120} required />
            </label>
            <button className="boton auth__microsoft" type="submit" disabled={cargando || !rol || !carrera.trim()}>
              {cargando ? <><span className="auth__spinner" aria-hidden="true" />Creando cuenta...</> : 'Confirmar registro'}
            </button>
          </form>
        )}

        {error && <p className="auth__error" role="alert">{error}</p>}
        {(!TENANT_ID || !CLIENT_ID) && (
          <p className="auth__configuracion">El acceso Microsoft requiere configurar el tenant y el cliente de Entra ID.</p>
        )}
        {DEMO_HABILITADO && (
          <button className="boton auth__demo" onClick={entrarModoDemo} disabled={cargando}>
            Entrar en modo demo
          </button>
        )}
        <div className="auth__api-status"><ApiStatus /></div>
      </section>
      <p className="auth__footer">UPB CONECTA · PLATAFORMA DE COLABORACIÓN</p>
    </main>
  )
}
