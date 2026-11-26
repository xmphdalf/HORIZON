# HORIZON: Product Requirements

Status: v1.0
Platform: Android only. See section 15 for the iOS decision.
License model: free, every feature, forever.

Writing conventions for every document in this repo: ASCII only, no em dashes, no emoji, no decorative characters.

---

## 1. What Horizon Is

A system-wide screen warmth and dimming utility for Android. It tints the display toward amber, dims it below the manufacturer's brightness floor, and schedules itself around local sunset.

The interface is built around one idea: the screen is the preview. Dragging a control warms the entire app in real time, so the user feels the change rather than reading a number. The same arc geometry appears on the home screen, the schedule, the widget, the tile, and the notification, so the manual control and the schedule are visibly the same object.

Success is a person installing it once, configuring it in ninety seconds, and never thinking about it again.

---

## 2. Core Ideology

Screen comfort software has been colonized by dark patterns: filters that nag, tint apps that watch you, premium tiers that hold a single slider hostage. Horizon is the inverse.

**The four commitments.**

1. **No paywall.** There is no free tier, because there is no paid tier. Every feature in this document ships unlocked on day one.
2. **No advertising.** No banners, no interstitials, no sponsored presets, no affiliate links.
3. **No data collection.** No accounts. No analytics. No crash reporter that transmits. No `INTERNET` permission at all.
4. **No dishonesty.** Horizon does not overstate the science of blue light in order to sell itself. It states plainly what is well evidenced (brightness and timing affect sleep) and what is contested (blue wavelengths specifically).

**Why giving up revenue is the point.** An app requesting "draw over other apps" has the exact shape of malware. Trust is the only currency in this category. Having nothing to sell is what makes the trust claim credible instead of marketing copy.

**Non-goals.** Growth targets. Engagement metrics. Retention optimization. A user base as an asset.

---

## 3. Sustainability Without Revenue

Free is an architectural constraint, not only a pricing decision.

| Cost center | Mitigation |
|---|---|
| Servers | None. Fully offline. Nothing to pay for or keep patched. |
| Sync | Local export and import files instead of hosted sync |
| Store fees | Google Play one-time 25 USD. F-Droid free. |
| Support burden | Thorough FAQ; issues via public tracker only, no email SLA |
| Development | Open source, so the project survives the author losing interest |

**Donations are strictly passive.** A quiet entry point in the app, described in section 10. Never a modal, never a nag, never a badge, never gated behind a feature. If a donation prompt ever appears more than once per install without the user opening it, that is a bug.

**Forbidden permanently:** telemetry sold or shared, VPN bundling, recommended-app placements, and any acquisition that flips a commitment.

---

## 4. Platform Reality

These constraints shape the product more than any design preference. They are not implementation detail; they change what can be promised.

**4.1 The touch opacity ceiling.** Android 12 and higher block touch events from untrusted overlays. A `TYPE_APPLICATION_OVERLAY` window is untrusted, and touches only pass through when combined opacity is at or below the system maximum obscuring opacity, which defaults to 0.8. Above that, the phone stops responding to touch entirely.

Consequence: an overlay alone cannot reach the deep dim this product needs. Horizon must lower real system brightness as well as layering the overlay, and must read the cap at runtime rather than assuming 0.8, because manufacturers can lower it.

**4.2 Foreground service ordering.** On API 35 and higher, an app holding `SYSTEM_ALERT_WINDOW` can only start a foreground service from the background if it already has a visible overlay window. Horizon must add the window first, then start the service, or the system throws `ForegroundServiceStartNotAllowedException`. This affects boot restore and scheduled starts.

**4.3 Coverage limits.** `TYPE_APPLICATION_OVERLAY` cannot cover the status bar, the navigation bar, or certain system dialogs and permission prompts. This is by design. Horizon documents it rather than working around it.

**4.4 Target API.** Google Play requires new apps and updates to target Android 16 (API 36) from 2026-08-31. Horizon targets 36 from the first commit. Minimum is API 26.

**4.5 Manufacturer battery managers** kill foreground services aggressively on some skins. Horizon detects the kill and warns, and ships a per-manufacturer guide.

---

## 5. Target Users

- **P1, the late-night reader.** Reads in a dark bedroom. Cares most about getting dimmer than the OS allows. Configures once and forgets.
- **P2, the photophobic or migraine sufferer.** Needs non-amber tints and precise control. Currently underserved; most apps ship only "warm."
- **P3, the privacy-conscious installer.** Reads the permission list before installing. Checks whether the app requests network access. Reachable mainly through F-Droid and a public repository.
- **P4, the shift worker.** Inverted schedule. Clock-based automation is useless to them.

