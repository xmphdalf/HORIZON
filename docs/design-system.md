# Design System

Source of record: the Claude Design handoff bundle in `docs/design/`. Open
`docs/design/project/Blue Light Filter.dc.html` for the screens themselves. This
file records the parts that constrain engineering, so a change to a token or a
constant is reviewable without reading a 126 KB prototype.

Requirements this implements: `requirement.md` section 9, and N-07 and N-10.

---

## 1. The Horizon ramp

Tokens are not fixed values. They are functions of two inputs, warmth and dim.
Every surface interpolates in Oklch along the row, so hue rotates and chroma rises
together while lightness stays pinned to its step. A 2400 K surface is never
brighter than the same token at 3400 K. Dim multiplies lightness afterwards,
uniformly, which is why contrast ratios hold at every brightness.

Four sampled points. These hex values are the acceptance targets for
`core/tokens`: the Oklch implementation must reproduce them.

| Token | OFF / DAY | 3400 K | 2400 K | 1700 K |
|---|---|---|---|---|
| `surface/0` | `#08090B` | `#100C09` | `#140B05` | `#170A02` |
| `surface/1` | `#101216` | `#191310` | `#1E120A` | `#231004` |
| `surface/2` | `#171A1F` | `#221A15` | `#29190F` | `#2F1606` |
| `text/hi` | `#E9ECEE` | `#E5D2BC` | `#DEC198` | `#D3A876` |
| `text/lo` | `#6C7580` | `#7E7062` | `#846248` | `#80532C` |
| `accent` | `#6E7C8C` | `#F2A65A` | `#DE8F41` | `#C97A34` |

The three previews required for every screen by scaffolding.md section 6.4 map
onto this table: **off** is the OFF/DAY column, **mild** is 3400 K, **deep night**
is 1700 K.

### The prototype interpolates in sRGB. Production must not.

`renderVals()` in the bundle mixes RGB component arrays directly. That is an
approximation for a browser mockup and it does not match the table above: at
3400 K it produces `#0E0907` where `surface/0` is `#100C09`. The table is the
truth. `core/tokens` implements Oklch and is tested against the table, not
against the JavaScript.

The prototype remains useful as the reference for *structure*: which tokens exist,
how dim scales each one, and the relative weightings below.

### Dim scaling per token

From the prototype, with `t` the warmth position in `0.0 .. 1.0` and
`b = 0.45 + 0.55 * (dim / 100)`:

| Token | Dim multiplier |
|---|---|
| `surface/0` | `0.55 + 0.45 * b` |
| `text/hi` | `b` |
| `text/lo` | `0.7 + 0.3 * b` |
| `accent` | `0.65 + 0.35 * b` |
| arc fill | `0.7 + 0.3 * b` |

Hairline and arc track are the warmth mix at fixed alpha: `.12` and `.11`.

---

## 2. Elevation

No shadows. A shadow needs light behind the surface to exist, and there is none.

A raised surface is one step up the ramp, about `+3 %` lightness, plus, only where
a boundary must be unambiguous, a 1 px hairline at 8 to 14 percent of the
surface's own hue. Three levels, no more: ground, sheet, control. These are
`surface/0`, `surface/1`, `surface/2`.

---

## 3. The gesture

Anywhere below the status bar is the control. The value moves relative to where
the pointer went down, so touching never causes a jump. There is no track to find
in the dark.

| Axis | Range | Rate |
|---|---|---|
| Vertical | 4600 K down to 1600 K | 4.5 K per dp |
| Horizontal | 8 % up to 100 %, rightward brightens | derived from width |

**The vertical rate is per dp, not per pixel.** 3000 K over a 1080p screen is
1.56 K per physical pixel; the figure the design intends is 4.5 K per dp across
roughly 640 dp of height. The constant is named `KELVIN_PER_DP` for that reason.
It is deliberately coarse: a 40 dp thumb twitch moves about 180 K, below the
noticeable-difference threshold in a dark room.

**The horizontal rate is derived, not literal.** The prototype uses `0.16` percent
per pixel, which is calibrated to its own 340 px frame. On a device it must be
`(100 - 8) / widthDp` so that the documented full-width span holds on every screen.

