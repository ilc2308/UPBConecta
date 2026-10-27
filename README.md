# UPB Conecta

## Descripcion general del producto

UPB Conecta es una plataforma web colaborativa de microeconomia (P2P) para la comunidad de la Universidad Pontificia Bolivariana (UPB). Permite a los estudiantes publicar, buscar y postularse a microtrabajos, favores de campus, tutorias y servicios remunerados entre companeros, formalizando los acuerdos mediante un ciclo de vida supervisado (Publicada -> Asignada -> En Proceso -> Finalizada) y un sistema de reputacion basado en calificaciones mutuas.

## Contexto del problema y propuesta de solucion

### Contexto del problema

En la Universidad Pontificia Bolivariana coexisten dos necesidades frecuentes: estudiantes que buscan generar ingresos extra en sus ventanas de tiempo libre entre clases, y estudiantes que requieren apoyo inmediato para resolver urgencias academicas o logisticas en el campus. Sin un canal institucional dedicado, estas necesidades se gestionan por grupos de WhatsApp o Telegram, lo cual genera perdida de visibilidad de las publicaciones, desconfianza entre las partes, ausencia de seguimiento del estado del encargo e ineficiencia al no poder filtrar por ubicacion, precio o disponibilidad horaria.

### Propuesta de solucion

UPB Conecta centraliza estas interacciones en una plataforma segura y estructurada que ofrece:
- Dashboard en tiempo real con filtros por bloque del campus, tipo de servicio y nivel de urgencia.
- Ciclo de vida formal y auditable para cada encargo publicado.
- Acceso restringido a la comunidad institucional mediante correo @upb.edu.co y autenticacion SSO con Microsoft.
- Sistema de reputacion intracomunitario activado al cierre de cada servicio.

## Guia de instalacion y ejecucion del proyecto

### Requisitos previos

- Docker Desktop 4.x o superior instalado y en ejecucion.
- Git instalado.

### Pasos de ejecucion

1. Clonar el repositorio:
   ```
   git clone https://github.com/ilc2308/UPBConecta.git
   cd UPBConecta
   ```

2. Crear el archivo de variables de entorno:
   ```
   copy .env.example .env
   ```
   Abrir `.env` y configurar las credenciales de base de datos:
   - DB_HOST=localhost
   - DB_PORT=5432
   - DB_NAME=upbconecta
   - DB_USER=upbconecta_user
   - DB_PASSWORD=upbconecta_password_2026

3. Levantar todos los servicios con Docker:
   ```
   docker compose up --build
   ```

4. Acceder a la plataforma:
   - Frontend: http://localhost:3000
   - API Backend: http://localhost:8080/api/v1/health
   - Base de datos PostgreSQL: localhost:5432

## Guia basica de uso de la aplicacion

1. Ingresar a http://localhost:3000 y autenticarse con cuenta institucional @upb.edu.co.
2. En el Dashboard explorar las solicitudes activas filtradas por ubicacion o urgencia.
3. Para publicar un encargo: clic en Publicar, ingresar titulo, descripcion, ubicacion en el campus, valor ofrecido y plazo.
4. Para postularse: abrir el detalle de una oferta y seleccionar Postularse. El solicitante revisara los perfiles y asignara al encargado.
5. Al completar la labor, el solicitante marca el encargo como Finalizado y ambas partes se califican mutuamente.

## Enlace al tablero de Jira

https://upbconecta.atlassian.net/jira/software/projects/UPBCONECT/boards/2/backlog

## Enlace al prototipo de media fidelidad

- Diseno y wireframes: https://www.figma.com/make/XmldQdk9JJvw7BcyZnOagr/Wireframes-media-fidelidad-UpbConecta?fullscreen=1&t=BuNbJ0x1JjYviIdB-1&code-node-id=0-6
- Prototipo interactivo navegable: https://www.figma.com/proto/u9yzIadLRLYvwg3TZiPDkd/Untitled?node-id=3-7&t=A2nrPhqUf6FTkIpH-1&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1

## Integrantes del equipo

- Tatiana Maria Ramirez Arroyave
- Ismael Lopez Cardozo
- Thomas Sining Salazar
- Fredy Manuel Guzman Herrera
- Maria Fernanda Toro Torres

## Autenticación y modo demo

### Cómo funciona la sesión
- La sesión la crea el **backend** y se entrega en una cookie `UPBC_SESSION` con los atributos
  `HttpOnly` (JavaScript no puede leerla), `SameSite=Strict` y `Secure` (configurable con `COOKIE_SECURE`).
- El frontend **no guarda ningún token** en `localStorage`; todas las llamadas usan `credentials: 'include'`
  y el navegador envía la cookie automáticamente.
- En el backend, `SesionCookieFilter` lee la cookie y marca la petición como autenticada para Spring Security.
  Los endpoints protegidos responden **401** si no hay sesión.
- Las sesiones se guardan en memoria: se pierden al reiniciar el backend (suficiente mientras exista el modo demo).

| Endpoint | Método | Descripción |
|---|---|---|
| `/api/v1/auth/demo` | POST | Inicia sesión con el usuario demo (solo si `DEMO_ENABLED=true`; si no, 403) |
| `/api/v1/auth/me` | GET | Devuelve el usuario de la sesión actual (401 si no hay sesión) |
| `/api/v1/auth/logout` | POST | Cierra la sesión y borra la cookie |

### Modo demo (solo desarrollo)
Mientras no esté integrado Microsoft SSO (UPBCONECT-5/6/7), se puede entrar con un usuario de prueba.
Está **desactivado por defecto** y requiere dos banderas:

| Variable | Dónde | Valor en desarrollo | Valor en ambientes compartidos |
|---|---|---|---|
| `DEMO_ENABLED` | `.env` de la raíz (backend) | `true` | `false` |
| `COOKIE_SECURE` | `.env` de la raíz (backend) | `false` (http local) | `true` (https) |
| `VITE_DEMO_MODE` | `app/frontend/.env.development` | `true` | no definida |

En el build de producción del frontend (`npm run build` o Docker) el botón de modo demo **no aparece**,
y el botón "Ingresar con cuenta institucional" queda deshabilitado hasta integrar SSO.

### Probar el inicio de sesión en local
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
   - En **Cookies** aparece `UPBC_SESSION` con **HttpOnly** marcado.
   - En **Local Storage** no hay ningún token.
   - Al recargar (F5) la sesión se mantiene; al cerrar sesión la cookie desaparece.

### Manejo de errores de la API
Todas las respuestas de error usan el formato estándar `ProblemDetail` (RFC 9457):
`400` JSON inválido o datos inválidos · `401` sin sesión · `403` acceso denegado ·
`404` recurso no encontrado · `409` conflicto de negocio · `500` error interno (sin exponer detalles).
