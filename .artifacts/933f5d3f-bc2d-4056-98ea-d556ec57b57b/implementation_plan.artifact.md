# Fix App Build and Navigation (Onboarding Flow)

The app currently fails to build due to syntax errors in `OnboardingScreen.kt`. Additionally, the newly created `OnboardingScreen` is not integrated into the application's navigation flow, making it unreachable.

## User Review Required

> [!IMPORTANT]
> I will change the application's starting screen to **Onboarding**. This assumes you want new users to see the introduction before logging in. If you prefer to start at the Login screen, please let me know.

## Proposed Changes

### UI Screens

#### [MODIFY] [OnboardingScreen.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/ui/screens/OnboardingScreen.kt)

- **Fix Syntax Errors**:
    - Remove the `clickable` parameter from `OnboardingScreen` which shadowed the Compose extension.
    - Add `import androidx.compose.foundation.clickable`.
    - Correct the `Box` modifier for the "Omitir" (Skip) area to use `.clickable { onFinish() }`.
    - Remove the invalid `androidx.compose.foundation.clickable { ... }` block inside the `Box`.

### Core / Navigation

#### [MODIFY] [MainActivity.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/MainActivity.kt)

- **Integrate Onboarding**:
    - Add the `onboarding` destination to the `NavHost`.
    - Set `startDestination = "onboarding"`.
    - Configure `OnboardingScreen` to navigate to `login` when finished or skipped.

### Data Layer

#### [MODIFY] [SessionManager.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/data/session/SessionManager.kt)

- **Add Onboarding Flag**:
    - Add a `booleanPreferencesKey` to track if the user has completed onboarding.
    - Add methods `saveOnboardingCompleted()` and `isOnboardingCompleted()`.

#### [MODIFY] [UserViewModel.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/ui/viewmodel/UserViewModel.kt)

- **Expose Onboarding State**:
    - Add a `StateFlow<Boolean>` for onboarding status.
    - Add `completeOnboarding()` method to update the state and persist it.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure all build errors are resolved.

### Manual Verification
- Deploy the app and verify:
    1. The app starts with the Onboarding screens.
    2. Paging through the screens works.
    3. Clicking "Omitir" or finishing the flow takes the user to the Login screen.
