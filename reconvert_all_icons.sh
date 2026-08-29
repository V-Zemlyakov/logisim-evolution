#!/usr/bin/env bash
set -e

# ==============================================================================
# Master Script: Batch Re-convert all vector icons
# ==============================================================================
# Calls svg2baseicon.sh and baseicon2svg.sh for each icon in the registry.
#
# Classpath cache is pre-warmed ONCE at the start. Child scripts are told to
# skip cache invalidation (ICON_TOOLS_CP_SKIP_CHECK=1) so that the *Icon.java
# files generated in Phase 1 don't trigger unnecessary cache rebuilds in Phase 2.
#
# To add a new icon: append a line to ICONS[] below.
# Format: "SVG_PATH|JAVA_PATH|PKG_NAME|CLASS_NAME|BASE_NAME"
# ==============================================================================

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

mkdir -p build/resources/docgen

echo "======================================================================"
echo "=== Batch Re-converting All $TOTAL Vector Icons ==="
echo "======================================================================"

# Create stubs for any missing icon Java files before compilation
for entry in "${ICONS[@]}"; do
  IFS='|' read -r SVG_PATH JAVA_PATH PKG_NAME CLASS_NAME BASE_NAME <<< "$entry"
  if [ ! -f "$JAVA_PATH" ]; then
    mkdir -p "$(dirname "$JAVA_PATH")"
    cat <<EOF > "$JAVA_PATH"
package $PKG_NAME;
import com.cburch.logisim.gui.icons.BaseIcon;
import java.awt.Graphics2D;
public class $CLASS_NAME extends BaseIcon {
    @Override
    protected void paintIcon(Graphics2D g2) {}
}
EOF
  fi
done

CLASSPATH_CACHE=".gradle/icon-tools-classpath.txt"
if [ ! -f "$CLASSPATH_CACHE" ]; then
    echo ">> Building test classes (first-time setup, ~30-60s)..."
    mkdir -p .gradle
    ./gradlew --no-configuration-cache -q classes testClasses
    ./gradlew --no-configuration-cache -q printIconToolsClasspath > "$CLASSPATH_CACHE"
    echo ">> Classpath cached."
else
    echo ">> Classpath cache is ready (remove .gradle/icon-tools-classpath.txt to force rebuild)."
fi

# Child scripts must not re-check or rebuild the cache during the batch run.
export ICON_TOOLS_CP_SKIP_CHECK=1

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
mkdir -p build/resources/docgen

echo ""
echo ">> Phase 2: Exporting documentation assets (SVG & PNGs)..."
mkdir -p build/resources/docgen
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
