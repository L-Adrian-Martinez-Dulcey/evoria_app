# Visualización de Cupos Disponibles en Detalle

He implementado un indicador visual para los cupos disponibles dentro de la pantalla de detalle del evento. Esto permite que los usuarios vean rápidamente la disponibilidad antes de inscribirse.

## Cambios Realizados

### UI y Diseño
- **[EventScreens.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/ui/screens/EventScreens.kt)**:
    - Se agregó un "Chip" estilizado dentro de la tarjeta de información principal de `EventDetailScreen`.
    - **Lógica de Colores**:
        - **Verde**: Si hay cupos disponibles (ej. "15 cupos").
        - **Rojo**: Si el evento está lleno ("Sin cupos").
    - Se organizó el diseño usando una `Row` para que la categoría y los cupos aparezcan uno al lado del otro de forma elegante.

## Verificación

### Manual
1.  **Con cupos**: Al abrir un evento con disponibilidad, aparece un indicador verde con el número de cupos.
2.  **Sin cupos**: Si los cupos llegan a 0, el indicador cambia a rojo con el texto "Sin cupos".
3.  **Coherencia**: El estilo es idéntico al utilizado en la pantalla de búsqueda para mantener la unidad visual de la marca **EVORIA**.

render_diffs(file:///C:/EvoriaApp/app/src/main/java/com/example/p3/ui/screens/EventScreens.kt)
