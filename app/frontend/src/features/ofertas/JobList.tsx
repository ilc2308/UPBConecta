import { useEffect, useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../AuthContext'
import { jobsService } from './jobsService'
import type { OfertaResumen, Pagina } from './jobsService'
import styles from './JobList.module.css'

const TAMANIO_PAGINA = 10

/** Formatea un numero como pesos colombianos: 80000 -> "$ 80.000" */
function formatearPesos(valor: number): string {
  return `$ ${valor.toLocaleString('es-CO')}`
}

/**
 * Listado paginado de trabajos activos para el Dashboard (UPBCONECT-37).
 * Consume GET /api/v1/ofertas?page=&size= a traves de jobsService.
 * Estados: cargando, vacio, error y lista con paginacion.
 */
export function JobList() {
  const { cerrarSesion } = useAuth()
  const navigate = useNavigate()
  const [pagina, setPagina] = useState<Pagina<OfertaResumen> | null>(null)
  const [paginaActual, setPaginaActual] = useState(0)
  const [error, setError] = useState(false)

  useEffect(() => {
    jobsService
      .getActive(paginaActual, TAMANIO_PAGINA)
      .then(setPagina)
      .catch(async (err) => {
        if (axios.isAxiosError(err) && err.response?.status === 401) {
          await cerrarSesion()
        } else {
          setError(true)
        }
      })
  }, [paginaActual, cerrarSesion])

  if (error) {
    return (
      <p className={`${styles.estado} ${styles.estadoError}`} role="alert">
        No se pudieron cargar los trabajos. Recarga la pagina para intentarlo de nuevo.
      </p>
    )
  }

  if (pagina === null) {
    return <p className={styles.estado}>Cargando trabajos disponibles...</p>
  }

  if (pagina.content.length === 0) {
    return (
      <p className={styles.estado}>
        Aun no hay trabajos publicados.
      </p>
    )
  }

  const cambiarPagina = (nueva: number) => {
    setError(false)
    setPagina(null)
    setPaginaActual(nueva)
  }

  return (
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
              {/* TODO(UPBCONECT-41): enlazar al detalle de la oferta cuando exista */}
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
            onClick={() => cambiarPagina(paginaActual - 1)}
            disabled={paginaActual === 0}
          >
            &larr; Anterior
          </button>
          <span className={styles.paginaInfo}>
            Página {paginaActual + 1} de {pagina.totalPages}
          </span>
          <button
            className="boton boton-secundario"
            onClick={() => cambiarPagina(paginaActual + 1)}
            disabled={pagina.last}
          >
            Siguiente &rarr;
          </button>
        </div>
      )}
    </>
  )
}
