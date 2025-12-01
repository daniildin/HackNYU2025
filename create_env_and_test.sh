#!/usr/bin/env bash
set -euo pipefail

# Usage: ./create_env_and_test.sh
# Prompts for GEMINI_API_KEY and PORTFOLIO_TICKERS, writes .env, runs the Java app, prints scraped articles.

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

read -rp "Enter GEMINI_API_KEY: " GEMINI_API_KEY
read -rp "Enter PORTFOLIO_TICKERS (comma-separated, e.g. AAPL,MSFT): " PORTFOLIO_TICKERS

cat > "$PROJECT_ROOT/.env" <<EOF
GEMINI_API_KEY=${GEMINI_API_KEY}
PORTFOLIO_TICKERS=${PORTFOLIO_TICKERS}
EOF

echo ".env written to $PROJECT_ROOT/.env"
echo "Running Java app (this will call scrapers)..."

cd "$PROJECT_ROOT"

# Prefer project gradle wrapper if present
if [ -x "./gradlew" ]; then
  ./gradlew --no-daemon run
else
  # fallback to system gradle if available
  if command -v gradle >/dev/null 2>&1; then
    gradle wrapper
    chmod +x ./gradlew
    ./gradlew --no-daemon run
  else
    echo "Gradle wrapper not found and system gradle not installed. Please install gradle or create wrapper."
    exit 1
  fi
fi

echo ""
echo "Scraped articles (data/articles.json):"
if [ -f "data/articles.json" ]; then
  cat data/articles.json
else
  echo "data/articles.json not found."
fi
