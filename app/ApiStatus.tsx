import { useEffect, useState } from 'react';
import { getHealth } from '../services/healthService';

type Estado = 'cargando' | 'ok' | 'error';

/** Muestra si el backend responde en GET /api/v1/health. */
export function ApiStatus() {
  const [estado, setEstado] = useState<Estado>('cargando');

  useEffect(() => {
    getHealth()
      .then((res) => setEstado(res.status === 'UP' ? 'ok' : 'error'))
      .catch(() => setEstado('error'));
  }, []);

  const textos: Record<Estado, string> = {
    cargando: 'Comprobando conexión con el backend…',
    ok: 'Backend conectado',
    error: 'No se pudo conectar con el backend',
  };

  const colores: Record<Estado, string> = {
    cargando: 'var(--color-texto-suave)',
    ok: 'var(--color-exito)',
    error: 'var(--color-error)',
  };

  return (
    <p style={{ color: colores[estado], fontWeight: 600 }} role="status">
      ● {textos[estado]}
    </p>
  );
}
