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
          <p>Aqui aparecera el listado de trabajos activos (UPBCONECT-37).</p>
          <ApiStatus />
        </section>
      </main>
    </>
  )
}