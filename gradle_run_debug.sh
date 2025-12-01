#!/usr/bin/env bash
# Run from project root: ./gradle_run_debug.sh
set -euo pipefail

LOG="gradle-run.log"
echo "Running Gradle with stacktrace, output -> $LOG"
if [ -x "./gradlew" ]; then
  ./gradlew --no-daemon run --stacktrace --info >"$LOG" 2>&1 || true
else
  if command -v gradle >/dev/null 2>&1; then
    gradle run --stacktrace --info >"$LOG" 2>&1 || true
  else
    echo "Gradle wrapper not found and system gradle missing. Install gradle or create wrapper first."
    exit 1
  fi
fi

echo "Done. Tail of $LOG:"
tail -n 200 "$LOG"
echo ""
echo "If there is a FAILURE block or Java stacktrace, copy-paste the last 200 lines here so I can fix the code."
