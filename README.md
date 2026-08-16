# Math Facts

A small iOS flash-card app for practicing math facts, built with Kotlin Multiplatform and Compose Multiplatform.

## Requirements

- macOS with Xcode
- JDK 17 or newer

If you installed the JDK with Homebrew, add it to your shell before running Gradle directly:

```shell
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

## Run the app

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Choose an iPhone simulator.
3. Run the `MathFacts` scheme.

Xcode invokes Gradle to compile the shared Kotlin UI into an iOS framework before building the SwiftUI host app.

## Project structure

- `shared/src/commonMain`: shared Kotlin UI and flash-card logic
- `shared/src/iosMain`: the Compose-to-UIKit entry point
- `shared/src/commonTest`: shared unit tests
- `iosApp`: the native iOS host project
