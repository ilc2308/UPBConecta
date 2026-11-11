import { api } from './api'

export type RolUsuario = 'ESTUDIANTE' | 'EMPLEADOR'
export type Usuario = {
  id?: string
  correo: string
  nombre: string
  rol?: RolUsuario
  carrera?: string
}
export type AuthResponse = { accessToken: string; usuario: Usuario }

export const authApi = {
  me: async () => (await api.get<Usuario>('/auth/me')).data,
  loginDemo: async () => (await api.post<Usuario>('/auth/demo')).data,
  logout: async () => { await api.post('/auth/logout') },
  loginMicrosoft: async (idToken: string) =>
    (await api.post<AuthResponse>('/auth/microsoft/login', { idToken })).data,
  registrarMicrosoft: async (idToken: string, rol: RolUsuario, carrera: string) =>
    (await api.post<AuthResponse>('/auth/microsoft/register', { idToken, rol, carrera })).data,
}
