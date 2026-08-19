# Horizon

A system-wide screen warmth and dimming utility for Android. It tints the display
toward amber, dims it below the manufacturer's brightness floor, and schedules
itself around local sunset.

The interface is built around one idea: the screen is the preview. Dragging a
control warms the entire app in real time, so the user feels the change rather
than reading a number.

## The four commitments

These are not preferences. Breaking one is a different product.

1. **No paywall.** No free tier, because there is no paid tier.
2. **No advertising.** No banners, interstitials, sponsored presets, affiliate links.
3. **No data collection.** No accounts, no analytics, no crash reporter that
   transmits, and no `INTERNET` permission at all.
4. **No dishonesty.** The app does not overstate the science of blue light in
   order to sell itself.

An app requesting "draw over other apps" has the exact shape of malware. Trust is
the only currency here, and having nothing to sell is what makes the claim
credible. Every claim the app makes is meant to be falsifiable by checking
Android's own settings screen.

See `.claude/rules/privacy-invariants.md`, which loads every session.

## Where the truth lives

| Document | Holds |
|---|---|
| `requirement.md` | Product requirements, the permission list, the budgets |
| `scaffolding.md` | Repository contract, module rules, the code quality bar |
| `docs/architecture.md` | Module graph and why `core/tokens` is separate |
| `docs/design-system.md` | The Horizon ramp, the gesture, motion, contrast |
| `docs/permissions.md` | The permission list the build parses |
| `docs/design/` | The Claude Design handoff bundle, the visual source of record |

These point at each other rather than restating. Duplicating a rule into a second
file means the two will disagree later.

## Stack

Locked. Reopening one of these means updating `scaffolding.md` section 1 first.

- Kotlin, JVM target 17, min SDK 26, target SDK 36, compile SDK 37
- Jetpack Compose for app UI; the overlay is a plain `View`, never Compose
- Jetpack Glance for widgets; framework `TileService` for the tile
- Proto DataStore, coroutines and Flow
- Manual constructor injection. No Hilt, no Koin
- Solar maths is a pure Kotlin NOAA implementation in `core/solar`. No network
- Gradle Kotlin DSL, version catalog, convention plugins in `build-logic/`

AGP 9 carries Kotlin built in. Never apply `org.jetbrains.kotlin.android` to a
module; it is a hard error. The Kotlin version is set by declaring the plugin in
the root `plugins` block.

Every version literal lives in `gradle/libs.versions.toml` and nowhere else.

## Commands

```
./gradlew build                  Compile everything and run the tests
./gradlew check                  The above plus detekt, lint, and the four gates
./gradlew test                   JVM tests only
./gradlew :core:model:test       One module
./gradlew connectedCheck         Instrumented tests, needs a device or emulator
./gradlew -p build-logic test    Tests for the build gates themselves
```

## The gates

`check` runs four structural gates beyond detekt and lint. Each fails the build.

| Gate | Fails when |
|---|---|
| `moduleDirection` | A module depends on one its layer may not reach |
| `noBangBang` | A `!!` appears in any Kotlin source |
| `dependencyAudit` | Any analytics, ads, crash-reporting, or networking coordinate resolves |
| `permissionAudit` | The merged manifest declares a permission `docs/permissions.md` does not list |
| `apkSizeCheck` | The release APK exceeds 6 MB |

Their logic lives in `build-logic/convention/src/main/kotlin/dev/horizon/buildlogic/`
and is unit tested. If you change a gate, change its test.

## Module rules

Dependencies point downward only. A module never depends on a sibling in
`feature/` or `surface/`.

```
app        -> feature/*, surface/*, core/*
feature/*  -> core/model, core/common, core/tokens, core/design, core/data
surface/*  -> core/model, core/common, core/tokens, core/data   (never core/design)
core/data  -> core/model, core/common
core/design-> core/model, core/tokens
core/tokens-> core/model          (pure JVM)
core/solar -> core/model          (pure JVM)
```

`core/tokens` is separate from `core/design` so the widget, tile, and
notification can share one ramp without Compose UI on a RemoteViews classpath.

## Code quality

The bar: a reader should never have to guess why something is written the way it
is, and should never find a shortcut taken because it was faster.

- **Comments explain why, never what.** Write one when a platform constraint
  forced the shape of the code, when a value was derived, when something looks
  wrong but is correct, or as KDoc for a non-obvious public contract. Nothing else.
- No commented-out code. No `TODO` without an issue number, as `TODO(#42)`.
- Immutability by default. Value classes for domain units; never a bare `Int`
  crossing a module boundary.
- Sealed interfaces for closed sets, and `when` without `else` so the compiler
  catches additions.
- No `!!`, no `lateinit` outside framework classes that force it, no global
  mutable state, no `GlobalScope`.
- Every `catch` handles the failure or rethrows.

Full detail is in `scaffolding.md` section 6. Task-specific rules live in
`.claude/rules/` and load when you open a matching file.

## Writing conventions

ASCII only. No em dashes, no emoji, no decorative characters. Plain hyphens,
plain quotes. This applies to every document and every comment in the repository.

## Definition of done

- detekt, lint, and the compiler pass with no warnings
- Tests cover the new behaviour; a bug fix has a test that failed before the fix
- No new permission, or it is added to `requirement.md` section 8,
  `docs/permissions.md`, and the in-app permission screen in the same commit
- Public API has KDoc, and no comment restates code
- Previews exist at three ramp points for any UI change
- TalkBack labels exist for any new interactive element
- If the change touches the overlay, an instrumented test proves the opacity cap
  and the brightness restore still hold

## Current state

Scaffolding only. The module tree, the build logic, and the gates exist and are
green. `core/model` carries `Kelvin` and `DimPercent` as the pilot that proves the
gates on real code. Every other module is wired but empty.

Next, in order: `core/common`, `core/solar` against published NOAA values,
`core/data`, then `surface/overlay`, which is the riskiest module and should be
proven before any UI is built on top of it.
