# UPB Conecta

Plataforma web colaborativa que conecta a estudiantes de la Universidad Pontificia Bolivariana (UPB) con oportunidades de apoyo, favores de campus, tutorías y microtrabajos remunerados, así como con personas y empresas que publican ofertas. Los ofertantes/empleadores publican solicitudes de trabajo, los estudiantes las buscan y se postulan según su disponibilidad entre clases, y se concreta y cierra la colaboración de forma segura.

---

## Integrantes del equipo

- Tatiana María Ramírez Arroyave
- Ismael López Cardozo
- Thomas Sining Salazar
- Fredy Manuel Guzmán Herrera
- María Fernanda Toro Torres

---

## Descripción general del producto

UPB Conecta es una solución digital concebida para formalizar, estructurar y dinamizar la microeconomía y la red de servicios estudiantiles dentro del campus universitario. Transforma el intercambio informal y desorganizado de favores y tareas puntuales en un flujo transparente, auditable y supervisado.

La plataforma permite a los usuarios:
1. Publicar ofertas y solicitudes de ayuda: Definir claramente la actividad (tutorías, soporte técnico, diligencias en el campus, préstamo de materiales), la tarifa fija o recompensa acordada, la ubicación exacta en el campus (bloques, cafeterías, biblioteca) y el nivel de urgencia o plazo límite.
2. Postularse y gestionar oportunidades: Identificar labores compatibles con los huecos o ventanas de tiempo libre entre clases según su cercanía física en el campus.
3. Controlar el ciclo de vida de la tarea: Seguimiento en tiempo real mediante estados formales (Publicada -> Asignada -> En Proceso -> Entregada -> Finalizada).
4. Construir reputación comunitaria: Calificación bidireccional obligatoria al cierre del servicio que incentiva el cumplimiento, la puntualidad y la confianza entre compañeros.

---

## Contexto del problema y propuesta de solución

### Contexto del problema
En el día a día universitario coexisten dos necesidades críticas: estudiantes que buscan formas flexibles y constantes de generar ingresos extra, y miembros de la comunidad que requieren apoyo inmediato para resolver urgencias académicas, logísticas o diligencias cotidianas.

Ante la ausencia de un espacio institucional dedicado, estas necesidades se canalizan a través de medios informales (grupos masivos de WhatsApp o Telegram por facultad e historias de Instagram), lo cual genera:
* Pérdida de visibilidad e ineficiencia: Los anuncios se traspapelan rápidamente ante el flujo continuo de mensajes, memes y temas personales, quedando obsoletos en cuestión de minutos.
* Ausencia de estructura y categorización: No existe forma de filtrar por materia, precio, bloque o disponibilidad horaria.
* Falta de confianza e informalidad: No hay trazabilidad de acuerdos, historial de cumplimiento ni seguridad en los pagos entre personas desconocidas.
* Inadecuación de plataformas globales: Herramientas como TaskRabbit, Fiverr o Freelancer cobran comisiones elevadas inviables para microtransacciones de bajo costo, exigen procesos de contratación lentos y están totalmente desconectadas de la inmediatez e hiperlocalidad que exige la vida en el campus.

### Propuesta de solución
UPB Conecta implementa un entorno centralizado y confiable basado en economía colaborativa (P2P):
* Catálogo y Dashboard en tiempo real: Muro estructurado con filtros por facultad, bloque del campus, palabra clave, urgencia y disponibilidad entre clases.
* Flujo de tareas transparente: Gestión de compromisos por estados formales que erradica la informalidad de los acuerdos de palabra.
* Ecosistema cerrado y seguro: Acceso condicionado a la pertenencia institucional (correo @upb.edu.co con autenticación SSO de Microsoft y validación 2FA/TOTP).
* Módulo de reputación intracomunitario: Registro visible de calificaciones y comentarios habilitado exclusivamente tras completar cada encargo para respaldar el compromiso y la seriedad.

---

## Tecnologías y Arquitectura

