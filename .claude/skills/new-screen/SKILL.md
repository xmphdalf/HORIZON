---
description: Scaffolds a new feature module the Horizon way, with an entry composable, immutable state, a presenter, previews at three ramp points, and module wiring. Use when adding a screen under feature/.
argument-hint: "[name]"
arguments: name
---

# New screen: $name

Scaffold a feature module for `$name`. If `$name` is empty, ask which screen
before doing anything else.

## 1. Check it belongs

A feature module is one screen. If `$name` describes a surface the system renders
(a widget, a tile, a notification), it belongs under `surface/` instead and this
skill does not apply.

## 2. Create the module

```
feature/$name/
├── build.gradle.kts
└── src/
    ├── main/kotlin/dev/horizon/feature/$name/
    │   ├── ${Name}Screen.kt      Composable entry point, stateless
    │   ├── ${Name}State.kt       Immutable state, and the events sealed interface
    │   ├── ${Name}Presenter.kt   State holder, owns the coroutine scope
    │   └── internal/             Pure helpers, marked internal
    └── test/kotlin/dev/horizon/feature/$name/
```

`build.gradle.kts`:

```kotlin
plugins {
    id("horizon.android.library")
    id("horizon.android.compose")
}

android {
    namespace = "dev.horizon.feature.$name"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:tokens"))
    implementation(project(":core:design"))
}
```

Declare only the modules the screen actually uses. `moduleDirection` permits
`core/model`, `core/common`, `core/tokens`, `core/design`, and `core/data`, and
nothing else. It does not permit a sibling feature.

Then add `include(":feature:$name")` to `settings.gradle.kts`, and add the module
to `app/build.gradle.kts`.

Use [screen-template.kt.txt](screen-template.kt.txt) as the starting shape.

## 3. The rules that apply

- One public entry composable. Everything else `internal`.
- State is an immutable data class. Events are a sealed interface. Never expose
  `MutableState`.
- The composable takes state and lambdas, never the presenter.
- `Modifier` is the first optional parameter and is applied to the root.
- No colour literals. Everything comes from `core/tokens`.
- No composable longer than one screen of code.

`.claude/rules/compose-ui.md` loads when you open a file here and has the rest.

## 4. Previews are not optional

Four are required: **off**, **mild** at 3400 K, **deep night** at 1700 K, and one
at 200 percent font scale. The ramp values are in `docs/design-system.md`.

## 5. Verify

```
./gradlew :feature:$name:check
```

This runs detekt, lint, and `moduleDirection`. A new module that skips the
wiring in step 2 will fail here rather than at merge time.
