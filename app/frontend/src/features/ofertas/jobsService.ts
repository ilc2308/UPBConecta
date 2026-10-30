import { api } from '../../api'
import type { Oferta } from './ofertasApi'

/** Respuesta paginada de Spring Data (Page<T>). */
export interface Pagina<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number     // pagina actual (0-based)
  size: number
  last: boolean
}

/**
 * Resumen de oferta para el listado del Dashboard (UPBCONECT-37).
 * No incluye condiciones; igual que OfertaResumenDTO del backend.
 */
export type OfertaResumen = Pick<
  Oferta,
  'id' | 'titulo' | 'actividad' | 'valorOfrecido' | 'fechaLimite' |
  'urgente' | 'ubicacion' | 'estado' | 'fechaPublicacion' | 'publicadorCorreo'
>

export const jobsService = {
  /**
   * Obtiene la pagina de ofertas ABIERTAS y vigentes, ordenadas de la mas reciente a la mas antigua.
   * Requiere sesion activa (el backend responde 401 si no la hay).
   */
  getActive: async (page = 0, size = 10): Promise<Pagina<OfertaResumen>> =>
    (await api.get<Pagina<OfertaResumen>>('/ofertas', { params: { page, size } })).data,
}
