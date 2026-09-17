# Fix Missing Available Slots in Event Detail

The "Available Slots" (cupos disponibles) information is not being displayed in the `EventDetailScreen`, even though the logic to track them exists in the backend and ViewModel. This plan adds a visual indicator for the remaining slots.

## Proposed Changes

### UI Screens

#### [MODIFY] [EventScreens.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/ui/screens/EventScreens.kt)

- **Add Slots Indicator**: Inside `EventDetailScreen`, add a new `EventInfoLine` or a styled chip to display the `availableSlots` property of the event.
- **Visual Style**: Use a colored chip (similar to the one in search results) to make the availability status clear (e.g., green for available, red for sold out).
- **Placement**: Place the slots information within the main info card, alongside the date, location, and category.

## Verification Plan

### Manual Verification
1. Open any event from the Home or Search screen.
2. Verify that a new section or line appears showing the "Cupos disponibles".
3. Verify that if an event has 0 slots, it shows a "Sin cupos" message in a different color.
