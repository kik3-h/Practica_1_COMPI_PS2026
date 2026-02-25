#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# setup.sh  –  Download JFlex and CUP jars into libs/
# Run once before opening Android Studio.
# ─────────────────────────────────────────────────────────────────

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIBS="$SCRIPT_DIR/../libs"
mkdir -p "$LIBS"

echo "⬇  Downloading JFlex 1.9.1 ..."
curl -L "https://github.com/jflex-de/jflex/releases/download/v1.9.1/jflex-full-1.9.1.jar" \
     -o "$LIBS/jflex-full-1.9.1.jar"

echo "⬇  Downloading CUP 11b (generator) ..."
curl -L "https://repo1.maven.org/maven2/com/github/vbmacher/java-cup/11b/java-cup-11b.jar" \
     -o "$LIBS/java-cup-11b.jar"

echo "⬇  Downloading CUP 11b runtime ..."
curl -L "https://repo1.maven.org/maven2/com/github/vbmacher/java-cup/11b/java-cup-11b-runtime.jar" \
     -o "$LIBS/java-cup-11b-runtime.jar"

echo ""
echo "✅  All JARs downloaded to $LIBS"
echo ""
echo "Next steps:"
echo "  1. Open the project root in Android Studio."
echo "  2. Sync Gradle (the generateLexer / generateParser tasks run automatically)."
echo "  3. Connect your Xiaomi Redmi Note 10S via USB (enable USB debugging)."
echo "  4. Run the app."
