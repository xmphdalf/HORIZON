# Changelog

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versions follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

Repository scaffolding. No application yet.

### Added

- Gradle build on AGP 9.3.1, Gradle 9.7.0, and Kotlin 2.4.10, with every version
  pinned in `gradle/libs.versions.toml` and five convention plugins in
  `build-logic/convention/`
- The full module tree: six `core` modules, six `feature` modules, four `surface`
  modules, the application, and the benchmark and baseline profile modules
- `core/model` with `Kelvin` and `DimPercent`, the pilot that proves the quality
  gates against real code
- Quality gates: detekt failing on any finding, Android Lint with
  `warningsAsErrors`, Kotlin `-Werror`, and explicit API mode for `core/*`
- Four structural gates wired into `check`, each with unit tests:
  `moduleDirection`, `noBangBang`, `dependencyAudit`, `permissionAudit`, and
  `apkSizeCheck` against the 6 MB budget
- The Claude Design handoff bundle, committed to `docs/design/` and documented in
  `docs/design-system.md`
- `.claude/` configuration: six rules, five skills, two agents, settings, and a
  Kotlin formatting hook
- Continuous integration running the gates on every push and pull request

### Decided

- Licence is GPLv3, closing `requirement.md` open question 1
- `core/design` is split into `core/tokens` and `core/design` so `surface/*`
  modules can share the ramp without Compose UI reaching a RemoteViews classpath.
  Recorded in `docs/architecture.md`
- `compileSdk` is 37 while `targetSdk` stays 36. Compose requires compiling
  against 37; targeting 36 is the Play requirement in `requirement.md` 4.4

### Corrected

- The gesture constant is 4.5 K per **dp**, not per pixel. `scaffolding.md`
  section 6.1 gives it as "4.5 K per pixel on a 1080p display", but 3000 K across
  1920 px is 1.56 K per pixel. The design intends 4.5 K per dp across roughly
  640 dp of height
- The horizontal drag rate must be derived from screen width rather than taken as
  the prototype's literal `0.16` percent per pixel, which is calibrated to its own
  340 px frame

### Known gaps

- The Outfit and JetBrains Mono binaries are not in the design bundle and must be
  added to `core/design/src/main/res/font/` before any screen ships. Both are OFL
- The foreground service type for API 34 and above is still open,
  `requirement.md` question 15.4
- `core/solar` has no declared consumer yet, resolved when `feature/schedule` is
  built