De acuerdo con el Documento de Diseño Técnico, el sistema adopta una arquitectura de Monolito Simple por capas con Frontend y Backend Desacoplados, estructurado y gestionado mediante Monorepo, comunicados exclusivamente mediante una API REST Stateless:

| Capa | Tecnología | Justificación técnica |
|---|---|---|
| Frontend | React 19, TypeScript, Vite, React Router DOM, Axios, CSS Modules | Renderizado de alto rendimiento, tipado estricto para reducir errores en compilación y navegación SPA ágil. |
| Backend | Java 21, Spring Boot 4 (Web MVC, Data JPA, Security, Validation) | Alto rendimiento y concurrencia optimizada mediante Virtual Threads; arquitectura limpia en capas (Controllers, Services, Repositories). |
| Seguridad | Spring Security + JWT | Autenticación stateless que garantiza la independencia total entre cliente y servidor. |
| Base de datos | PostgreSQL 16 | Motor relacional en Tercera Forma Normal (3FN) para integridad y consistencia transaccional. |
| Tiempo Real | WebSockets + STOMP (Spring WebSocket / SockJS) | Mensajería instantánea y notificaciones en vivo de cambio de estado de las tareas. |
| Contenedores | Docker y Docker Compose | Paridad total entre entornos de desarrollo y despliegue local aislado. |

---

## Estructura del repositorio

```text
UPBConecta/
├── README.md                # Documentación principal, arquitectura y guías del proyecto
├── docker-compose.yml       # Orquestación de contenedores (db + backend + frontend)
├── .env.example             # Plantilla de variables de entorno (sin credenciales reales)
├── docs/                    # Entregables del semestre (requisitos, casos de uso, procesos, wireframes)
│   ├── Documento de requisitos.pdf
│   ├── Diagrama de casos de uso.png
│   ├── Diagrama de procesos.png
│   └── Wireframes.pdf
└── app/                     # Código fuente del primer release desarrollado
    ├── frontend/            # Cliente SPA en React 19 + TypeScript + Vite
    │   ├── package.json
    │   ├── vite.config.ts
    │   ├── index.html
    │   └── src/
    │       ├── pages/       # Pantallas principales (AuthPage, DashboardPage)
    │       ├── components/  # Componentes reutilizables (Navbar, Cards, Modales)
    │       ├── services/    # Instancia de Axios y consumo de la API REST
    │       ├── context/     # AuthContext (Estado global de sesión)
    │       ├── routes/      # Enrutamiento React Router (/auth, /dashboard)
    │       └── styles/      # Variables de color UPB y estilos modulares
    └── backend/             # API REST Spring Boot 4 + Java 21
        ├── pom.xml
        └── src/main/java/co/edu/upb/upbconecta/
            ├── controller/  # Endpoints REST (prefijo /api/v1)
            ├── service/     # Lógica y reglas de negocio transaccionales
            ├── repository/  # Interfaces Spring Data JPA
            ├── model/       # Entidades del dominio relacional
            ├── dto/         # Objetos de transferencia de datos (Data Transfer Objects)
            ├── config/      # Configuración de seguridad JWT, CORS y filtros
            └── exception/   # Manejo global y centralizado de excepciones
```

---

## Guía de instalación y ejecución

Hay dos formas de correr el proyecto. La opción A (Docker) es la recomendada: no hace falta instalar Java, Node ni PostgreSQL localmente, garantizando paridad total en todo el equipo.

### Opción A: con Docker (recomendada)

#### Requisitos

| Herramienta | Versión | Cómo comprobarla |
|---|---|---|
| Git | cualquiera reciente | `git --version` |
| Docker Desktop (incluye Docker Compose) | 4.x o superior | `docker --version` y `docker compose version` |

Docker Desktop se descarga en https://www.docker.com/products/docker-desktop/. En Windows requiere habilitar WSL 2 durante la instalación. Antes de continuar, abre Docker Desktop y verifica que indique Engine running.

#### Pasos de ejecución
1. Clonar el repositorio y entrar a la carpeta:
   ```bash
   git clone https://github.com/ilc2308/UPBConecta.git
   cd UPBConecta
   ```
