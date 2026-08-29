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
#   1. On first run (or after code changes): builds classpath once via Gradle, caches to
#      .gradle/icon-tools-classpath.txt  (~30-60s total, amortised over all conversions).
#   2. On subsequent runs: invokes java -cp directly — no Gradle overhead (~1-2s per icon).
#
# To force a classpath rebuild (e.g. after changing converter code):
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

CLASSPATH_CACHE=".gradle/icon-tools-classpath.txt"

# ==============================================================================
# Step 1: Ensure classpath cache is valid
# ==============================================================================
# The cache is considered stale if:
#   - it doesn't exist yet, OR
#   - any Java source file in src/ is newer than the cache
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
# Step 2 (Optional): Flatten Inkscape transforms via Inkscape CLI
# ==============================================================================
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

# ==============================================================================
# Step 3 (Optional): Clean SVG via SVGO
# ==============================================================================
if command -v npx &> /dev/null; then
    echo ">> Cleaning SVG via SVGO..."
    npx -y svgo --config=svgo.config.cjs "$PROCESSING_SVG" -o "$TMP_DIR/svgo_clean.svg" &> /dev/null || true
    if [ -f "$TMP_DIR/svgo_clean.svg" ]; then
        PROCESSING_SVG="$TMP_DIR/svgo_clean.svg"
    fi
fi

# ==============================================================================
# Step 4: Create stub Java file if it doesn't exist (prevents Gradle compile error)
# ==============================================================================
if [ ! -f "$JAVA_PATH" ]; then
    mkdir -p "$(dirname "$JAVA_PATH")"
    cat <<EOF > "$JAVA_PATH"
package $PKG_NAME;
import com.cburch.logisim.gui.icons.BaseIcon;
import java.awt.Graphics2D;
public class $CLS_NAME extends BaseIcon {
    @Override
    protected void paintIcon(Graphics2D g2) {}
}
EOF
fi

# ==============================================================================
# Step 5: Run the converter directly via java -cp (fast, ~1-2s)
# ==============================================================================
echo ">> Generating BaseIcon Java class via java -cp (fast path)..."
java --enable-native-access=ALL-UNNAMED \
     -cp "$CLASSPATH" \
     com.cburch.logisim.gui.icons.SvgConverterCli \
     "$PROCESSING_SVG" "$JAVA_PATH" "$PKG_NAME" "$CLS_NAME"

echo "SUCCESS: Vector BaseIcon class generated at $JAVA_PATH"
