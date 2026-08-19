# Horizon

A system-wide screen warmth and dimming utility for Android. It tints the display
toward amber, dims it below the manufacturer's brightness floor, and schedules
itself around local sunset.

Free. Every feature, forever. No paywall, no advertising, no data collection, and
no `INTERNET` permission at all.

## Status

Scaffolding. The repository structure, build logic, and quality gates are in
place and green. The application itself is not built yet. See
[CHANGELOG.md](CHANGELOG.md).

## Why it exists

Screen comfort software has been colonised by dark patterns: filters that nag,
tint apps that watch you, premium tiers that hold a single slider hostage.
Horizon is the inverse.

An app requesting "draw over other apps" has the exact shape of malware. Trust is
the only currency in this category. Having nothing to sell is what makes the
trust claim credible instead of marketing copy.

Every claim here is meant to be falsifiable by checking Android's own settings,
not believed on the strength of a privacy policy.

## What it does

- System-wide colour temperature control, with per-channel RGB for users who need
  a tint other than amber
- Dimming below the system floor, by combining the overlay with real brightness
  control
- Schedules on a fixed clock, on local sunrise and sunset computed offline, or
  inverted for shift workers
- Presets, a Quick Settings tile, home screen widgets, and a control notification
- Panic disable that restores brightness and removes the tint in one action, from
  the notification, the widget, or the launcher icon

## Building

Requires JDK 17 or newer and the Android SDK.

```
./gradlew build     Compile everything and run the tests
./gradlew check     The above plus detekt, lint, and the structural gates
```

## Privacy

The app collects nothing, transmits nothing, and stores all settings locally on
the device. There is no network code because there is no network permission.

The complete permission list is in [docs/permissions.md](docs/permissions.md).
The build fails if the merged manifest ever declares anything outside it, and
fails by name on `INTERNET`.

See [PRIVACY.md](PRIVACY.md).

## Documentation

| Document | Holds |
|---|---|
| [requirement.md](requirement.md) | Product requirements, permissions, budgets |
| [scaffolding.md](scaffolding.md) | Repository contract and code quality bar |
| [docs/architecture.md](docs/architecture.md) | Module graph |
| [docs/design-system.md](docs/design-system.md) | The Horizon ramp, gesture, motion |
| [docs/science.md](docs/science.md) | What the evidence supports, and what it does not |
| [docs/oem-quirks.md](docs/oem-quirks.md) | Per-manufacturer behaviour |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). The short version: the four commitments
are not negotiable, and a change that adds a network capability will be declined
regardless of what it enables.

## Licence

GPLv3. See [LICENSE](LICENSE).

Strong copyleft is deliberate. When the privacy claim is the product, a derivative
must not be able to close the source and quietly add network code.
