import { Link } from 'react-router-dom'
import { Header } from './Header'
import { ApiStatus } from './ApiStatus'
import { useAuth } from './AuthContext'

export function DashboardPage() {
  const { usuario } = useAuth()

  return (
    <>
      <Header />
      <main className="contenedor">
        <section className="tarjeta">
          <h1>Hola, {usuario?.nombre}</h1>
          {/* Cualquier usuario puede publicar (UPBCONECT-39) */}
          <Link to="/publicar" className="boton">Publicar trabajo</Link>
          <p>Aqui aparecera el listado de trabajos activos (UPBCONECT-37).</p>
          <ApiStatus />
        </section>
      </main>
    </>
  )
}