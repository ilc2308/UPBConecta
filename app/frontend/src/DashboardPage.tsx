import { Link } from 'react-router-dom'
import { Header } from './Header'
import { useAuth } from './AuthContext'
import { JobList } from './features/ofertas/JobList'
import styles from './DashboardPage.module.css'

/**
 * Dashboard principal. Muestra el listado de trabajos activos (UPBCONECT-37).
 * Menú lateral según el prototipo: Dashboard, Mis publicaciones (Your posts),
 * Tareas pendientes (Pending tasks), Mis chats (Your chats), Logros (Achievements).
 */
export function DashboardPage() {
  const { usuario } = useAuth()

  return (
    <>
      <Header />
      <div className={styles.layout}>
        {/* Menú lateral según el prototipo */}
        <nav className={styles.lateral} aria-label="Menú principal">
          <ul className={styles.menu}>
            <li>
              <Link to="/dashboard" className={`${styles.enlace} ${styles.enlaceActivo}`}>
                🏠 Dashboard
              </Link>
            </li>
            <li>
              <Link to="/publicar" className={styles.enlace}>
                ➕ Publicar trabajo
              </Link>
            </li>
            <li>
              <Link to="/mis-publicaciones" className={styles.enlace}>
                📋 Mis publicaciones
              </Link>
            </li>
            <li>
              {/* TODO(UPBCONECT-41/42): tareas pendientes */}
              <span className={`${styles.enlace} ${styles.enlaceDeshabilitado}`}>
                ⏳ Tareas pendientes
              </span>
            </li>
            <li>
              {/* TODO(UPBCONECT-43): chats */}
              <span className={`${styles.enlace} ${styles.enlaceDeshabilitado}`}>
                💬 Mis chats
              </span>
            </li>
            <li>
              {/* TODO(UPBCONECT-44): logros */}
              <span className={`${styles.enlace} ${styles.enlaceDeshabilitado}`}>
                🏆 Logros
              </span>
            </li>
          </ul>
        </nav>

        {/* Contenido principal */}
        <main className={styles.contenido}>
          <div className={styles.cabecera}>
            <div>
              <h1 className={styles.titulo}>Trabajos disponibles</h1>
              <p className={styles.saludo}>Hola, {usuario?.nombre} — encuentra una oportunidad en el campus.</p>
            </div>
            <Link to="/publicar" className="boton">Publicar trabajo</Link>
          </div>

          <JobList />
        </main>
      </div>
    </>
  )
}
