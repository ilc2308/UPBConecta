import { api } from '../../api'

/** Datos que envia el formulario "Publicar trabajo" (POST /ofertas). */
export interface OfertaNueva {
  titulo: string
  actividad: string
  condiciones: string
  valorOfrecido: number
  /** Fecha y hora local, formato yyyy-MM-ddTHH:mm */
  fechaLimite: string
  urgente: boolean
  ubicacion: string
}

/** Oferta tal como la devuelve el backend (OfertaDTO). */
export interface Oferta extends OfertaNueva {
  id: number
  estado: 'ABIERTA' | 'ASIGNADA' | 'EN_PROCESO' | 'FINALIZADA' | 'CANCELADA'
  fechaPublicacion: string
  publicadorCorreo: string
}

// Las llamadas usan la instancia de Axios de api.ts, que envia la cookie de sesion.
export const ofertasApi = {
  crear: async (oferta: OfertaNueva) => (await api.post<Oferta>('/ofertas', oferta)).data,
}
