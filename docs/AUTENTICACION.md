# Autenticación y modo demo

## Cómo funciona la sesión
- La sesión la crea el **backend** y se entrega en una cookie `UPBC_SESSION` con los atributos
  `HttpOnly` (JavaScript no puede leerla), `SameSite=Strict` y `Secure` (configurable con `COOKIE_SECURE`).
- El frontend **no guarda ningún token ni datos de sesión** (ni `localStorage` ni `sessionStorage`);
  todas las llamadas usan `credentials: 'include'` y el navegador envía la cookie automáticamente.
- Al cargar la app, `AuthContext` consulta `GET /api/v1/auth/me` para saber si hay una sesión activa.
- En el backend, `SesionCookieFilter` lee la cookie y marca la petición como autenticada para Spring Security.
  Los endpoints protegidos responden **401** si no hay sesión.
- Las sesiones se guardan en memoria: se pierden al reiniciar el backend (suficiente mientras exista el modo demo).

| Endpoint | Método | Descripción |
|---|---|---|
| `/api/v1/auth/demo` | POST | Inicia sesión con el usuario demo (solo si `DEMO_ENABLED=true`; si no, 403) |
| `/api/v1/auth/me` | GET | Devuelve el usuario de la sesión actual (401 si no hay sesión) |
| `/api/v1/auth/logout` | POST | Cierra la sesión y borra la cookie |

## Modo demo (solo desarrollo)
Mientras no esté integrado Microsoft SSO (UPBCONECT-5/6/7), se puede entrar con un usuario de prueba.
Está **desactivado por defecto** y requiere dos banderas:

| Variable | Dónde | Valor en desarrollo | Valor en ambientes compartidos |
|---|---|---|---|
| `DEMO_ENABLED` | `.env` de la raíz (backend) | `true` | `false` |
| `COOKIE_SECURE` | `.env` de la raíz (backend) | `false` (http local) | `true` (https) |
| `VITE_DEMO_MODE` | `app/frontend/.env.development` | `true` | no definida |

En el build de producción del frontend (`npm run build` o Docker) el botón de modo demo **no aparece**,
y el botón "Ingresar con cuenta institucional" queda deshabilitado hasta integrar SSO.

## Probar el inicio de sesión en local
1. En el `.env` de la raíz poner `DEMO_ENABLED=true` y `COOKIE_SECURE=false`, y verificar que
   `CORS_ALLOWED_ORIGINS` incluya `http://localhost:5173`.
2. Levantar base de datos y backend:
   ```bash
   docker compose up -d --build db backend
   ```
3. Verificar el backend con `curl`:
   ```bash
   curl -i -c /tmp/cookies.txt -X POST http://localhost:8080/api/v1/auth/demo   # 200 + Set-Cookie
   curl -b /tmp/cookies.txt http://localhost:8080/api/v1/auth/me                  # datos del usuario
   curl -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/auth/me    # 401 sin cookie
   ```
4. Levantar el frontend en modo desarrollo:
   ```bash
   cd app/frontend
   npm install
   npm run dev
   ```
5. Abrir `http://localhost:5173`, pulsar **"Entrar en modo demo"** y comprobar con F12 → Application:
   - En **Cookies** aparece `UPBC_SESSION` con **HttpOnly** y **SameSite=Strict**.
   - **Local Storage** y **Session Storage** no tienen datos de sesión.
   - Al recargar (F5) la sesión se mantiene; al cerrar sesión la cookie desaparece.

> Al abrir la app sin sesión, la consola muestra `401` en `/auth/me`: es la consulta inicial de sesión y es el comportamiento esperado.

## Manejo de errores de la API
Todas las respuestas de error usan el formato estándar `ProblemDetail` (RFC 9457):

| Código | Cuándo |
|---|---|
| 400 | JSON inválido o datos que no pasan la validación |
| 401 | No hay sesión activa |
| 403 | Acceso denegado (por ejemplo, modo demo desactivado) |
| 404 | Recurso no encontrado |
| 409 | Conflicto de negocio |
| 500 | Error interno (el detalle queda en el log, no en la respuesta) |
