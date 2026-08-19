## What this changes

<!-- One or two sentences. What behaviour is different afterwards. -->

## Why

<!-- The problem, not the solution. If it fixes an issue, link it. -->

## Definition of done

From `scaffolding.md` section 11. Tick what applies, delete what does not, and
say why for anything left unticked.

- [ ] detekt, lint, and the compiler pass with no warnings
- [ ] Tests cover the new behaviour
- [ ] For a bug fix: a test that failed before the fix and passes after
- [ ] No new permission, or it is added to `requirement.md` section 8,
      `docs/permissions.md`, and the in-app permission screen in this commit
- [ ] No new dependency, or it is justified below and adds no network capability
- [ ] Public API has KDoc, and no comment restates code
- [ ] Previews at three ramp points for any UI change
- [ ] TalkBack labels for any new interactive element
- [ ] If this touches `surface/overlay`: an instrumented test proves the opacity
      cap and the brightness restore still hold

## APK size delta

<!-- Required. State the before and after, even when it is zero. -->

Before: ... MB
After: ... MB

## New dependencies

<!-- Delete if none. Otherwise: what it is, why nothing already present does the
job, and confirmation that it adds no network capability. -->

None.

## The commitments

Confirm these still hold. They are not negotiable, so a change that breaks one is
declined regardless of what it enables.

- [ ] No `INTERNET` permission, in any variant
- [ ] No analytics, advertising, attribution, or crash-reporting dependency
- [ ] No font, image, or configuration fetched at runtime
- [ ] Nothing appears on screen that the user did not ask for
