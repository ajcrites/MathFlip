# Math Flip agent guidance
Math Flip is a flash-card app for practicing arithmetic by revealing answers when the device is flipped face down. It currently targets iOS, with shared application logic and UI implemented using Kotlin Multiplatform and Compose Multiplatform.

## Project

- This is a Kotlin Multiplatform application using Compose Multiplatform, with a native iOS host in `iosApp`.
- Shared UI and application logic live under `shared/src/commonMain`; iOS-specific Kotlin code lives under `shared/src/iosMain`.
- The iOS bundle identifier is `com.mathfactsexp.app`.
- Preserve unrelated user changes. Do not commit changes unless explicitly requested.

## Build and verification

- Run commands from the repository root.
- Reference the development scripts in `scripts/` before constructing build or deployment commands. Run a script with `--help` when its interface is relevant.
- When a change affects the app, prefer building for the physical iPhone after relevant tests pass.

## Deployment

- Device and roles, names, and identifiers are stored locally in the gitignored `docs/devices.md`; use `docs/devices.example.md` as the format reference. Never add a personal device identifier to a tracked file.

## Maintaining this guidance

- Keep this file concise and operational.
- Add durable corrections, recurring commands, and repository conventions here when they would help future tasks avoid rediscovery.
