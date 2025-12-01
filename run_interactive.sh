#!/usr/bin/env bash
set -euo pipefail
# Build the project and run the installed distribution so stdin is available for prompts.
# Usage: ./run_interactive.sh

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "Building project..."
./gradlew --no-daemon clean installDist

echo "Running interactive app (enter API key and tickers when prompted)..."
# Run the installed distribution - this script will forward stdin to the app
if [ -x "$ROOT/build/install/nova/bin/nova" ]; then
  "$ROOT/build/install/nova/bin/nova"
else
  echo "Distribution binary not found. You can also run the app class directly (may require setting CLASSPATH):"
  echo "  ./gradlew run --no-daemon"
  exit 1
fi
