# EvoriaApp - Gestión de Eventos

EvoriaApp es una aplicación Android moderna diseñada para la gestión y descubrimiento de eventos. Permite a los usuarios explorar, inscribirse y calificar eventos, así como a los organizadores crear y administrar sus propias actividades.

## 🚀 Características Principales

- **Autenticación de Usuarios:** Registro e inicio de sesión seguro.
- **Gestión de Perfil:** Actualización de datos personales y foto de perfil.
- **Exploración de Eventos:** Pantalla de inicio con tarjetas visuales tipo poster para una navegación intuitiva.
- **Detalle de Eventos:** Información completa, incluyendo ubicación, fecha, hora, cupos disponibles y reseñas.
- **Inscripción:** Los usuarios pueden unirse a eventos con un solo clic.
- **Creación y Edición:** Herramientas para que los creadores gestionen sus eventos (incluyendo carga de imágenes).
- **Sistema de Reseñas:** Calificación y comentarios post-evento.
- **Mis Eventos:** Sección dedicada para ver eventos creados e inscritos.

## 🛠️ Stack Tecnológico

- **Lenguaje:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) con **Material 3**.
- **Arquitectura:** MVVM (Model-View-ViewModel) con StateFlow para manejo de estado reactivo.
- **Networking:** [Retrofit](https://square.github.io/retrofit/) + OkHttp + Gson para consumo de APIs REST.
- **Carga de Imágenes:** [Coil](https://coil-kt.github.io/coil/) para manejo eficiente de imágenes remotas.
- **Persistencia Local:** [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) para gestión de sesiones.
- **Navegación:** Compose Navigation.
- **Diseño Visual:**
    - Fuente personalizada: **Tinos** (Regular, Bold, Italic).
    - Paleta de colores personalizada basada en tonos azules y grises profesionales.

## 📁 Estructura del Proyecto

```text
app/src/main/java/com/example/p3/
├── data/
│   ├── api/          # Interfaces de Retrofit
│   ├── model/        # Clases de datos (Event, User, Registration, Review)
│   ├── repository/   # Lógica de acceso a datos
│   └── session/      # Gestión de sesión de usuario (DataStore)
├── ui/
│   ├── screens/      # Pantallas de la aplicación (Compose)
│   ├── theme/        # Configuración de Material 3 (Color, Type, Theme)
│   └── viewmodel/    # Lógica de negocio y estado de la UI
└── MainActivity.kt   # Punto de entrada y configuración de navegación
```

## 🎨 Diseño Visual

La aplicación utiliza un diseño limpio y moderno con tarjetas de eventos optimizadas:
- **EventCard:** Diseño horizontal con imagen a la izquierda y jerarquía de texto clara a la derecha.
- **Tipografía:** Implementación global de la fuente **Tinos** para un aspecto editorial y profesional.
- **Tematización:** Soporte dinámico para contrastes y colores de contenedor que mejoran la legibilidad.

## 📦 Instalación y Requisitos

1. Clonar el repositorio.
2. Abrir con **Android Studio Jellyfish** o superior.
3. Asegurarse de tener configurado el SDK 34 (Android 14).
4. Sincronizar Gradle y ejecutar en un emulador o dispositivo físico (Min SDK 24).

---
Desarrollado como parte del proyecto P3 para gestión de eventos comunitarios.
