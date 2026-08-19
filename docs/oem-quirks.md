# Manufacturer Quirks

`requirement.md` N-06 requires graceful degradation on skins that restrict
overlays, and section 13 lists battery managers killing the foreground service as
a top risk. This file is where per-manufacturer behaviour is recorded as it is
confirmed on real devices.

**Nothing here is verified yet.** The app does not exist. The sections below name
the behaviours that are known to exist in this product category so that testing
has a checklist, and each one is marked unverified until someone reproduces it on
hardware. Do not ship user-facing guidance from this file until its entries carry
a device and an OS version.

## What to test on every device

1. Does the overlay survive the screen locking and unlocking?
2. Does it survive the app being swiped out of recents?
3. Does it survive an hour idle with the screen off?
4. Does it come back after a reboot, with boot restore enabled?
5. Does the touch opacity cap read back as 0.8, or has the manufacturer lowered it?
6. Does lowering system brightness through `WRITE_SETTINGS` stick, or does an
   adaptive brightness feature fight it?
7. Does panic disable restore the original brightness exactly?

## Known behaviour by skin

### Xiaomi, MIUI and HyperOS

Unverified. Historically the most aggressive skin in this category. Expect a
separate autostart permission that is not the standard overlay grant, and a
battery saver that stops foreground services regardless of their type.

### Samsung, One UI

Unverified. Expect a "Put unused apps to sleep" setting that must be turned off
per app.

### Huawei and Honor, EMUI and MagicOS

Unverified. Expect a protected apps list separate from the standard battery
settings.

### Oppo, Realme, and OnePlus, ColorOS and OxygenOS

Unverified. Expect aggressive background limits and a separate autostart toggle.

### Vivo, Funtouch and OriginOS

Unverified. Expect a high background power consumption prompt.

## The opacity cap

Android 12 and above block touch events from untrusted overlays when combined
opacity exceeds the system maximum obscuring opacity. The platform default is
0.8, but it is a device configuration value and a manufacturer can lower it.

This is why the code reads
`InputManager.getMaximumObscuringOpacityForTouch()` at runtime and never
hardcodes 0.8. Recording per-device values here is useful once devices are
tested, because a device with an unusually low cap will need more of its dimming
to come from real brightness reduction.

| Device | Android version | Reported cap | Confirmed by |
|---|---|---|---|
| | | | |

## Detecting a kill

When the foreground service is killed by a battery manager, the app should notice
on next launch and say so plainly, then link to the guide for that manufacturer.
It should not silently restart and pretend nothing happened, and it should not
nag: one clear explanation, once.

TODO(#2): populate this file from real device testing before the first release.
