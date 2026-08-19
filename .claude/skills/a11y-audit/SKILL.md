---
description: Runs the accessibility checklist over a module. Covers TalkBack labels, 200 percent font scaling, contrast at both ends of the ramp, colour-only meaning, and equal-weight action pairs. Use when a UI change lands or before a release.
argument-hint: "[module]"
arguments: module
---

# Accessibility audit: $module

If `$module` is empty, audit every module under `feature/` and report per module.

`requirement.md` N-07 requires full TalkBack support, system font scaling to 200
percent, and no colour-only affordances.

## 1. TalkBack

- Every interactive element has a label. No unlabelled icon buttons.
- Labels read state before name: "On, filter, 1700 kelvin", not "filter on".
- Decorative elements are excluded from the tree rather than given empty labels.
- The drag gesture is never the only path. The surface exposes seekbars for
  warmth and brightness, at 10 percent increments, adjustable with the volume
  keys.
- Reading order is sensible. On the home screen, the donation entry point sits
  behind both real controls in reading order even though it is visually between
  them.

## 2. Font scaling to 200 percent

- Every preview has a 200 percent counterpart, and nothing clips or overlaps.
- The 96 px value numeral caps at 1.3x rather than scaling fully, so it stays on
  one line. Everything else scales.
- Touch targets stay at least 44 dp, and 56 dp for anything used in the dark.

## 3. Contrast at both ends of the ramp

Check at the OFF/DAY column and at 1700 K, not just one.

- Body text at or above 4.6:1 against its surface, at every ramp position.
- The 96 px numerals sit near 3.2:1 deliberately. Do not "fix" this: at that size
  more contrast is glare rather than legibility.
- No pure white. `text/hi` tops out at `#E9ECEE`. Only the panic strip may exceed
  80 percent luminance.

The ramp table is in `docs/design-system.md`.

## 4. Colour is never the only signal

- Every colour-carrying element also carries a monogram or a spec. The Migraine
  preset must be identifiable greyscaled or read aloud.
- State is never conveyed by colour alone. An enabled control differs in more
  than hue.

## 5. Equal-weight action pairs

`requirement.md` section 10 makes this non-negotiable for the donation flow, and
it is a good rule everywhere.

- Paired actions share height, radius, fill, border, font, weight, and colour,
  differing only in label, at fifty-fifty width.
- Nothing is ghosted. Nothing is default-focused.
- A decline action is a full-size button, never a text link.

## 6. Nothing appears unasked

No dialog, toast, or snackbar the user did not cause. On a dark screen at 1 a.m.
an unrequested surface is a flash of light in a dark room.

## Run the tests

```
./gradlew :$module:connectedCheck
```

Compose tests assert on semantics, never pixels. If a check in this list has no
corresponding test, add one rather than confirming it by eye.

## Report

List findings by section with a file and line. For each, give the concrete fix,
not just the problem. If a section passes, say so explicitly so the reader knows
it was checked rather than skipped.
