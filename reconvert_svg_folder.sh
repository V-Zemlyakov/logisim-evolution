#!/usr/bin/env bash
# ==============================================================================
# Bash script to process all SVG files in svg/ directory via SvgConverterBatchCli
# and export them back to svg/<name>_converted.svg
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

SVG_DIR="svg"
TMP_DIR="build/tmp/batch_icons"

if [ ! -d "$SVG_DIR" ]; then
  echo "Error: Directory $SVG_DIR not found!"
  exit 1
fi

echo ">> Compiling project classes..."
./gradlew classes testClasses -q --no-daemon

# Construct clean classpath without empty path entries
GRADLE_JARS=$(find ~/.gradle/caches/modules-2/files-2.1 -name "*.jar" 2>/dev/null | tr '\n' ':')
RAW_CP="build/classes/java/main:build/classes/java/test:build/resources/main:build/resources/test:${GRADLE_JARS}"
CLASSPATH=$(echo "$RAW_CP" | sed -E 's/::+/:/g' | sed -E 's/^:|:$//g')

rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR/com/cburch/logisim/gui/icons/batch"

CONVERT_ARGS=()
EXPORT_ARGS=()

for svg_file in "$SVG_DIR"/*.svg; do
  [ -e "$svg_file" ] || continue
  filename="$(basename "$svg_file")"
  
  # Process ALL .svg files except previously generated _converted.svg outputs
  if [[ "$filename" == *"_converted.svg"* ]]; then
    continue
  fi

  # Skip non-component large logos or files larger than 10KB
  fsize=$(wc -c < "$svg_file" | tr -d ' ')
  if [[ "$filename" == *"logo"* ]] || [ "$fsize" -gt 10000 ]; then
    continue
  fi

  base_name="${filename%.svg}"
  # Generate safe unique class name from filename (replacing spaces and non-alphanumeric chars)
  clean_name=$(echo "$base_name" | sed -E 's/[^a-zA-Z0-9]+([a-zA-Z0-9])/\U\1/g' | sed -E 's/[^a-zA-Z0-9]//g' | sed -E 's/^([a-z])/\U\1/g')
  if [ -z "$clean_name" ]; then clean_name="Icon"; fi
  class_name="U${clean_name}BatchIcon"
  pkg_name="com.cburch.logisim.gui.icons.batch"
  java_file="$TMP_DIR/com/cburch/logisim/gui/icons/batch/${class_name}.java"

  CONVERT_ARGS+=("$svg_file|$java_file|$pkg_name|$class_name")
  EXPORT_ARGS+=("${pkg_name}.${class_name}|$base_name")
done

echo ">> Step 1: Batch SVG -> Java conversion via SvgConverterBatchCli (${#CONVERT_ARGS[@]} files)..."
java --enable-native-access=ALL-UNNAMED \
     -cp "$CLASSPATH" \
     com.cburch.logisim.gui.icons.SvgConverterBatchCli \
     "${CONVERT_ARGS[@]}"

echo ">> Step 2: Compiling generated BaseIcon Java classes..."
javac -cp "$CLASSPATH" -d "$TMP_DIR" "$TMP_DIR/com/cburch/logisim/gui/icons/batch/"*.java

echo ">> Step 3: Batch export Java -> svg/<name>_converted.svg via IconExporterBatchCli..."
# Temporarily override export target in environment or move exported SVGs to svg/ folder
java --enable-native-access=ALL-UNNAMED \
     -cp "$CLASSPATH:$TMP_DIR" \
     com.cburch.logisim.gui.icons.IconExporterBatchCli \
     "${EXPORT_ARGS[@]}"

for arg in "${EXPORT_ARGS[@]}"; do
  bname="${arg#*|}"
  src_svg="src/main/resources/doc/icons/svgwithoutbackground/${bname}.svg"
  dest_svg="$SVG_DIR/${bname}_converted.svg"
  if [ -f "$src_svg" ]; then
    cp "$src_svg" "$dest_svg"
    echo "  -> Saved: $dest_svg"
  fi
done

echo ">> DONE! All files processed to $SVG_DIR/<name>_converted.svg"
