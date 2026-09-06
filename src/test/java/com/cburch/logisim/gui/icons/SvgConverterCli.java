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
 * CLI entry point for the SVG -&gt; BaseIcon Java class converter.
 *
 * <p>Designed to be invoked directly via {@code java -cp} without any Gradle or JUnit overhead,
 * making repeated conversions significantly faster than using the Gradle test task.
 *
 * <p>Usage:
 * <pre>
 *   java -cp "&lt;classpath&gt;" com.cburch.logisim.gui.icons.SvgConverterCli \
 *        &lt;svgPath&gt; &lt;javaPath&gt; &lt;packageName&gt; &lt;className&gt;
 * </pre>
 *
 * <p>The classpath can be cached once with:
 * <pre>
 *   ./gradlew -q printIconToolsClasspath &gt; .gradle/icon-tools-classpath.txt
 * </pre>
 */
public class SvgConverterCli {

  public static void main(String[] args) throws Exception {
    if (args.length != 4) {
      System.err.println("Usage: SvgConverterCli <svgPath> <javaPath> <packageName> <className>");
      System.err.println("  svgPath     - path to the source SVG file");
      System.err.println("  javaPath    - path to the output Java file");
      System.err.println("  packageName - Java package name (e.g. com.cburch.logisim.std.io.extra)");
      System.err.println("  className   - Java class name (e.g. SwitchIcon)");
      System.exit(1);
    }

    final var svgPath = args[0];
    final var javaPath = args[1];
    final var packageName = args[2];
    final var className = args[3];

    final var svgFile = new File(svgPath);
    if (!svgFile.exists() || !svgFile.isFile()) {
      System.err.println("ERROR: SVG file not found: " + svgPath);
      System.exit(2);
    }

    final var targetJavaFile = new File(javaPath);

    System.out.println("Converting SVG -> BaseIcon Java class:");
    System.out.println("  SVG:     " + svgFile.getAbsolutePath());
    System.out.println("  Java:    " + targetJavaFile.getAbsolutePath());
    System.out.println("  Package: " + packageName);
    System.out.println("  Class:   " + className);

    SvgToBaseIconConverterTest.convertAndWriteFile(svgFile, targetJavaFile, packageName, className);

    System.out.println("SUCCESS: Generated " + targetJavaFile.getAbsolutePath());
  }
}
