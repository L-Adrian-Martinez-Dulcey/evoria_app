# Evoria - Gestión de Eventos 📅

Evoria es una aplicación Android moderna diseñada para centralizar la planificación, descubrimiento y gestión de eventos. Desde la coordinación de recursos hasta la inscripción de asistentes, Evoria ofrece una experiencia fluida y elegante para organizadores y participantes.

## 🚀 Características Principales

- **Experiencia de Bienvenida:** Proceso de **Onboarding** dinámico que introduce las capacidades de la plataforma.
- **Autenticación y Sesión:** Sistema de login seguro con persistencia de sesión mediante **DataStore**.
- **Gestión de Perfil:** Personalización completa de datos, incluyendo carga de fotos de perfil.
- **Exploración Visual:** Pantalla de inicio con tarjetas tipo poster diseñadas para una navegación intuitiva y visualmente atractiva.
- **Generación de Códigos QR:** Generación instantánea de códigos QR para la ubicación de eventos, integrando enlaces directos a **Google Maps**.
- **Detalle de Eventos:** Información exhaustiva: ubicación, cronograma, cupos en tiempo real y sistema de reseñas.
- **Gestión Integral:** Herramientas para creadores que permiten subir imágenes, definir categorías y administrar la disponibilidad de cupos.
- **Inscripción y Feedback:** Inscripción con un clic y sistema de calificación post-evento.

## 🛠️ Stack Tecnológico

- **Lenguaje:** [Kotlin](https://kotlinlang.org/) (Coroutines & Flows)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) con **Material 3**.
- **Arquitectura:** MVVM (Model-View-ViewModel) con StateFlow.
- **Redes:** [Retrofit 2](https://square.github.io/retrofit/) & OkHttp para integración con API (MockAPI).
- **Imágenes:** [Coil](https://coil-kt.github.io/coil/) para carga asíncrona y caché de imágenes.
- **Utilidades:** 
    - **ZXing:** Para la generación de códigos QR de ubicación.
    - **DataStore:** Para gestión de preferencias y sesiones.
- **Tipografía:** Fuente **Tinos** (Google Fonts) en todas sus variantes para un estilo profesional.

## 🎨 Identidad Visual

Evoria utiliza una paleta de colores sofisticada diseñada para transmitir confianza y elegancia:
- **Navy (#2F4156):** Autoridad y profesionalismo.
- **Teal (#567C8D):** Modernidad y tecnología.
- **Beige (#F5EFE6):** Calidez y equilibrio.
- **Gradientes:** Fondos radiales y lineales que aportan profundidad a la interfaz.

## 📁 Estructura del Proyecto

```text
app/src/main/java/com/example/p3/
├── data/
│   ├── api/          # Cliente Retrofit e interfaces de servicio
│   ├── model/        # Modelos (Event, User, Registration, Review)
│   ├── repository/   # Repositorios de datos
│   └── session/      # SessionManager con DataStore
├── ui/
│   ├── screens/      # Pantallas (Onboarding, Login, EventScreens, etc.)
│   ├── theme/        # Tematización M3 (Color, Typography, Theme)
│   └── viewmodel/    # ViewModels para gestión de estado
└── MainActivity.kt   # Router y punto de entrada
```

## 📦 Instalación y Uso

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/SalomeGarcia2006/EvoriaApp
   ```
2. Abrir el proyecto en **Android Studio** (Koala o superior).
3. Sincronizar Gradle para descargar las dependencias.
4. Ejecutar en un emulador o dispositivo físico con **Android 7.0 (API 24)** o superior.

---
Desarrollado como una solución integral para la gestión de eventos modernos.

## Configuración de subida de imágenes

La app sube las imágenes mediante GitHub REST API al repositorio externo
`Esthefany-Chavez/EvoriaImages`. Antes de compilar, agrega localmente en
`local.properties`:

```properties
github.token=TU_TOKEN_DE_GITHUB
```

El archivo `local.properties` está excluido de Git. El token debe ser un Fine-grained
Personal Access Token con acceso de escritura únicamente al contenido de ese repositorio.
