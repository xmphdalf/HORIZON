---
name: Bug report
about: Something does not work as described
labels: bug
---

## What happened

<!-- What you saw. -->

## What you expected

<!-- What you expected instead. -->

## Steps to reproduce

1.
2.
3.

## Device

Required. Battery managers on some manufacturer skins kill foreground services
aggressively, and that is the single most common cause of "the filter stopped
working". Knowing your skin is usually the difference between a fix and a
guess.

- Device:
- Android version:
- Manufacturer skin and version (One UI, MIUI, HyperOS, ColorOS, and so on):
- Horizon version:
- Installed from (F-Droid, Play, or a direct APK):

## If the filter stopped on its own

- [ ] It stopped after the screen was off for a while
- [ ] It stopped after the app was swiped out of recents
- [ ] It stopped after a reboot
- [ ] Battery optimisation is disabled for Horizon
- [ ] Autostart is enabled for Horizon, if your skin has that setting

## If the screen became hard to read

Say so first and clearly. `requirement.md` F-08 makes it a hard safety floor that
no combination of settings leaves you unable to reach a disable control, so this
is a serious bug rather than a preference.

- Did panic disable work?
- Was system brightness restored to what it was before?

## Logs

If you can capture one, `adb logcat` output around the failure helps. There is no
crash reporter that transmits, so nothing reaches us unless you send it.
