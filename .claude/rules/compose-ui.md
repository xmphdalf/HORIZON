---
paths:
  - "feature/**"
  - "core/design/**"
---

# Compose UI Rules

## State reads

- Read state in the smallest possible scope. During a drag, read it inside
  `Modifier.drawBehind { }` or the `Modifier.graphicsLayer { }` lambda so
  composition and layout are skipped and only the draw phase runs.
  `requirement.md` N-05: no dropped frames during the drag at 120 Hz.
- No `remember` of a derived value that should be `derivedStateOf`.
- UI state is an immutable data class. Events are a sealed interface. Never
  expose `MutableState` outward.
- No composable takes a presenter as a parameter. Composables take state and a
  lambda.

## Structure

- One public entry composable per feature module. Everything else is `internal`.
- Every composable that takes a `Modifier` takes it as the first optional
  parameter and applies it to its root.
- No composable exceeds one screen of code. Extract, do not scroll.
- No `LaunchedEffect(Unit)` where a keyed effect is correct.

## Colour

- Never write a colour literal. Every colour comes from `core/tokens` as a
  function of warmth and dim. See `.claude/rules/design-tokens.md`.
- No pure white anywhere. `text/hi` tops out at `#E9ECEE`. The panic strip is the
  only element allowed above 80 percent luminance, and only after the user has
  asked for light.

## Motion

- Colour crossfades over 420 ms on `cubic-bezier(.32, .02, .2, 1)`. Nothing steps.
- No springs, no overshoot, no scale-in.
- Reduced motion shortens crossfades to 120 ms but never removes them. An instant
  tint change is a flash, which is the one thing this product must never do.

## Previews

Every screen needs previews at three points on the ramp: **off**, **mild**
(3400 K), and **deep night** (1700 K), plus one at 200 percent font scale. The
hex values for each point are in `docs/design-system.md`.

## Nothing unasked for

No dialog, toast, or snackbar the user did not cause. On a dark screen at 1 a.m.
an unrequested surface is a flash of light in a dark room.

## Accessibility

- TalkBack reads state before name: "On, filter, 1700 kelvin".
- Every colour-carrying element also carries a monogram or a spec. Nothing means
  anything by colour alone.
- Targets are 44 dp minimum, 56 dp for anything used in the dark.
- The gesture is always an enhancement. Expose seekbars as the accessible path.
