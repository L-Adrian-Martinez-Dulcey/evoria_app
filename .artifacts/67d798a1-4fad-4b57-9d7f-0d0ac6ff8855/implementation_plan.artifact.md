# Plan de Documentación Visual para Exposición (Persona 3)

El objetivo es añadir comentarios técnicos descriptivos en los archivos clave del proyecto para facilitar la explicación del Stack Tecnológico, Arquitectura (MVVM) y Trabajo Colaborativo durante la exposición.

## User Review Required

> [!NOTE]
> Estos comentarios están redactados de forma profesional y técnica. Se han colocado en puntos estratégicos donde la audiencia suele prestar atención al revisar código fuente.

## Proposed Changes

### [Component Name] Stack Tecnológico y Configuración

#### [MODIFY] [build.gradle.kts](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/build.gradle.kts)
- Comentar bloques de dependencias: Material 3, Retrofit (REST), Coil (Imágenes), DataStore (Persistencia) y ZXing (QR).

#### [MODIFY] [Theme.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/ui/theme/Theme.kt), [Color.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/ui/theme/Color.kt), [Type.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/ui/theme/Type.kt)
- Explicar la implementación de Material Design 3, soporte para temas dinámicos y tipografía personalizada (Tinos).

### [Component Name] Arquitectura y Lógica (MVVM)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/MainActivity.kt)
- Documentar el uso de **Navigation Compose** para el control declarativo de rutas.

#### [MODIFY] [EventViewModel.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/ui/viewmodel/EventViewModel.kt)
- Resaltar el patrón **MVVM**, el uso de `StateFlow` para estados reactivos y **Corrutinas** para llamadas asíncronas seguras.

#### [MODIFY] [ApiService.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/data/api/ApiService.kt)
- Comentar la definición de la interfaz para **Retrofit** y la comunicación con servicios REST.

### [Component Name] UI y Funcionalidades

#### [MODIFY] [EventScreens.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/ui/screens/EventScreens.kt)
- Comentar componentes de Jetpack Compose, integración de **Coil** y generación de **QR**.

## Verification Plan

### Automated Tests
- Ejecutar `gradle assembleDebug` para asegurar que los comentarios no introdujeron errores de sintaxis (altamente improbable).

### Manual Verification
- Abrir los archivos modificados y verificar que los comentarios sean claros y sirvan como apoyo para el guion de la exposición.
