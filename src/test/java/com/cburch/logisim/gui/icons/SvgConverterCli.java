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
    boolean useNativeSvgSize = false;
    String svgPath = null;
    String javaPath = null;
    String packageName = null;
    String className = null;

    final var argList = new java.util.ArrayList<String>();
    for (var arg : args) {
      if (arg.equals("--snippet")) {
        snippetMode = true;
      } else if (arg.equals("--raw") || arg.equals("--native")) {
        useNativeSvgSize = true;
      } else {
        argList.add(arg);
      }
    }

    if (argList.size() == 1) {
      snippetMode = true;
      svgPath = argList.get(0);
    } else if (argList.size() == 4 && !snippetMode) {
      svgPath = argList.get(0);
      javaPath = argList.get(1);
      packageName = argList.get(2);
      className = argList.get(3);
    } else {
      System.err.println("Usage:");
      System.err.println("  BaseIcon mode: SvgConverterCli <svgPath> <javaPath> <packageName> <className>");
      System.err.println("  Snippet mode:  SvgConverterCli [--raw] [--snippet] <svgPath>");
      System.exit(1);
    }

    final var svgFile = new File(svgPath);
    if (!svgFile.exists() || !svgFile.isFile()) {
      System.err.println("ERROR: SVG file not found: " + svgPath);
      System.exit(2);
    }

    if (snippetMode) {
      final var snippet = SvgToBaseIconConverterTest.convertSvgToSnippet(svgFile, useNativeSvgSize);
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
