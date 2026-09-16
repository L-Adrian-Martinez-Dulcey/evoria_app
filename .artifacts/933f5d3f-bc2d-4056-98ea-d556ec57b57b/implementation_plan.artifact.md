# Documentación Integral del Proyecto EVORIA

Este plan detalla la actualización completa del archivo `README.md` para reflejar el estado actual y real del desarrollo de EVORIA, basado en el análisis profundo del código fuente y la configuración técnica.

## User Review Required

> [!IMPORTANT]
> - El README documentará el uso de **GitHub REST API** para el almacenamiento de imágenes en el repositorio `Esthefany-Chavez/EvoriaImages`, un componente crítico del proyecto.
> - Se incluirá la guía de configuración de `local.properties` para el token de GitHub, asegurando que el flujo de subida de imágenes funcione correctamente para nuevos desarrolladores.

## Proposed Changes

### Documentación

#### [MODIFY] [README.md](file:///C:/EvoriaApp/README.md)
- **Introducción**: Descripción profesional de EVORIA como plataforma de gestión de eventos.
- **✨ Características**:
    - **👋 Onboarding**: Flujo de 3 pasos con navegación inteligente y persistencia.
    - **🔐 Autenticación**: Registro robusto y Login con persistencia mediante DataStore.
    - **👤 Perfil**: Gestión integral de datos de usuario y personalización visual.
    - **🎫 Gestión de Eventos**: CRUD completo, administración de cupos y registro de asistentes.
    - **⭐ Reseñas**: Sistema de retroalimentación para eventos finalizados.
    - **📍 Código QR**: Generación dinámica para ubicación en Google Maps.
- **🖼️ Gestión de Imágenes**: Documentación técnica del repositorio de imágenes en GitHub y el flujo de carga.
- **🌐 APIs y Servicios**: Detalle de endpoints en MockAPI y GitHub API.
- **🏗️ Arquitectura**: Explicación del flujo de datos entre capas (UI -> ViewModel -> Repository -> API).
- **🛠️ Tecnologías**: Listado fiel a las dependencias reales del proyecto.
- **🎨 Identidad Visual**: Documentación del diseño basado en Material 3 y la paleta de colores Navy/Teal/Beige.
- **⚙️ Configuración**: Instrucciones para la puesta en marcha local.

## Verification Plan

### Manual Verification
- Comprobar que los campos de modelos descritos (`User`, `Event`) coinciden exactamente con la implementación.
- Validar que los comandos de ejecución documentados funcionan en un entorno estándar.
- Confirmar que no se expone información sensible en los ejemplos de configuración.
