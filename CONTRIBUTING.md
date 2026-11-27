# Contributing

## Development setup

Use JDK 17 and Android SDK Platform 37. Run the project checks from the repository root.

```sh
./gradlew check
./gradlew -p build-logic test
./gradlew :app:connectedDebugAndroidTest
```

The last command requires a connected device or emulator. CI runs the device tests on API 26 and 36.

## The gates will stop you

`check` runs four structural gates. They are not advisory:

| Gate | Fails when |
|---|---|
| `moduleDirection` | A module depends on one its layer may not reach |
| `noBangBang` | A `!!` appears in any Kotlin source |
| `dependencyAudit` | An analytics, ads, crash-reporting, or networking coordinate resolves |
| `permissionAudit` | The merged manifest declares a permission `docs/permissions.md` does not list |
| `apkSizeCheck` | The release APK exceeds 6 MB |

If a gate is wrong, change the gate and its test in the same commit, and say why
in the pull request. Do not work around one.

## Writing code

The bar is in [scaffolding.md](scaffolding.md) section 6. The parts people trip
on most:

**Comments explain why, never what.** The code already says what it does. Write a
comment when a platform constraint forced the shape of the code, when a value was
derived and the derivation is not obvious, when something looks wrong but is
correct, or as KDoc for a non-obvious public contract.

This is a good comment because it explains a constraint you cannot see:

```kotlin
// Touches are blocked when combined overlay opacity exceeds the system cap
// (Android 12+, InputManager.getMaximumObscuringOpacityForTouch, default 0.8).
// Read it at runtime because OEMs can lower it.
```

This is not a comment, it is noise:

```kotlin
// Set the alpha
view.alpha = alpha
```

**No commented-out code.** Git remembers it.

**`TODO` carries an issue number.** `TODO(#42): ...` passes; a bare `TODO:` fails
detekt.

## Writing prose

ASCII only. No em dashes, no emoji, no decorative characters. Plain hyphens,
plain quotes. This applies to documentation, comments, commit messages, and pull
request descriptions.

Be direct. State the constraint and the reason.

## Pull requests

A change is done when all of these hold:

- detekt, lint, and the compiler pass with no warnings
- Tests cover the new behaviour. A bug fix has a test that failed before the fix
  and passes after; write the failing test first so you know it covers the bug
- No new permission, or the permission is added to `requirement.md` section 8,
  `docs/permissions.md`, and the in-app permission screen in the same commit
- No new dependency, or it is justified in the pull request and adds no network
  capability
- Public API has KDoc, and no comment restates code
- Previews exist at three ramp points for any UI change
- TalkBack labels exist for any new interactive element
- The APK size delta is reported in the pull request
- If the change touches the overlay, an instrumented test proves the opacity cap
  and the brightness restore still hold

## The overlay is different

`surface/overlay` is the riskiest module in the project. A mistake there can make
a phone stop responding to touch entirely, which the user cannot easily undo.

Read `.claude/rules/overlay.md` before editing anything under it, and write the
instrumented test first if the change touches opacity, brightness, or the service
lifecycle.

## Reporting bugs

Include your device model, Android version, steps to reproduce, and what you expected to happen. Report security issues privately using the [security policy](SECURITY.md).
