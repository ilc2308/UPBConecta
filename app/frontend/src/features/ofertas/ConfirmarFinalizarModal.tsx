import { useEffect, useRef } from 'react'
import comun from './ofertas.module.css'
import styles from './ConfirmarPublicacionModal.module.css'

interface Props {
  titulo: string
  enviando: boolean
  onCancelar: () => void
  onConfirmar: () => void
}

/** Diálogo de confirmación antes de finalizar un trabajo asignado (UPBCONECT-44). */
export function ConfirmarFinalizarModal({ titulo, enviando, onCancelar, onConfirmar }: Props) {
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
      aria-labelledby="finalizar-titulo"
      onCancel={(e) => {
        e.preventDefault()
        if (!enviando) onCancelar()
      }}
    >
      <h2 id="finalizar-titulo" className={styles.encabezado}>Finalizar trabajo</h2>
      <p className={styles.ayuda}>
        ¿Confirmas que la colaboración para «{titulo}» ha finalizado? El trabajo quedará registrado como completado.
      </p>
      <div className={styles.acciones}>
        <button type="button" className="boton boton-secundario" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </button>
        <button type="button" className={`boton ${comun.botonFucsia}`} onClick={onConfirmar} disabled={enviando}>
          {enviando ? 'Finalizando...' : 'Finalizar trabajo'}
        </button>
      </div>
    </dialog>
  )
}