---

## 6. Functional Requirements

Priority: **M** must have for v1.0, **S** should have, **C** could have after 1.0.

### 6.1 Filter engine

| ID | Requirement | Pri |
|---|---|---|
| F-01 | System-wide color overlay via `TYPE_APPLICATION_OVERLAY` | M |
| F-02 | Color temperature control, 1000 K to 6500 K, continuous | M |
| F-03 | Filter opacity control, independent of temperature, capped at the runtime value of `InputManager.getMaximumObscuringOpacityForTouch()` | M |
| F-04 | Dimming below the system floor, achieved by combining overlay opacity with lowered system brightness. The user's brightness value is saved before any change and restored on disable. | M |
| F-05 | Per-channel RGB control for custom tints, serving P2 | S |
| F-06 | Named presets: Reading, Deep Night, Migraine, Custom. User-creatable, renameable, deletable with an eight-second undo strip and no confirmation dialog. | M |
| F-07 | Unlimited profiles, each storing temperature, dim, RGB, and schedule binding | S |
| F-08 | **Hard safety floor.** No combination of settings may make the screen unreadable to the point that the user cannot reach a disable control. Panic disable (F-24) must remain functional at the darkest setting. | M |
| F-09 | Gradual transitions, configurable from instant to sixty minutes | S |
| F-10 | Night grayscale, independent of tint | C |

### 6.2 Scheduling and automation

| ID | Requirement | Pri |
|---|---|---|
| F-11 | Manual on and off, always available | M |
| F-12 | Fixed clock time schedule | M |
| F-13 | Sunset and sunrise schedule computed locally from coarse location or a manually entered city. No network call. | M |
| F-14 | Offsets set by dragging an anchor along the arc, in five-minute steps, with a tick at exact sunset. No numeric entry. | S |
| F-15 | Inverted schedule for P4, filling the middle of the arc rather than its ends | S |
| F-16 | Ambient light sensor mode. Optional, and labeled as battery-relevant. | C |
| F-17 | Automation hooks: intent API for Tasker, Quick Settings tile, home screen shortcuts | S |
| F-18 | Scheduled ramps are computed from the clock and stepped at intervals, never driven by a frame-rate animation loop. A forty-five minute ramp must not hold the CPU awake. | M |

### 6.3 Per-app behavior

| ID | Requirement | Pri |
|---|---|---|
| F-19 | Per-app exclusion list | M |
| F-20 | Sensible defaults pre-excluded: camera, gallery, photo and video editors. User can clear them. | S |
| F-21 | Per-app profiles, not only on and off | C |
| F-22 | Foreground app detection uses the least invasive API available. If it requires Usage Access, that permission is optional and the feature degrades gracefully when denied. | M |

### 6.4 Controls and escape hatches

| ID | Requirement | Pri |
|---|---|---|
| F-23 | Persistent notification with a turn-off action, intensity step controls, and pause. Low priority, silent. The turn-off action is always first in the row. | M |
| F-24 | **Panic disable.** Removes the overlay and restores saved system brightness in one action. Available from the notification, the 2x2 widget, and the launcher icon long-press menu. Never behind a confirmation, never behind an unlock, never waits on disk IO. Fixed position so it can be hit from muscle memory. | M |
| F-25 | Quick Settings tile. Long-press cycles the last three presets in place. | M |
| F-26 | Timed bypass: pause for five, fifteen, or sixty minutes with auto-resume | S |
| F-27 | Boot persistence, opt-in, respecting the service ordering rule in 4.2 | M |

### 6.5 Comfort extras, all free

| ID | Requirement | Pri |
|---|---|---|
| F-28 | Settings export and import as a plain JSON file. This is the sync story, without a server. | S |
| F-29 | An honest "About the science" screen: what the evidence supports, what it does not, with citations. Required by commitment four. | M |
| F-30 | 20-20-20 break reminders. Off by default. No streaks, no gamification, no guilt copy. | C |
| F-31 | Jet lag mode: enter origin, destination, and flight time; receive a day-by-day light schedule | C |

---

## 7. Non-Functional Requirements

