import axios from 'axios'
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
  estudianteAsignadoNombre?: string | null
  estudianteAsignadoCorreo?: string | null
  fechaAsignacion?: string | null
  fechaCierre?: string | null
    /** Cantidad de postulaciones recibidas; solo viene en GET /ofertas/mias (UPBCONECT-42). */
  totalPostulados?: number
}

/** Detalle de una oferta (OfertaDetalleDTO), con los datos del empleador. */
export interface OfertaDetalle extends OfertaNueva {
  id: number
  estado: Oferta['estado']
  /** Si aun admite postulaciones: esta abierta y su fecha limite no ha pasado. */
  disponible: boolean
  fechaPublicacion: string
  publicadorNombre: string
  publicadorCorreo: string
  publicadorCarrera: string | null
  yaPostulado: boolean
}

/** Estudiante postulado a una oferta (PostulanteDTO). */
export interface Postulante {
  postulacionId: number
  nombre: string
  correo: string
  carrera: string
  estado: 'PENDIENTE' | 'SELECCIONADA' | 'RECHAZADA'
  fechaPostulacion: string
}

/** Mensaje de negocio de un 403 o 409 del backend; para cualquier otro error, el texto alternativo. */
export function mensajeDelServidor(err: unknown, alternativo: string): string {
  if (axios.isAxiosError(err) && (err.response?.status === 403 || err.response?.status === 409)) {
    const detalle: unknown = err.response?.data?.detail
    if (typeof detalle === 'string') return detalle
  }
  return alternativo
}

// Las llamadas usan la instancia de Axios de api.ts, que envia la cookie de sesion.
export const ofertasApi = {
  crear: async (oferta: OfertaNueva) => (await api.post<Oferta>('/ofertas', oferta)).data,
  /** Publicaciones del usuario de la sesion, la mas reciente primero (UPBCONECT-40). */
  mias: async () => (await api.get<Oferta[]>('/ofertas/mias')).data,
  /** Trabajos asignados al usuario de la sesion (UPBCONECT-43). */
  asignadas: async () => (await api.get<Oferta[]>('/ofertas/asignadas')).data,
  /** Trabajos asignados y finalizados del estudiante de la sesion (UPBCONECT-44). */
  misAsignadas: async () => (await api.get<Oferta[]>('/ofertas/asignadas/mias')).data,
  detalle: async (id: string) => (await api.get<OfertaDetalle>(`/ofertas/${id}`)).data,
  postular: async (id: string) => (await api.post<Postulante>(`/ofertas/${id}/postulaciones`)).data,
  postulantes: async (id: string) => (await api.get<Postulante[]>(`/ofertas/${id}/postulaciones`)).data,
  /** Asigna la oferta a un postulante y devuelve la lista actualizada (UPBCONECT-43). */
  seleccionar: async (id: string, postulacionId: number) =>
    (await api.patch<Postulante[]>(`/ofertas/${id}/postulaciones/${postulacionId}/seleccionar`)).data,
  /** Actualiza el estado de la oferta (UPBCONECT-44). */
  actualizarEstado: async (id: number | string, estado: 'FINALIZADA' | 'EN_PROCESO') =>
    (await api.patch<Oferta>(`/ofertas/${id}/estado`, { estado })).data,
}
