# Architecture

Horizon is a single-activity Kotlin app with a Jetpack Compose interface. The activity hosts navigation and binds to a foreground service so the tint can stay active when the user leaves Home.

| Area | Responsibility |
|---|---|
| `app` | Navigation, permissions, alarm scheduling, widgets, the Quick Settings tile, and Google Play Billing |
| `feature/*` | Compose screens for Home, schedules, presets, settings, permissions, and the maker’s note |
| `core/design` | Shared palette, fonts, and Compose controls |
| `core/data` | Reading and writing named presets in local preferences |
| `core/solar` | Computing sunrise and sunset for a date, location, and time zone |
| `surface/overlay` | Touch-through overlay, brightness capture and restoration, and notification controls |

During a drag, the service updates the existing overlay window. When the gesture finishes, it commits the settings. Turning the filter off removes the window and restores the saved brightness and brightness mode.

The app schedules local alarms for the next start and stop events. Clock and time-zone changes trigger recalculation. Optional usage access lets the service suspend the filter when an excluded app is in the foreground.

Dependency versions are pinned in [the version catalog](../gradle/libs.versions.toml). Shared build conventions and permission audits live in [build logic](../build-logic/convention).
