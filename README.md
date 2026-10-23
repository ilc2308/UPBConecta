# UPB Conecta

Plataforma web de microeconomía colaborativa (P2P) y red de apoyo diseñada para la comunidad de la **Universidad Pontificia Bolivariana (UPB)**, que conecta a estudiantes que requieren ayuda, favores logísticos o tutorías en el campus con compañeros dispuestos a brindar su colaboración a cambio de una retribución acordada.

---

## 👥 Integrantes del Equipo
* **Ismael López Cardozo**
* **María Fernanda Toro Torres**
* **Thomas Sining Salazar**
* **Fredy Guzmán**
* **Tatiana María Ramírez Arroyave**

* **Docente:** María Paula Flórez Vargas  
* **Asignatura:** Ingeniería de Software  
* **Institución:** Universidad Pontificia Bolivariana (Medellín, 2026)

---

## 📌 Descripción General del Producto
**UPB Conecta** es una solución digital que formaliza, centraliza y dinamiza la oferta y demanda de servicios estudiantiles dentro de la universidad. La aplicación permite a los estudiantes solicitantes publicar encargos de manera ágil con especificaciones claras (ubicación exacta en el campus, remuneración fija y nivel de urgencia), y a los estudiantes ejecutantes postularse, gestionar su tiempo libre entre clases, asegurar acuerdos transparentes y construir una reputación verificable mediante valoraciones recíprocas.

---

## 💡 Contexto del Problema y Propuesta de Solución

### Contexto del Problema
En el día a día universitario, los estudiantes acuden a canales informales (grupos masivos de WhatsApp, canales de Telegram o historias de Instagram) para resolver necesidades cotidianas, desde el préstamo de materiales e impresiones de última hora, hasta tutorías académicas o relevos logísticos. Esta dinámica informal acarrea serias dificultades:
* **Pérdida de visibilidad:** Las peticiones se extravían con rapidez ante la avalancha de mensajes en chats grupales.
* **Incertidumbre e informalidad:** No existe certidumbre respecto al cumplimiento de los encargos ni garantía del pago acordado entre desconocidos.
* **Falta de seguimiento del encargo:** Ausencia de un flujo que registre en qué estado se encuentra la labor (`Publicada` → `Asignada` → `En Proceso` → `Finalizada`).
* **Dependencia de círculos cerrados:** Quienes no poseen una amplia red de contactos dentro del campus tienen menor probabilidad de encontrar u ofrecer ayuda a tiempo.

### Propuesta de Solución
**UPB Conecta** transforma esta interacción caótica en un entorno digital estructurado, seguro y confiable mediante:
1. **Muro cronológico y filtros por campus:** Búsqueda ágil de publicaciones activas organizadas de forma cronológica, con filtros por ubicación física precisa (bloques, cafeterías, biblioteca) y nivel de urgencia (*Normal* o *Urgente*).
2. **Ciclo de vida supervisado:** Trazabilidad paso a paso del estado de cada encargo, permitiendo formalizar los tratos y controlar los compromisos de entrega.
3. **Confianza institucional y reputación:** Validación de identidad mediante correo institucional (`@upb.edu.co`), mostrando carrera y semestre con un sistema transparente de calificaciones y comentarios públicos.

---

## 🏗️ Arquitectura de Software Seleccionada
Para el desarrollo de este primer release se seleccionó una **Arquitectura en Capas basada en el patrón Modelo - Vista - Controlador (MVC)**:
* **Modelo (`app/models.py`):** Define las entidades del dominio (`User`, `Task`, `Postulation`) y las reglas de integridad y persistencia con SQLAlchemy y SQLite.
* **Vista (`app/templates/` y `app/static/`):** Vistas dinámicas en HTML5 (Jinja2) y estilos CSS responsivos para computador y dispositivos móviles.
* **Controlador (`app/app.py`):** Lógica de negocio, enrutamiento web, validación de sesiones con Flask-Login y control de cambios de estado de los encargos.

---

## 🚀 Guía de Instalación y Ejecución del Proyecto

### Requisitos Previos
* **Python 3.10** o superior instalado en el equipo.
* **Git** instalado.

### Pasos para la Puesta en Marcha
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/ilc2308/UPBConecta.git
   cd UPBConecta/app
   ```

2. **Crear y activar un entorno virtual de Python:**
   * **En Windows (PowerShell):**
     ```powershell
     python -m venv venv
     .\venv\Scripts\activate
     ```
   * **En Linux / macOS:**
     ```bash
     python3 -m venv venv
     source venv/bin/activate
     ```

3. **Instalar las dependencias del proyecto:**
   ```bash
   pip install -r requirements.txt
   ```

4. **Ejecutar la aplicación:**
   ```bash
   python app.py
   ```

5. **Abrir la plataforma:**
   Ingresa desde tu navegador web a la siguiente dirección:  
   👉 `http://127.0.0.1:5000`

---

## 📖 Guía Básica de Uso de la Aplicación

1. **Registro e Inicio de Sesión:**
   * Ingresa a la opción **"Registrarse"** e introduce tu nombre, correo institucional de la universidad (`@upb.edu.co`), carrera, semestre y contraseña.
   * Inicia sesión con tus credenciales institucionales.
2. **Explorar el Muro de Encargos:**
   * En el tablero principal podrás consultar todas las solicitudes abiertas ordenadas por fecha de creación, identificando de inmediato su ubicación dentro del campus, el pago ofrecido y el nivel de urgencia.
3. **Publicar un Encargo:**
   * Si requieres apoyo, haz clic en **"Publicar Encargo"**, llena los datos del formulario (título, descripción detallada, ubicación en campus, valor ofrecido y plazo) y presiona **"Publicar"**.
4. **Postularse a un Encargo:**
   * Si encuentras una solicitud que se ajusta a tus tiempos libres, presiona **"Aceptar / Postularse"**. El encargo pasará al estado `Asignado`.
5. **Finalización y Calificación:**
   * Al completarse la tarea, el solicitante la marca como `Finalizada`, permitiendo calificar el servicio y mantener un ecosistema confiable entre compañeros.

---

## 🔗 Enlaces del Proyecto

* 📌 **Tablero de Jira:**  
  [https://upbconecta.atlassian.net/jira/software/projects/UPBCONECT/boards/2/backlog](https://upbconecta.atlassian.net/jira/software/projects/UPBCONECT/boards/2/backlog)

* 🎨 **Prototipo de media fidelidad (Figma):**  
  [https://www.figma.com](https://www.figma.com) *(Enlace del prototipo de media fidelidad en Figma)*
