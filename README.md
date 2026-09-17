# 🎉 EVORIA

> 💡 **Plataforma móvil integral para la creación, gestión y descubrimiento de eventos.**

EVORIA es una aplicación Android moderna diseñada para centralizar la planificación de eventos, la coordinación de recursos y la interacción entre organizadores y asistentes. Desde la bienvenida hasta la calificación post-evento, ofrece una experiencia fluida y profesional.

---

## 👋 Experiencia de Bienvenida (Onboarding)

La aplicación implementa un flujo de bienvenida obligatorio de **3 páginas** diseñado para introducir al usuario en la plataforma:

*   **Navegación Inteligente:** Proceso guiado mediante un `HorizontalPager`. Se ha eliminado el botón "Omitir" para asegurar que los nuevos usuarios conozcan las capacidades clave.
*   **Acceso Rápido:** La primera página incluye el enlace **"¿Ya tienes una cuenta? Inicia sesión"** para usuarios recurrentes.
*   **Conversión:** La última página concluye con el botón **"Registrarse"** para nuevos miembros.

## 🔐 Autenticación y Sesión

EVORIA garantiza una gestión de identidad robusta y persistente:

*   **Registro de Usuarios:** Formulario detallado que captura nombre, correo, contraseña, teléfono y ciudad, almacenando los datos en **MockAPI**.
*   **Login Seguro:** Validación de credenciales en tiempo real contra el servidor.
*   **Persistencia (DataStore):** Utiliza **Jetpack DataStore Preferences** para almacenar el `user_id` y el estado del onboarding.
*   **Comportamiento de Inicio:** Si el usuario tiene una sesión activa, la aplicación omite el Onboarding y el Login, navegando directamente al **Home**.
*   **Cierre de Sesión:** Al cerrar sesión, se eliminan los datos locales de DataStore y se reinicia el flujo desde la primera página del Onboarding.

## 👤 Perfil del Usuario

Un centro de control personalizable para cada miembro:

*   **Gestión de Datos:** Visualización y edición de nombre, correo, teléfono y ciudad.
*   **Foto de Perfil:** Carga dinámica de imágenes desde la galería del dispositivo, con almacenamiento persistente en la nube.
*   **Secciones de Actividad:**
    *   **⭐ Populares:** Muestra eventos creados por el usuario con una valoración promedio entre **4.5 y 5.0**.
    *   **📅 Próximos:** Lista de eventos en los que el usuario está inscrito que ocurrirán en los **siguientes 7 días**.
*   **Confirmación de Cambios:** Diálogos de seguridad para guardar datos o actualizar la foto de perfil.

## 🎫 Gestión de Eventos

El núcleo funcional de la aplicación permite un control total sobre las actividades:

*   **CRUD Completo:** Creación, lectura, edición y eliminación de eventos por parte de sus organizadores.
*   **Detalle de Eventos:** Información exhaustiva que incluye descripción, ubicación, cronograma, categoría y cupos disponibles en tiempo real.
*   **Inscripción Dinámica:** Registro de asistentes con actualización automática de cupos disponibles y opción de desinscripción.
*   **Búsqueda y Exploración:** Pantalla dedicada para filtrar eventos por **nombre, categoría o fecha** de forma reactiva.
*   **⭐ Reseñas y Calificaciones:** Sistema de feedback para eventos ya finalizados, permitiendo una valoración de 1 a 5 estrellas y comentarios.
*   **📍 Código QR:** Generación instantánea de códigos QR basados en la ubicación del evento, vinculando directamente a **Google Maps**.
*   **🔔 Notificaciones:** El creador recibe un aviso local cuando otro usuario se inscribe en uno de sus eventos.
*   **📄 Reportes PDF:** El creador puede generar un PDF organizado con los datos del evento y la lista de asistentes inscritos.

## 🖼️ Gestión de Imágenes (GitHub API)

EVORIA utiliza una arquitectura innovadora para el manejo de archivos multimedia:

