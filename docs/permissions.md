# Permissions

This is the complete set. It mirrors `requirement.md` section 8 exactly.

The `permissionAudit` Gradle task parses the table below and fails the build if
the merged manifest declares anything that is not in it. The in-app permission
screen must show exactly this list and nothing else. Any addition requires
updating `requirement.md` section 8, this file, and the in-app screen in the same
commit.

| Permission | Why | Optional |
|---|---|---|
| `android.permission.SYSTEM_ALERT_WINDOW` | The overlay itself | No |
| `android.permission.WRITE_SETTINGS` | Lowering and restoring system brightness, required to dim past the touch opacity ceiling | No |
| `android.permission.FOREGROUND_SERVICE` | Keeping the overlay alive | No |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | User-enabled screen tint service on API 34 and above | No |
| `android.permission.POST_NOTIFICATIONS` | The control notification | Yes |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Restore state after reboot | Yes |
| `android.permission.PACKAGE_USAGE_STATS` | Per-app rules. Feature disabled if denied. | Yes |
| `android.permission.SCHEDULE_EXACT_ALARM` | User-selected schedule times | Yes |
| `android.permission.ACCESS_COARSE_LOCATION` | Local sunset calculation. Manual city entry is the alternative. | Yes |

## INTERNET is never declared

This is the single most legible privacy signal available in this category and it
is treated as a product feature, stated on the store listing and in the app. Any
future feature requiring it is rejected.

The audit fails on `android.permission.INTERNET` specifically, before it checks
anything else, so the failure message names the commitment rather than reading as
a generic list mismatch.

## Manual filter build

The manual filter declares overlay access, write settings, foreground service,
its special-use type, and notifications. The service subtype describes the
user-enabled tint and immediate off action. Store review is pending.
