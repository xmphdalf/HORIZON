---
paths:
  - "**/test/**"
  - "**/androidTest/**"
---

# Testing Rules

## Naming

Test names describe behaviour, not method names:

```
restoresSystemBrightnessWhenPanicDisableFires
rejectsAValueBelowTheSafetyFloor
```

Backtick names with spaces are fine and usually read better.

## Hard rules

- **No sleeps.** Virtual time or idling resources only. A `Thread.sleep` in a test
  is a flake that has not happened yet.
- **No asserting on private state.** If a test needs to reach inside, the design
  is wrong. Fix the design.
- **A bug fix lands with a test that fails before it and passes after.** Write the
  failing test first so you know it actually covers the bug.

## What each layer uses

| Layer | Tool |
|---|---|
| `core/model`, `core/common`, `core/solar`, `core/tokens` | JUnit 5, plain JVM |
| `core/data` | JUnit 5, `TestScope`, in-memory DataStore |
| `core/tokens` | Contrast assertions against the ramp table in docs/design-system.md |
| State holders | JUnit 5, Turbine |
| Compose UI | `createComposeRule`, asserting on semantics, never pixels |
| Overlay service | Instrumented, JUnit 4 |
| Performance | Macrobenchmark |

JVM modules use JUnit 5. Instrumented tests use JUnit 4, because
`androidx.test` requires it. That split is a platform constraint, not a choice.

## Coverage that matters

- `core/solar` is tested against published NOAA values for at least six
  locations across latitudes and both hemispheres.
- `core/tokens` asserts every ramp position produces a contrast ratio at or above
  the documented minimum.
- Compose tests assert TalkBack labels, layout at 200 percent font scale, and
  that paired actions carry equal visual weight. `requirement.md` section 10
  makes the equal-weight rule non-negotiable for the donation flow.
- Overlay instrumented tests cover window add and remove, the opacity cap,
  brightness restore on panic, and service restart after a kill.
