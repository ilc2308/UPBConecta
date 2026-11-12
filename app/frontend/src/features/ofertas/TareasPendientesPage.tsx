import { useEffect, useState } from 'react'
import axios from 'axios'
import { Link } from 'react-router-dom'
import { Header } from '../../Header'
import { useAuth } from '../../AuthContext'
import { mensajeDelServidor, ofertasApi } from './ofertasApi'
import type { Oferta } from './ofertasApi'
import { NOMBRE_ESTADO, formatearFecha, formatearPesos } from './formato'
import { ConfirmarFinalizarModal } from './ConfirmarFinalizarModal'
import comun from './ofertas.module.css'
import styles from './MisPublicacionesPage.module.css'

type Tab = 'pendientes' | 'completadas'

/**
 * "Tareas pendientes" (Pending tasks): trabajos asignados al estudiante de la sesion (UPBCONECT-43 y 44).
 * Permite iniciar las tareas asignadas, finalizar las que estan en proceso y ver los trabajos finalizados.
 */
export function TareasPendientesPage() {
  const { cerrarSesion } = useAuth()
  const [tareas, setTareas] = useState<Oferta[] | null>(null)
  const [error, setError] = useState(false)
  const [tab, setTab] = useState<Tab>('pendientes')
  const [iniciando, setIniciando] = useState<number | null>(null)
  const [errorAccion, setErrorAccion] = useState<string | null>(null)
  const [tareaAFinalizar, setTareaAFinalizar] = useState<Oferta | null>(null)
  const [finalizando, setFinalizando] = useState(false)
  const [notaFinalizada, setNotaFinalizada] = useState<string | null>(null)

  useEffect(() => {
    ofertasApi
      .misAsignadas()
      .then(setTareas)
      .catch(async (err) => {
        if (axios.isAxiosError(err) && err.response?.status === 401) await cerrarSesion()
        else setError(true)
      })
  }, [cerrarSesion])

  /** Pasa una tarea de ASIGNADA a EN_PROCESO (UPBCONECT-44). */
  const iniciarTrabajo = async (tarea: Oferta) => {
    setErrorAccion(null)
    setIniciando(tarea.id)
    try {
      const actualizada = await ofertasApi.actualizarEstado(tarea.id, 'EN_PROCESO')
      setTareas((prev) => (prev ? prev.map((t) => (t.id === actualizada.id ? actualizada : t)) : prev))
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        await cerrarSesion()
      } else {
        setErrorAccion(mensajeDelServidor(err, 'No se pudo iniciar el trabajo. Inténtalo de nuevo.'))
      }
    } finally {
      setIniciando(null)
    }
  }

  /** El estudiante que hizo el trabajo lo marca como FINALIZADA (UPBCONECT-44). */
  const finalizarTrabajo = async () => {
    if (!tareaAFinalizar) return
    setErrorAccion(null)
    setFinalizando(true)
    try {
      const actualizada = await ofertasApi.actualizarEstado(tareaAFinalizar.id, 'FINALIZADA')
      setTareas((prev) => (prev ? prev.map((t) => (t.id === actualizada.id ? actualizada : t)) : prev))
      setNotaFinalizada(`El trabajo «${tareaAFinalizar.titulo}» fue finalizado correctamente`)
      setTareaAFinalizar(null)
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        await cerrarSesion()
      } else {
        setErrorAccion(mensajeDelServidor(err, 'No se pudo finalizar el trabajo. Inténtalo de nuevo.'))
      }
    } finally {
      setFinalizando(false)
    }
  }

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

        {errorAccion && (
          <p className={`${comun.nota} ${comun.notaError}`} role="alert">
            {errorAccion}
          </p>
        )}

        {notaFinalizada && (
          <p className={`${comun.nota} ${styles.exito}`} role="status">
            <span className={comun.check} aria-hidden="true">✓</span>
            <span><strong>¡Trabajo finalizado!</strong> {notaFinalizada}</span>
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
                {t.estado === 'ASIGNADA' && (
                  <button
                    type="button"
                    className={styles.botonIniciar}
                    onClick={() => iniciarTrabajo(t)}
                    disabled={iniciando === t.id}
                  >
                    {iniciando === t.id ? 'Iniciando...' : 'Iniciar trabajo'}
                  </button>
                )}
                {t.estado === 'EN_PROCESO' && (
                  <button
                    type="button"
                    className={styles.botonFinalizar}
                    onClick={() => setTareaAFinalizar(t)}
                  >
                    Finalizar trabajo
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </main>

      {tareaAFinalizar && (
        <ConfirmarFinalizarModal
          titulo={tareaAFinalizar.titulo}
          enviando={finalizando}
          onCancelar={() => {
            if (!finalizando) setTareaAFinalizar(null)
          }}
          onConfirmar={finalizarTrabajo}
        />
      )}
    </>
  )
}
