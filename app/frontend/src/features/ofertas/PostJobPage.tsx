import { useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import axios from 'axios'
import { Link, useNavigate } from 'react-router-dom'
import { Header } from '../../Header'
import { useAuth } from '../../AuthContext'
import { ofertasApi } from './ofertasApi'
import { ConfirmarPublicacionModal } from './ConfirmarPublicacionModal'
import { FORMULARIO_VACIO, LIMITES, formatearMiles, validarOferta } from './validarOferta'
import type { CampoOferta, ErroresOferta, FormularioOferta } from './validarOferta'
import comun from './ofertas.module.css'
import styles from './PostJobPage.module.css'

/** Lo que recibe "Mis publicaciones" al llegar desde aqui, para mostrar "¡Publicado!". */
export interface EstadoPublicada {
  publicadaId: number
}

/**
 * Formulario "Publicar trabajo". Cualquier usuario autenticado puede publicar.
 * - UPBCONECT-39: valida igual que el backend y deshabilita "Publicar" mientras haya errores.
 * - UPBCONECT-40: "Publicar" abre un resumen; al confirmar se envia una sola vez y se va a
 *   "Mis publicaciones". Si falla, los datos siguen en el formulario.
 * - El publicador lo pone el backend a partir de la sesion; no viaja en el formulario.
 * TODO(UPBCONECT-37): mostrar dentro del layout con menu lateral cuando exista.
 */
export function PostJobPage() {
  const { cerrarSesion } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState<FormularioOferta>(FORMULARIO_VACIO)
  const [tocados, setTocados] = useState<Partial<Record<CampoOferta, boolean>>>({})
  const [erroresServidor, setErroresServidor] = useState<ErroresOferta>({})
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

  const errores = validarOferta(form)
  const esValido = Object.keys(errores).length === 0

  function cambiar<K extends keyof FormularioOferta>(campo: K, valor: FormularioOferta[K]) {
    setForm((f) => ({ ...f, [campo]: valor }))
    setErroresServidor((e) => ({ ...e, [campo]: undefined }))
    setErrorGeneral(null)
  }

  const tocar = (campo: CampoOferta) => setTocados((t) => ({ ...t, [campo]: true }))

  // Un campo muestra su error cuando el usuario ya paso por el, o si el servidor lo rechazo.
  const errorDe = (campo: CampoOferta) => erroresServidor[campo] ?? (tocados[campo] ? errores[campo] : undefined)

  function revisar(e: FormEvent) {
    e.preventDefault()
    if (esValido) setConfirmando(true)
  }

  async function confirmar() {
    if (enviando) return // evita el doble envio si se pulsa varias veces
    setEnviando(true)
    try {
      const creada = await ofertasApi.crear({
        titulo: form.titulo.trim(),
        actividad: form.actividad.trim(),
        condiciones: form.condiciones.trim(),
        valorOfrecido: Number(form.valorOfrecido),
        fechaLimite: form.fechaLimite,
        urgente: form.urgente,
        ubicacion: form.ubicacion.trim(),
      })
      navigate('/mis-publicaciones', { state: { publicadaId: creada.id } satisfies EstadoPublicada })
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        await cerrarSesion() // la sesion expiro: ProtectedRoute lleva a /auth
        return
      }
      // Los datos se conservan para que el usuario pueda corregir y reintentar.
      setErroresServidor(erroresDelServidor(err))
      setErrorGeneral(mensajeDeError(err))
      setConfirmando(false)
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
        <p className={comun.migas}><Link to="/mis-publicaciones">Mis publicaciones</Link> / Nueva</p>
        <h1 className={comun.titulo}>Publicar trabajo</h1>

        <form className={`tarjeta ${styles.formulario}`} onSubmit={revisar} noValidate>
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
            {errorGeneral && (
              <p className={`${comun.nota} ${comun.notaError}`} role="alert">{errorGeneral}</p>
            )}
            <button type="submit" className={`boton ${comun.botonFucsia}`} disabled={!esValido}>
              Publicar
            </button>
          </div>
        </form>
      </main>

      {confirmando && (
        <ConfirmarPublicacionModal
          datos={form}
          enviando={enviando}
          onEditar={() => setConfirmando(false)}
          onConfirmar={confirmar}
        />
      )}
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

/** Mensaje para el usuario segun la respuesta del backend (409 duplicado, 400 datos, otros). */
function mensajeDeError(err: unknown): string {
  if (axios.isAxiosError(err)) {
    if (err.response?.status === 409) return 'Ya publicaste esta oferta hace un momento. Revisa "Mis publicaciones".'
    if (err.response?.status === 400) return 'Revisa los campos marcados e inténtalo de nuevo.'
  }
  return 'No se pudo publicar. Revisa tu conexión e inténtalo de nuevo.'
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
