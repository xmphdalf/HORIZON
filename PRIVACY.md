# Privacy

Horizon runs the filter on your device. It has no Horizon account, advertising, or analytics. The app’s merged manifest omits the Android internet permission.

## Local settings

Horizon stores presets, schedules, app exclusions, and filter preferences in private app storage. It saves your brightness and brightness mode so it can restore them when the filter turns off. Android cloud backup is disabled.

Solar scheduling uses a selected city or coordinates you enter. If you grant approximate location access, Horizon suggests a nearby supported city and asks you to confirm it. Sunrise and sunset are calculated on the device.

If you enable per-app exclusions, Horizon uses Android usage events to identify the foreground app and decide when to suspend the tint. You can revoke usage access in Android settings. [Permission details](docs/permissions.md)

## Optional tips

Google Play processes one-time tips through your Play account. Horizon displays the price supplied by Play and checks the purchase status. Your card details stay with the payment provider.
