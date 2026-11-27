<p align="center"><img src="docs/assets/horizon.svg" width="88" height="88" alt="Horizon’s amber arc and sun"></p>
<h1 align="center">Horizon</h1>
<p align="center">A warmer, dimmer screen for late-night reading on Android.</p>

Horizon lets you adjust warmth and brightness with a thumb, save a comfortable preset, and have it switch on at sunset. The filter works offline. Every feature is free.

## Try the Android test build

Open a successful run in [Android test APK](https://github.com/xmphdalf/HORIZON/actions/workflows/test-apk.yml) while signed in to GitHub. Under **Artifacts**, download and unzip **Horizon-test**, then install `Horizon-test.apk` on Android 8 or later.

Tap **Turn filter on**. Android will ask for permission to display the tint over other apps and to adjust system brightness. Drag up to warm the screen or left to dim it. To restore your original brightness, tap **Turn off** in the notification.

## Everyday controls

- **Presets** let you switch between Reading and Deep Night or save your own warmth, brightness, and tint.
- **Schedules** follow fixed times or locally calculated sunrise and sunset. Choose a city or enter coordinates to use solar scheduling.
- **Quick Settings, widgets, and the notification** put the filter within reach while another app is open.
- **Per-app exclusions** let you turn the filter off for selected apps when usage access is enabled.

## Build locally

Use JDK 17 and Android SDK Platform 37. From the repository root, run:

```sh
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. For checks and device tests, see [Contributing](CONTRIBUTING.md).

## Project notes

Horizon stores filter settings on your device. Google Play handles optional one-time tips through **Why Horizon is free** on Home. [Privacy](PRIVACY.md) · [Android permissions](docs/permissions.md)

[Architecture](docs/architecture.md) · [Design](docs/design/README.md) · [Science behind the filter](docs/science.md) · [GPLv3 license](LICENSE)
