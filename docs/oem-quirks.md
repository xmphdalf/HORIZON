# Device testing

Record the phone model, Android version, manufacturer software version, and navigation mode when reporting overlay issues. Emulator results do not establish how a manufacturer handles its system bars or background services.

- **Coverage:** Turn the filter on and compare the top and bottom edges in portrait and landscape. Note whether the difference is in app content, a system bar, or the keyboard.
- **Touch:** At the darkest setting, try scrolling and tapping in another app. On Android 12 and later, Horizon reads the device’s maximum obscuring opacity to keep touches working.
- **Brightness:** Record the original brightness and automatic brightness setting. Turn the filter off from the notification and verify both are restored.
- **Background operation:** If a schedule stops working, record the scheduled time, screen-lock state, and battery settings. Include the steps that reproduce it.