2. Crear el archivo de variables de entorno a partir de la plantilla:
   ```bash
   # En Linux / macOS / Git Bash:
   cp .env.example .env

   # En Windows (PowerShell):
   copy .env.example .env
   ```
   Abrir `.env` y configurar `DB_PASSWORD` con una clave personal. El archivo `.env` nunca debe subirse al repositorio.
3. Construir y levantar los tres contenedores (base de datos, backend y frontend):
   ```bash
   docker compose up --build
   ```
   La primera ejecución descargará las imágenes y dependencias necesarias.
4. Verificar el despliegue de los servicios:

   | Servicio | URL / Puerto | Verificación esperada |
   |---|---|---|
   | Frontend | http://localhost:3000 | Pantalla de inicio con el indicador Backend conectado en verde |
   | Backend | http://localhost:8080/api/v1/health | Respuesta JSON con `{"status":"UP","service":"upbconecta-backend",...}` |
   | Base de datos | `localhost:5432` | Contenedor `upbconecta-db` en estado saludable (`healthy`) |

#### Comandos útiles de Docker

| Propósito | Comando |
|---|---|
| Ejecutar en segundo plano | `docker compose up -d --build` |
| Consultar estado de los contenedores | `docker compose ps` |
| Ver registros en vivo del backend | `docker compose logs -f backend` |
| Detener los contenedores | `docker compose down` |
| Detener y eliminar volúmenes de datos | `docker compose down -v` |
| Reconstruir tras cambios en código | `docker compose up --build` |

---

### Opción B: ejecución local sin Docker (para desarrollo)

Recomendada durante el desarrollo individual para aprovechar la recarga en caliente (Hot Module Replacement) del frontend.

#### Requisitos

| Herramienta | Versión mínima | Verificación |
|---|---|---|
| Node.js | 20 o 22 LTS | `node -v` |
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9 o superior | `mvn -v` |
| PostgreSQL | 16 | `psql --version` |

#### Pasos de ejecución
1. Base de datos: Levantar únicamente el contenedor de PostgreSQL con Docker:
   ```bash
   docker compose up -d db
   ```
   (O disponer de una instancia local en el puerto 5432 con los datos de base de datos definidos en `.env`).
2. Backend (en una terminal):
   ```bash
   cd app/backend
   # En Linux / macOS / Git Bash:
   DB_PASSWORD=la_contraseña_del_env mvn spring-boot:run

   # En Windows PowerShell:
   $env:DB_PASSWORD="la_contraseña_del_env"; mvn spring-boot:run
   ```
   Quedará escuchando en `http://localhost:8080/api/v1/health`.
3. Frontend (en una segunda terminal):
   ```bash
   cd app/frontend
   npm install
   npm run dev
   ```
   Quedará accesible en `http://localhost:5173`.

---

### Resolución de problemas frecuentes

| Síntoma | Causa probable y solución |
|---|---|
| `Cannot connect to the Docker daemon` | Docker Desktop no está iniciado. Abrirlo y esperar el estado Engine running. |
| `port is already allocated` (5432, 8080 o 3000) | Conflicto con un servicio local previo. Detener el proceso o cambiar el puerto en `.env`. |
| El frontend muestra "No se pudo conectar con el backend" | El backend está inicializando o ocurrió un fallo: consultar `docker compose logs backend`. |
| `password authentication failed` | La base fue creada previamente con otra credencial: ejecutar `docker compose down -v` y reiniciar. |
| Error de CORS en el navegador | Verificar que la URL del frontend esté registrada en `CORS_ALLOWED_ORIGINS` dentro del archivo `.env`. |

---

## Guía básica de uso

UPB Conecta atiende dos perfiles principales: el estudiante solicitante/empleador (quien publica necesidades y selecciona al encargado) y el estudiante ejecutante/trabajador (quien explora y se postula a las ofertas).

