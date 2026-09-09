# Plan para Corregir el Error de Conexión en el Ingreso

El usuario reporta un "Error de conexión" al intentar ingresar. Este error es capturado de forma genérica en el `UserViewModel`, lo que oculta la causa real (404, error de red, timeout, etc.).

## User Review Required

> [!IMPORTANT]
> El error "Error de conexión" es un mensaje genérico. Para diagnosticarlo mejor, primero mejoraremos la visibilidad del error real. Es posible que el endpoint `user` no exista o que la URL base de MockAPI haya cambiado.

## Proposed Changes

### [Componente de Datos]

#### [MODIFY] [ApiService.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/data/api/ApiService.kt)
- Verificar si el recurso debe ser `users` (plural) en lugar de `user` (singular), ya que es el estándar de MockAPI.

### [Componente de UI/Lógica]

#### [MODIFY] [UserViewModel.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/ui/viewmodel/UserViewModel.kt)
- Mejorar el manejo de excepciones en `loginUser` y `restoreSession` para mostrar el mensaje de error real (`e.message`) o al menos diferenciar entre error de red y otros.
- Añadir estado de carga (`isLoading`) durante el proceso de login.

## Verification Plan

### Manual Verification
1. Intentar el login y observar si el mensaje de error cambia a algo más descriptivo (ej. "HTTP 404 Not Found").
2. Si es un 404, se procederá a corregir el nombre del recurso en `ApiService.kt`.
3. Si es un error de red, se verificará la `BASE_URL` en `RetrofitClient.kt`.
