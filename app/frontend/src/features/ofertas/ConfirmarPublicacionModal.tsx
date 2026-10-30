import { useEffect, useRef } from 'react'
import { formatearFecha, formatearPesos } from './formato'
import type { FormularioOferta } from './validarOferta'
import comun from './ofertas.module.css'
import styles from './ConfirmarPublicacionModal.module.css'

interface Props {
  datos: FormularioOferta
  enviando: boolean
  onEditar: () => void
  onConfirmar: () => void
}

/**
 * Resumen de la publicacion antes de enviarla (UPBCONECT-40).
 * Usa <dialog> nativo: atrapa el foco y se cierra con Esc (equivale a "Editar").
 * Mientras se envia, ambos botones quedan deshabilitados para evitar el doble envio.
 */
export function ConfirmarPublicacionModal({ datos, enviando, onEditar, onConfirmar }: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const d = dialogo.current
    d?.showModal()
    return () => d?.close()
  }, [])

  const filas: [string, string][] = [
    ['Nombre de la publicación', datos.titulo.trim()],
    ['Actividad', datos.actividad.trim()],
    ['Condiciones', datos.condiciones.trim()],
    ['Valor ofrecido', formatearPesos(Number(datos.valorOfrecido))],
    ['Tiempo límite', formatearFecha(datos.fechaLimite) + (datos.urgente ? ' · Urgente' : '')],
    ['Ubicación', datos.ubicacion.trim()],
  ]

  return (
    <dialog
      ref={dialogo}
      className={styles.dialogo}
      aria-labelledby="confirmar-titulo"
      onCancel={(e) => {
        e.preventDefault()
        if (!enviando) onEditar()
      }}
    >
      <h2 id="confirmar-titulo" className={styles.encabezado}>Revisa tu publicación</h2>
      <p className={styles.ayuda}>Así la verán los demás estudiantes. Puedes editarla antes de publicar.</p>

      <dl className={styles.resumen}>
        {filas.map(([etiqueta, valor]) => (
          <div key={etiqueta} className={styles.fila}>
            <dt>{etiqueta}</dt>
            <dd>{valor}</dd>
          </div>
        ))}
      </dl>

      <div className={styles.acciones}>
        <button type="button" className="boton boton-secundario" onClick={onEditar} disabled={enviando}>
          Editar
        </button>
        <button type="button" className={`boton ${comun.botonFucsia}`} onClick={onConfirmar}
                disabled={enviando} autoFocus>
          {enviando ? 'Publicando...' : 'Confirmar y publicar'}
        </button>
      </div>
    </dialog>
  )
}
