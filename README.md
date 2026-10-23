# UPB Conecta (Red de Servicios Estudiantiles)

Plataforma web colaborativa de microeconomía (P2P) y microtrabajos ocasionales para la comunidad de la Universidad Pontificia Bolivariana (UPB). Permite a los estudiantes centralizar, ordenar y hacer seguimiento a la oferta y demanda de tareas cortas, favores de campus y servicios remunerados, promoviendo la generación de ingresos extra y la colaboración segura entre pares.

---

## Integrantes del Equipo
* Tatiana María Ramírez Arroyave
* Ismael López Cardozo
* Thomas Sining Salazar
* Fredy Manuel Guzmán Herrera
* María Fernanda Toro Torres

---

## Descripción General del Producto
UPB Conecta es una solución digital concebida para formalizar, estructurar y dinamizar la microeconomía estudiantil dentro del campus universitario. Transforma el intercambio informal y desorganizado de favores y tareas puntuales en un flujo transparente y supervisado.

La plataforma permite a los estudiantes:
1. Publicar requerimientos u ofertas: Definir de forma clara la actividad (tutorías, soporte técnico, diligencias de campus, préstamo de materiales), la tarifa fija o recompensa acordada, la ubicación exacta en el campus (bloques, cafeterías, biblioteca) y el nivel de urgencia o plazo límite.
2. Postularse y gestionar oportunidades: Encontrar labores compatibles con los huecos o ventanas de tiempo libre entre clases según su cercanía física en el campus.
3. Controlar el ciclo de vida de la tarea: Seguimiento en tiempo real mediante estados auditables (Publicada -> Asignada -> En Proceso -> Entregada -> Finalizada).
4. Construir reputación comunitaria: Calificación bidireccional obligatoria al cierre del servicio que incentiva el cumplimiento, la puntualidad y la confianza entre compañeros.

---

## Contexto del Problema y Propuesta de Solución

### Contexto del Problema
En el día a día universitario coexisten dos necesidades críticas: estudiantes que buscan formas flexibles y constantes de generar ingresos extra, y estudiantes que requieren apoyo inmediato para resolver urgencias académicas o logísticas cotidianas.

Ante la ausencia de un espacio institucional dedicado, estas necesidades se canalizan a través de medios informales (grupos masivos de WhatsApp o Telegram por facultad e historias de Instagram), lo cual genera:
* Pérdida de visibilidad e ineficiencia: Los anuncios se traspapelan rápidamente ante el flujo continuo de mensajes, memes y temas personales, quedando obsoletos en cuestión de minutos.
* Ausencia de estructura y categorización: No existe forma de filtrar por materia, precio, bloque o disponibilidad horaria.
* Falta de confianza e informalidad: No hay trazabilidad de acuerdos, historial de cumplimiento ni seguridad en los pagos entre personas desconocidas.
* Inadecuación de plataformas globales: Soluciones como TaskRabbit, Fiverr o Freelancer cobran comisiones elevadas inviables para microtransacciones de bajo costo, exigen procesos de contratación lentos y están desconectadas de la inmediatez e hiperlocalidad que exige la vida en el campus.

### Propuesta de Solución
UPB Conecta implementa un entorno centralizado y confiable basado en economía colaborativa (P2P):
* Catálogo y Dashboard en tiempo real: Muro estructurado con filtros por facultad, bloque del campus, urgencia y disponibilidad entre clases.
* Flujo de tareas transparente: Gestión de compromisos por estados formales que erradica la informalidad de los acuerdos de palabra.
* Ecosistema cerrado y seguro: Acceso condicionado a la pertenencia institucional (correo @upb.edu.co y autenticación en dos pasos 2FA/TOTP).
* Módulo de reputación intracomunitario: Registro visible de calificaciones y comentarios habilitado exclusivamente tras completar cada encargo.

---

## Arquitectura de Software y Stack Tecnológico

Según el Documento de Diseño Técnico, el sistema adopta una arquitectura de Monolito Simple por capas con Frontend y Backend Desacoplados, estructurado y gestionado mediante Monorepo, comunicados exclusivamente mediante una API REST Stateless:

### Backend (Arquitectura en Capas Limpias - Layered Architecture)
* Tecnología: Spring Boot 4 + Java 21+ (utilizando Virtual Threads para alto rendimiento de concurrencia).
* Controladores (REST Controllers): Exposición de endpoints HTTP, validación con DTOs y control de autenticación JWT.
* Servicios (Service Layer): Lógica y reglas del negocio bajo contexto transaccional.
* Acceso a Datos (Repositories): Spring Data JPA sobre PostgreSQL 16 estructurado en Tercera Forma Normal (3FN).
* Seguridad: Spring Security con autenticación stateless basada en tokens JWT.
* Tiempo Real: WebSockets + STOMP (Spring WebSocket / SockJS) para notificaciones instantáneas de cambio de estado y mensajería en vivo.

### Frontend (Feature-Based Structure)
* Tecnología: React 19 + TypeScript + Vite (renderizado optimizado y tipado estricto).
* Navegación y Estilos: React Router DOM para Single Page Application (SPA) y CSS Modules para encapsulamiento visual sin colisiones globales.
* Consumo de API: Servicios centralizados con Axios e interceptores para inyección automática de cabeceras de autorización JWT.

