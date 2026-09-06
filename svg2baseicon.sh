#!/usr/bin/env bash
# ==============================================================================
# Fast SVG -> BaseIcon Java Class Converter Pipeline
# ==============================================================================
# Usage:
#   ./svg2baseicon.sh <svg_file_path> <target_java_path> <package_name> <class_name>
#
# Example:
#   ./svg2baseicon.sh src/main/resources/resources/logisim/icons/switch_orig.svg \
#                     src/main/java/com/cburch/logisim/std/io/extra/SwitchIcon.java \
#                     com.cburch.logisim.std.io.extra \
#                     SwitchIcon
#
# Performance strategy:
#   1. On first run: builds classpath once via Gradle, caches to
#      .gradle/icon-tools-classpath.txt  (~30-60s, amortised over all conversions).
#   2. On subsequent runs: invokes java -cp directly (~1-2s per icon).
#
# To force a classpath rebuild:
#   rm .gradle/icon-tools-classpath.txt && ./svg2baseicon.sh ...
# ==============================================================================

set -e

SVG_PATH="$1"
JAVA_PATH="$2"
PKG_NAME="$3"
CLS_NAME="$4"

if [ -z "$SVG_PATH" ] || [ -z "$JAVA_PATH" ] || [ -z "$PKG_NAME" ] || [ -z "$CLS_NAME" ]; then
  echo "Usage: ./svg2baseicon.sh <svg_path> <target_java_path> <package_name> <class_name>"
  exit 1
fi

if [ ! -f "$SVG_PATH" ]; then
  echo "Error: SVG file not found at $SVG_PATH"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# shellcheck source=_icon_tools_common.sh
source "./_icon_tools_common.sh"
icon_tools_ensure_classpath

# Step 1 (Optional): Flatten Inkscape transforms
TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

PROCESSING_SVG="$SVG_PATH"

if command -v inkscape &> /dev/null; then
  echo ">> Flattening Inkscape matrices via Inkscape CLI..."
  inkscape "$PROCESSING_SVG" \
    --actions="select-all:all;object-to-path;export-plain-svg" \
    --export-filename="$TMP_DIR/inkscape_flat.svg" &> /dev/null || true
  if [ -f "$TMP_DIR/inkscape_flat.svg" ]; then
    PROCESSING_SVG="$TMP_DIR/inkscape_flat.svg"
  fi
fi

# Step 2 (Optional): Clean SVG via SVGO
if command -v npx &> /dev/null; then
  echo ">> Cleaning SVG via SVGO..."
  npx -y svgo --config=svgo.config.cjs "$PROCESSING_SVG" -o "$TMP_DIR/svgo_clean.svg" &> /dev/null || true
  if [ -f "$TMP_DIR/svgo_clean.svg" ]; then
    PROCESSING_SVG="$TMP_DIR/svgo_clean.svg"
  fi
fi

# Step 3: Ensure stub Java file exists (prevents Gradle compile error)
icon_tools_write_stub_if_missing "$JAVA_PATH" "$PKG_NAME" "$CLS_NAME"

# Step 4: Run the converter via java -cp (~1-2s)
echo ">> Generating BaseIcon Java class via java -cp (fast path)..."
java --enable-native-access=ALL-UNNAMED \
     -cp "$CLASSPATH" \
     com.cburch.logisim.gui.icons.SvgConverterCli \
     "$PROCESSING_SVG" "$JAVA_PATH" "$PKG_NAME" "$CLS_NAME"

echo "SUCCESS: Vector BaseIcon class generated at $JAVA_PATH"
