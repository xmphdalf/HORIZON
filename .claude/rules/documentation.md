---
paths:
  - "**/*.md"
---

# Documentation Rules

## Writing conventions

ASCII only. No em dashes, no emoji, no decorative characters. Plain hyphens,
plain quotes. This applies to every document in the repository, including this
one.

Two exceptions, both structural rather than decorative:

- Box-drawing characters in directory trees and dependency graphs, as
  `scaffolding.md` section 2 uses them. A tree drawn with pipes and backticks is
  harder to read, not more compliant.
- `docs/design/` is a vendored handoff bundle. It is third-party output and is
  not edited to match our conventions.

## Tone

Be direct. State the constraint and the reason. A reader should never have to
guess why something is written the way it is.

Do not write marketing copy. `requirement.md` commits to not overstating the
science of blue light, and that restraint applies to the documentation as much as
to the app: say plainly what is well evidenced (brightness and timing affect
sleep) and what is contested (blue wavelengths specifically).

## Structure

- Keep `CLAUDE.md` under 200 lines. Detail that only matters for one part of the
  codebase belongs in a path-scoped rule under `.claude/rules/`, not here.
- Documents point at each other rather than restating. `requirement.md` is the
  product source of truth, `scaffolding.md` is the repository contract,
  `docs/design-system.md` is the visual one. Duplicating a rule into a second
  file means the two will disagree later.

## Things that must stay in sync

Some documents are parsed by the build. Changing their shape breaks it:

- `docs/permissions.md` is parsed by `permissionAudit`. The permission table rows
  must keep the form `` | `android.permission.NAME` | ``.
- `requirement.md` N-03 holds the APK budget that `apkSizeCheck` enforces.
- `docs/design-system.md` holds the ramp table that `core/tokens` is tested
  against.