*   **Almacenamiento:** Integración con el repositorio `Esthefany-Chavez/EvoriaImages` mediante **GitHub REST API**.
*   **Organización:** Las imágenes se clasifican automáticamente en las rutas `/users` y `/events`.
*   **Especificaciones Técnicas:**
    *   Formatos permitidos: **JPG, JPEG, PNG**.
    *   Límite de tamaño: **5 MB** por archivo.
    *   Seguridad: Los nombres de archivo son únicos (ID + UUID) y se codifican en **Base64** para la subida.
*   **URLs persistentes:** Las rutas resultantes se almacenan en MockAPI para su consumo global.

## 🌐 APIs y Servicios

| Servicio | Propósito | Principales Endpoints |
| :--- | :--- | :--- |
| **MockAPI** | Persistencia de datos de negocio | `GET /Evento`, `POST /user`, `PUT /Evento/{id}` |
| **GitHub REST API** | Almacenamiento de multimedia | `PUT /repos/.../contents/{path}` |

## 🏗️ Arquitectura

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)**, asegurando un código limpio, testeable y mantenible:

```text
UI (Jetpack Compose) 
     ↓
ViewModel (StateFlow & Coroutines)
     ↓
Repository (Abstracción de datos)
     ↓
API (Retrofit / OkHttp)
```

*   **UI:** Componentes declarativos y reactivos.
*   **ViewModel:** Gestión del estado de la pantalla y lógica de negocio.
*   **Repository:** Centraliza el acceso a datos desde la API y DataStore.

## 🛠️ Tecnologías Principales

*   **Lenguaje:** Kotlin (Coroutines & Flows).
*   **UI:** Jetpack Compose con Material 3.
*   **Redes:** Retrofit 2 & OkHttpClient (con Logging Interceptor).
*   **Imágenes:** Coil (Carga asíncrona y caché eficiente).
*   **Persistencia:** DataStore Preferences.
*   **Utilidades:** ZXing (Generación de QR), Gson (Serialización), `PdfDocument` (reportes PDF).

## 🔔 Notificaciones y reportes

Las notificaciones de nuevas inscripciones, reseñas y recordatorios se almacenan localmente mediante DataStore. Al cargar la lista de eventos, EVORIA sincroniza los avisos derivados de las inscripciones y reseñas que existen en MockAPI, y crea un recordatorio cuando un evento inscrito comienza dentro de la próxima hora. Esta solución funciona al abrir o actualizar la aplicación; no reemplaza las notificaciones push de un backend.

Desde el detalle de un evento propio, el organizador puede seleccionar **Generar PDF del evento** y elegir dónde guardar un reporte con la marca EVORIA, la información principal del evento y los asistentes inscritos con sus datos disponibles.

## 🎨 Identidad Visual

*   **Paleta de Colores:**
    *   **Navy (`#2F4156`):** Elegancia y profesionalismo.
    *   **Teal (`#567C8D`):** Modernidad y tecnología.
    *   **Beige (`#F5EFE6`):** Equilibrio y calidez.
*   **Tipografía:** Fuente **Tinos** (Serif) para un estilo editorial y legible.
*   **Interfaz:** Fondos con gradientes radiales y lineales, tarjetas con elevación y componentes redondeados.

## ⚙️ Configuración Local

Para habilitar temporalmente la subida de imágenes durante la demostración, el proyecto requiere un token personal de GitHub:

1.  Crea un archivo `local.properties` en la raíz del proyecto (si no existe).
2.  Agrega la siguiente línea con tu token (con permisos de `repo` o `contents`):
    ```properties
    github.token=TU_TOKEN_AQUÍ
    ```
3.  **Seguridad:** El archivo `local.properties` está excluido de Git por defecto. Nunca compartas ni subas tu token real.

> **Importante:** el token se incorpora al APK mediante `BuildConfig`, por lo que esta integración es únicamente para desarrollo/presentación. Revoca inmediatamente cualquier token que haya sido compartido públicamente y no uses un token con permisos superiores a los estrictamente necesarios. Para una versión de producción, la subida debe pasar por un backend y el token nunca debe llegar a la aplicación.

## 🚀 Ejecución y Compilación

Para compilar el proyecto en modo depuración:
```bash
./gradlew assembleDebug
```

Para ejecutar las pruebas unitarias:
```bash
./gradlew test
```

Requisitos mínimos: **Android 7.0 (API 24)**.

---
© 2026 EVORIA - Gestión Profesional de Eventos.
