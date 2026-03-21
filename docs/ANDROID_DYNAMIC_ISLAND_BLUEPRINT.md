# Android Dynamic Island — Full Product Blueprint

This document describes how to build a production-grade Android app that recreates an iOS-style Dynamic Island with premium animations, broad device compatibility (all cutout styles), and robust performance.

## 1) Product vision

Build a floating, interactive “island” that:
- Anchors to camera cutouts or top center fallback.
- Expands for live activities (calls, media, timers, charging, navigation, downloads, recording, etc.).
- Supports quick actions and deep links.
- Feels premium through motion, haptics, blur, spring physics, and attention to detail.
- Works across punch-hole, notch, corner cutout, waterfall, and no-cutout devices.

## 2) Platform strategy (realistic constraints)

To support all Android devices, use a layered approach:

1. **Accessibility Service mode (core)**
   - Highest practical cross-app compatibility.
   - Can draw an always-on overlay and react to notifications/media/state.

2. **Foreground Service + Overlay (`SYSTEM_ALERT_WINDOW`)**
   - Required for persistent background behavior.
   - Keep transparent notification for service survival.

3. **Rootless by default**
   - Avoid root dependencies.
   - Build optional advanced integrations behind feature flags.

4. **OEM variance handling**
   - Battery optimization whitelisting flow.
   - MIUI/ColorOS/EMUI permission helpers and onboarding prompts.

## 3) Core feature set

### 3.1 Island interactions
- Compact idle pill aligned to cutout.
- Expand on event.
- Drag-to-reposition (optional locked mode).
- Swipe up/down gestures for dismiss/peek.
- Long-press for quick toggles.
- Multi-event queue (primary + secondary bubble).

### 3.2 Live activities
- Incoming/ongoing call controls.
- Media playback controls and progress.
- Timer/countdown and stopwatch.
- Charging + battery health and estimated time.
- Navigation next-turn card.
- Voice recording state.
- File download/upload progress.
- Hotspot/vpn status, silent mode, and more.

### 3.3 Premium UX
- Spring-based expansion/collapse.
- Physically plausible overshoot and damped settle.
- GPU-friendly blur/glass effect fallback chain.
- Adaptive color extraction from app icon/media artwork.
- Contextual haptics.
- 60/90/120Hz frame pacing aware animations.

### 3.4 Personalization
- Themes (light, dark, AMOLED, translucent).
- Shape presets per cutout style.
- App-specific behavior rules.
- Animation style packs.
- Smart scheduling / DND integration.

## 4) Technical architecture

## 4.1 Modules
- `app` — UI shell, settings, onboarding.
- `core-ui` — composables, motion system, tokens.
- `core-overlay` — window manager, overlay lifecycle.
- `core-cutout` — cutout detection and anchor calculation.
- `core-events` — event ingestion pipeline.
- `feature-media`, `feature-call`, `feature-timer`, etc.
- `data` — Room + DataStore.
- `domain` — use-cases and policies.

## 4.2 App stack
- Kotlin + Coroutines + Flow.
- Jetpack Compose for UI.
- Foreground Service + Accessibility Service.
- Hilt for dependency injection.
- Room + Proto DataStore.
- Macrobenchmark + Baseline Profile.

## 4.3 Event pipeline
1. Collect events from:
   - NotificationListenerService.
   - MediaSessionManager.
   - Telephony callbacks.
   - BatteryManager / BroadcastReceiver.
   - Optional app integrations.
2. Normalize events into a shared `IslandEvent` model.
3. Score/prioritize events.
4. Feed state machine for transitions: `Idle -> Peek -> Expanded -> Pinned -> Dismissed`.

## 4.4 State machine (must-have)
Implement a deterministic reducer:
- Input: user gesture + system event + timeout.
- Output: render state + animation command + haptic cue.

This avoids animation glitches, race conditions, and “stuck” overlays.

## 5) Cutout compatibility engine