Other rules:

- Warmth quantises to 50 K steps.
- Axis dominance: diagonal drags apply both, with the non-dominant axis divided
  by 1.4. No axis lock, because locking feels broken when your eyes are closed.
- Detents: soft snap within 60 K of any preset value, one haptic tick each,
  pullable and never sticky. Ticks are the only feedback available at 8 percent.
- Numerals hold full opacity while touching, then 500 ms after release crossfade
  to 22 percent over 600 ms. They never move.
- TalkBack exposes two seekbars, Warmth and Brightness, at 10 percent increments
  with volume-key adjustment. The gesture is an enhancement, never the only path.

---

## 4. Motion

Motion behaves like light. Nothing steps.

| What | Duration | Curve |
|---|---|---|
| Colour crossfade | 420 ms | `cubic-bezier(.32, .02, .2, 1)` |
| Value fade-back | 600 ms, after a 500 ms hold | opacity only, no movement |
| Sheet rise | 520 ms | soft-out, scrim fades rather than sliding |
| Scheduled ramp | the full set duration | a 45 minute ramp is a 45 minute crossfade |
| Reduced motion | 120 ms | shortened, never removed |

Reduced motion never removes a crossfade. An instant tint change is a flash, which
is the one thing this product must never do.

A scheduled ramp is computed from the clock and stepped, never driven by a frame
loop. requirement.md F-18: a 45 minute ramp must not hold the CPU awake.

---

## 5. Typography

Outfit and JetBrains Mono, both bundled in the APK, never fetched. requirement.md
N-08.

| Role | Size | Weight |
|---|---|---|
| `value/96` | 96 | 200 |
| `display/30` | 30 | 200 |
| `title/24` | 24 | 300 |
| `body/15` | 15 | 300 |
| `label/13` | 13 | 500 |
| `spec/12` | 12 | 400, mono |
| `eyebrow/11` | 11 | 400, mono, `.18em` tracking |

Weights stop at 500. Bold text on a dim screen reads as glare. All numerals are
tabular so a changing value never shifts layout. Everything scales with the system
font setting except the 96 px value, which caps at 1.3x to stay on one line.

Outfit and JetBrains Mono are bundled in `core/design/src/main/res/font/`.

---

## 6. Spacing and radii

Spacing: 4, 8, 16, 22, 34.

Radii: 7, 12, 20, 26.

---

## 7. Dark room and accessibility

- **No pure white anywhere.** `text/hi` tops out at `#E9ECEE`. Only the panic strip
  is allowed a light background, and it is the only element in the product above
  80 percent luminance. It appears only after the user has asked for light.
- **Contrast.** Body text holds at least 4.6:1 against its surface at every point
  on the ramp. The 96 px numerals sit at 3.2:1 deliberately: at that size a higher
  ratio is glare, not legibility. These ratios are design targets.
- **Nothing appears without being asked for.** No modals, toasts, or snackbars the
  user did not cause.
- **Every primary control sits in the bottom third**, within 260 px of the bottom
  edge.
- **Targets are 44 px minimum**, 56 px for anything used in the dark. Arc anchors
  get 56 px of invisible padding around a 20 px visible ring.
- **TalkBack reads state before name**, as "On, filter, 1700 kelvin". Every
  colour-carrying element also carries a monogram or a spec, so nothing means
  anything by colour alone.

---

## 8. Screen inventory

The table maps the original prototype to the current Android implementation.

| Screens in the bundle | Module |
|---|---|
| Home: off, mild, deep night, interactive, with banner | `feature/home` |
| Schedule: sun-linked, fixed, shift | `feature/schedule` |
| Presets: list, edit | `feature/presets` |
| Advanced | `feature/settings` |
| About Horizon, inline tip amounts, thank you | `feature/mission` |
| Permission explainer | `feature/permission` |
| Notification: collapsed, expanded | `surface/overlay` |
| QS tile: on, off | `app` |
| Widget: 4x1, 2x2 | `app` |
| Panic disable strip | `surface/overlay` |
| App icon options A through E | `app` |

requirement.md section 14 records icon option B as chosen.