- **N-01 Battery.** Idle overlay under one percent per day. Ambient sensor mode may cost more and must disclose it.
- **N-02 Memory.** Resident service under 40 MB.
- **N-03 APK size.** Release APK under 6 MB. Raised from an earlier 5 MB target to accommodate Compose, Glance, and bundled fonts. No analytics, ad, or crash libraries.
- **N-04 Cold start.** Settings UI interactive within 400 ms on a mid-range 2020 device, with a baseline profile shipped.
- **N-05 Frame timing.** No dropped frames during the drag gesture at 120 Hz. The overlay color path must not depend on Compose recomposition.
- **N-06 Compatibility.** API 26 through current. Graceful degradation on skins that restrict overlays, with an in-app guide for their permission screens.
- **N-07 Accessibility.** Full TalkBack support. Respects system font scaling to 200 percent. No color-only affordances: the Migraine preset must be identifiable greyscaled or read aloud.
- **N-08 Offline.** One hundred percent of functionality works in airplane mode, permanently. Fonts are bundled in the APK, never fetched.
- **N-09 Reproducible builds**, so the published APK can be verified against the public source.
- **N-10 Contrast.** Body text holds at least 4.6:1 against its surface at every point on the ramp. Large numerals may sit lower deliberately; above roughly 3:1 at 96 px, more contrast is glare rather than legibility.

---

## 8. Permissions and Privacy

This list is the complete set. The in-app permission screen must show exactly this list and nothing else. Any addition requires updating this table, `docs/permissions.md`, and the in-app screen in the same commit. CI fails if the merged manifest contains a permission not listed here.

| Permission | Why | Optional |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | The overlay itself | No |
| `WRITE_SETTINGS` | Lowering and restoring system brightness, required to dim past the touch opacity ceiling (4.1) | No |
| `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_SPECIAL_USE` | Keeping the overlay alive | No |
| `POST_NOTIFICATIONS` | The control notification | Yes |
| `RECEIVE_BOOT_COMPLETED` | Restore state after reboot | Yes |
| `PACKAGE_USAGE_STATS` | Per-app rules (F-19). Feature disabled if denied. | Yes |
| `ACCESS_COARSE_LOCATION` | Local sunset calculation. Manual city entry is the alternative. | Yes |

**`INTERNET` is never declared.** This is the single most legible privacy signal available in this category and it is treated as a product feature, stated on the store listing and in the app. Any future feature requiring it is rejected.

**Privacy policy, in full:** the app collects nothing, transmits nothing, and stores all settings locally on the device.

**Permission screen requirements.** One screen, not a carousel. It names the alarming permission first, in Android's own words, before asking for anything. Every claim is falsifiable by checking Settings, not a promise about intentions. No shield graphics, no green checkmarks, no lock icons; reassurance imagery is what malware uses. The decline action is a real exit at full visual weight.

---

## 9. Design System Reference

The visual direction is Horizon, delivered as a design handoff bundle. Full detail lives in `docs/design-system.md`. The requirements that constrain engineering:

- **One ramp.** Color tokens are not fixed values. They are functions of two inputs, warmth and dim, interpolated in Oklch so hue rotates and chroma rises together while lightness stays pinned to its step. Dim multiplies lightness afterward, uniformly, which is why contrast ratios hold at every brightness.
- **Elevation without shadow.** Shadows need light behind the surface. A raised surface is one step up the ramp plus, only where a boundary must be unambiguous, a hairline at 8 to 14 percent of the surface's own hue. Three levels: ground, sheet, control.
- **No pure white anywhere** except the panic strip, which is the only element allowed above 80 percent luminance and appears only after the user has asked for light.
- **Motion behaves like light.** Color crossfades over 420 ms, never steps. No springs, no overshoot, no scale-in. Reduced motion shortens crossfades to 120 ms but never removes them, because an instant tint change is a flash, which is the one thing this product must never do.
- **Nothing appears without being asked for.** No modals, toasts, or snackbars the user did not cause.
- **Every primary control sits in the bottom third** of the screen.
- **Typography.** Outfit and JetBrains Mono, both bundled. Weights stop at 500; bold on a dim screen reads as glare. All numerals tabular so a changing value never shifts layout.
- **App icon: option B, Horizon.** A sun half-set behind the arc. Adaptive icon foreground is the arc alone on a flat near-black background; nothing critical enters the outer 18 percent. A monochrome variant is required for themed icons. The icon never tracks state, because a launcher icon that changes is an attention mechanic.

---

## 10. Donations

**Entry point.** A small element in the middle of the home layout, above the preset chips and below the value readout: quieter than either real control, inside the thumb zone but behind both in reading order. It tints and dims with the rest of the app, so it lives in the room rather than sitting on top of it.

**Three states.** Collapsed pill with one line of text. Expanded card with the mission copy and two actions. After one expansion it collapses permanently to a bare ring, still tappable, still carrying its full TalkBack label, never badged, never re-expanded, never animated again.

