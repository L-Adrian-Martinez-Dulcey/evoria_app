# Fix Build Errors and Navigation Issues

The app is currently failing to build because of incorrect Wear Compose dependencies and SDK version mismatches. Additionally, `MainActivity.kt` contains syntax errors in the `NavHost` configuration and incorrect imports.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/EvoriaApp/gradle/libs.versions.toml)

- **Remove Wear Compose dependency**: Remove the incorrect `compose-material3` entry pointing to `androidx.wear.compose`.
- **Remove Wear version reference**: Remove `composeMaterial3 = "1.6.2"`.

#### [MODIFY] [app/build.gradle.kts](file:///C:/EvoriaApp/app/build.gradle.kts)

- **Remove redundant material3 implementation**: Remove `implementation(libs.compose.material3)` which was pointing to the Wear library.
- **Keep standard material3**: Ensure `implementation(libs.androidx.compose.material3)` remains.

### Core / UI

#### [MODIFY] [MainActivity.kt](file:///C:/EvoriaApp/app/src/main/java/com/example/p3/MainActivity.kt)

- **Fix Imports**: Remove `import androidx.wear.compose.material3.AppScaffold`.
- **Fix Navigation Routes**:
    - Move `composable("my_events")`, `composable("profile")`, `composable("event_detail/{eventId}")`, `composable("event_form")`, and `composable("event_form/{eventId}")` **inside** the `NavHost` block.
    - Fix the `NavHost` builder scope to include all application destinations.
- **Fix Syntax**: Ensure the closing braces for `if (onboardingDone != null && sessionChecked)` and `NavHost` are correctly placed.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the build issues are resolved.

### Manual Verification
- Deploy the app to a phone/emulator to ensure:
    - The splash/session logic correctly routes to Onboarding, Login, or Home.
    - Navigation between all screens (Home, Profile, My Events) works as expected.
