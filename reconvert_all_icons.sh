#!/usr/bin/env bash
# ==============================================================================
# Master Script: Batch re-convert all vector icons
# ==============================================================================
# Calls svg2baseicon.sh and baseicon2svg.sh for each icon in the registry.
#
# The classpath cache is pre-warmed ONCE at startup. Child scripts skip the
# cache check (ICON_TOOLS_CP_SKIP_CHECK=1) so newly-generated *Icon.java files
# do not trigger unnecessary rebuilds during Phase 1.
#
# To add a new icon: append a line to ICONS[] below.
# Format: "SVG_PATH|JAVA_PATH|PKG_NAME|CLASS_NAME|BASE_NAME"
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

ICONS=(
  "src/main/resources/resources/logisim/icons/switch_orig.svg|src/main/java/com/cburch/logisim/std/io/extra/SwitchIcon.java|com.cburch.logisim.std.io.extra|SwitchIcon|switch"
  "src/main/resources/resources/logisim/icons/buzzer_orig.svg|src/main/java/com/cburch/logisim/std/io/extra/BuzzerIcon.java|com.cburch.logisim.std.io.extra|BuzzerIcon|buzzer"
  "src/main/resources/resources/logisim/icons/twopinled_orig.svg|src/main/java/com/cburch/logisim/std/io/extra/TwoPinLedIcon.java|com.cburch.logisim.std.io.extra|TwoPinLedIcon|twopinled"
  "src/main/resources/resources/logisim/icons/twowayswitch_orig.svg|src/main/java/com/cburch/logisim/std/io/extra/TwoWaySwitchIcon.java|com.cburch.logisim.std.io.extra|TwoWaySwitchIcon|twowayswitch"
  "src/main/resources/resources/logisim/icons/plarom_orig.svg|src/main/java/com/cburch/logisim/std/io/extra/PlaIcon.java|com.cburch.logisim.std.io.extra|PlaIcon|pla"
  "src/main/resources/resources/logisim/icons/slider_orig.svg|src/main/java/com/cburch/logisim/std/io/extra/SliderIcon.java|com.cburch.logisim.std.io.extra|SliderIcon|slider"
  "src/main/resources/resources/logisim/icons/digitaloscilloscope_orig.svg|src/main/java/com/cburch/logisim/std/io/extra/DigitalOscilloscopeIcon.java|com.cburch.logisim.std.io.extra|DigitalOscilloscopeIcon|digitaloscilloscope"
)

TOTAL=${#ICONS[@]}

echo "======================================================================"
echo "=== Batch Re-converting All $TOTAL Vector Icons ==="
echo "======================================================================"

# shellcheck source=_icon_tools_common.sh
source "./_icon_tools_common.sh"

# Write stubs for any missing icon Java files before compilation
for entry in "${ICONS[@]}"; do
  IFS='|' read -r SVG_PATH JAVA_PATH PKG_NAME CLASS_NAME BASE_NAME <<< "$entry"
  icon_tools_write_stub_if_missing "$JAVA_PATH" "$PKG_NAME" "$CLASS_NAME"
done

# Pre-warm the classpath cache once (child scripts will skip the check)
if [ ! -f "$CLASSPATH_CACHE" ]; then
  echo ">> Building test classes (first-time setup, ~30-60s)..."
  mkdir -p .gradle
  ./gradlew --no-configuration-cache -q classes testClasses
  ./gradlew --no-configuration-cache -q printIconToolsClasspath > "$CLASSPATH_CACHE"
  echo ">> Classpath cached."
else
  echo ">> Classpath cache is ready (remove $CLASSPATH_CACHE to force rebuild)."
fi

export ICON_TOOLS_CP_SKIP_CHECK=1
mkdir -p build/resources/docgen

echo ""
echo ">> Phase 1: Generating all BaseIcon Java classes..."
CURRENT=0
for entry in "${ICONS[@]}"; do
  CURRENT=$((CURRENT + 1))
  IFS='|' read -r SVG_PATH JAVA_PATH PKG_NAME CLASS_NAME BASE_NAME <<< "$entry"
  echo "[$CURRENT/$TOTAL] $CLASS_NAME"
  ./svg2baseicon.sh "$SVG_PATH" "$JAVA_PATH" "$PKG_NAME" "$CLASS_NAME"
done

echo ""
echo ">> Compiling generated icon classes..."
./gradlew --no-configuration-cache -q classes testClasses
./gradlew --no-configuration-cache -q printIconToolsClasspath > "$CLASSPATH_CACHE"

echo ""
echo ">> Phase 2: Exporting documentation assets (SVG & PNGs)..."
CURRENT=0
for entry in "${ICONS[@]}"; do
  CURRENT=$((CURRENT + 1))
  IFS='|' read -r SVG_PATH JAVA_PATH PKG_NAME CLASS_NAME BASE_NAME <<< "$entry"
  echo "[$CURRENT/$TOTAL] $BASE_NAME"
  ./baseicon2svg.sh "${PKG_NAME}.${CLASS_NAME}" "$BASE_NAME"
done

echo ""
echo "======================================================================"
echo "=== All $TOTAL icons re-converted & exported ==="
echo "======================================================================"
