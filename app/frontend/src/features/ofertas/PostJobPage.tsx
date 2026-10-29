import { useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import axios from 'axios'
import { Header } from '../../Header'
import { useAuth } from '../../AuthContext'
import { ofertasApi } from './ofertasApi'
import { FORMULARIO_VACIO, LIMITES, formatearMiles, validarOferta } from './validarOferta'
import type { CampoOferta, ErroresOferta, FormularioOferta } from './validarOferta'
import styles from './PostJobPage.module.css'

/**
 * Formulario "Publicar trabajo" (UPBCONECT-39). Cualquier usuario autenticado puede publicar.
 * - Valida igual que el backend y deshabilita "Publicar" mientras haya errores.
 * - El publicador lo pone el backend a partir de la sesion; no viaja en el formulario.
 * TODO(UPBCONECT-40): resumen de confirmacion antes de publicar y redireccion a "Mis publicaciones".
 * TODO(UPBCONECT-37): mostrar dentro del layout con menu lateral cuando exista.
 */
export function PostJobPage() {
  const { cerrarSesion } = useAuth()
  const [form, setForm] = useState<FormularioOferta>(FORMULARIO_VACIO)
  const [tocados, setTocados] = useState<Partial<Record<CampoOferta, boolean>>>({})
  const [erroresServidor, setErroresServidor] = useState<ErroresOferta>({})
  const [enviando, setEnviando] = useState(false)
  const [resultado, setResultado] = useState<'exito' | 'error' | null>(null)

  const errores = validarOferta(form)
  const esValido = Object.keys(errores).length === 0

  function cambiar<K extends keyof FormularioOferta>(campo: K, valor: FormularioOferta[K]) {
    setForm((f) => ({ ...f, [campo]: valor }))
    setErroresServidor((e) => ({ ...e, [campo]: undefined }))
    setResultado(null)
  }

  const tocar = (campo: CampoOferta) => setTocados((t) => ({ ...t, [campo]: true }))

  // Un campo muestra su error cuando el usuario ya paso por el, o si el servidor lo rechazo.
  const errorDe = (campo: CampoOferta) => erroresServidor[campo] ?? (tocados[campo] ? errores[campo] : undefined)

  async function publicar(e: FormEvent) {
    e.preventDefault()
    if (!esValido || enviando) return

    setEnviando(true)
    try {
      await ofertasApi.crear({
        titulo: form.titulo.trim(),
        actividad: form.actividad.trim(),
        condiciones: form.condiciones.trim(),
        valorOfrecido: Number(form.valorOfrecido),
        fechaLimite: form.fechaLimite,
        urgente: form.urgente,
        ubicacion: form.ubicacion.trim(),
      })
      setForm(FORMULARIO_VACIO)
      setTocados({})
      setResultado('exito')
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        await cerrarSesion() // la sesion expiro: ProtectedRoute lleva a /auth
        return
      }
      // Los datos se conservan para que el usuario pueda corregir y reintentar.
      setErroresServidor(erroresDelServidor(err))
      setResultado('error')
    } finally {
      setEnviando(false)
    }
  }

  const props = (campo: CampoOferta) => ({
    id: campo,
    value: form[campo],
    onBlur: () => tocar(campo),
    'aria-invalid': Boolean(errorDe(campo)),
    'aria-describedby': errorDe(campo) ? `${campo}-error` : undefined,
    className: `${styles.entrada} ${errorDe(campo) ? styles.entradaInvalida : ''}`,
  })

  return (
    <>
      <Header />
      <main className="contenedor">
        <p className={styles.migas}>Mis publicaciones / Nueva</p>
        <h1 className={styles.titulo}>Publicar trabajo</h1>

        <form className={`tarjeta ${styles.formulario}`} onSubmit={publicar} noValidate>
          <div className={styles.columna}>
            <Campo id="titulo" etiqueta="Nombre de la publicación" error={errorDe('titulo')}>
              <input {...props('titulo')} type="text" maxLength={LIMITES.titulo}
                     placeholder="Ej. Apoyo para feria de emprendimiento"
                     onChange={(e) => cambiar('titulo', e.target.value)} />
            </Campo>
            <Campo id="actividad" etiqueta="Actividad" error={errorDe('actividad')}>
              <textarea {...props('actividad')} maxLength={LIMITES.actividad}
                        placeholder="¿Qué necesitas que hagan?"
                        onChange={(e) => cambiar('actividad', e.target.value)} />
            </Campo>
            <Campo id="condiciones" etiqueta="Condiciones" error={errorDe('condiciones')}>
              <textarea {...props('condiciones')} maxLength={LIMITES.condiciones}
                        placeholder="Requisitos, experiencia, puntualidad..."
                        onChange={(e) => cambiar('condiciones', e.target.value)} />
            </Campo>
          </div>

          <div className={styles.columna}>
            <Campo id="valorOfrecido" etiqueta="Valor ofrecido" error={errorDe('valorOfrecido')}>
              <div className={styles.conPrefijo}>
                <span className={styles.prefijo} aria-hidden="true">$</span>
                <input {...props('valorOfrecido')} type="text" inputMode="numeric" placeholder="80.000"
                       value={formatearMiles(form.valorOfrecido)}
                       onChange={(e) => cambiar('valorOfrecido', e.target.value.replace(/\D/g, '').slice(0, 9))} />
              </div>
            </Campo>
            <Campo id="fechaLimite" etiqueta="Tiempo límite" error={errorDe('fechaLimite')}>
              <input {...props('fechaLimite')} type="datetime-local" min={ahoraLocal()}
                     onChange={(e) => cambiar('fechaLimite', e.target.value)} />
            </Campo>
            <label className={styles.casilla}>
              <input type="checkbox" checked={form.urgente}
                     onChange={(e) => cambiar('urgente', e.target.checked)} />
              Es urgente
            </label>
            <Campo id="ubicacion" etiqueta="Ubicación" error={errorDe('ubicacion')}>
              <input {...props('ubicacion')} type="text" maxLength={LIMITES.ubicacion}
                     placeholder="Ej. Campus UPB · Bloque 7"
                     onChange={(e) => cambiar('ubicacion', e.target.value)} />
            </Campo>
          </div>

          <div className={styles.acciones}>
            {resultado === 'exito' && (
              <p className={styles.nota} role="status">
                <span className={styles.check} aria-hidden="true">✓</span>
                La publicación fue creada correctamente
              </p>
            )}
            {resultado === 'error' && (
              <p className={`${styles.nota} ${styles.notaError}`} role="alert">
                No se pudo publicar. Revisa los datos e inténtalo de nuevo.
              </p>
            )}
            <button type="submit" className={`boton ${styles.publicar}`} disabled={!esValido || enviando}>
              {enviando ? 'Publicando...' : 'Publicar'}
            </button>
          </div>
        </form>
      </main>
    </>
  )
}

function Campo({ id, etiqueta, error, children }: { id: CampoOferta; etiqueta: string; error?: string; children: ReactNode }) {
  return (
    <div className={styles.campo}>
      <label htmlFor={id} className={styles.etiqueta}>{etiqueta}</label>
      {children}
      {error && <p id={`${id}-error`} className={styles.error}>{error}</p>}
    </div>
  )
}

/** Fecha y hora actual en formato yyyy-MM-ddTHH:mm, para el minimo del selector. */
function ahoraLocal(): string {
  const d = new Date()
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
  return d.toISOString().slice(0, 16)
}

/** Convierte el 400 del backend ({ errores: ["campo: mensaje"] }) en errores por campo. */
function erroresDelServidor(err: unknown): ErroresOferta {
  const lista: unknown = axios.isAxiosError(err) ? err.response?.data?.errores : undefined
  if (!Array.isArray(lista)) return {}
  const errores: ErroresOferta = {}
  for (const item of lista) {
    const [campo, ...mensaje] = String(item).split(':')
    if (campo in FORMULARIO_VACIO && campo !== 'urgente') {
      errores[campo as CampoOferta] = mensaje.join(':').trim()
    }
  }
  return errores
}
