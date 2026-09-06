#!/usr/bin/env bash
# ==============================================================================
# Shared helpers for icon-tools scripts (svg2baseicon.sh, baseicon2svg.sh, etc.)
#
# Source this file at the top of each script:
#   source "$(dirname "${BASH_SOURCE[0]}")/_icon_tools_common.sh"
#
# Provides:
#   icon_tools_ensure_classpath  — build/refresh the classpath cache if needed
#   CLASSPATH_CACHE              — path to the cache file
#   CLASSPATH                    — populated after calling icon_tools_ensure_classpath
# ==============================================================================

CLASSPATH_CACHE=".gradle/icon-tools-classpath.txt"

# Build the classpath cache when it is absent or stale.
# Skipped when ICON_TOOLS_CP_SKIP_CHECK=1 (set by reconvert_all_icons.sh).
icon_tools_ensure_classpath() {
  local needs_rebuild=false

  if [ "${ICON_TOOLS_CP_SKIP_CHECK:-0}" != "1" ]; then
    if [ ! -f "$CLASSPATH_CACHE" ]; then
      echo ">> Classpath cache not found. Building test classes (one-time, ~30-60s)..."
      needs_rebuild=true
    else
      local newer_src
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
}

# Write a minimal BaseIcon stub to JAVA_PATH if the file does not exist yet.
# Arguments: <java_path> <pkg_name> <class_name>
icon_tools_write_stub_if_missing() {
  local java_path="$1"
  local pkg_name="$2"
  local class_name="$3"

  if [ ! -f "$java_path" ]; then
    mkdir -p "$(dirname "$java_path")"
    cat > "$java_path" <<EOF
package $pkg_name;
import com.cburch.logisim.gui.icons.BaseIcon;
import java.awt.Graphics2D;
public class $class_name extends BaseIcon {
    @Override
    protected void paintIcon(Graphics2D g2) {}
}
EOF
  fi
}
