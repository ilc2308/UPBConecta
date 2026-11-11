import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../AuthContext'
import { jobsService } from './jobsService'
import type { OfertaResumen, Pagina } from './jobsService'
import styles from './JobList.module.css'

const TAMANIO_PAGINA = 10
/** Busqueda por palabra clave (UPBCONECT-38): minimo de caracteres, espera al escribir y maximo. */
const MINIMO_CARACTERES = 2
const ESPERA_MS = 400
const MAXIMO_CARACTERES = 100

interface Consulta {
  q: string
  pagina: number
}

interface Resultado {
  consulta: Consulta
  datos: Pagina<OfertaResumen>
}

/** Formatea un numero como pesos colombianos: 80000 -> "$ 80.000" */
function formatearPesos(valor: number): string {
  return `$ ${valor.toLocaleString('es-CO')}`
}

/** Sin espacios al inicio ni al final; con menos de 2 caracteres no se busca (queda vacio). */
function normalizar(texto: string): string {
  const limpio = texto.trim()
  return limpio.length >= MINIMO_CARACTERES ? limpio : ''
}

/** Cambia la palabra clave y vuelve a la primera pagina; si no cambio, devuelve la misma consulta. */
const conBusqueda = (q: string) => (c: Consulta): Consulta => (c.q === q ? c : { q, pagina: 0 })

/**
 * Listado paginado de trabajos activos para el Dashboard (UPBCONECT-37)
 * con barra de busqueda por palabra clave (UPBCONECT-38).
 * Consume GET /api/v1/ofertas?page=&size=&q= a traves de jobsService.
 * Estados: cargando, vacio, sin resultados, error y lista con paginacion.
 */
export function JobList() {
  const { cerrarSesion } = useAuth()
  const navigate = useNavigate()
  const [texto, setTexto] = useState('')
  const [consulta, setConsulta] = useState<Consulta>({ q: '', pagina: 0 })
  const [resultado, setResultado] = useState<Resultado | null>(null)
  const [fallida, setFallida] = useState<Consulta | null>(null)

  // Busca cuando la persona deja de escribir durante 400 ms.
  useEffect(() => {
    const id = window.setTimeout(() => setConsulta(conBusqueda(normalizar(texto))), ESPERA_MS)
    return () => window.clearTimeout(id)
  }, [texto])

  useEffect(() => {
    let vigente = true
    jobsService
      .getActive(consulta.pagina, TAMANIO_PAGINA, consulta.q)
      .then((datos) => {
        if (!vigente) return
        setFallida(null)
        setResultado({ consulta, datos })
      })
      .catch(async (err) => {
        if (!vigente) return
        if (axios.isAxiosError(err) && err.response?.status === 401) {
          await cerrarSesion()
        } else {
          setFallida(consulta)
        }
      })
    return () => {
      vigente = false
    }
  }, [consulta, cerrarSesion])

  const enviar = (e: FormEvent) => {
    e.preventDefault()
    setConsulta(conBusqueda(normalizar(texto)))
  }

  const borrar = () => {
    setTexto('')
    setConsulta(conBusqueda(''))
  }

  const cambiarPagina = (nueva: number) => setConsulta((c) => ({ ...c, pagina: nueva }))

  const error = fallida === consulta
  const cargando = !error && resultado?.consulta !== consulta
  const pagina = resultado?.datos
  const textoCorto = texto.trim().length > 0 && texto.trim().length < MINIMO_CARACTERES

  return (
    <>
      <form className={styles.busqueda} role="search" onSubmit={enviar}>
        <div className={styles.campoContenedor}>
          <label htmlFor="busqueda-trabajos" className={styles.soloLectores}>Buscar trabajos</label>
          <span className={styles.lupa} aria-hidden="true">🔍</span>
          <input
            id="busqueda-trabajos"
            type="text"
            className={styles.campo}
            placeholder="Search works"
            value={texto}
            maxLength={MAXIMO_CARACTERES}
            onChange={(e) => setTexto(e.target.value)}
          />
          {texto && (
            <button type="button" className={styles.borrar} onClick={borrar} aria-label="Borrar búsqueda">
              ✕
            </button>
          )}
        </div>
        <button type="submit" className="boton">Buscar</button>
      </form>

      {textoCorto && (
        <p className={styles.ayuda} role="status">Escribe al menos 2 caracteres para buscar.</p>
      )}

      {error && (
        <p className={`${styles.estado} ${styles.estadoError}`} role="alert">
          No se pudieron cargar los trabajos. Recarga la pagina para intentarlo de nuevo.
        </p>
      )}

      {cargando && <p className={styles.estado}>Cargando trabajos disponibles...</p>}

      {!error && !cargando && pagina && pagina.content.length === 0 && (
        <p className={styles.estado}>
          {consulta.q ? 'No encontramos trabajos con esa palabra' : 'Aun no hay trabajos publicados.'}
        </p>
      )}

      {!error && !cargando && pagina && pagina.content.length > 0 && (
        <>
          <ul className={styles.lista}>
            {pagina.content.map((oferta) => (
              <li key={oferta.id} className={styles.tarjeta}>
                <div className={styles.encabezado}>
                  <h3 className={styles.titulo}>{oferta.titulo}</h3>
                  <div className={styles.etiquetas}>
                    {oferta.urgente && (
                      <span className={`${styles.etiqueta} ${styles.etiquetaUrgente}`}>Urgente</span>
                    )}
                    <span className={styles.etiqueta}>{oferta.ubicacion}</span>
                  </div>
                </div>

                <p className={styles.publicador}>
                  Publicado por <strong>{oferta.publicadorCorreo}</strong>
                </p>

                <p className={styles.descripcion}>{oferta.actividad}</p>

                <div className={styles.pie}>
                  <span className={styles.valor}>{formatearPesos(oferta.valorOfrecido)}</span>
                  <a
                    href={`/ofertas/${oferta.id}`}
                    className={styles.masInfo}
                    onClick={(e) => {
                      e.preventDefault()
                      navigate(`/ofertas/${oferta.id}`)
                    }}
                  >
                    Más info &rarr;
                  </a>
                </div>
              </li>
            ))}
          </ul>

          {pagina.totalPages > 1 && (
            <div className={styles.paginacion}>
              <button
                className="boton boton-secundario"
                onClick={() => cambiarPagina(consulta.pagina - 1)}
                disabled={consulta.pagina === 0}
              >
                &larr; Anterior
              </button>
              <span className={styles.paginaInfo}>
                Página {consulta.pagina + 1} de {pagina.totalPages}
              </span>
              <button
                className="boton boton-secundario"
                onClick={() => cambiarPagina(consulta.pagina + 1)}
                disabled={pagina.last}
              >
                Siguiente &rarr;
              </button>
            </div>
          )}
        </>
      )}
    </>
  )
}