---
paths:
  - "core/tokens/**"
  - "core/design/**"
---

# Design Token Rules

## Tokens are functions, not values

A token is a function of two inputs, warmth and dim. There is no fixed colour
constant anywhere outside the ramp definition itself. Interpolation happens in
Oklch, so hue rotates and chroma rises together while lightness stays pinned to
its step. Dim multiplies lightness afterwards, uniformly, which is why contrast
ratios hold at every brightness.

A 2400 K surface is never brighter than the same token at 3400 K. If a change
breaks that, the change is wrong.

## The acceptance targets

`docs/design-system.md` holds the ramp table: `surface/0`, `surface/1`,
`surface/2`, `text/hi`, `text/lo`, and `accent`, sampled at OFF/DAY, 3400 K,
2400 K, and 1700 K. The Oklch implementation must reproduce those hex values.
They are the test, not an illustration.

## The prototype is not the specification

`docs/design/project/Blue Light Filter.dc.html` contains a `renderVals()`
function that mixes sRGB component arrays. It is a browser mockup and it does not
match the table: at 3400 K it yields `#0E0907` where `surface/0` is `#100C09`.
Use it to understand which tokens exist and how dim scales each one. Do not port
its maths.

## Contrast

Body text holds at least 4.6:1 against its surface at every point on the ramp.
The 96 px numerals sit at 3.2:1 deliberately, because at that size more contrast
is glare rather than legibility. `core/tokens` tests both bounds; a new token
needs a contrast test before it is used.

## Elevation

No shadows. A shadow needs light behind the surface. A raised surface is one step
up the ramp, about `+3 %` lightness, plus, only where a boundary must be
unambiguous, a hairline at 8 to 14 percent of the surface's own hue. Three levels
only: ground, sheet, control.

## Module boundary

`core/tokens` is pure JVM. No Android imports, no Compose. That is what lets
`surface/widget`, `surface/tile`, and `surface/notification` share one ramp
without putting Compose UI on a RemoteViews classpath. See
`docs/architecture.md`.
