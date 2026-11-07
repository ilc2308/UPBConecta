import { useCallback, useEffect, useState } from 'react'
import axios from 'axios'
import { Link, useParams } from 'react-router-dom'
import { Header } from '../../Header'
import { useAuth } from '../../AuthContext'
import { mensajeDelServidor, ofertasApi } from './ofertasApi'
import type { OfertaDetalle } from './ofertasApi'
import { formatearFecha, formatearPesos } from './formato'
import comun from './ofertas.module.css'
import styles from './OfertaDetallePage.module.css'

type Carga = 'cargando' | 'lista' | 'noExiste' | 'error'

/**
 * Detalle de una oferta y postulacion (UPBCONECT-41, pantalla "Apply" del prototipo).
 * "Aplicar" solo aparece a estudiantes, y nunca en una oferta propia.
 */
export function OfertaDetallePage() {
  const { id = '' } = useParams()
  const { usuario, cerrarSesion } = useAuth()
  const [oferta, setOferta] = useState<OfertaDetalle | null>(null)
  const [carga, setCarga] = useState<Carga>('cargando')
  const [enviando, setEnviando] = useState(false)
  const [postulado, setPostulado] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const manejarErrorDeCarga = useCallback(async (err: unknown) => {
    const status = axios.isAxiosError(err) ? err.response?.status : undefined
    if (status === 401) await cerrarSesion()
    // 400 llega cuando el id de la URL no es un numero: para el usuario esa oferta no existe.
    else if (status === 404 || status === 400) setCarga('noExiste')
    else setCarga('error')
  }, [cerrarSesion])

  const cargar = useCallback(async () => {
    try {
      setOferta(await ofertasApi.detalle(id))
      setCarga('lista')
    } catch (err) {
      await manejarErrorDeCarga(err)
    }
  }, [id, manejarErrorDeCarga])

  useEffect(() => {
    void cargar()
  }, [cargar])

  const aplicar = async () => {
    setError(null)
    setEnviando(true)
    try {
      await ofertasApi.postular(id)
      setPostulado(true)
      await cargar()
    } catch (err) {
      const status = axios.isAxiosError(err) ? err.response?.status : undefined
      if (status === 401 || status === 404) {
        await manejarErrorDeCarga(err)
      } else {
        setError(mensajeDelServidor(err, 'No se pudo enviar tu postulación. Inténtalo de nuevo.'))
        // Un 409 significa que la oferta cambio: se recarga para mostrar su estado real.
        if (status === 409) await cargar()
      }
    } finally {
      setEnviando(false)
    }
  }

  const migas = (
    <p className={comun.migas}><Link to="/dashboard">Dashboard</Link> / Detalle del trabajo</p>
  )

  if (carga !== 'lista' || !oferta) {
    return (
      <>
        <Header />
        <main className="contenedor">
          {migas}
          {carga === 'cargando' && <p className={styles.estado}>Cargando trabajo...</p>}
          {carga === 'noExiste' && (
            <p className={`${comun.nota} ${comun.notaError}`} role="alert">
              Esta oferta no existe o fue eliminada.
            </p>
          )}
          {carga === 'error' && (
            <p className={`${comun.nota} ${comun.notaError}`} role="alert">
              No se pudo cargar la oferta. Recarga la página para intentarlo de nuevo.
            </p>
          )}
        </main>
      </>
    )
  }

  const propia = usuario?.correo.toLowerCase() === oferta.publicadorCorreo.toLowerCase()
  const puedeAplicar = usuario?.rol === 'ESTUDIANTE' && !propia

  return (
    <>
      <Header />
      <main className="contenedor">
        {migas}
        <div className={styles.cabecera}>
          <h1 className={comun.titulo}>{oferta.titulo}</h1>
          {oferta.urgente && <span className={styles.urgente}>Urgente</span>}
        </div>

        {postulado && (
          <p className={`${comun.nota} ${styles.aviso}`} role="status">
            <span className={comun.check} aria-hidden="true">✓</span>
            <span>Te postulaste a este trabajo</span>
          </p>
        )}
        {error && <p className={`${comun.nota} ${comun.notaError} ${styles.aviso}`} role="alert">{error}</p>}
        {!oferta.disponible && !error && (
          <p className={`${comun.nota} ${comun.notaError} ${styles.aviso}`} role="status">
            Esta oferta ya no está disponible y no admite postulaciones.
          </p>
        )}

        <div className={`tarjeta ${styles.tarjeta}`}>
          <section aria-labelledby="empleador-titulo">
            <h2 id="empleador-titulo" className={styles.seccion}>Empleador</h2>
            <dl className={styles.datos}>
              <div><dt>Nombre</dt><dd>{oferta.publicadorNombre}</dd></div>
              <div><dt>Correo</dt><dd>{oferta.publicadorCorreo}</dd></div>
              <div><dt>Carrera</dt><dd>{oferta.publicadorCarrera ?? 'No registrada'}</dd></div>
            </dl>
          </section>

          <section aria-labelledby="trabajo-titulo">
            <h2 id="trabajo-titulo" className={styles.seccion}>Trabajo</h2>
            <dl className={styles.datos}>
              <div className={styles.ancho}><dt>Actividad</dt><dd>{oferta.actividad}</dd></div>
              <div className={styles.ancho}><dt>Condiciones</dt><dd>{oferta.condiciones}</dd></div>
              <div><dt>Valor ofrecido</dt><dd>{formatearPesos(oferta.valorOfrecido)}</dd></div>
              <div><dt>Tiempo límite</dt><dd>{formatearFecha(oferta.fechaLimite)}</dd></div>
              <div><dt>Ubicación</dt><dd>{oferta.ubicacion}</dd></div>
            </dl>
          </section>

          {puedeAplicar && oferta.yaPostulado && (
            <button type="button" className={`boton ${comun.botonFucsia} ${styles.accion}`} disabled>
              Ya te postulaste
            </button>
          )}
          {puedeAplicar && !oferta.yaPostulado && oferta.disponible && (
            <button
              type="button"
              className={`boton ${comun.botonFucsia} ${styles.accion}`}
              onClick={aplicar}
              disabled={enviando}
            >
              {enviando ? 'Enviando...' : 'Aplicar'}
            </button>
          )}
          {propia && (
            <Link to={`/ofertas/${oferta.id}/postulados`} className={`boton ${comun.botonFucsia} ${styles.accion}`}>
              Ver postulados
            </Link>
          )}
        </div>
      </main>
    </>
  )
}
