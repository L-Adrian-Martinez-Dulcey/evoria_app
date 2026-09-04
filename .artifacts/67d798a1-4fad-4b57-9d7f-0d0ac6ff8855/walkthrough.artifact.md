# Sincronización con MockAPI (Recurso `Evento`) Finalizada

Se han realizado los ajustes necesarios para que la aplicación se comunique correctamente con el recurso `Evento` en MockAPI, solucionando el error HTTP 400.

## Cambios realizados

### API y Red
- **[ApiService.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/data/api/ApiService.kt)**: Se actualizó el endpoint a `Evento` (respetando la mayúscula indicada).

### Modelo de Datos
- **[Event.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/data/model/Event.kt)**: Se mapearon los campos para coincidir con el esquema de MockAPI:
    - `title` -> `@SerializedName("name")`
    - `coverImage` -> `@SerializedName("avatar")`
    - `description` y `id` se mantuvieron igual.

## Ajustes de UI
- **[EventScreens.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/EvoriaApp/app/src/main/java/com/example/p3/ui/screens/EventScreens.kt)**: Se aumentó significativamente el tamaño del título en la tarjeta del evento (`headlineMedium`) para darle mayor importancia visual.

## Verificación
- **Compilación**: Exitosa.
- **Configuración**: Se validó que el endpoint coincide con `https://6a8ee8baa12b7de8cc0f2245.mockapi.io/Evento`.

> [!IMPORTANT]
> Al guardar un nuevo evento, MockAPI ahora recibirá el título en el campo `name` y la imagen en `avatar`. Los demás campos (fecha, hora, etc.) se enviarán pero solo se almacenarán si MockAPI los permite en su configuración.

Ya puedes probar la creación y visualización de eventos en la aplicación.
