# Overlay Platform Constraints

Each of these changed the shape of the product, not just the code.

## 1. The touch opacity ceiling, Android 12 and above

Android 12 blocks touch events from untrusted overlays. A `TYPE_APPLICATION_OVERLAY`
window is untrusted. Touches pass through only when the combined opacity of all
such windows is at or below the system maximum obscuring opacity.

The platform default is 0.8. It is a device configuration value and a
manufacturer can lower it.

**Failure mode:** exceed the cap and the phone stops responding to touch
entirely. Not the app, the phone. The user cannot tap their way out of it.

**What the code must do:** read
`InputManager.getMaximumObscuringOpacityForTouch()` at runtime and clamp to it.
Never hardcode 0.8, and never assume it is the same on the next device.

**Consequence for the product:** an overlay alone cannot reach the deep dim this
product needs. That is why `WRITE_SETTINGS` is a required permission, reversing
an earlier decision. `requirement.md` section 14 records the reversal.

## 2. Foreground service ordering, API 35 and above

On API 35 and above, an app holding `SYSTEM_ALERT_WINDOW` can only start a
foreground service from the background if it already has a visible overlay
window.

**Failure mode:** `ForegroundServiceStartNotAllowedException`.

**What the code must do:** add the window first, then start the service. This
affects boot restore and scheduled starts, which are exactly the paths that run
without a user in front of the screen.

## 3. Coverage limits

`TYPE_APPLICATION_OVERLAY` cannot cover the status bar, the navigation bar, or
certain system dialogs and permission prompts. This is by design and there is no
supported way around it.

**What the code must do:** nothing. Document it. Do not reach for an
`AccessibilityService` to get past it: Play policy restricts accessibility APIs
to accessibility purposes, and removal risk is real. `scaffolding.md` section 1
records that rejection.

## 4. Brightness control

Dimming below the manufacturer's floor means lowering real system brightness
alongside the overlay.

**What the code must do:**

- Save the user's brightness value before the first change.
- Restore exactly that value on disable, on panic, and on service death.
- Never let the combination reach a state where the disable control cannot be
  found. `requirement.md` F-08 makes this a hard safety floor.

## 5. Battery managers

Manufacturer battery managers kill foreground services aggressively on some
skins, regardless of service type. See `docs/oem-quirks.md`.

**What the code must do:** detect that it was killed, tell the user plainly once,
and link to the guide for their manufacturer. Do not silently restart and pretend
nothing happened. Do not nag.

## 6. The scheduled ramp

A scheduled transition can last up to 45 minutes.

**Failure mode:** driving it from a frame loop or an animator holds the CPU awake
for the whole duration, which is a battery bug the user will blame on the
overlay.

**What the code must do:** compute the current value from the clock and step at
intervals. `requirement.md` F-18.

## 7. The drag path

`requirement.md` N-05 requires no dropped frames during the drag at 120 Hz.

**What the code must do:** allocate nothing per frame. No lambdas capturing, no
collection operations, no boxing, no `List` allocation. The overlay colour update
runs on the main thread and must stay cheap enough that it never competes with
the app's own rendering.
