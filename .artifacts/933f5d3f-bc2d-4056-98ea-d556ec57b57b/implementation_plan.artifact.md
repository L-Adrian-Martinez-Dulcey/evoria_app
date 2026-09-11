# Fix Redirection to RegisterScreen from Onboarding

The app incorrectly navigates to `LoginScreen` instead of `RegisterScreen` when clicking "Crear cuenta" in the onboarding flow. This happens because the `NavHost` recomposes and resets its `startDestination` to `"login"` as soon as the onboarding completion flag is updated in DataStore.

## Proposed Changes

### Core / Navigation

#### [MODIFY] [MainActivity.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/MainActivity.kt)

- **Stabilize `startDestination`**: Wrap the `startDestination` logic in a `remember` block tied to the `sessionChecked` state. This ensures the `NavHost` graph doesn't reset its identity mid-navigation when `onboardingDone` or `user` state changes during the same app session.

## Verification Plan

### Automated Tests
- Build the project: `./gradlew :app:assembleDebug`

### Manual Verification
1. **Fresh Install**: Open the app -> see Onboarding.
2. **Redirection**: Go to the last onboarding page and click "Crear cuenta".
3. **Outcome**: The app must navigate to **RegisterScreen**. It should **not** jump to the Login screen.
4. **Persistence**: Close and reopen the app. It should now start at **LoginScreen** (since onboarding was marked as done).
