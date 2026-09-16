# Evoria - Gestión de Eventos

Evoria es una aplicación Android para descubrir, crear y administrar eventos. Permite a los usuarios mantener un perfil, publicar eventos, inscribirse, cancelar inscripciones, generar códigos QR y compartir reseñas.

## Características principales

### Cuenta y sesión

- Onboarding inicial.
- Registro e inicio de sesión mediante MockAPI.
- Persistencia de sesión con DataStore.
- Cierre de sesión con confirmación.
- Soporte para tema claro y oscuro.

### Perfil

- Visualización de la foto de perfil y nombre del usuario.
- El correo electrónico permanece disponible dentro de la sección **Mis datos**.
- Edición de nombre, correo, teléfono y ciudad.
- Flujo de actualización en dos pasos:
  - lectura inicial;
  - modo de edición mediante **Actualizar datos**;
  - confirmación antes de **Guardar cambios**.
- Selección de una nueva foto con previsualización local y confirmación antes de subirla.
- Secciones expandibles:
  - **Mis datos**;
  - **Próximos**, con eventos inscritos dentro de los siguientes siete días;
  - **Populares**, con eventos creados por el usuario cuya valoración promedio está entre 4.5 y 5.0.

### Eventos

- Exploración y búsqueda de eventos.
- Creación y edición de eventos.
- Campos para nombre, descripción, fecha, hora, ubicación, categoría y cupos.
- Imagen de portada opcional.
- Validación de campos y de fechas futuras.
- Conservación de la imagen anterior cuando no se selecciona una nueva.
- Eliminación de eventos creados por el usuario.
- Detalle de evento con:
  - imagen de portada;
  - fecha, hora, ubicación, categoría y cupos;
  - usuario organizador;
  - descripción;
  - promedio y listado de reseñas cuando existen.
- Inscripción y cancelación de inscripción.
- Actualización inmediata de cupos y estado visual.
- Generación de códigos QR con enlaces de ubicación.
- Reseñas de eventos finalizados, con calificación de 1 a 5 y comentario.

## Subida de imágenes

Las imágenes no se almacenan dentro de este repositorio ni se agrega como dependencia el repositorio de imágenes. La app utiliza GitHub REST API para comunicarse con:

`https://github.com/Esthefany-Chavez/EvoriaImages`

Se utiliza el endpoint GitHub Contents API sobre:

`https://api.github.com/`

La implementación se encuentra en:

- `GithubApiService`: endpoint Retrofit para crear archivos en GitHub.
- `GithubRetrofitClient`: cliente Retrofit de GitHub.
- `GithubFileModels`: modelos de request y response.
- `ImageRepository`: lectura del `Uri`, validación y subida.

El flujo de subida:

1. Lee el archivo seleccionado mediante `ContentResolver`.
2. Acepta únicamente JPG, JPEG y PNG.
3. Rechaza archivos mayores de 5 MB.
4. Convierte el contenido a Base64.
5. Genera un nombre único con el identificador del usuario o evento y un UUID.
6. Guarda el archivo sin eliminar imágenes anteriores:
   - `users/{userId}_{uuid}.jpg`
   - `events/{eventId}_{uuid}.png`
7. Obtiene la URL pública de GitHub.
8. Guarda esa URL en MockAPI:
   - `User.avatar` para fotos de perfil;
   - `Event.coverImage` para portadas de eventos.

Para eventos nuevos, primero se crea el evento en MockAPI para obtener su ID. Después se sube la imagen y se actualiza el evento con la URL resultante. Al editar un evento, solo se reemplaza `coverImage` cuando se selecciona una nueva imagen.

## Servicios de datos

MockAPI continúa siendo la fuente de datos de la aplicación. Sus recursos principales son:

- `GET /Evento`
- `GET /Evento/{id}`
- `POST /Evento`
- `PUT /Evento/{id}`
- `DELETE /Evento/{id}`
- `GET /user`
- `GET /user/{id}`
- `GET /user?email={email}`
- `POST /user`
- `PUT /user/{id}`
- `DELETE /user/{id}`

