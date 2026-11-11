import { useEffect, useState } from 'react'
import axios from 'axios'
import { Link } from 'react-router-dom'
import { Header } from '../../Header'
import { useAuth } from '../../AuthContext'
import { ofertasApi } from './ofertasApi'
import type { Oferta } from './ofertasApi'
import { NOMBRE_ESTADO, formatearFecha, formatearPesos } from './formato'
import comun from './ofertas.module.css'
import styles from './MisPublicacionesPage.module.css'

type Tab = 'pendientes' | 'completadas'

/**
 * "Tareas pendientes" (Pending tasks): trabajos asignados al estudiante de la sesion (UPBCONECT-43 y 44).
 * Permite ver las tareas en progreso y los trabajos finalizados.
 */
export function TareasPendientesPage() {
  const { cerrarSesion } = useAuth()
  const [tareas, setTareas] = useState<Oferta[] | null>(null)
  const [error, setError] = useState(false)
  const [tab, setTab] = useState<Tab>('pendientes')

  useEffect(() => {
    ofertasApi
      .misAsignadas()
      .then(setTareas)
      .catch(async (err) => {
        if (axios.isAxiosError(err) && err.response?.status === 401) await cerrarSesion()
        else setError(true)
      })
  }, [cerrarSesion])

  const pendientes = tareas?.filter((t) => t.estado !== 'FINALIZADA') ?? []
  const completadas = tareas?.filter((t) => t.estado === 'FINALIZADA') ?? []
  const listaActual = tab === 'pendientes' ? pendientes : completadas

  return (
    <>
      <Header />
      <main className="contenedor">
        <p className={comun.migas}><Link to="/dashboard">Dashboard</Link> / Tareas pendientes</p>
        <h1 className={comun.titulo}>Tareas pendientes</h1>

        {error && (
          <p className={`${comun.nota} ${comun.notaError}`} role="alert">
            No se pudieron cargar tus tareas. Recarga la página para intentarlo de nuevo.
          </p>
        )}

        {!error && (
          <div className={styles.pestanas} role="tablist">
            <button
              type="button"
              role="tab"
              aria-selected={tab === 'pendientes'}
              className={`${styles.pestana} ${tab === 'pendientes' ? styles.pestanaActiva : ''}`}
              onClick={() => setTab('pendientes')}
            >
              Pendientes ({pendientes.length})
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={tab === 'completadas'}
              className={`${styles.pestana} ${tab === 'completadas' ? styles.pestanaActiva : ''}`}
              onClick={() => setTab('completadas')}
            >
              Completadas ({completadas.length})
            </button>
          </div>
        )}

        {!error && tareas === null && <p className={styles.vacio}>Cargando tareas...</p>}

        {!error && tareas !== null && listaActual.length === 0 && (
          <div className={`tarjeta ${styles.vacio}`}>
            {tab === 'pendientes' ? (
              <p>No tienes tareas pendientes por realizar.</p>
            ) : (
              <p>Aún no tienes trabajos completados.</p>
            )}
          </div>
        )}

        {listaActual.length > 0 && (
          <ul className={styles.lista}>
            {listaActual.map((t) => (
              <li key={t.id} className={`tarjeta ${styles.oferta}`}>
                <div className={styles.encabezado}>
                  <h2 className={styles.nombre}><Link to={`/ofertas/${t.id}`}>{t.titulo}</Link></h2>
                  <div className={styles.etiquetas}>
                    {t.urgente && <span className={`${styles.etiqueta} ${styles.urgente}`}>Urgente</span>}
                    <span className={`${styles.etiqueta} ${styles[t.estado.toLowerCase()] ?? ''}`}>
                      {NOMBRE_ESTADO[t.estado]}
                    </span>
                  </div>
                </div>
                <p className={styles.actividad}>{t.actividad}</p>
                <dl className={styles.datos}>
                  <div><dt>Empleador</dt><dd>{t.publicadorCorreo}</dd></div>
                  <div><dt>Valor</dt><dd>{formatearPesos(t.valorOfrecido)}</dd></div>
                  {t.fechaCierre ? (
                    <div><dt>Finalizado</dt><dd>{formatearFecha(t.fechaCierre)}</dd></div>
                  ) : (
                    <div><dt>Tiempo límite</dt><dd>{formatearFecha(t.fechaLimite)}</dd></div>
                  )}
                  <div><dt>Ubicación</dt><dd>{t.ubicacion}</dd></div>
                </dl>
              </li>
            ))}
          </ul>
        )}
      </main>
    </>
  )
}
