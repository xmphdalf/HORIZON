# Security

## Reporting a vulnerability

Open a private security advisory through the repository's security tab. If that
is not available to you, open a normal issue that says only that you have found a
security problem and would like a private channel, without describing the problem
itself.

There is no bounty. This is a free project with no revenue.

Expect a slower response than a commercial project would offer. That is stated
plainly here rather than promised and then missed.

## Threat model

Horizon holds no user data worth stealing: no accounts, no identifiers, no
history. The interesting attack surface is not data theft, it is the overlay
permission itself.

`SYSTEM_ALERT_WINDOW` is a genuinely dangerous permission. An overlay can be used
for tapjacking, where an attacker draws a transparent window over the screen to
capture or misdirect touches. The mitigations that matter here are:

- The overlay is a solid colour with no interactive elements. It never handles a
  touch event.
- Combined opacity is capped at the value the system reports at runtime from
  `InputManager.getMaximumObscuringOpacityForTouch()`, never a hardcoded
  constant, because manufacturers can lower it. Exceeding that cap is what makes
  a device stop responding to touch.
- The panic disable path removes the overlay and restores brightness without
  waiting on disk IO, so it cannot be wedged by a slow or corrupt data store.

The other real risk is availability, not confidentiality: a bug that leaves the
screen too dark to operate. `requirement.md` F-08 makes that a hard safety floor,
and the instrumented tests for `surface/overlay` cover it.

## Supply chain

The dependency list is short and deliberately so. `dependencyAudit` fails the
build on any analytics, advertising, crash-reporting, or networking coordinate
anywhere on a classpath, including transitive ones.

The app declares no `INTERNET` permission, so even a fully compromised dependency
cannot open a network connection from within the app process.

Releases are reproducible so the published APK can be verified against this
source. Verify the signature before installing a build you did not compile
yourself, especially one obtained outside F-Droid or Play. Clones repackaged with
ads are inevitable under a free licence; the published signature is how you tell
them apart.

## Signing material

No keystore, key, or credential is ever committed. `.gitignore` excludes `*.jks`,
`*.keystore`, `keystore/`, and `local.properties`, and `.claude/settings.json`
denies reading them.

If you believe a key has been exposed, treat it as compromised and say so in a
security advisory rather than opening a public issue.
