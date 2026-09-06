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
 * CLI entry point for the SVG -&gt; BaseIcon Java class &amp; AWT snippet converter.
 *
 * <p>Designed to be invoked directly via {@code java -cp} without any Gradle or JUnit overhead,
 * making repeated conversions significantly faster than using the Gradle test task.
 *
 * <p>Usage:
 * <pre>
 *   BaseIcon mode: java -cp "&lt;classpath&gt;" com.cburch.logisim.gui.icons.SvgConverterCli \
 *                  &lt;svgPath&gt; &lt;javaPath&gt; &lt;packageName&gt; &lt;className&gt;
 *   Snippet mode:  java -cp "&lt;classpath&gt;" com.cburch.logisim.gui.icons.SvgConverterCli \
 *                  --snippet &lt;svgPath&gt;  (or single arg: SvgConverterCli &lt;svgPath&gt;)
 * </pre>
 *
 * <p>The classpath can be cached once with:
 * <pre>
 *   ./gradlew -q printIconToolsClasspath &gt; .gradle/icon-tools-classpath.txt
 * </pre>
 */
public class SvgConverterCli {

  public static void main(String[] args) throws Exception {
    boolean snippetMode = false;
    String svgPath = null;
    String javaPath = null;
    String packageName = null;
    String className = null;

    if (args.length == 1) {
      snippetMode = true;
      svgPath = args[0];
    } else if (args.length == 2 && (args[0].equals("--snippet") || args[1].equals("--snippet"))) {
      snippetMode = true;
      svgPath = args[0].equals("--snippet") ? args[1] : args[0];
    } else if (args.length == 4) {
      svgPath = args[0];
      javaPath = args[1];
      packageName = args[2];
      className = args[3];
    } else {
      System.err.println("Usage:");
      System.err.println("  BaseIcon mode: SvgConverterCli <svgPath> <javaPath> <packageName> <className>");
      System.err.println("  Snippet mode:  SvgConverterCli --snippet <svgPath>  OR  SvgConverterCli <svgPath>");
      System.exit(1);
    }

    final var svgFile = new File(svgPath);
    if (!svgFile.exists() || !svgFile.isFile()) {
      System.err.println("ERROR: SVG file not found: " + svgPath);
      System.exit(2);
    }

    if (snippetMode) {
      final var snippet = SvgToBaseIconConverterTest.convertSvgToSnippet(svgFile);
      System.out.print(snippet);
    } else {
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
}
