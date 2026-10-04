#!/usr/bin/env bash
set -eu

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

# Prefer a local Gradle installation if one is already available.
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

GRADLE_VERSION="${GRADLE_VERSION:-8.5}"
GRADLE_HOME="$ROOT_DIR/.gradle/gradle-${GRADLE_VERSION}"
GRADLE_ZIP="$ROOT_DIR/.gradle/gradle-${GRADLE_VERSION}-bin.zip"

mkdir -p "$ROOT_DIR/.gradle"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  if [ ! -f "$GRADLE_ZIP" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -L --fail -o "$GRADLE_ZIP" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  fi

  rm -rf "$GRADLE_HOME"
  unzip -q "$GRADLE_ZIP" -d "$ROOT_DIR/.gradle"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
