# HORIZON: Repository Scaffolding

Status: v1.1
Purpose: defines the repository layout, the locked technical decisions, the code
quality bar, and the `.claude/` configuration. This document is the contract.
Anything not described here is a decision that still needs making, not a decision
left to improvisation.

Writing conventions for every document in this repo: ASCII only, no em dashes, no
emoji, no decorative characters. Plain hyphens, plain quotes.

Changes since v1.0 are listed in section 13, so the original contract and the
amendments to it stay distinguishable.

---

## 1. Locked Technical Decisions

These are settled. Reopening one requires updating this section first.

| Area | Decision | Reason |
|---|---|---|
| Language | Kotlin, JVM target 17 | Native is required; the overlay is the product |
| Min SDK | 26 (Android 8.0) | `TYPE_APPLICATION_OVERLAY` floor |
| Target SDK | 36 (Android 16) | Play requires it for new apps from 2026-08-31 |
| Compile SDK | 37 | Compose requires it; independent of target SDK |
| App UI | Jetpack Compose | Jank parity with Views since Compose 1.9 |
| Overlay UI | Plain `View`, never Compose | A solid color needs no recomposition loop |
| Widgets | Jetpack Glance | Purpose built for RemoteViews |
| QS tile | Framework `TileService` | No Compose in tiles |
| Storage | Proto DataStore | Typed, async, no SQLite needed |
| Async | Coroutines and Flow | Standard |
| DI | Manual constructor injection | Keeps APK small and startup short; no Hilt |
| Build | Gradle Kotlin DSL, version catalog, convention plugins | Reproducible, no copy-paste build logic |
| Solar math | Pure Kotlin NOAA implementation, in-repo | Offline, no dependency, testable |
| Networking | None. `INTERNET` is never declared | The privacy claim must be verifiable |
| License | GPLv3 | Derivatives cannot close the source and add network code |
| Distribution | F-Droid primary, Play secondary, signed APK from repo | Reaches the audience that cares |

Rejected, with reasons recorded so they are not revisited casually:

- Flutter, React Native, Kotlin Multiplatform: the core feature needs a platform
  channel, the APK grows by megabytes, and a bridge sits between the thumb and
  the pixel.
- Hilt or Koin: unnecessary for this graph size, adds startup cost and APK weight.
- `AccessibilityService` for deeper dimming: Play policy restricts accessibility
  APIs to accessibility purposes. Removal risk is real.
- Room or SQLite: there is no relational data here.
- Any crash reporting SDK that transmits: violates the no-network commitment.

---

## 2. Repository Tree

```
horizon/
├── CLAUDE.md
├── README.md
├── LICENSE
├── requirement.md
├── scaffolding.md
├── CONTRIBUTING.md
├── SECURITY.md
├── PRIVACY.md
├── CHANGELOG.md
├── .gitignore
├── .editorconfig
├── .claude/
│   ├── settings.json
│   ├── rules/
│   │   ├── privacy-invariants.md
│   │   ├── overlay.md
│   │   ├── compose-ui.md
│   │   ├── design-tokens.md
│   │   ├── testing.md
│   │   └── documentation.md
│   ├── skills/
│   │   ├── overlay-change/
│   │   ├── new-screen/
│   │   ├── perf-check/
│   │   ├── a11y-audit/
│   │   └── release-check/
│   ├── agents/
│   │   ├── code-reviewer.md
│   │   └── privacy-auditor.md
│   └── hooks/
│       └── format-edited-kotlin.sh
├── .github/
│   ├── workflows/{ci,benchmark,release}.yml
│   ├── ISSUE_TEMPLATE/
│   └── PULL_REQUEST_TEMPLATE.md
├── gradle/libs.versions.toml
├── build-logic/convention/
├── config/
│   ├── detekt/detekt.yml
│   └── lint/lint.xml
├── docs/
│   ├── architecture.md
│   ├── design-system.md
│   ├── permissions.md
│   ├── science.md
│   ├── oem-quirks.md
│   └── design/            The Claude Design handoff bundle
├── app/
├── core/{model,common,tokens,solar,design,data}/
├── feature/{home,schedule,presets,settings,permission,mission}/
├── surface/{overlay,notification,tile,widget}/
├── benchmark/
└── baselineprofile/
```

---

## 3. Module Responsibilities

Dependencies point downward only. A module never depends on a sibling in
`feature/` or `surface/`. Violations are caught by the `moduleDirection` Gradle
task, not by review discipline.

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

The module table is in `docs/architecture.md`, along with the reason
`core/tokens` is separate from `core/design`.

Namespace root: `dev.horizon`. Application id: `dev.horizon.filter`.

---

## 4. Package Layout Inside a Module

Layer by feature, not by type. A person reading `feature/schedule` should see
everything about the schedule screen in one place. The layout and its rules are
in `docs/architecture.md`.

