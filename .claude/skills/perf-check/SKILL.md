---
description: Runs the macrobenchmarks, compares against the committed baseline, and reports regressions in cold start and drag frame timing. Use before merging a change to surface/overlay, core/tokens, core/design, or feature/home.
---

# Performance check

The two numbers that matter, from `requirement.md`:

- **N-04 Cold start.** Settings UI interactive within 400 ms on a mid-range 2020
  device, with a baseline profile shipped.
- **N-05 Frame timing.** No dropped frames during the drag gesture at 120 Hz. The
  overlay colour path must not depend on Compose recomposition.

## Run

Needs a connected physical device. An emulator produces numbers that are not
comparable to the baseline and must not be used to claim a result.

```
./gradlew :benchmark:connectedBenchmarkAndroidTest
```

If no physical device is connected, stop and say so. Do not report emulator
numbers as a pass.

## Compare

Results land in `benchmark/build/outputs/connected_android_test_additional_output/`.

Compare against the committed baseline. Report:

- Cold start median and P95, against the 400 ms budget
- Frame duration P50, P95, and P99 during the drag
- The count of frames over the 120 Hz budget of 8.3 ms

State the delta from baseline, not just the absolute number. A regression inside
budget is still a regression and is worth naming.

## When something regressed

The usual causes, in the order worth checking:

1. **A state read moved out of the draw phase.** If a value that changes during
   the drag is read in composition rather than inside `drawBehind` or
   `graphicsLayer`, every frame recomposes. This is the most common cause by a
   wide margin.
2. **Allocation in the drag path.** A lambda capturing per frame, a collection
   operation, or boxing. The path must allocate nothing.
3. **The baseline profile is stale.** If startup regressed after adding a screen,
   regenerate it before concluding anything else.
4. **Work moved onto the main thread.** Check that disk access still goes through
   the injected dispatcher.

## Regenerating the baseline profile

```
./gradlew :baselineprofile:generateBaselineProfile
```

Commit the regenerated profile with the change that needed it, not separately.
