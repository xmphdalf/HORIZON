---
paths:
  - "surface/overlay/**"
---

# Overlay Rules

- Read the opacity cap at runtime from
  `InputManager.getMaximumObscuringOpacityForTouch()`. Never hardcode 0.8.
  Exceeding it blocks all touch input on the device, and manufacturers can
  lower it. Android 12 and above.
- Dimming below the system floor requires lowering system brightness as well as
  layering the overlay. An overlay alone cannot reach a usable dark state without
  crossing the cap. Save the user's brightness before changing it.
- Panic disable restores brightness and removes the overlay in one action. It
  never asks for confirmation, never waits on disk IO, and stays at a fixed
  position so it can be hit from muscle memory.
- On API 35 and above, add the overlay window **before** starting the foreground
  service, or the system throws `ForegroundServiceStartNotAllowedException`. This
  affects boot restore and scheduled starts.
- `TYPE_APPLICATION_OVERLAY` cannot cover the status bar, the navigation bar, or
  certain system dialogs. That is by design. Document it rather than working
  around it, and never reach for an `AccessibilityService` to get past it: Play
  policy restricts accessibility APIs to accessibility purposes and removal risk
  is real.
- The drag path allocates nothing. No lambdas capturing per frame, no collection
  operations, no boxing. It runs on the main thread.
- A scheduled ramp is computed from the clock and stepped at intervals. Never
  drive one from a frame loop: a 45 minute ramp would hold the CPU awake for 45
  minutes.
- The overlay is a plain `View`. Do not introduce Compose into this module. A
  solid colour needs no recomposition loop.
- This module may depend on `core/tokens` for colours, never on `core/design`.

Any change here needs an instrumented test proving the opacity cap and the
brightness restore still hold. `scaffolding.md` section 11.