---

## 5. Build Configuration

`gradle/libs.versions.toml` is the only place versions are written. No version
literal ever appears in a module `build.gradle.kts`.

Convention plugins in `build-logic/convention/` carry all shared configuration:
`horizon.android.application`, `horizon.android.library`, `horizon.android.compose`,
`horizon.jvm.library`, `horizon.quality`, plus `horizon.checks` and
`horizon.app.checks` for the structural gates.

A module build file should be short enough to read at a glance:

```kotlin
plugins {
    id("horizon.android.library")
    id("horizon.android.compose")
}

android {
    namespace = "dev.horizon.feature.schedule"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:design"))
}
```

Required and in place:

- Detekt with the config in `config/detekt/`, failing the build on any finding
- Android Lint with `abortOnError` and `warningsAsErrors`
- Explicit API mode for all `core/*` modules, applied by project path so a new
  core module cannot silently skip it
- Kotlin `-Werror`
- R8 full mode in release, minify and shrink on
- The five structural gates in section 8

**AGP 9 note.** AGP 9 carries Kotlin built in. Applying
`org.jetbrains.kotlin.android` to a module is a hard error. The Kotlin version is
set by declaring the plugin in the root `plugins` block, which upgrades the
built-in Kotlin from the version AGP bundles.

---

## 6. Code Quality Standards

The bar is: a reader should never have to guess why something is written the way
it is, and should never find a shortcut that was taken because it was faster.

### 6.1 Commenting policy

Comments explain **why**, never **what**. The code says what it does.

Write a comment when, and only when, one of these is true:

1. A platform constraint forced the shape of the code. Cite the constraint and
   the API level.
2. A value is not obvious and was derived. Show the derivation.
3. Something looks wrong but is correct. Say why.
4. A public API has non-obvious contract, threading, or lifecycle requirements.
   Use KDoc.

Good:

```kotlin
// Touches are blocked when combined overlay opacity exceeds the system cap
// (Android 12+, InputManager.getMaximumObscuringOpacityForTouch, default 0.8).
// Read it at runtime because OEMs can lower it.
private fun safeAlpha(requested: Float): Float =
    min(requested, inputManager.maximumObscuringOpacityForTouch)
```

```kotlin
// 4.5 K per dp: a 640 dp screen height spans 4600 K to 1600 K, so a 40 dp thumb
// twitch moves about 180 K, below the noticeable difference threshold in a dark
// room.
private const val KELVIN_PER_DP = 4.5f
```

Forbidden:

```kotlin
// Set the alpha
view.alpha = alpha

/**
 * Gets the kelvin.
 * @return the kelvin
 */
fun kelvin(): Int
```

Also forbidden anywhere in the repository:

- Commented-out code. Git remembers it.
- `TODO` or `FIXME` without an issue number: `// TODO(#42): ...`
- Section banner comments made of repeated characters
- Comments that duplicate the KDoc of the thing they sit next to
- Changelog comments inside source files

### 6.2 Kotlin conventions

- Immutability by default. `val` unless mutation is required and justified.
- Value classes for domain units: `Kelvin`, `DimPercent`, `Minutes`. Never a bare
  `Int` crossing a module boundary.
- Sealed interfaces for closed sets. `when` without an `else` branch so the
  compiler catches additions.
- No `!!`. Enforced by the `noBangBang` gate.
- No `lateinit` outside Android framework classes that force it.
- No global mutable state. No `object` holding mutable fields.
- Extension functions only when they read better at the call site, and only in
  the module that owns the receiver type.
- Named arguments for any call with more than two parameters of the same type.
- Every `catch` either handles the failure or rethrows.

### 6.3 Threading

- The overlay color update path runs on the main thread and allocates nothing per
  frame. No boxing, no lambda capture inside the drag loop, no `List` operations.
- All disk work goes through the injected `Dispatchers` interface so tests control it.
- No `GlobalScope`. Every coroutine has an owner with a defined cancellation point.
- `withContext` at the lowest level that needs it, not sprinkled at call sites.

### 6.4 Compose conventions

- State is read in the smallest possible scope. During a drag, read state inside
  `Modifier.drawBehind { }` or `Modifier.graphicsLayer { }` so composition and
  layout are skipped and only the draw phase runs.
- No `remember` of anything derived that could be `derivedStateOf`.
- Every composable that takes a `Modifier` takes it as the first optional
  parameter and applies it to its root.
- No composable exceeds one screen of code. Extract, do not scroll.
- Previews for every screen at three points on the ramp: off, mild, deep night.
  Plus one at 200 percent font scale.
- No `LaunchedEffect(Unit)` where a keyed effect is correct.

### 6.5 Forbidden patterns, repository wide

