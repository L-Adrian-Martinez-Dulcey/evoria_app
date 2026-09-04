# Plan para Sincronizar con Recurso `Evento` en MockAPI

El error HTTP 400 se debe a una discrepancia entre el nombre del recurso en la API y los nombres de los campos enviados. Según tu indicación, el recurso se llama `Evento` y tiene los campos `id`, `name`, `avatar` y `description`.

## User Review Required

> [!IMPORTANT]
> Dado que MockAPI solo tiene los campos `id`, `name`, `avatar` y `description`, el resto de la información del evento (fecha, hora, lugar, etc.) **no se guardará en el servidor** a menos que agregues esos campos manualmente en la configuración de MockAPI.
> Por ahora, mapearemos los campos principales para que la aplicación funcione y deje de dar el error 400.

## Proposed Changes

### [Component Name] Data & API

#### [MODIFY] [ApiService.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/data/api/ApiService.kt)
- Cambiar el endpoint de `event` a `Evento` (respetando la mayúscula).

#### [MODIFY] [Event.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/data/model/Event.kt)
- Mapear `title` a `name` usando `@SerializedName("name")`.
- Mapear `coverImage` a `avatar` usando `@SerializedName("avatar")`.
- Mantener `id` y `description` como están.

## Verification Plan

### Automated Tests
- Compilar la aplicación para verificar que los cambios no rompan la lógica interna.

### Manual Verification
- Intentar guardar un evento. Al usar el endpoint `Evento` y los nombres de campos correctos (`name` y `avatar`), el servidor debería responder con un HTTP 201 (Creado) en lugar de un 400.
