import { useCallback, useEffect, useState } from 'react'
import axios from 'axios'
import { Link, useParams } from 'react-router-dom'
import { Header } from '../../Header'
import { useAuth } from '../../AuthContext'
import { mensajeDelServidor, ofertasApi } from './ofertasApi'
import type { OfertaDetalle, Postulante } from './ofertasApi'
import { ConfirmarSeleccionModal } from './ConfirmarSeleccionModal'
import { formatearFecha } from './formato'
import comun from './ofertas.module.css'
import lista from './MisPublicacionesPage.module.css'
import styles from './PostuladosPage.module.css'

/**
 * Postulados de una oferta y seleccion de uno de ellos (UPBCONECT-43).
 * Tras elegir, la fila queda como "Seleccionado" y los demas botones se deshabilitan.
 */
export function PostuladosPage() {
  const { id = '' } = useParams()
  const { cerrarSesion } = useAuth()
  const [oferta, setOferta] = useState<OfertaDetalle | null>(null)
  const [postulantes, setPostulantes] = useState<Postulante[] | null>(null)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [aConfirmar, setAConfirmar] = useState<Postulante | null>(null)
  const [asignadoA, setAsignadoA] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const cargar = useCallback(async () => {
    try {
      const [detalle, postulados] = await Promise.all([ofertasApi.detalle(id), ofertasApi.postulantes(id)])
      setOferta(detalle)
      setPostulantes(postulados)
    } catch (err) {
      const status = axios.isAxiosError(err) ? err.response?.status : undefined
      if (status === 401) await cerrarSesion()
      else if (status === 404 || status === 400) setErrorCarga('Esta oferta no existe o fue eliminada.')
      else setErrorCarga(mensajeDelServidor(err, 'No se pudieron cargar los postulados. Recarga la página.'))
    }
  }, [id, cerrarSesion])

  useEffect(() => {
    void cargar()
  }, [cargar])

  const confirmar = async () => {
    if (!aConfirmar) return
    setError(null)
    setEnviando(true)
    try {
      setPostulantes(await ofertasApi.seleccionar(id, aConfirmar.postulacionId))
      setOferta((actual) => (actual ? { ...actual, estado: 'ASIGNADA', disponible: false } : actual))
      setAsignadoA(aConfirmar.nombre)
    } catch (err) {
      const status = axios.isAxiosError(err) ? err.response?.status : undefined
      if (status === 401) {
        await cerrarSesion()
        return
      }
      setError(mensajeDelServidor(err, 'No se pudo asignar el trabajo. Inténtalo de nuevo.'))
      // Un 409 significa que la oferta cambio: se recarga para mostrar su estado real.
      if (status === 409) await cargar()
    } finally {
      setEnviando(false)
      setAConfirmar(null)
    }
  }

  const seleccionado = postulantes?.find((p) => p.estado === 'SELECCIONADA')
  const bloqueada = oferta?.estado !== 'ABIERTA' || seleccionado !== undefined

  return (
    <>
      <Header />
      <main className="contenedor">
        <p className={comun.migas}>
          <Link to="/dashboard">Dashboard</Link> / <Link to="/mis-publicaciones">Mis publicaciones</Link> / Postulados
        </p>
        <h1 className={comun.titulo}>Postulados{oferta ? `: ${oferta.titulo}` : ''}</h1>

        {asignadoA && (
          <p className={`${comun.nota} ${styles.aviso}`} role="status">
            <span className={comun.check} aria-hidden="true">✓</span>
            <span>Asignaste este trabajo a <strong>{asignadoA}</strong></span>
          </p>
        )}
        {(errorCarga || error) && (
          <p className={`${comun.nota} ${comun.notaError} ${styles.aviso}`} role="alert">{errorCarga ?? error}</p>
        )}

        {!errorCarga && postulantes === null && <p className={lista.vacio}>Cargando postulados...</p>}

        {postulantes?.length === 0 && (
          <div className={`tarjeta ${lista.vacio}`}>
            <p>Aún no hay postulados para este trabajo.</p>
          </div>
        )}

        {postulantes && postulantes.length > 0 && (
          <ul className={lista.lista}>
            {postulantes.map((p) => (
              <li key={p.postulacionId} className={`tarjeta ${styles.fila}`}>
                <div>
                  <h2 className={lista.nombre}>{p.nombre}</h2>
                  <p className={styles.dato}>{p.correo} · {p.carrera}</p>
                  <p className={styles.dato}>Se postuló el {formatearFecha(p.fechaPostulacion)}</p>
                </div>
                {p.estado === 'SELECCIONADA' ? (
                  <span className={lista.etiqueta}>Seleccionado</span>
                ) : (
                  <button
                    type="button"
                    className={`boton ${comun.botonFucsia}`}
                    disabled={bloqueada}
                    onClick={() => setAConfirmar(p)}
                  >
                    Seleccionar
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </main>

      {aConfirmar && (
        <ConfirmarSeleccionModal
          nombre={aConfirmar.nombre}
          enviando={enviando}
          onCancelar={() => setAConfirmar(null)}
          onConfirmar={confirmar}
        />
      )}
    </>
  )
}