Las inscripciones y reseñas se almacenan dentro del objeto `Event`:

- `registrations`: contiene `userId`, `eventId`, fecha e identificador de inscripción.
- `reviews`: contiene `userId`, `eventId`, `rating` y `comment`.

No existe un endpoint separado para cancelar una inscripción. La cancelación utiliza el `PUT` existente del evento, elimina únicamente la inscripción del usuario actual y devuelve un cupo.

## Stack tecnológico

- **Lenguaje:** Kotlin.
- **UI:** Jetpack Compose y Material 3.
- **Arquitectura:** MVVM con `StateFlow`.
- **Persistencia de sesión:** DataStore Preferences.
- **Redes:** Retrofit 2, Gson y OkHttp.
- **Datos de la aplicación:** MockAPI.
- **Imágenes remotas:** Coil mediante `AsyncImage`.
- **Imágenes subidas:** GitHub REST API.
- **Códigos QR:** ZXing.
- **Tipografía:** Tinos.
- **SDK mínimo:** Android API 24.
- **Compile SDK:** Android API 34.

## Identidad visual

La interfaz utiliza una identidad visual sobria basada en color, superficies, espacio y jerarquía:

- **Navy:** `#2F4156`
- **Teal:** `#567C8D`
- **Beige:** `#F5EFE6`
- **Texto secundario:** `#7A8F9E`
- Fondos claros y superficies diferenciadas.
- Bordes redondeados y elevaciones sutiles.
- Componentes consistentes entre Login, Registro, Perfil, Crear Evento, Editar Evento y Detalle de Evento.
- Adaptación al tema claro y oscuro.

Las interfaces de Crear Evento y Editar Evento comparten la misma estructura visual y mantienen el flujo existente de validación, carga y subida de imágenes.

## Arquitectura y estructura

```text
app/src/main/java/com/example/p3/
├── data/
│   ├── api/
│   │   ├── ApiService.kt
│   │   ├── GithubApiService.kt
│   │   ├── GithubRetrofitClient.kt
│   │   └── RetrofitClient.kt
│   ├── model/
│   │   ├── Event.kt
│   │   ├── GithubFileModels.kt
│   │   └── User.kt
│   ├── repository/
│   │   ├── EventRepository.kt
│   │   ├── ImageRepository.kt
│   │   └── UserRepository.kt
│   └── session/
│       └── SessionManager.kt
├── ui/
│   ├── screens/
│   │   ├── EventScreens.kt
│   │   ├── LoginScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   └── RegisterScreen.kt
│   ├── theme/
│   └── viewmodel/
│       ├── EventViewModel.kt
│       └── UserViewModel.kt
└── MainActivity.kt
```

El flujo principal conserva la arquitectura:

`UI -> ViewModel -> Repository -> API`

## Configuración local

1. Clona el repositorio:

   ```bash
   git clone https://github.com/SalomeGarcia2006/EvoriaApp.git
   ```

2. Abre el proyecto en Android Studio.
3. Sincroniza Gradle.
4. Crea o modifica `local.properties` en la raíz del proyecto:

   ```properties
   github.token=TU_TOKEN_DE_GITHUB
   ```

5. Ejecuta la aplicación en un dispositivo o emulador con Android 7.0 (API 24) o superior.

El token se lee durante la configuración de Gradle y se expone únicamente al código de la aplicación mediante `BuildConfig.GITHUB_TOKEN`. `local.properties` está excluido de Git y el token no debe escribirse directamente en archivos Kotlin ni imprimirse en logs.

El token debe ser un Fine-grained Personal Access Token con permiso de escritura sobre el contenido del repositorio `Esthefany-Chavez/EvoriaImages`. Aunque no se publica en el repositorio, cualquier secreto incluido en una aplicación móvil compilada puede ser extraído; para producción se recomienda mover la subida a un backend seguro.

## Compilación

Para generar la versión debug:

```bash
./gradlew.bat :app:assembleDebug
```

En Windows también puede ejecutarse:

```powershell
.\gradlew.bat :app:assembleDebug
```

---

Desarrollado como una solución integral para la gestión de eventos y comunidades.
