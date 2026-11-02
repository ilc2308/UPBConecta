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

## Guia de instalacion, ejecucion y autenticacion

### Acceso por internet

Cuando el despliegue este activo, se puede abrir:

- **Frontend:** [https://upbconecta-frontend.onrender.com](https://upbconecta-frontend.onrender.com)
- **Estado de la API:** [https://upbconecta-backend.onrender.com/api/v1/health](https://upbconecta-backend.onrender.com/api/v1/health)

La disponibilidad y el inicio de sesion dependen de que los servicios de nube esten desplegados y configurados con sus credenciales. En Render gratuito, un servicio puede entrar en reposo y tardar en responder al primer acceso.

#### Configuracion del despliegue en Render

Para publicar la aplicacion en internet se necesitan un servicio PostgreSQL, el backend Spring Boot y el frontend. Configura las variables en el panel de Render; no subas secretos al repositorio:

1. Crea una base de datos PostgreSQL y un servicio backend con Dockerfile `app/backend/Dockerfile`. En el backend configura `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JPA_DDL_AUTO=update`, `SERVER_PORT=8080`, `JWT_SECRET`, `AZURE_TENANT_ID`, `AZURE_CLIENT_ID` y `CORS_ALLOWED_ORIGINS`.
2. Crea el servicio frontend con Dockerfile `app/frontend/Dockerfile`. Define los argumentos de build `VITE_API_URL=https://<url-publica-del-backend>/api/v1`, `VITE_AZURE_TENANT_ID` y `VITE_AZURE_CLIENT_ID`.
3. En `CORS_ALLOWED_ORIGINS`, especifica el origen exacto del frontend, por ejemplo `https://upbconecta-frontend.onrender.com` (sin `/` al final).
4. En Microsoft Entra ID registra el origen publico del frontend seguido de `/auth` como redirect URI de tipo **Single-page application (SPA)**. Para la URL de ejemplo: `https://upbconecta-frontend.onrender.com/auth`.
5. Despliega y comprueba que `https://<url-publica-del-backend>/api/v1/health` responde correctamente antes de probar el login.

Genera un secreto distinto para cada ambiente con `openssl rand -base64 32`. Usa HTTPS en internet y `COOKIE_SECURE=true`; no habilites el modo demo en un despliegue publico.

### Ejecucion local con Docker (recomendada)

#### Requisitos

- Git y Docker Desktop instalados y en ejecucion.
- Una aplicacion registrada en Microsoft Entra ID con plataforma SPA y redirect URI local `http://localhost:3000/auth`.
- El `Tenant ID` y `Application (client) ID` de esa aplicacion.

#### Pasos

1. Clona el repositorio y entra a su carpeta:
   ```powershell
   git clone https://github.com/ilc2308/UPBConecta.git
   cd UPBConecta
   ```
2. Copia el ejemplo de configuracion y genera una clave JWT:
   ```powershell
   Copy-Item .env.example .env
   openssl rand -base64 32
   ```
3. Edita `.env` y reemplaza los valores de ejemplo:
   - `AZURE_TENANT_ID`: Tenant ID de Entra ID.
   - `AZURE_CLIENT_ID`: Application (client) ID de Entra ID.
   - `VITE_AZURE_TENANT_ID` y `VITE_AZURE_CLIENT_ID`: usa los mismos valores.
   - `JWT_SECRET`: resultado de `openssl rand -base64 32`.
   - `VITE_API_URL=http://localhost:8080/api/v1`.
   - `CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173`.
   - Para desarrollo local, `COOKIE_SECURE=false` y `DEMO_ENABLED=false`.

   Conserva las credenciales de base de datos de ejemplo para uso local. No compartas ni subas `.env` a Git.
4. Construye y levanta la base de datos, el backend y el frontend:
   ```powershell
   docker compose up --build
   ```
5. Abre `http://localhost:3000`. La API está en `http://localhost:8080/api/v1/health`.
6. Para detener los servicios, pulsa `Ctrl+C`; para iniciarlos después en segundo plano, usa `docker compose up -d`.

### Como autenticarse con el correo Microsoft de la UPB

1. En la aplicación, pulsa **Continuar con Microsoft** e inicia sesion usando la cuenta institucional completa, terminada en `@upb.edu.co`.
2. Si es el primer ingreso, selecciona el rol **Estudiante** o **Empleador**, escribe la carrera y confirma el registro.
3. Al completar el registro, la aplicación crea la cuenta y abre el dashboard. En los siguientes ingresos, basta con continuar con Microsoft; no se vuelve a crear la cuenta.
4. Las cuentas fuera del dominio `@upb.edu.co` no pueden registrarse. Si el correo ya está registrado, inicia sesión con Microsoft en lugar de volver a registrarlo.

Si el botón Microsoft aparece deshabilitado o informa que no está configurado, revisa `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `VITE_AZURE_TENANT_ID` y `VITE_AZURE_CLIENT_ID`, y comprueba que el redirect URI coincide exactamente con la URL usada para abrir el frontend. En el despliegue en internet, estos valores debe configurarlos quien administra los servicios de nube y la aplicación Entra ID de la universidad.

Consulta [docs/AUTENTICACION.md](docs/AUTENTICACION.md) para más información sobre las variables, los endpoints y el modo demo.

## Guia basica de uso de la aplicacion

1. Ingresar al frontend desplegado o a `http://localhost:3000` si se ejecuta localmente, y autenticarse con una cuenta Microsoft institucional `@upb.edu.co`.
2. En el Dashboard explorar las solicitudes activas filtradas por ubicacion o urgencia.
3. Para publicar un encargo: clic en Publicar, ingresar titulo, descripcion, ubicacion en el campus, valor ofrecido y plazo.
4. Para postularse: abrir el detalle de una oferta y seleccionar Postularse. El solicitante revisara los perfiles y asignara al encargado.
5. Al completar la labor, el solicitante marca el encargo como Finalizado y ambas partes se califican mutuamente.

## Enlace al tablero de Jira

https://upbconecta.atlassian.net/jira/software/projects/UPBCONECT/boards/2/backlog

## Enlace al prototipo de media fidelidad

- Diseno y wireframes: https://www.figma.com/make/XmldQdk9JJvw7BcyZnOagr/Wireframes-media-fidelidad-UpbConecta?fullscreen=1&t=BuNbJ0x1JjYviIdB-1&code-node-id=0-6
- Prototipo interactivo navegable: https://www.figma.com/proto/u9yzIadLRLYvwg3TZiPDkd/Untitled?node-id=3-7&t=A2nrPhqUf6FTkIpH-1&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1

## Documentación
- Autenticación y modo demo: [docs/AUTENTICACION.md](docs/AUTENTICACION.md)

- ## Integrantes del equipo

- Tatiana Maria Ramirez Arroyave
- Ismael López Cardozo
- Thomas Sining Salazar
- Fredy Manuel Guzman Herrera
- Maria Fernanda Toro Torres