### 1. Ingreso a la plataforma
1. Abrir `http://localhost:3000` (Docker) o `http://localhost:5173` (local).
2. Verificar el indicador de conexión con el backend en verde.
3. Presionar "Ingresar con cuenta institucional". En esta entrega el acceso opera en sesión de demostración (la autenticación SSO completa con Microsoft corresponde a UPBCONECT-5, -6 y -7).
4. Para finalizar sesión, seleccionar "Cerrar sesión" en la barra de navegación superior.

### 2. Búsqueda y postulación a trabajos (Rol Estudiante Trabajador)
1. En el Dashboard se presenta el listado de labores y solicitudes activas ordenadas cronológicamente.
2. Emplear el cuadro de búsqueda para filtrar oportunidades por palabras clave, ubicación en el campus o tipo de labor.
3. Abrir la oferta para revisar el detalle de la actividad, plazo y pago ofrecido, y pulsar "Postularme".

### 3. Publicación y asignación de trabajos (Rol Estudiante Solicitante / Empleador)
1. Abrir el formulario de publicación, diligenciar los datos requeridos (título, descripción, ubicación en el campus, remuneración y urgencia) y enviar la solicitud.
2. Confirmar la publicación para recibir el mensaje de confirmación y habilitar la visibilidad en el muro común.
3. Ingresar al detalle de la oferta creada para revisar la lista de personas postuladas y sus antecedentes.
4. Seleccionar al postulante idóneo para formalizar la asignación.
5. Al concretarse el favor o trabajo, marcar la colaboración como finalizada y emitir la calificación respectiva.

---

### Estado de las funcionalidades en este release

| Funcionalidad | Historia de Usuario | Estado |
|---|---|---|
| Estructura base del proyecto y arquitectura monorepo | UPBCONECT-45 | Disponible |
| Pantalla de ingreso, Dashboard y cierre de sesión (modo demo) | UPBCONECT-45 | Disponible |
| Registro e inicio de sesión con Microsoft (SSO) | UPBCONECT-5, UPBCONECT-6, UPBCONECT-7 | Sprint 1 |
| Listado de trabajos activos en el Dashboard | UPBCONECT-37 | Sprint 1 |
| Búsqueda y filtrado por palabra clave | UPBCONECT-38 | Sprint 1 |
| Detalle de una oferta y postulación | UPBCONECT-41 | Sprint 1 |
| Publicación de trabajo por formulario y nota de éxito | UPBCONECT-39, UPBCONECT-40 | Sprint 1 |
| Lista de postulados y selección de encargado | UPBCONECT-42, UPBCONECT-43 | Sprint 1 |
| Actualizar o cerrar la colaboración activa | UPBCONECT-44 | Sprint 1 |

---

## Flujo de trabajo en Git (GitHubFlow)

- `main`: Versión estable y release oficial entregado. Rama protegida: únicamente recibe cambios mediante Pull Requests aprobados, impidiendo fusiones no autorizadas o auto-merge directo.
- Ramas feature: Una rama dedicada por cada historia de usuario del backlog de Jira, respetando la nomenclatura `feat/UPBCONECT-<número>` (ejemplo: `feat/UPBCONECT-39`).
- Integración continua: Cada rama feature se somete a revisión de código (Code Review) y aprobación de al menos un compañero antes de su integración definitiva a `main`.
- Trazabilidad: Los títulos de los commits y pull requests contienen la clave de Jira (ejemplo: `UPBCONECT-39: formulario de publicación`) asegurando sincronización con el tablero de gestión.

---

## Enlaces del Proyecto

* Tablero de Jira (Backlog del Proyecto):  
  https://upbconecta.atlassian.net/jira/software/projects/UPBCONECT/boards/2/backlog

* Prototipo de Media Fidelidad (Figma):  
  * Diseño y Wireframes: https://www.figma.com/make/XmldQdk9JJvw7BcyZnOagr/Wireframes-media-fidelidad-UpbConecta?fullscreen=1&t=BuNbJ0x1JjYviIdB-1&code-node-id=0-6  
  * Prototipo Interactivo Navegable: https://www.figma.com/proto/u9yzIadLRLYvwg3TZiPDkd/Untitled?node-id=3-7&t=A2nrPhqUf6FTkIpH-1&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1