**Fairness requirements, non-negotiable.**
- The decline action and the donate action share the same height, radius, fill, border, font, weight, and color, differing only in label, at fifty-fifty width. Nothing is ghosted, nothing is default-focused.
- No amount is preselected, marked popular, or visually emphasized. Continue stays inert until an amount is chosen.
- The decline option in the donate sheet is a full-size button beside the others, not a text link.
- Zero is a valid amount and returns straight to the app with no confirmation and no second ask.

**Implementation constraint, unresolved.** Google Play Billing uses fixed product IDs. It has no arbitrary amount and no zero. A custom amount therefore requires an external payment link opened as a browser intent, which needs no `INTERNET` permission but is a different screen than the one designed. Google Play policy on donations by individual developers must be checked before this ships. See section 15.

---

## 11. Roadmap

**v1.0, the honest filter.** All M items. Ships as a complete product, not a trial.

**v1.1, control.** Remaining S items: per-channel RGB, profiles, ramping, inverted schedule, export and import, automation hooks.

**v1.2, comfort.** C items: ambient sensor, per-app profiles, grayscale, break reminders, jet lag.

**Post-1.2, desktop companion.** Windows, macOS, Linux, sharing the same profile JSON. Also free.

---

## 12. Out of Scope

- Accounts, login, or cloud sync
- Sleep tracking, health data integration, or wellness scoring
- Social features, sharing, leaderboards
- Screen-time blocking or app-locking. Different product, different ethics.
- Medical claims of any kind

---

## 13. Risks

| Risk | Response |
|---|---|
| Play restricts overlay apps or the foreground service type | F-Droid and direct APK are first-class channels, not fallbacks |
| Manufacturer battery managers kill the service | In-app per-manufacturer guide; detect the kill and warn |
| Users expect paid-app support responsiveness | Store listing states plainly: community support, no SLA |
| Maintainer burnout with no revenue | Open source from the first commit; architecture documented for contributors |
| Clones repackage it with ads | Inevitable under a free license. Mitigate with a published signature and an official-builds page. |
| Play policy blocks the donation flow | Donations are decorative to the product. If blocked, remove them; nothing else changes. |

---

## 14. Decision Log

Recorded so settled questions are not silently reopened.

| Date | Decision | Reason |
|---|---|---|
| v0.1 | Drop `WRITE_SETTINGS`, dim via overlay alpha alone | Believed a shorter permission list was achievable |
| v1.0 | **Reversed. `WRITE_SETTINGS` is required.** | The 0.8 touch opacity ceiling (4.1) means overlay alpha alone cannot reach a usable dark state without blocking all touch input |
| v1.0 | Native Kotlin, not Flutter, React Native, or KMP | The core feature needs a platform channel, the APK grows by megabytes, and a bridge sits between the thumb and the pixel |
| v1.0 | Overlay is a plain View, app UI is Compose | A solid color needs no recomposition loop |
| v1.0 | App icon option B, Horizon | Strongest silhouette at 24 px; accepted proximity to sunset iconography |
| v1.0 | APK budget raised from 5 MB to 6 MB | Compose, Glance, and bundled fonts |

---

## 15. Open Questions

1. **License.** GPLv3 blocks closed forks but is awkward against some app store terms. MPL-2.0 is more permissive with weaker anti-clone protection. Blocks the first public commit.
2. **iOS.** A system-wide filter is impossible there. Options: skip iOS entirely, ship a limited in-app reader with the filter applied, or publish a Shortcuts-based guide instead of an app. Currently leaning toward skipping.
3. **Donation mechanism.** Play Billing versus an external link, and what Play policy currently permits for an individual developer. Blocks section 10.
4. **Foreground service type.** Which declared type fits a screen filter on API 34 and above, and what justification Play requires for it.
5. **Migraine preset reachability.** The preset sits at 1000 K but the drag range bottoms out at 1600 K, so it is preset-only. Extend the gesture range or accept the split, deliberately.
6. **Dim floor by panel type.** How far the combined overlay and brightness reduction can go on OLED versus LCD before F-08 is violated.
7. **Crash reporting.** Zero network is the cleanest promise but means shipping blind. Alternative: an on-device crash log the user can export manually and attach to an issue.
8. **Advanced dim path.** Android's Reduce Bright Colors remaps RGB values before rendering and has no opacity ceiling, but it is a secure setting requiring a one-time ADB grant of `WRITE_SECURE_SETTINGS`. Worth investigating as a documented power-user option. Never the default path.
