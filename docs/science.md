# About the Science

Commitment four in `requirement.md` section 2 is no dishonesty: Horizon does not
overstate the science of blue light in order to sell itself. This document is the
source for the in-app "About the science" screen required by F-29, and it is
written to the same standard.

The honest summary is that the general claim is well supported and the specific
one this product category is named after is not.

## What the evidence supports

**Light exposure in the evening affects sleep.** This is well established. Light
suppresses melatonin and shifts circadian phase, and the effect is measurable.

**Intensity matters, and it matters a lot.** The dose-response relationship
between light level and melatonin suppression is one of the better characterised
findings in the area. A dimmer screen is doing more for you than a warmer one.

**Timing matters.** The same light has different effects depending on when in
your circadian cycle you receive it. Light in the hours before your usual bedtime
is the exposure that shifts things.

**Screens are usually not the dominant source.** Room lighting is typically
brighter than a phone held at arm's length and covers far more of the visual
field. A filter on the phone while a bright ceiling light is on is treating the
smaller problem.

## What is contested

**That blue wavelengths specifically are the problem.** The intrinsically
photosensitive retinal ganglion cells that drive circadian response peak in the
blue range, which is where the reasoning comes from. But trials that isolate
wavelength while holding intensity constant have produced mixed results, and
several well-run studies have found little or no additional benefit from colour
shifting once brightness is controlled for.

**That blue light filters improve sleep in practice.** Studies on the actual
feature, as shipped on phones, have been largely unimpressive. Some found no
effect on sleep onset or quality. The most defensible reading is that any real
benefit comes mostly from the accompanying reduction in brightness rather than
from the colour change.

**That blue light from screens damages the eyes.** There is no good evidence for
this at the intensities screens produce. Eye strain from screen use is real, but
the mechanisms that matter are reduced blink rate, focus distance, and glare, not
wavelength.

## What that means for this app

Horizon dims as well as tints, and the dimming is the part with the stronger
evidence behind it. That is why `WRITE_SETTINGS` is a required permission rather
than an optional one, and why the product reversed an earlier decision to dim
with overlay alpha alone. `requirement.md` section 14 records that reversal.

The warmth control is still worth having. Reasons that do not require the blue
light hypothesis to be true:

- A dimmer, warmer screen is more comfortable to look at in a dark room, and
  comfort is a sufficient reason on its own
- It reduces the perceived brightness of the screen against a dark background,
  which is a glare problem rather than a circadian one
- Some users, particularly those with migraine or photophobia, get real relief
  from specific tints. That is why per-channel control exists rather than only an
  amber preset

What the app will not do is tell you it will improve your sleep. If your goal is
sleep, the more effective moves are dimming everything including the room lights,
and stopping earlier.

## Sources

This document deliberately does not cite a list of study titles without also
representing the studies that disagree, because a one-sided citation list is a
sales tactic wearing a lab coat. When this ships, each claim above should carry a
citation to a review or meta-analysis rather than to a single trial, so a reader
can see the weight of evidence rather than the most convenient result.

TODO(#1): attach citations before the About the science screen ships.
