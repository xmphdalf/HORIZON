---
description: Full pre-release gate. Verifies the permission list, the absence of network dependencies, the baseline profile, a reproducible build, APK size, and the changelog. Human-triggered only.
disable-model-invocation: true
---

# Release check

Run every step. Report each as pass or fail with the evidence, not a summary. If
any step fails, the release does not go out.

Do not skip a step because it passed last time.

## 1. The four commitments

```
./gradlew check
```

This runs `permissionAudit`, `dependencyAudit`, `moduleDirection`, `noBangBang`,
and `apkSizeCheck` across every module, plus detekt and lint.

Then confirm by hand, because a gate can only catch what it was told to look for:

```
grep -r "android.permission.INTERNET" --include=AndroidManifest.xml .
./gradlew :app:dependencies --configuration releaseRuntimeClasspath
```

Read the dependency tree yourself. A new transitive dependency that is not on the
deny list but does open sockets is exactly the thing the automated check will
miss.

## 2. Permissions match the documents

The merged release manifest must declare only what `requirement.md` section 8 and
`docs/permissions.md` list, and the in-app permission screen must show exactly
that same set.

```
./gradlew :app:assembleRelease
```

Then read `app/build/intermediates/merged_manifest/release/AndroidManifest.xml`
and compare all three lists by eye. Three places, one list.

## 3. Baseline profile

Regenerate it against the code being shipped:

```
./gradlew :baselineprofile:generateBaselineProfile
```

A stale profile silently costs startup time and `requirement.md` N-04 promises
400 ms.

## 4. Performance

```
./gradlew :benchmark:connectedBenchmarkAndroidTest
```

On a physical device. Cold start within 400 ms, no dropped frames in the drag at
120 Hz. Compare against the committed baseline and report the delta.

## 5. APK size

`apkSizeCheck` enforces the 6 MB budget from `requirement.md` N-03 and runs as
part of step 1. Report the actual figure and the headroom, because a release that
lands at 5.9 MB is a problem worth knowing about even though it passes.

## 6. Reproducible build

`requirement.md` N-09. Build twice from a clean tree and compare the APKs. If
they differ, find out why before shipping: reproducibility is what lets a user
verify the published binary against this source, and it is part of the privacy
claim rather than a nicety.

## 7. Accessibility

Run `/a11y-audit` across every feature module. TalkBack, 200 percent font,
contrast at both ends of the ramp.

## 8. Documents

- `CHANGELOG.md` has an entry for this version, with the date
- Version code and name are bumped in `app/build.gradle.kts`
- Any decision made during this cycle is recorded in `requirement.md` section 14
- Any question resolved is removed from `requirement.md` section 15
- `docs/oem-quirks.md` reflects what was actually tested

## 9. Signature

Confirm the release is signed with the expected key and publish the signature
alongside the APK. Clones repackaged with ads are inevitable under a free
licence, and the published signature is how a user tells them apart.

Never read, print, or commit the keystore. `.claude/settings.json` denies access
to it and that denial is not to be worked around.
