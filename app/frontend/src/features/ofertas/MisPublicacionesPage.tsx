import { useEffect, useState } from 'react'
import axios from 'axios'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { Header } from '../../Header'
import { useAuth } from '../../AuthContext'
import { ofertasApi } from './ofertasApi'
import type { Oferta } from './ofertasApi'
import type { EstadoPublicada } from './PostJobPage'
import { NOMBRE_ESTADO, formatearFecha, formatearPesos } from './formato'
import { ConfirmarFinalizarModal } from './ConfirmarFinalizarModal'
import comun from './ofertas.module.css'
import styles from './MisPublicacionesPage.module.css'

/**
 * "Mis publicaciones" (Your Posts): ofertas del usuario de la sesion (UPBCONECT-40 y 44).
 * Al llegar desde "Publicar trabajo" muestra "¡Publicado!" y resalta la oferta nueva.
 * Permite al empleador finalizar colaboraciones en estado ASIGNADA (UPBCONECT-44).
 */
export function MisPublicacionesPage() {
  const { cerrarSesion } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [publicadaId] = useState(() => (location.state as EstadoPublicada | null)?.publicadaId)
  const [ofertas, setOfertas] = useState<Oferta[] | null>(null)
  const [error, setError] = useState(false)
  const [ofertaAFinalizar, setOfertaAFinalizar] = useState<Oferta | null>(null)
  const [finalizando, setFinalizando] = useState(false)
  const [notaFinalizada, setNotaFinalizada] = useState<string | null>(null)
  const [errorFinalizar, setErrorFinalizar] = useState<string | null>(null)

  useEffect(() => {
    // Se limpia el estado de la navegacion para que "¡Publicado!" no reaparezca al recargar.
    if (location.state) navigate(location.pathname, { replace: true, state: null })
  }, [location, navigate])

  useEffect(() => {
    ofertasApi
      .mias()
      .then(setOfertas)
      .catch(async (err) => {
        if (axios.isAxiosError(err) && err.response?.status === 401) await cerrarSesion()
        else setError(true)
      })
  }, [cerrarSesion])

  const handleConfirmarFinalizar = async () => {
    if (!ofertaAFinalizar) return
    setFinalizando(true)
    setErrorFinalizar(null)
    try {
      const actualizada = await ofertasApi.actualizarEstado(ofertaAFinalizar.id, 'FINALIZADA')
      setOfertas((prev) =>
        prev ? prev.map((o) => (o.id === actualizada.id ? actualizada : o)) : prev
      )
      setNotaFinalizada(`El trabajo «${ofertaAFinalizar.titulo}» fue finalizado correctamente`)
      setOfertaAFinalizar(null)
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        await cerrarSesion()
      } else {
        setErrorFinalizar('No se pudo finalizar el trabajo. Inténtalo de nuevo.')
      }
    } finally {
      setFinalizando(false)
    }
  }

  return (
    <>
      <Header />
      <main className="contenedor">
        <p className={comun.migas}><Link to="/dashboard">Dashboard</Link> / Mis publicaciones</p>
        <div className={styles.cabecera}>
          <h1 className={comun.titulo}>Mis publicaciones</h1>
          <Link to="/publicar" className={`boton ${comun.botonFucsia}`}>Publicar trabajo</Link>
        </div>

        {publicadaId !== undefined && (
          <p className={`${comun.nota} ${styles.exito}`} role="status">
            <span className={comun.check} aria-hidden="true">✓</span>
            <span><strong>¡Publicado!</strong> La publicación fue creada correctamente</span>
          </p>
        )}

        {notaFinalizada && (
          <p className={`${comun.nota} ${styles.exito}`} role="status">
            <span className={comun.check} aria-hidden="true">✓</span>
            <span><strong>¡Trabajo finalizado!</strong> {notaFinalizada}</span>
          </p>
        )}

        {errorFinalizar && (
          <p className={`${comun.nota} ${comun.notaError}`} role="alert">
            {errorFinalizar}
          </p>
        )}

        {error && (
          <p className={`${comun.nota} ${comun.notaError}`} role="alert">
            No se pudieron cargar tus publicaciones. Recarga la página para intentarlo de nuevo.
          </p>
        )}

        {!error && ofertas === null && <p className={styles.vacio}>Cargando publicaciones...</p>}

        {ofertas?.length === 0 && (
          <div className={`tarjeta ${styles.vacio}`}>
            <p>Aún no has publicado trabajos.</p>
            <Link to="/publicar">Publica el primero</Link>
          </div>
        )}

        {ofertas && ofertas.length > 0 && (
          <ul className={styles.lista}>
            {ofertas.map((o) => (
              <li key={o.id} className={`tarjeta ${styles.oferta} ${o.id === publicadaId ? styles.nueva : ''}`}>
                <div className={styles.encabezado}>
                  <h2 className={styles.nombre}>{o.titulo}</h2>
                  <div className={styles.etiquetas}>
                    {o.urgente && <span className={`${styles.etiqueta} ${styles.urgente}`}>Urgente</span>}
                    <span className={`${styles.etiqueta} ${styles[o.estado.toLowerCase()] ?? ''}`}>
                      {NOMBRE_ESTADO[o.estado]}
                    </span>
                  </div>
                </div>
                <p className={styles.actividad}>{o.actividad}</p>
                <dl className={styles.datos}>
                  <div><dt>Valor</dt><dd>{formatearPesos(o.valorOfrecido)}</dd></div>
                  <div><dt>Tiempo límite</dt><dd>{formatearFecha(o.fechaLimite)}</dd></div>
                  <div><dt>Ubicación</dt><dd>{o.ubicacion}</dd></div>
                </dl>

                {(o.estudianteAsignadoNombre || o.estudianteAsignadoCorreo) && (
                  <dl className={styles.asignado}>
                    <dt>Estudiante asignado</dt>
                    <dd>
                      {o.estudianteAsignadoNombre ?? o.estudianteAsignadoCorreo}
                      {o.estudianteAsignadoNombre && o.estudianteAsignadoCorreo && ` (${o.estudianteAsignadoCorreo})`}
                    </dd>
                  </dl>
                )}

                <div className={styles.pie}>
                  <p className={styles.publicada}>
                    Publicada el {formatearFecha(o.fechaPublicacion)}
                    {o.fechaCierre && ` · Finalizada el ${formatearFecha(o.fechaCierre)}`}
                  </p>
                  <p className={styles.publicada}>Postulados: {o.totalPostulados ?? 0}</p>
                  <div className={styles.acciones}>
                    {o.estado === 'ABIERTA' && (
                      <Link to={`/ofertas/${o.id}/postulados`}>Ver postulados</Link>
                    )}
                    {(o.estado === 'ASIGNADA' || o.estado === 'EN_PROCESO') && (
                      <>
                        <Link to={`/ofertas/${o.id}/postulados`}>Ver postulados</Link>
                        <button
                          type="button"
                          className={styles.botonFinalizar}
                          onClick={() => setOfertaAFinalizar(o)}
                        >
                          Finalizar trabajo
                        </button>
                      </>
                    )}
                    {o.estado === 'FINALIZADA' && (
                      <span className={styles.finalizadaNota}>Trabajo finalizado</span>
                    )}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}

        {ofertaAFinalizar && (
          <ConfirmarFinalizarModal
            titulo={ofertaAFinalizar.titulo}
            enviando={finalizando}
            onCancelar={() => {
              if (!finalizando) setOfertaAFinalizar(null)
            }}
            onConfirmar={handleConfirmarFinalizar}
          />
        )}
      </main>
    </>
  )
}
