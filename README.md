# UPB Conecta

Plataforma web que conecta a estudiantes de la Universidad Pontificia Bolivariana con personas y empresas que publican trabajos. Los empleadores publican ofertas, los estudiantes las buscan y se postulan, y el empleador elige a quién contratar y cierra la colaboración.

## Contexto del problema y propuesta de solución

> _Completar con la definición del Taller 1: necesidad identificada, usuarios afectados y propuesta de valor._

## Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | React 19, TypeScript, Vite, React Router, Axios |
| Backend | Java 21, Spring Boot 4 (Web MVC, Data JPA, Security, Validation) |
| Base de datos | PostgreSQL 16 |
| Contenedores | Docker y Docker Compose |

## Estructura del repositorio

```
UPBConecta/
├── README.md
├── docker-compose.yml       # db + backend + frontend
├── .env.example             # variables necesarias (sin credenciales reales)
├── docs/                    # requisitos, casos de uso, procesos, wireframes
└── app/                     # código fuente del primer release
    ├── frontend/            # React 19 + TypeScript
    │   └── src/
    │       ├── pages/       # pantallas (AuthPage, DashboardPage)
    │       ├── components/  # componentes reutilizables
    │       ├── services/    # instancia de Axios y llamadas a la API
    │       ├── context/     # AuthContext
    │       ├── routes/      # React Router (/auth, /dashboard)
    │       └── styles/      # variables de color UPB y estilos globales
    └── backend/             # Spring Boot 4
        └── src/main/java/co/edu/upb/upbconecta/
            ├── controller/  # endpoints REST (prefijo /api/v1)
            ├── service/     # lógica de negocio
            ├── repository/  # Spring Data JPA
            ├── model/       # entidades
            ├── dto/         # objetos de entrada y salida
            ├── config/      # seguridad, CORS, prefijo de la API
            └── exception/   # manejo global de errores
```

## Requisitos previos

- **Opción A, con Docker (recomendada):** Docker Desktop con Docker Compose.
- **Opción B, sin Docker:** Node.js 22 o superior, Java 21 (JDK), Maven 3.9 o superior y PostgreSQL 16.

## Instalación y ejecución

### Opción A: todo con Docker

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/ilc2308/UPBConecta.git
   cd UPBConecta
   ```
2. Crear el archivo de variables y cambiar la contraseña de la base de datos:
   ```bash
   cp .env.example .env
   ```
3. Levantar los tres contenedores:
   ```bash
   docker compose up --build
   ```
4. Abrir:
   - Frontend: http://localhost:3000
   - Health check de la API: http://localhost:8080/api/v1/health

Para detenerlo: `docker compose down`. Para borrar también los datos de la base: `docker compose down -v`.

### Opción B: ejecución local

1. **Base de datos.** Crear en PostgreSQL 16 la base y el usuario que vayan a usar, o levantar solo la base con Docker:
   ```bash
   cp .env.example .env
   docker compose up -d db
   ```
2. **Backend.** Definir las variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` (los valores por defecto están en `application.yml`) y ejecutar:
   ```bash
   cd app/backend
   mvn spring-boot:run
   ```
   Queda en http://localhost:8080/api/v1/health
3. **Frontend.** En otra terminal:
   ```bash
   cd app/frontend
   cp .env.example .env
   npm install
   npm run dev
   ```
   Queda en http://localhost:5173

## Guía básica de uso

1. Entrar a la aplicación: se abre la pantalla de ingreso (`/auth`), que muestra si el backend está conectado.
2. Pulsar **Ingresar con cuenta institucional**. Por ahora entra en modo demo; el inicio de sesión con Microsoft llega con UPBCONECT-5 y UPBCONECT-6.
3. En el **Dashboard** (`/dashboard`) aparecerán los trabajos activos (UPBCONECT-37).

> _Actualizar esta guía a medida que se integren las historias del release._

## Flujo de trabajo en Git

- `main`: versión estable y release entregado. Protegida: solo recibe cambios por pull request.
- `develop`: rama de integración.
- Una rama por historia de usuario, con la clave de Jira: `feat/UPBCONECT-<número>` (por ejemplo `feat/UPBCONECT-39`).
- Cada rama entra por pull request revisado por al menos un integrante.
- Los commits y pull requests incluyen la clave de la historia (por ejemplo `UPBCONECT-39: formulario de publicación`) para que Jira los enlace.

## Enlaces

- Tablero de Jira: https://upbconecta.atlassian.net/jira/software/projects/UPBCONECT/boards/2/backlog
- Prototipo de media fidelidad: _pendiente_

## Integrantes del equipo

- Ismael López Cardozo
- María Fernanda Toro Torres
- Thomas Sining Salazar
- Fredy Guzmán
- Tatiana María Ramírez Arroyave
