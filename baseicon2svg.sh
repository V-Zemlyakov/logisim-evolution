#!/usr/bin/env bash
# ==============================================================================
# Fast BaseIcon Java Class -> SVG + PNG 16x16 + PNG 64x64 Exporter
# ==============================================================================
# Usage:
#   ./baseicon2svg.sh <class-name> <base-file-name>
#
# Example:
#   ./baseicon2svg.sh com.cburch.logisim.std.io.extra.TwoWaySwitchIcon twowayswitch
#
# Performance strategy:
#   1. On first run (or after code changes): builds classpath once via Gradle, caches to
#      .gradle/icon-tools-classpath.txt  (~30-60s total, amortised over all exports).
#   2. On subsequent runs: invokes java -cp directly — no Gradle overhead (~1-2s per icon).
#
# To force a classpath rebuild (e.g. after changing icon code):
#   rm .gradle/icon-tools-classpath.txt && ./baseicon2svg.sh ...
# ==============================================================================

set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <class-name> <base-file-name>"
    echo "Example: $0 com.cburch.logisim.std.io.extra.TwoWaySwitchIcon twowayswitch"
    exit 1
fi

CLS_NAME="$1"
BASE_NAME="$2"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

CLASSPATH_CACHE=".gradle/icon-tools-classpath.txt"

# ==============================================================================
# Ensure classpath cache is valid.
# Set ICON_TOOLS_CP_SKIP_CHECK=1 to skip this check (used by reconvert_all_icons.sh
# which pre-warms the cache once before the loop).
# ==============================================================================
needs_rebuild=false

if [ "${ICON_TOOLS_CP_SKIP_CHECK:-0}" != "1" ]; then
    if [ ! -f "$CLASSPATH_CACHE" ]; then
        echo ">> Classpath cache not found. Building test classes (one-time, ~30-60s)..."
        needs_rebuild=true
    else
        newer_src=$(find src -name "*.java" -newer "$CLASSPATH_CACHE" 2>/dev/null | head -1)
        if [ -n "$newer_src" ]; then
            echo ">> Source changed ($newer_src). Rebuilding classpath cache..."
            needs_rebuild=true
        fi
    fi
fi

if [ "$needs_rebuild" = true ]; then
    mkdir -p .gradle
    ./gradlew --no-configuration-cache -q classes testClasses 2>/dev/null
    ./gradlew --no-configuration-cache -q printIconToolsClasspath > "$CLASSPATH_CACHE"
    echo ">> Classpath cached to $CLASSPATH_CACHE"
fi

CLASSPATH="$(cat "$CLASSPATH_CACHE")"

# ==============================================================================
# Run the exporter directly via java -cp (fast, ~1-2s)
# ==============================================================================
# AppPreferences.<clinit> -> FpgaBoards -> BoardList() iterates all classpath
# entries including build/resources/docgen which Gradle puts on test classpath
# but only creates during a full build. Create it as empty dir to avoid crash.
mkdir -p build/resources/docgen

echo ">> Exporting $CLS_NAME -> SVG + PNG assets via java -cp (fast path)..."
java --enable-native-access=ALL-UNNAMED \
     -cp "$CLASSPATH" \
     com.cburch.logisim.gui.icons.IconExporterCli \
     "$CLS_NAME" "$BASE_NAME"
