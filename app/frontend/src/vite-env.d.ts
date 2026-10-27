/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL base de la API, por ejemplo http://localhost:8080/api/v1 */
  readonly VITE_API_URL?: string
  /** Habilita el modo demo (solo en desarrollo). Nunca "true" en produccion. */
  readonly VITE_DEMO_MODE?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}