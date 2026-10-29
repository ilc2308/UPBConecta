/** Valores del formulario tal como los escribe el usuario (todo texto). */
export interface FormularioOferta {
  titulo: string
  actividad: string
  condiciones: string
  /** Solo digitos, sin puntos de miles (ej. "80000") */
  valorOfrecido: string
  fechaLimite: string
  urgente: boolean
  ubicacion: string
}

export type CampoOferta = Exclude<keyof FormularioOferta, 'urgente'>
export type ErroresOferta = Partial<Record<CampoOferta, string>>

export const LIMITES = { titulo: 100, actividad: 1000, condiciones: 1000, ubicacion: 150 } as const

export const FORMULARIO_VACIO: FormularioOferta = {
  titulo: '',
  actividad: '',
  condiciones: '',
  valorOfrecido: '',
  fechaLimite: '',
  urgente: false,
  ubicacion: '',
}

/**
 * Mismas reglas que OfertaCreateDTO en el backend (UPBCONECT-39):
 * todo obligatorio, valor mayor que 0, fecha limite futura y longitudes maximas.
 */
export function validarOferta(f: FormularioOferta, ahora = new Date()): ErroresOferta {
  const errores: ErroresOferta = {}

  for (const campo of ['titulo', 'actividad', 'condiciones', 'ubicacion'] as const) {
    const valor = f[campo].trim()
    if (!valor) errores[campo] = 'Este campo es obligatorio'
    else if (valor.length > LIMITES[campo]) errores[campo] = `Máximo ${LIMITES[campo]} caracteres`
  }

  if (!f.valorOfrecido) errores.valorOfrecido = 'Este campo es obligatorio'
  else if (Number(f.valorOfrecido) <= 0) errores.valorOfrecido = 'Debe ser mayor que 0'

  if (!f.fechaLimite) errores.fechaLimite = 'Este campo es obligatorio'
  else if (new Date(f.fechaLimite) <= ahora) errores.fechaLimite = 'Debe ser una fecha futura'

  return errores
}

/** "80000" -> "80.000" (formato colombiano). */
export function formatearMiles(digitos: string): string {
  return digitos ? Number(digitos).toLocaleString('es-CO') : ''
}
