import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from './AuthContext'
import './Header.css'

const ETIQUETA = { ESTUDIANTE: 'Estudiante', EMPLEADOR: 'Empleador' } as const

export function Header() {
  const { usuario, cerrarSesion, cambiarRol } = useAuth()
  const navigate = useNavigate()
  const [cambiando, setCambiando] = useState(false)
  const [errorRol, setErrorRol] = useState(false)

  // La sesion demo no tiene rol, por eso el selector solo aparece con cuenta institucional.
  const rolActual = usuario?.rol
  const rolDestino = rolActual === 'EMPLEADOR' ? 'ESTUDIANTE' : 'EMPLEADOR'

  async function alternarRol() {
    setCambiando(true)
    setErrorRol(false)
    try {
      await cambiarRol(rolDestino)
      navigate('/dashboard')
    } catch {
      setErrorRol(true)
    } finally {
      setCambiando(false)
    }
  }

  return (
    <header className="header">
      <div className="header__contenido">
        <span className="header__marca">
          UPB <strong>Conecta</strong>
        </span>
        {usuario && (
          <div className="header__usuario">
            <span>{usuario.nombre}</span>
            {rolActual && (
              <button className="boton boton-secundario" onClick={alternarRol} disabled={cambiando}>
                {ETIQUETA[rolActual]} · Cambiar a {ETIQUETA[rolDestino]}
              </button>
            )}
            {errorRol && <span role="alert">No se pudo cambiar el rol</span>}
            <button className="boton boton-secundario" onClick={cerrarSesion}>
              Cerrar sesion
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
