#!/usr/bin/env bash
# ==============================================================================
# Fast BaseIcon Java Class -> SVG + PNG 16x16 + PNG 64x64 Exporter
# ==============================================================================
# Usage:
#   ./baseicon2svg.sh <fully-qualified-class-name> <base-file-name>
#
# Example:
#   ./baseicon2svg.sh com.cburch.logisim.std.io.extra.TwoWaySwitchIcon twowayswitch
#
# Performance strategy:
#   1. On first run: builds classpath once via Gradle, caches to
#      .gradle/icon-tools-classpath.txt  (~30-60s, amortised over all exports).
#   2. On subsequent runs: invokes java -cp directly (~1-2s per icon).
#
# To force a classpath rebuild:
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

# shellcheck source=_icon_tools_common.sh
source "./_icon_tools_common.sh"
icon_tools_ensure_classpath

# AppPreferences.<clinit> -> FpgaBoards -> BoardList() iterates all classpath
# entries including build/resources/docgen, which Gradle puts on test classpath
# but only creates during a full build. Create it as empty dir to avoid crash.
mkdir -p build/resources/docgen

echo ">> Exporting $CLS_NAME -> SVG + PNG assets via java -cp (fast path)..."
java --enable-native-access=ALL-UNNAMED \
     -cp "$CLASSPATH" \
     com.cburch.logisim.gui.icons.IconExporterCli \
     "$CLS_NAME" "$BASE_NAME"
