# Architecture

## Module graph

Dependencies point downward only. A module never depends on a sibling in
`feature/` or `surface/`. This is enforced by the `moduleDirection` Gradle task,
not by review discipline.

```
app
 ├─> feature/*     (UI destinations)
 ├─> surface/*     (overlay, notification, tile, widget)
 └─> core/*

feature/*  ─> core/model, core/common, core/tokens, core/design, core/data
surface/*  ─> core/model, core/common, core/tokens, core/data   (never core/design)
core/data  ─> core/model, core/common
core/design─> core/model, core/tokens
core/tokens─> core/model                            (pure JVM, no Android)
core/solar ─> core/model                            (pure JVM, no Android)
core/model ─> nothing
core/common─> nothing
```

## Why core/tokens is separate from core/design

scaffolding.md section 3 forbids `surface/*` from depending on `core/design`.
requirement.md section 1 requires the same arc geometry on the widget, the tile,
and the notification, and section 9 makes every colour a function of warmth and
dim rather than a fixed value. Those surfaces therefore need the ramp.

Splitting the ramp out resolves both. `core/tokens` is a pure JVM module holding
the Oklch ramp and the token functions. `core/design` keeps the Compose theme and
the arc composables. `surface/*` depends on `core/tokens` and still never on
`core/design`, so the section 3 rule holds and the ramp has exactly one
implementation.

## Module table

| Module | Type | Contains | Must not contain |
|---|---|---|---|
| `core/model` | JVM library | `Kelvin`, `DimPercent`, `Minutes`, `FilterState`, `Preset`, `ScheduleRule`. Value classes and pure functions. | Android imports |
| `core/common` | JVM library | Result types, dispatchers interface, clock abstraction | Android imports |
| `core/tokens` | JVM library | The Horizon ramp, token functions of (warmth, dim), Oklch conversion | Android imports, Compose |
| `core/solar` | JVM library | NOAA sunrise and sunset for a lat/long and date | Android imports, network |
| `core/design` | Android library | Compose theme, arc drawing, typography | Screen-level composables |
| `core/data` | Android library | Proto DataStore schema, `SettingsRepository`, preset storage, export and import | UI, Android views |
| `feature/*` | Android library | One screen each: composables, state holder, and its own preview data | Direct DataStore access, `WindowManager` |
| `surface/overlay` | Android library | Foreground service, `WindowManager` handling, the colour `View`, brightness control | Compose, any UI beyond the single view |
| `surface/notification` | Android library | The persistent control notification | Business logic |
| `surface/tile` | Android library | `TileService` | Business logic |
| `surface/widget` | Android library | Glance widgets, 4x1 and 2x2 | `androidx.compose.ui` imports |
| `app` | Android application | Manifest, single Activity, navigation, dependency graph assembly | Feature logic |
| `benchmark` | Test module | Macrobenchmark for cold start and drag | Production code |
| `baselineprofile` | Test module | Baseline profile generator | Production code |

`surface/widget` is the one module where the Compose rule needs care. Glance is
built on the Compose *runtime*, so `androidx.compose.runtime` is required there.
The ban is on `androidx.compose.ui` specifically. A check that bans Compose
wholesale breaks Glance.

## core/solar has no consumer yet

Nothing depends on it. The section 3 graph does not say who calls it: `feature/*`
may not depend on it, and `core/data` is limited to `core/model` and
`core/common`. This is left open until `feature/schedule` is built, at which point
either `core/data` gains the dependency or a scheduler module takes it. A module
with no consumers is legal under the direction check, so nothing is blocked.

## Package layout inside a module

Layer by feature, not by type.

```
feature/schedule/src/main/kotlin/dev/horizon/feature/schedule/
├── ScheduleScreen.kt         Composable entry point, stateless
├── ScheduleState.kt          Immutable UI state, and the events sealed interface
├── SchedulePresenter.kt      State holder, owns the coroutine scope
├── ScheduleArc.kt            The arc drawing and drag handling for this screen
└── internal/
    └── AnchorMath.kt         Pure helpers, marked internal
```

- One public entry composable per feature module. Everything else is `internal`.
- State holders are plain classes, not `ViewModel`, unless a screen genuinely
  needs the `ViewModel` lifecycle. Constructor injection, no service locator.
- UI state is an immutable data class. Events are a sealed interface. No
  `MutableState` exposed outward.
- No composable takes a presenter as a parameter. Composables take state and a
  lambda.

## Build

`gradle/libs.versions.toml` is the only place a version is written. The five
convention plugins in `build-logic/convention/` carry all shared configuration.

AGP 9 carries Kotlin built in, so `org.jetbrains.kotlin.android` is never applied
to a module; applying it is a hard error. Declaring the Kotlin plugin in the root
`plugins` block is what upgrades that built-in Kotlin from AGP's bundled version
to the one pinned in the catalog.

`compileSdk` is 37 while `targetSdk` is 36. Compose requires compiling against 37;
targeting 36 is the Play requirement from requirement.md 4.4. The two are
independent and this arrangement is the normal one.