| Pattern | Why |
|---|---|
| `android.permission.INTERNET` in any manifest | Breaks the core promise. CI fails on this. |
| Any analytics, ads, attribution, or crash-reporting dependency | Same |
| A font, image, or config loaded from a URL at runtime | Same, and it would break offline use |
| Hardcoding `0.8` for the obscuring opacity cap | OEMs can change it; read it at runtime |
| Animating a scheduled ramp with a frame loop | Keeps the CPU awake for up to 45 minutes |
| A dialog, toast, or snackbar that the user did not cause | Lights the room |
| `Color.WHITE` or `#FFFFFF` anywhere except the panic strip | Glare |
| Reflection to reach hidden APIs | Breaks across OEMs and versions, unauditable |

---

## 7. Testing Strategy

| Layer | Tool | What is covered |
|---|---|---|
| `core/model`, `core/common`, `core/solar`, `core/tokens` | JUnit 5, plain JVM | All of it. Solar math against published NOAA values for at least six locations across latitudes and both hemispheres. |
| `core/data` | JUnit 5, `TestScope`, in-memory DataStore | Migration, export and import round trip, default values |
| `core/tokens` | JUnit 5 | Every ramp position produces a contrast ratio at or above the documented minimum |
| State holders | JUnit 5, Turbine | Event to state transitions |
| Compose UI | `createComposeRule` | Semantics, not pixels |
| Overlay service | Instrumented, JUnit 4 | Window add and remove, opacity cap, brightness restore on panic, service restart after kill |
| Performance | Macrobenchmark | Cold start, drag frame timing |
| The gates themselves | JUnit 5 in `build-logic` | Each gate fails on the violation it exists to catch |

Rules:

- A bug fix lands with a test that fails before it and passes after.
- Test names describe behavior: `restoresSystemBrightnessWhenPanicDisableFires`.
- No test asserts on private state. If it needs to, the design is wrong.
- No sleeps. Virtual time or idling resources only.

---

## 8. Continuous Integration and the Gates

`check` runs these on every push and pull request, and each fails the build:

| Gate | Fails when |
|---|---|
| detekt | Any finding, with the config in `config/detekt/` |
| Android Lint | Any warning, with `abortOnError` and `warningsAsErrors` |
| `moduleDirection` | A module depends on one its layer may not reach |
| `noBangBang` | A `!!` appears in any Kotlin source |
| `dependencyAudit` | Any analytics, ads, crash-reporting, or networking coordinate resolves |
| `permissionAudit` | The merged manifest declares a permission `docs/permissions.md` does not list |
| `apkSizeCheck` | The release APK exceeds the budget in `requirement.md` N-03 |

The gate logic lives in
`build-logic/convention/src/main/kotlin/dev/horizon/buildlogic/` and is unit
tested. Changing a gate means changing its test in the same commit.

`benchmark.yml` runs macrobenchmarks on pull requests touching performance
sensitive modules. `release.yml` builds twice, verifies the two APKs are
identical, checks the size budget, and publishes the checksum.

---

## 9. The `.claude/` Directory

`CLAUDE.md` loads every session, so it stays under 200 lines. Everything
task-specific goes into a rule or a skill so it loads only when relevant.

**Rules** are guidance loaded into context. A rule without `paths:` frontmatter
loads at session start; a rule with `paths:` loads when Claude reads a matching
file. Subdirectories are discovered automatically.

| File | `paths:` | Contents |
|---|---|---|
| `privacy-invariants.md` | none, always loaded | The no-network rule, forbidden dependencies, what may never reach the manifest |
| `overlay.md` | `surface/overlay/**` | Opacity cap, brightness save and restore, service ordering, allocation-free drag |
| `compose-ui.md` | `feature/**`, `core/design/**` | Draw-phase state reads, modifier ordering, previews, no pure white |
| `design-tokens.md` | `core/tokens/**`, `core/design/**` | Tokens are functions of warmth and dim; no colour literals |
| `testing.md` | `**/test/**`, `**/androidTest/**` | Naming, no sleeps, no private state assertions |
| `documentation.md` | `**/*.md` | ASCII only, no em dashes, no emoji, to the point |

**Skills** are directories with a `SKILL.md` and supporting files. The
`description` frontmatter determines when Claude invokes one automatically.

| Skill | Invocation | Purpose |
|---|---|---|
| `overlay-change` | Auto, when touching the overlay | Walks the platform constraints, then requires an instrumented test |
| `new-screen` | `/new-screen <name>` | Scaffolds a feature module with previews at three ramp points |
| `perf-check` | `/perf-check` | Runs macrobenchmarks and compares against the baseline |
| `a11y-audit` | `/a11y-audit <module>` | TalkBack, 200 percent font, contrast at both ramp ends, equal-weight pairs |
| `release-check` | `/release-check`, `disable-model-invocation: true` | Full pre-release gate |

