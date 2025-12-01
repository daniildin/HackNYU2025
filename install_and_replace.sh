#!/usr/bin/env bash
set -euo pipefail

# --- New: check for Java 17 before proceeding (and auto-install via brew if available) ---
echo "Checking for Java 17..."
if ! java -version 2>&1 | grep -q '"17' ; then
  echo ""
  echo "Java 17 not found. Gradle toolchain requires JDK 17."

  if command -v brew >/dev/null 2>&1; then
    echo ""
    echo "Homebrew detected. Installing OpenJDK 17 automatically..."
    brew update
    brew install openjdk@17

    # Configure shell environment for the installed JDK
    BREW_PREFIX="$(brew --prefix openjdk@17 2>/dev/null || true)"
    if [ -n "$BREW_PREFIX" ]; then
      SHELL_RC="$HOME/.zshrc"
      echo "" >> "$SHELL_RC"
      echo "# Added by Nova installer: OpenJDK 17" >> "$SHELL_RC"
      echo "export PATH=\"$BREW_PREFIX/bin:\$PATH\"" >> "$SHELL_RC"
      echo "export JAVA_HOME=\"$BREW_PREFIX/libexec/openjdk.jdk/Contents/Home\"" >> "$SHELL_RC"
      echo "Wrote Java 17 PATH/JAVA_HOME to $SHELL_RC. Sourcing..."
      # shellcheck disable=SC1090
      source "$SHELL_RC"
    fi

    # verify installation
    if java -version 2>&1 | grep -q '"17' ; then
      echo "Java 17 installed and available."
    else
      echo "Automatic install attempted but java -version does not show 17. You may need to restart your shell."
      echo "Please run 'source $SHELL_RC' or open a new terminal, then re-run this script."
      exit 1
    fi
  else
    echo ""
    echo "Homebrew not found. Please install JDK 17 manually and set JAVA_HOME so 'java -version' shows 17.x."
    echo "Suggested manual commands (macOS):"
    echo "  brew update && brew install openjdk@17"
    echo "  echo 'export PATH=\"\$(brew --prefix openjdk@17)/bin:\$PATH\"' >> ~/.zshrc"
    echo "  echo 'export JAVA_HOME=\"\$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home\"' >> ~/.zshrc"
    echo "  source ~/.zshrc"
    exit 1
  fi
fi
# --- End Java check ---

# Backup Python files to backup_python/
echo "Backing up Python files to backup_python/ ..."
mkdir -p backup_python

PY_FILES=(
  "__main__.py"
  "__init__.py"
  "logic/welcome.py"
  "logic/portfolioEffect.py"
  "logic/bucketParser.py"
  "logic/__init__.py"
  "parsing/scraper.py"
  "parsing/novanews.py"
  "parsing/cnbc.py"
  "parsing/driver.py"
  "parsing/__init__.py"
)

for f in "${PY_FILES[@]}"; do
  if [ -f "$f" ]; then
    echo " -> moving $f"
    mv "$f" backup_python/ || cp "$f" backup_python/
  fi
done

echo "Backup complete."

# Ensure Gradle wrapper exists, or attempt to create it if system gradle is available
if [ ! -f "./gradlew" ]; then
  if command -v gradle >/dev/null 2>&1; then
    echo "Creating Gradle wrapper using system 'gradle'..."
    gradle wrapper
  else
    echo "Gradle wrapper not present and 'gradle' not found on PATH."
    echo "Please install Gradle or run 'gradle wrapper' manually, then re-run this script."
    exit 1
  fi
fi

# Make sure wrapper is executable
chmod +x ./gradlew || true

# Build and run
echo "Running ./gradlew build..."
./gradlew build

echo "Running ./gradlew run..."
./gradlew run

echo "Done. Python files are in backup_python/ if you need to restore them."