## 5.1 Detection sources
- `WindowInsets.displayCutout` (primary modern API).
- `DisplayCutout.boundingRects` and safe insets.
- Fallback heuristics by resolution, status bar height, and OEM quirks.

## 5.2 Anchor strategy
- **Single center punch-hole/notch**: anchor center-top.
- **Corner cutout**: anchor near cutout then clamp to safe region.
- **No cutout**: synthetic anchor at top center with configurable offset.
- **Landscape**: switch to side anchor rules.

## 5.3 Safe area and collision
- Respect status icons and camera privacy indicators.
- Avoid overlap with heads-up notifications.
- Dynamic offset when keyboard/pip/system UI appears.

## 5.4 Device profile system
Maintain a profile table:
- Brand/model fingerprints.
- Known insets quirks.
- Override rules and tuning constants.

## 6) Animation system

## 6.1 Motion principles
- Use one motion language across all cards.
- Distinct durations by transition type (micro/standard/hero).
- Predictive transformation from compact to expanded geometry.

## 6.2 Implementation details
- Compose `Animatable` + spring specs.
- Shared transition primitives:
  - shape morph
  - position interpolation
  - icon-to-card transform
  - content crossfade + stagger
- Limit overdraw; precompute paths.

## 6.3 Performance targets
- Jank < 3% on mid-range devices.
- Render thread stable under 16ms @60Hz and 8ms @120Hz when possible.
- No ANR from overlay service.

## 7) Permissions and onboarding

Required permissions may include:
- Draw over other apps.
- Accessibility service.
- Notification access.
- Ignore battery optimizations (guided).
- Optional phone/media permissions depending on features.

Onboarding should:
1. Explain why each permission is needed.
2. Deep-link to correct settings screens.
3. Verify state after returning.
4. Offer reduced mode if user declines.

## 8) Security, privacy, trust

- Process minimal notification content.
- On-device only for sensitive data by default.
- Clear privacy policy and opt-outs.
- Redact private text on lock screen.
- Granular per-app permission controls.

## 9) QA and device testing matrix

## 9.1 Device categories
- Pixel (AOSP baseline).
- Samsung OneUI.
- Xiaomi/Redmi MIUI/HyperOS.
- OnePlus/Oppo/Realme ColorOS.
- Vivo/iQOO.
- Motorola/Nothing.

## 9.2 Form factors
- Punch-hole center/left.
- Notch wide.
- Corner cutout.
- No cutout.
- Foldable inner/outer displays.

## 9.3 Test suites
- Unit tests for reducer/state machine.
- UI tests for gesture interactions.
- Macrobenchmarks for animation and startup.
- Soak tests for background survivability.

## 10) Monetization (optional)

Free tier:
- Core island + limited cards/themes.

Premium tier:
- Advanced animations, custom packs, automation rules, cloud backup, pro widgets.

## 11) Delivery roadmap

### Phase 1 (4–6 weeks) — Foundation
- Overlay + cutout anchor engine.
- Core state machine.
- Media + battery + timer cards.
- Basic settings and onboarding.

### Phase 2 (4–5 weeks) — Premium polish
- Call/navigation/download cards.
- Animation pack v1 + haptics.
- Device profile overrides.
- Performance tuning + baseline profiles.

### Phase 3 (3–4 weeks) — Scale
- OEM hardening and kill-policy helpers.
- Localization + accessibility improvements.
- Premium features + subscription integration.

## 12) Definition of done

- Works on top OEMs with stable overlay behavior.
- Handles all major cutout types and no-cutout fallback.
- Maintains smooth animation under typical load.
- Passes privacy and permission transparency checks.
- Crash-free session rate target met.

## 13) Immediate next implementation tasks

1. Initialize a multi-module Android project with Compose + Hilt.
2. Build `core-cutout` with runtime anchor computation.
3. Implement `IslandEvent` model and reducer-based state machine.
4. Add `feature-media` card and one fully polished animation path.
5. Ship internal alpha for 10-device compatibility pass.