---

## Estructura del Repositorio

El proyecto cumple estrictamente la distribución exigida para la entrega final:

```text
UPBConecta/
│
├── README.md                          # Información general, arquitectura y guías del proyecto
│
├── docs/                              # Documentación de ingeniería del software
│   ├── Documento de requisitos.pdf    # Elicitación y especificación de requisitos (SRS)
│   ├── Diagrama de casos de uso.png   # Modelo de casos de uso (RF-01 a RF-24)
│   ├── Diagrama de procesos.png       # Modelo de procesos de negocio (BPMN)
│   └── Wireframes.pdf                 # Prototipos de baja y media fidelidad
│
└── app/                               # Código fuente del primer release desarrollado
    ├── backend/                       # API REST Spring Boot (Java 21)
    │   ├── src/main/java/com/upb/conecta/
    │   │   ├── controllers/           # REST Controllers
    │   │   ├── services/              # Lógica de negocio
    │   │   ├── repositories/          # Spring Data JPA
    │   │   └── models/                # Entidades del dominio
    │   └── pom.xml
    └── frontend/                      # Cliente SPA en React 19 + Vite
        ├── src/
        │   ├── components/            # Componentes reutilizables
        │   ├── pages/                 # Vistas (Dashboard, Login, Publish, Detalle)
        │   └── services/              # Clientes de API con Axios
        └── package.json
```

---

## Guía de Instalación y Ejecución del Proyecto

### Requisitos Previos
* Java Development Kit (JDK): Versión 21 o superior.
* Node.js: Versión 20 LTS o superior y gestor de paquetes npm.
* PostgreSQL: Versión 16 instalada y en ejecución (o Docker).
* Git instalado.

---

### Pasos de Puesta en Marcha

#### 1. Clonar el Repositorio
```bash
git clone https://github.com/ilc2308/UPBConecta.git
cd UPBConecta
```

#### 2. Configuración y Ejecución del Backend (Spring Boot)
1. Abrir una terminal en la subcarpeta del backend:
   ```bash
   cd app/backend
   ```
2. Configurar las credenciales de PostgreSQL en `src/main/resources/application.properties`.
3. Compilar y ejecutar la aplicación:
   * En Windows:
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
   * En Linux/macOS:
     ```bash
     ./mvnw spring-boot:run
     ```
   El backend quedará escuchando en `http://localhost:8080`.

#### 3. Configuración y Ejecución del Frontend (React + Vite)
1. Abrir una segunda terminal en la subcarpeta del frontend:
   ```bash
   cd app/frontend
   ```
2. Instalar las dependencias de Node:
   ```bash
   npm install
   ```
3. Iniciar el servidor de desarrollo:
   ```bash
   npm run dev
   ```
   La aplicación web estará disponible en `http://localhost:5173`.

---

## Guía Básica de Uso de la Aplicación

1. Acceso y Registro Institucional:
   * Regístrate utilizando tu cuenta de correo institucional @upb.edu.co, indicando tu nombre, carrera y semestre académico.
   * Inicia sesión para recibir el token de sesión JWT que habilita las funciones de la plataforma.
2. Navegación en el Dashboard Principal:
   * Visualiza el feed cronológico con las publicaciones de ayuda activa y ofertas de servicios.
   * Utiliza la barra de búsqueda y los filtros para ordenar por bloque/ubicación en el campus, remuneración ofrecida o urgencia de la tarea.
3. Publicar un Encargo o Solicitud de Ayuda:
   * Haz clic en "Publicar Trabajo / Encargo".
   * Especifica el nombre de la actividad, descripción detallada, ubicación física precisa (ej. Biblioteca Central - Sala 2), valor acordado y tiempo límite.
   * Confirma el envío para que aparezca inmediatamente en el muro de todos los estudiantes.
4. Postularse a un Encargo:
   * Consulta el detalle del encargo de tu interés y selecciona "Aplicar / Postularse".
   * El solicitante podrá revisar los perfiles de los postulantes con su respectiva calificación y confirmar la asignación del encargado.
5. Cierre del Servicio y Calificación:
   * Una vez entregada la labor en el campus, el solicitante actualiza el estado a Finalizada.
   * El sistema habilita la calificación bidireccional y comentarios, actualizando el puntaje de confianza del perfil.

---

## Enlaces del Proyecto

* Tablero de Jira (Backlog del Proyecto):  
  https://upbconecta.atlassian.net/jira/software/projects/UPBCONECT/boards/2/backlog

* Prototipo de Media Fidelidad (Figma):  
  * Diseño y Wireframes: https://www.figma.com/make/XmldQdk9JJvw7BcyZnOagr/Wireframes-media-fidelidad-UpbConecta?fullscreen=1&t=BuNbJ0x1JjYviIdB-1&code-node-id=0-6  
  * Prototipo Interactivo Navegable: https://www.figma.com/proto/u9yzIadLRLYvwg3TZiPDkd/Untitled?node-id=3-7&t=A2nrPhqUf6FTkIpH-1&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1
