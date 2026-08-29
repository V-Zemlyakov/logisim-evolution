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
import java.util.ArrayList;
import java.util.List;

/**
 * Batch CLI entry point for converting multiple SVG files to BaseIcon Java classes.
 *
 * <p>Processes all icon mappings in a single JVM invocation — much faster than
 * calling {@link SvgConverterCli} separately for each icon, because JVM startup
 * happens only once.
 *
 * <p>Usage: each argument is a pipe-delimited mapping:
 * <pre>
 *   java -cp "&lt;cp&gt;" com.cburch.logisim.gui.icons.SvgConverterBatchCli \
 *        "svgPath|javaPath|packageName|className" \
 *        "svgPath|javaPath|packageName|className" \
 *        ...
 * </pre>
 *
 * <p>On error in any entry the batch continues and reports a summary at the end.
 * Exit code is 0 only if ALL entries succeeded.
 */
public class SvgConverterBatchCli {

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Usage: SvgConverterBatchCli \"svgPath|javaPath|packageName|className\" ...");
      System.err.println("Each argument is a pipe-delimited mapping of one icon.");
      System.exit(1);
    }

    final var errors = new ArrayList<String>();
    int ok = 0;

    System.out.println("==================================================================");
    System.out.println("  Batch SVG -> BaseIcon conversion  (" + args.length + " icons)");
    System.out.println("==================================================================");

    for (int i = 0; i < args.length; i++) {
      final var parts = args[i].split("\\|", 4);
      if (parts.length != 4) {
        final var msg = "[" + (i + 1) + "/" + args.length + "] ERROR: bad mapping: " + args[i];
        System.err.println(msg);
        errors.add(msg);
        continue;
      }

      final var svgPath = parts[0];
      final var javaPath = parts[1];
      final var packageName = parts[2];
      final var className = parts[3];

      final var svgFile = new File(svgPath);
      if (!svgFile.exists() || !svgFile.isFile()) {
        final var msg = "[" + (i + 1) + "/" + args.length + "] SKIP: SVG not found: " + svgPath;
        System.out.println(msg);
        errors.add(msg);
        continue;
      }

      try {
        System.out.printf("[%d/%d] Converting: %s -> %s%n",
            i + 1, args.length, svgFile.getName(), javaPath);
        SvgToBaseIconConverterTest.convertAndWriteFile(svgFile, new File(javaPath), packageName, className);
        ok++;
      } catch (Exception ex) {
        final var msg = "[" + (i + 1) + "/" + args.length + "] ERROR converting " + className + ": " + ex.getMessage();
        System.err.println(msg);
        errors.add(msg);
      }
    }

    System.out.println("==================================================================");
    if (errors.isEmpty()) {
      System.out.println("  SUCCESS: all " + ok + " icons converted.");
    } else {
      System.out.println("  DONE: " + ok + " ok, " + errors.size() + " failed:");
      for (final var e : errors) {
        System.err.println("    " + e);
      }
    }
    System.out.println("==================================================================");

    if (!errors.isEmpty()) {
      System.exit(1);
    }
  }
}
