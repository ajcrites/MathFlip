# Math Flip

A small flash-card app for practicing arithmetic, built with Kotlin Multiplatform and Compose Multiplatform.

## Requirements

### iOS (current platform)

- macOS with Xcode
- JDK 17 or newer

## Run the app

### Script

Use the development scripts in `scripts/` to test, build, deploy, list devices, and inspect a physical device. Run a script with `--help` to inspect its options. Local physical-device identifiers can be stored in the gitignored `docs/devices.md`; use `docs/devices.example.md` as a template.

### Manual

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Choose an iPhone simulator.
3. Run the `MathFlip` scheme.

Xcode invokes Gradle to compile the shared Kotlin UI into an iOS framework before building the SwiftUI host app.

## Known limitations

- Motion scoring is tuned for deliberate turns. Very fast yaw gestures can produce inconsistent scoring.

## Project structure

- `shared/src/commonMain`: shared Kotlin UI and flash-card logic
- `shared/src/iosMain`: the Compose-to-UIKit entry point
- `shared/src/commonTest`: shared unit tests
- `iosApp`: the native iOS host project
