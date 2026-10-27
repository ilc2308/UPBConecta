import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <main className="contenedor">
      <section className="tarjeta">
        <h1>Página no encontrada</h1>
        <Link to="/dashboard">Volver al inicio</Link>
      </section>
    </main>
  );
}
