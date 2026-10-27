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
