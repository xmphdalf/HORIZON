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
| `android.permission.POST_NOTIFICATIONS` | The control notification | Yes |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Restore state after reboot | Yes |
| `android.permission.PACKAGE_USAGE_STATS` | Per-app rules. Feature disabled if denied. | Yes |
| `android.permission.ACCESS_COARSE_LOCATION` | Local sunset calculation. Manual city entry is the alternative. | Yes |

## INTERNET is never declared

This is the single most legible privacy signal available in this category and it
is treated as a product feature, stated on the store listing and in the app. Any
future feature requiring it is rejected.

The audit fails on `android.permission.INTERNET` specifically, before it checks
anything else, so the failure message names the commitment rather than reading as
a generic list mismatch.

## Currently declared

The scaffold declares only the two non-optional permissions the overlay will
need:

- `SYSTEM_ALERT_WINDOW`
- `FOREGROUND_SERVICE`

The remaining five land with the features that need them, in the same commit as
their entry on the in-app permission screen. The audit checks that nothing
outside the table appears; it does not require every row to be declared.

## Open

The foreground service type for API 34 and above is unresolved. See
`requirement.md` section 15.4. No type is declared yet because no service exists
yet, and `FOREGROUND_SERVICE_*` type permissions will need adding to the table
above when it is chosen.
