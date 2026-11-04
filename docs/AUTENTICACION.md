# Autenticación y modo demo

## Registro e inicio de sesión con Microsoft
- El frontend autentica al usuario mediante Microsoft Entra ID (MSAL) y envía el `id_token` al backend.
- El backend valida firma/JWKS, issuer, audience y expiración del token. Solo admite correos terminados exactamente en `@upb.edu.co`.
- Un usuario nuevo completa rol y carrera; `POST /api/v1/auth/microsoft/register` crea el registro persistente y devuelve un JWT de UPB Conecta.
- `POST /api/v1/auth/microsoft/login` inicia sesión de usuarios registrados. El registro duplicado responde `409 Conflict`.
- El JWT propio vive en `AuthContext`, se conserva durante la pestaña y Axios lo envía en `Authorization: Bearer ...`. Su vigencia es de ocho horas.

| Variable | Dónde | Descripción |
|---|---|---|
| `AZURE_TENANT_ID` | backend | Tenant institucional de Microsoft Entra ID |
| `AZURE_CLIENT_ID` | backend | Application (client) ID registrado en Entra ID |
| `VITE_AZURE_TENANT_ID` | frontend | Tenant usado por MSAL |
| `VITE_AZURE_CLIENT_ID` | frontend | Application (client) ID usado por MSAL |
| `JWT_SECRET` | backend | Clave Base64 de al menos 32 bytes; generar con `openssl rand -base64 32` |
| `JWT_EXPIRATION_HOURS` | backend | Vigencia del JWT propio en horas (por defecto, `8`) |

Registra `http://localhost:5173/auth` como redirect URI de tipo SPA en Entra ID. Para desarrollo local, define `VITE_AZURE_TENANT_ID` y `VITE_AZURE_CLIENT_ID` en `app/frontend/.env.development`; para Docker, configúralas en el `.env` de la raíz junto con `AZURE_TENANT_ID`, `AZURE_CLIENT_ID` y `JWT_SECRET`. `JWT_EXPIRATION_HOURS` es opcional; usa un entero positivo y por defecto es `8`. El redirect URI debe corresponder al origen público del frontend. No reutilices claves de desarrollo en producción.

## Sesión demo heredada
- La sesión demo se crea en el backend y viaja en una cookie `UPBC_SESSION` con atributos `HttpOnly`,
  `SameSite=Strict` y `Secure` (configurable con `COOKIE_SECURE`).
- Al cargar la app, `AuthContext` consulta `GET /api/v1/auth/me`; las sesiones demo siguen en memoria y se pierden al reiniciar.
- `SesionCookieFilter` traduce la cookie demo a autenticación de Spring Security.

| Endpoint | Método | Descripción |
|---|---|---|
| `/api/v1/auth/demo` | POST | Inicia sesión con el usuario demo (solo si `DEMO_ENABLED=true`; si no, 403) |
| `/api/v1/auth/me` | GET | Devuelve el usuario de la sesión actual (401 si no hay sesión) |
| `/api/v1/auth/logout` | POST | Cierra la sesión y borra la cookie |
| `/api/v1/auth/microsoft/login` | POST | Valida un `id_token` Microsoft de un usuario existente |
| `/api/v1/auth/microsoft/register` | POST | Crea una cuenta institucional y devuelve el JWT propio |

## Modo demo (solo desarrollo)
El modo demo permite entrar con un usuario de prueba sin completar el registro Microsoft.
Está **desactivado por defecto** y requiere dos banderas:

| Variable | Dónde | Valor en desarrollo | Valor en ambientes compartidos |
|---|---|---|---|
| `DEMO_ENABLED` | `.env` de la raíz (backend) | `true` | `false` |
| `COOKIE_SECURE` | `.env` de la raíz (backend) | `false` (http local) | `true` (https) |
| `VITE_DEMO_MODE` | `app/frontend/.env.development` | `true` | no definida |

En el build de producción del frontend (`npm run build` o Docker) el botón de modo demo **no aparece**.

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
   - El modo demo usa cookie HttpOnly; al cerrar sesión la cookie desaparece.
   - El inicio de sesión Microsoft guarda el JWT en `sessionStorage` por defecto. Si se selecciona **Recordarme**, se guarda en `localStorage` hasta que expire o se cierre sesión; evita esa opción en equipos compartidos.

> Al abrir la app sin sesión, la consola muestra `401` en `/auth/me`: es la consulta inicial de sesión y es el comportamiento esperado.

## Manejo de errores de la API
Los endpoints de autenticación (`/api/v1/auth/**`) responden con el formato:

```json
{ "codigo": "INVALID_TOKEN", "mensaje": "El token no es válido o ha vencido.", "timestamp": "2026-11-04T18:20:00Z" }
```

La interfaz traduce los códigos a mensajes seguros en español y no muestra detalles técnicos del servidor. Los códigos de autenticación son:

| Código | Cuándo |
|---|---|
| `INVALID_TOKEN` | Token inválido, vencido o falta de autenticación |
| `DOMAIN_NOT_ALLOWED` | La cuenta no pertenece al dominio institucional |
| `USER_NOT_REGISTERED` | El usuario debe completar su registro |
| `PROVIDER_UNAVAILABLE` | Microsoft o el servicio de autenticación no está disponible |
| `INVALID_REQUEST` | La solicitud no tiene el formato esperado |
| `FORBIDDEN` | La cuenta no está autorizada para acceder |
| `CONFLICT` | La cuenta ya existe o hay un conflicto de negocio |

Las respuestas de error de otros controladores mantienen su formato vigente.
