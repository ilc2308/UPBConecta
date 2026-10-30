/** 80000 -> "$ 80.000" */
export function formatearPesos(valor: number): string {
  return `$ ${valor.toLocaleString('es-CO')}`
}

/** "2026-11-06T18:00" -> "viernes, 6 de noviembre de 2026, 6:00 p. m." */
export function formatearFecha(fecha: string): string {
  return new Date(fecha).toLocaleString('es-CO', { dateStyle: 'full', timeStyle: 'short' })
}

export const NOMBRE_ESTADO = {
  ABIERTA: 'Abierta',
  ASIGNADA: 'Asignada',
  EN_PROCESO: 'En proceso',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
} as const
