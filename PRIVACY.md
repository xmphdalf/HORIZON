# Privacy Policy

The app collects nothing, transmits nothing, and stores all settings locally on
the device.

That is the whole policy. The rest of this document explains how you can check it
rather than take it on trust.

## How to verify

**The permission list.** Open Android Settings, find Horizon, and look at its
permissions. `INTERNET` is not there and never will be. An app without it cannot
open a network connection at all: this is enforced by the operating system, not
by our good intentions. The complete list of what the app does request is in
[docs/permissions.md](docs/permissions.md).

**The source.** The whole app is here. The build fails if any manifest declares
`INTERNET`, and fails if any analytics, advertising, crash-reporting, or
networking library appears anywhere on a classpath. Those checks live in
`build-logic/convention/src/main/kotlin/dev/horizon/buildlogic/` and run on every
pull request.

**The build.** Releases are reproducible, so the published APK can be verified
against this source. See [SECURITY.md](SECURITY.md) for the signature.

## What is stored, and where

Settings, presets, and schedules live in a Proto DataStore file in the app's
private storage. Uninstalling the app deletes them. Nothing is backed up to a
cloud service: `allowBackup` is off.

If you use the export feature, you choose the destination and the file is plain
JSON you can read. That is the entire sync story, and it is deliberate: a local
file instead of a hosted service means there is no server holding your data and
no server to breach.

## What is not collected

No accounts. No identifiers. No usage analytics. No crash reports. No advertising
or attribution SDKs. No "anonymous telemetry to improve the product".

There is no crash reporter that transmits, which does mean shipping partly blind.
The alternative under consideration is an on-device log you can read and attach
to an issue yourself, if you want to. It would never send anything on its own.

## Location

If you choose sunset scheduling, the app may ask for coarse location to compute
sunrise and sunset times. That computation happens entirely on the device using a
NOAA algorithm compiled into the app. The coordinates are used to calculate two
times and are not stored, logged, or sent anywhere.

Entering a city by hand does the same job without granting the permission.

## Changes

Any change to this document changes the four commitments in
[requirement.md](requirement.md), which is not something that happens quietly.
The git history of this file is the change log.

## Optional Google Play tips

If you choose to tip, the Play Store processes the payment using your Play account. Horizon displays the price returned by Play and checks whether the purchase completed. It does not collect card details, create a Horizon account, or send a receipt to a Horizon server.
