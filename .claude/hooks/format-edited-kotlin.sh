#!/usr/bin/env bash
# Formats a Kotlin file after Claude edits it.
#
# Runs on every Edit and Write, so it must be fast and must never block. It exits
# 0 in every path that is not an actual formatting failure: a missing formatter
# is not a reason to interrupt someone's work, and detekt in `check` is the gate
# that actually enforces style.
#
# Reads the hook payload on stdin and formats only the file that changed.
# Deliberately does not shell out to Gradle: a daemon start costs seconds on
# every keystroke-sized edit.

set -uo pipefail

payload=$(cat)

file_path=$(printf '%s' "$payload" | python3 -c '
import json
import sys

try:
    data = json.load(sys.stdin)
except (json.JSONDecodeError, ValueError):
    sys.exit(0)

tool_input = data.get("tool_input") or {}
print(tool_input.get("file_path") or "")
' 2>/dev/null)

[ -n "$file_path" ] || exit 0
[ -f "$file_path" ] || exit 0

case "$file_path" in
    *.kt|*.kts) ;;
    *) exit 0 ;;
esac

if command -v ktlint >/dev/null 2>&1; then
    # --format rewrites in place. Style findings that ktlint cannot fix are left
    # for detekt, which runs with the project's own config.
    ktlint --format --log-level=none "$file_path" >/dev/null 2>&1
fi

exit 0
