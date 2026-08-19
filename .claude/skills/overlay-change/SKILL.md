---
description: Required reading before changing anything in surface/overlay. Covers the Android touch-opacity cap, brightness handling, and foreground service ordering. Use whenever a task touches the overlay service, the colour view, brightness control, or panic disable.
---

# Changing the overlay

`surface/overlay` is the riskiest module in the project. A mistake here can make a
phone stop responding to touch entirely, which the user cannot easily undo, or
leave the screen too dark to find the disable control.

## Before editing

1. Read [platform-constraints.md](platform-constraints.md) in full. It has the
   API levels and the exact failure modes.
2. State which constraints the change touches. If the answer is none, check
   again: opacity, brightness, and service lifecycle cover almost everything in
   this module.
3. If the change affects opacity, brightness, or the service lifecycle, write the
   instrumented test first. Not after.

## While editing

- Read the opacity cap at runtime. Never hardcode 0.8.
- Save the user's brightness before changing it, and restore exactly that value.
- Add the overlay window before starting the foreground service on API 35 and
  above.
- Allocate nothing in the drag path. No lambdas capturing per frame, no
  collection operations, no boxing.
- Keep it a plain `View`. Compose does not belong in this module.

## After editing

Confirm each of these, and say so explicitly rather than assuming:

- The drag path still allocates nothing.
- Panic disable still restores brightness, still needs no confirmation, and still
  waits on no disk IO.
- The opacity cap is still read at runtime.

Then run:

```
./gradlew :surface:overlay:check
./gradlew :surface:overlay:connectedCheck
```

`connectedCheck` needs a connected device or a running emulator. If none is
available, say so rather than reporting the change as verified.
