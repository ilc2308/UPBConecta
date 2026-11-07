import { useEffect, useRef } from 'react'
import comun from './ofertas.module.css'
import styles from './ConfirmarPublicacionModal.module.css'

interface Props {
  nombre: string
  enviando: boolean
  onCancelar: () => void
  onConfirmar: () => void
}

/** Confirmacion antes de asignar un trabajo (UPBCONECT-43). Cancelar o pulsar Esc no cambia nada. */
export function ConfirmarSeleccionModal({ nombre, enviando, onCancelar, onConfirmar }: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const d = dialogo.current
    d?.showModal()
    return () => d?.close()
  }, [])

  return (
    <dialog
      ref={dialogo}
      className={styles.dialogo}
      aria-labelledby="seleccion-titulo"
      onCancel={(e) => {
        e.preventDefault()
        if (!enviando) onCancelar()
      }}
    >
      <h2 id="seleccion-titulo" className={styles.encabezado}>Asignar trabajo</h2>
      <p className={styles.ayuda}>¿Seguro que quieres asignar este trabajo a {nombre}?</p>
      <div className={styles.acciones}>
        <button type="button" className="boton boton-secundario" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </button>
        <button type="button" className={`boton ${comun.botonFucsia}`} onClick={onConfirmar} disabled={enviando}>
          {enviando ? 'Asignando...' : 'Confirmar'}
        </button>
      </div>
    </dialog>
  )
}
