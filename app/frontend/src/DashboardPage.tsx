import { Header } from '../components/Header';
import { ApiStatus } from '../components/ApiStatus';
import { useAuth } from '../context/AuthContext';

export function DashboardPage() {
  const { usuario } = useAuth();

  return (
    <>
      <Header />
      <main className="contenedor">
        <section className="tarjeta">
          <h1>Hola, {usuario?.nombre}</h1>
          <p>Aquí aparecerá el listado de trabajos activos (UPBCONECT-37).</p>
          <ApiStatus />
        </section>
      </main>
    </>
  );
}
