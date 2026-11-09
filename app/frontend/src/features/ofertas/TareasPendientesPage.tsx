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

/** "Tareas pendientes" (Pending tasks): trabajos asignados al estudiante de la sesion (UPBCONECT-43). */
export function TareasPendientesPage() {
  const { cerrarSesion } = useAuth()
  const [tareas, setTareas] = useState<Oferta[] | null>(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    ofertasApi
      .asignadas()
      .then(setTareas)
      .catch(async (err) => {
        if (axios.isAxiosError(err) && err.response?.status === 401) await cerrarSesion()
        else setError(true)
      })
  }, [cerrarSesion])

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
        {!error && tareas === null && <p className={styles.vacio}>Cargando tareas...</p>}
        {tareas?.length === 0 && (
          <div className={`tarjeta ${styles.vacio}`}>
            <p>Aún no tienes trabajos asignados.</p>
          </div>
        )}

        {tareas && tareas.length > 0 && (
          <ul className={styles.lista}>
            {tareas.map((t) => (
              <li key={t.id} className={`tarjeta ${styles.oferta}`}>
                <div className={styles.encabezado}>
                  <h2 className={styles.nombre}><Link to={`/ofertas/${t.id}`}>{t.titulo}</Link></h2>
                  <span className={styles.etiqueta}>{NOMBRE_ESTADO[t.estado]}</span>
                </div>
                <p className={styles.actividad}>{t.actividad}</p>
                <dl className={styles.datos}>
                  <div><dt>Valor</dt><dd>{formatearPesos(t.valorOfrecido)}</dd></div>
                  <div><dt>Tiempo límite</dt><dd>{formatearFecha(t.fechaLimite)}</dd></div>
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
