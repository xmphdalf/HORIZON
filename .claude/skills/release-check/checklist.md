# Release checklist

Copy into the release pull request and tick each line with evidence, not a
checkmark alone. "Passed" without a number is not evidence.

```
## Commitments
- [ ] ./gradlew check green
- [ ] No INTERNET in any manifest (grep output attached)
- [ ] Release dependency tree read by hand, nothing that opens a socket
- [ ] No analytics, ads, attribution, or crash-reporting coordinate

## Permissions
- [ ] Merged release manifest declares only the documented set
- [ ] requirement.md section 8, docs/permissions.md, and the in-app screen agree
- [ ] Foreground service type declared and justified

## Performance
- [ ] Baseline profile regenerated against this code
- [ ] Cold start median ......... ms of 400 ms budget
- [ ] Drag P99 frame ........... ms of 8.3 ms budget at 120 Hz
- [ ] Delta from previous release stated

## Size
- [ ] Release APK ......... MB of 6 MB budget
- [ ] Delta from previous release stated

## Build
- [ ] Two clean builds produce identical APKs
- [ ] Signed with the expected key
- [ ] Signature published alongside the artifact

## Accessibility
- [ ] TalkBack pass on every feature module
- [ ] 200 percent font pass, nothing clipped
- [ ] Contrast at OFF/DAY and at 1700 K, body at or above 4.6:1
- [ ] No colour-only meaning
- [ ] Paired actions equal weight

## Documents
- [ ] CHANGELOG.md entry with date
- [ ] Version code and name bumped
- [ ] Decisions recorded in requirement.md section 14
- [ ] Resolved questions removed from requirement.md section 15
- [ ] docs/oem-quirks.md reflects devices actually tested

## Devices
- [ ] Tested on API 26
- [ ] Tested on API 36
- [ ] Tested on at least one aggressive battery-manager skin
- [ ] Panic disable verified by hand on each
```
