/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.icons;

import java.io.File;

/**
 * CLI entry point for the BaseIcon -&gt; SVG + PNG exporter.
 *
 * <p>Designed to be invoked directly via {@code java -cp} without any Gradle or JUnit overhead,
 * making repeated exports significantly faster than using the Gradle test task.
 *
 * <p>Usage:
 * <pre>
 *   java -cp "&lt;classpath&gt;" com.cburch.logisim.gui.icons.IconExporterCli \
 *        &lt;iconClassName&gt; &lt;iconBaseName&gt;
 * </pre>
 *
 * <p>The classpath can be cached once with:
 * <pre>
 *   ./gradlew -q printIconToolsClasspath &gt; .gradle/icon-tools-classpath.txt
 * </pre>
 *
 * <p>Example:
 * <pre>
 *   java -cp "$(cat .gradle/icon-tools-classpath.txt)" \
 *        com.cburch.logisim.gui.icons.IconExporterCli \
 *        com.cburch.logisim.std.io.extra.SwitchIcon switch
 * </pre>
 */
public class IconExporterCli {

  public static void main(String[] args) throws Exception {
    // AppPreferences.<clinit> -> FpgaBoards -> BoardList iterates all classpath entries as ZipFiles.
    // build/resources/docgen is on the test classpath but only created during a full Gradle build.
    // Creating it upfront here prevents a NoSuchFileException crash before any icon code runs.
    new File("build/resources/docgen").mkdirs();

    if (args.length != 2) {
      System.err.println("Usage: IconExporterCli <iconClassName> <iconBaseName>");
      System.err.println("  iconClassName - fully qualified class name (e.g. com.cburch.logisim.std.io.extra.SwitchIcon)");
      System.err.println("  iconBaseName  - base file name without extension (e.g. switch)");
      System.exit(1);
    }

    final var className = args[0];
    final var baseName = args[1];

    System.out.println("Exporting BaseIcon to SVG + PNG assets:");
    System.out.println("  Class:    " + className);
    System.out.println("  BaseName: " + baseName);

    final var clazz = Class.forName(className);
    final var icon = (javax.swing.Icon) clazz.getDeclaredConstructor().newInstance();
    IconExporterTest.exportIconToAllFormats(icon, baseName);

    System.out.println("SUCCESS: Exported assets for " + baseName);
  }
}