**Agents** run in their own context with restricted tools. `code-reviewer` has
`Read, Grep, Glob` and reviews against section 6. `privacy-auditor` adds `Bash`
and verifies the manifest, the dependency tree, and the absence of runtime URLs.

**Settings** are enforced, unlike rules. Permission rules evaluate deny first.
`.claude/settings.local.json` is gitignored explicitly so the whole team shares
that ignore rule.

Nothing in `.claude/` holds secrets or signing material, and nothing duplicates
this document; rules and skills point here rather than restating.

---

## 10. Bootstrapping Order

Each step ends with a green build.

1. ~~Gradle wrapper, version catalog, `build-logic`, `.editorconfig`, `.gitignore`~~ done
2. ~~Quality gates. Verify they fail on a deliberate violation~~ done
3. `core/model` and `core/common` with tests (`core/model` partly done)
4. `core/solar` with tests against published NOAA values
5. `core/data` with the DataStore schema and a migration test
6. `surface/overlay` with the service, the colour view, and instrumented tests for
   the opacity cap and brightness restore. The riskiest module; prove it before
   building UI on top of it.
7. `core/tokens` and `core/design` with the ramp and contrast tests
8. `app` with a single Activity and the permission screen
9. `feature/home` with the drag gesture wired to the overlay
10. `surface/notification`, then `surface/tile`, then `surface/widget`
11. `feature/schedule`, `feature/presets`, `feature/settings`, `feature/mission`
12. `baselineprofile` and `benchmark`, then wire them into CI
13. ~~`CLAUDE.md`, `.claude/`~~ done, ahead of the original order; see section 13

---

## 11. Definition of Done

A change is not done until all of the following hold:

- Detekt, lint, and the compiler pass with no warnings
- Tests cover the new behavior, and a bug fix has a test that failed before the fix
- No new permission, or the permission is added to `requirement.md` section 8,
  `docs/permissions.md`, and the in-app permission screen in the same commit
- No new dependency, or the dependency is justified in the pull request and adds
  no network capability
- Public API has KDoc; no comment restates code
- Previews exist at three ramp points for any UI change
- TalkBack labels exist for any new interactive element
- APK size delta is reported in the pull request
- If the change touches the overlay, an instrumented test proves the opacity cap
  and the brightness restore still hold

---

## 12. Open Items

1. ~~Final package namespace and application id~~ `dev.horizon` and `dev.horizon.filter`
2. ~~License choice~~ GPLv3
3. ~~Whether `benchmark` and `baselineprofile` stay in this repository~~ they stay
4. Whether the advanced dim path using `WRITE_SECURE_SETTINGS` via a one-time ADB
   grant becomes a documented power-user feature, and if so which module owns it
5. The foreground service type for API 34 and above. `requirement.md` question 15.4
6. Which module consumes `core/solar`. Nothing does yet, which is legal under the
   direction check, so this is decided when `feature/schedule` is built

---

## 13. Changes since v1.0

Recorded so the original contract and the amendments stay distinguishable.

**`core/design` split into `core/tokens` and `core/design`.** Section 3 forbade
`surface/*` from depending on `core/design`, but `requirement.md` section 1
requires the same arc geometry on the widget, tile, and notification, and section
9 makes every colour a function of warmth and dim. Those surfaces need the ramp.
`core/tokens` is a pure JVM module holding the ramp; `core/design` keeps the
Compose theme and the arc. The section 3 rule now holds without duplicating the
ramp. See `docs/architecture.md`.

**`compileSdk` raised to 37.** Compose requires compiling against 37. `targetSdk`
stays 36 per `requirement.md` 4.4. The two are independent.

**`KELVIN_PER_PIXEL` corrected to `KELVIN_PER_DP`.** The v1.0 example comment in
section 6.1 read "4.5 K per pixel on a 1080p display", but 3000 K across 1920 px
is 1.56 K per pixel. The design intends 4.5 K per dp across roughly 640 dp of
height. Since that comment is the code-quality exemplar and will be copied, it is
corrected here rather than left to be inherited.

**Two gates added beyond the v1.0 list.** `noBangBang`, because detekt's
`UnsafeCallOnNullableType` needs type resolution that is not enabled here and
would otherwise silently never fire; and unit tests for the gates themselves, so
a broken gate fails CI rather than passing everything.

**`.claude/` written at step 1 rather than step 13.** v1.0 deferred it so the
rules would describe reality. Writing it now means they describe the
specification instead, which is the better trade because `.claude/` governs how
steps 3 through 12 get built. `overlay.md` and `design-tokens.md` must be
revisited when their modules actually land.

**Detekt kept at 1.23.8.** Verified working under Gradle 9.7 and Kotlin 2.4.10.
It uses a `ReportingExtension` API that Gradle 10 removes, so it will need
attention before that upgrade.
