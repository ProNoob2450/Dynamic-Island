# Dynamic Island (Android)

Production-oriented Android Dynamic Island prototype with modular architecture:

`event -> queue -> reducer -> overlay -> compose UI`

## Supported activities
- Call activity (incoming / in-call / ended, answer + end actions)
- Media activity (metadata + artwork + transport controls)
- Notification activity
- Battery / charging activity
- Timer activity
- Navigation activity (basic)

## Event priorities
1. Call = 5
2. Navigation = 4
3. Timer = 3
4. Media = 2
5. Notification = 1
6. Battery = 0

## Permissions
- `SYSTEM_ALERT_WINDOW`
- `FOREGROUND_SERVICE`
- `READ_PHONE_STATE`
- `ANSWER_PHONE_CALLS`
- Notification listener access (`BIND_NOTIFICATION_LISTENER_SERVICE` service)

## Build instructions
> If your environment blocks Gradle distribution downloads, configure a reachable Gradle mirror/proxy first.
>
> This repository pins Java 21 and Gradle 8.14.3 via `mise.toml`. The wrapper
> binary JAR is intentionally excluded so PR diffs stay text-only; `./gradlew`
> will bootstrap Gradle from `gradle-wrapper.properties` (or use `mise`) when
> the wrapper JAR is missing.

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

Expected APK outputs:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

## Runtime architecture overview
- `core/events`
  - event models + priorities
  - event queue and reducer state machine
  - providers: battery, timer, navigation
- `core/overlay`
  - foreground overlay service
  - gesture controller and window controller
  - composition of activity cards
- `core/ui`
  - media, call, battery, timer, navigation cards
  - motion controller and settings/debug screen
- `app`
  - notification/media/call system listener integration
  - settings persistence via DataStore

## Screenshots
_Add screenshots after running the app on-device/emulator._

- Settings / Debug screen
- Compact island (media / call / battery / timer / navigation)
- Expanded media controls
- Expanded call controls
