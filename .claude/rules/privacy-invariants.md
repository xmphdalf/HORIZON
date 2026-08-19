# Privacy Invariants

Always loaded. These are the commitments the product is built on, not
preferences. Breaking one is not a tradeoff to weigh, it is a different product.

## The four commitments

From `requirement.md` section 2:

1. **No paywall.** No free tier, because there is no paid tier.
2. **No advertising.** No banners, interstitials, sponsored presets, or affiliate links.
3. **No data collection.** No accounts, no analytics, no crash reporter that transmits, no `INTERNET` permission at all.
4. **No dishonesty.** The app does not overstate the science of blue light in order to sell itself.

## Never add these

- `android.permission.INTERNET` in any manifest, for any reason. There is no
  feature worth it. The `permissionAudit` task fails on it by name.
- Any analytics, ads, attribution, or crash-reporting dependency. The
  `dependencyAudit` task holds the deny list in
  `build-logic/convention/src/main/kotlin/dev/horizon/buildlogic/DependencyAudit.kt`.
- Any networking library. With no `INTERNET` permission it is either dead weight
  in the APK or a permission request waiting to happen.
- A font, image, or config fetched from a URL at runtime. Everything ships in the
  APK. `requirement.md` N-08: full functionality in airplane mode, permanently.

## Adding a permission

The complete set lives in `requirement.md` section 8 and is mirrored in
`docs/permissions.md`, which the build parses. Adding one means updating both,
plus the in-app permission screen, in the same commit. That is the definition of
done in `scaffolding.md` section 11, and the build enforces the first half of it.

## Why this is strict

An app requesting "draw over other apps" has the exact shape of malware. Trust is
the only currency in this category, and having nothing to sell is what makes the
claim credible rather than marketing copy. Every claim the app makes is meant to
be falsifiable by checking Android's own settings screen.
