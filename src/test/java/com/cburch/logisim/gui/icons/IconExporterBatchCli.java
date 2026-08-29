/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.icons;

import java.util.ArrayList;

/**
 * Batch CLI entry point for exporting multiple BaseIcon classes to SVG + PNG assets.
 *
 * <p>Processes all icons in a single JVM invocation — much faster than calling
 * {@link IconExporterCli} separately for each icon.
 *
 * <p>Usage: each argument is a pipe-delimited mapping:
 * <pre>
 *   java -cp "&lt;cp&gt;" com.cburch.logisim.gui.icons.IconExporterBatchCli \
 *        "com.cburch.logisim.std.io.extra.SwitchIcon|switch" \
 *        "com.cburch.logisim.std.io.extra.BuzzerIcon|buzzer" \
 *        ...
 * </pre>
 *
 * <p>Exit code is 0 only if ALL entries succeeded.
 */
public class IconExporterBatchCli {

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Usage: IconExporterBatchCli \"className|baseName\" ...");
      System.err.println("Each argument is a pipe-delimited mapping of one icon.");
      System.exit(1);
    }

    final var errors = new ArrayList<String>();
    int ok = 0;

    System.out.println("==================================================================");
    System.out.println("  Batch BaseIcon -> SVG + PNG export  (" + args.length + " icons)");
    System.out.println("==================================================================");

    for (int i = 0; i < args.length; i++) {
      final var parts = args[i].split("\\|", 2);
      if (parts.length != 2) {
        final var msg = "[" + (i + 1) + "/" + args.length + "] ERROR: bad mapping: " + args[i];
        System.err.println(msg);
        errors.add(msg);
        continue;
      }

      final var className = parts[0];
      final var baseName = parts[1];

      try {
        System.out.printf("[%d/%d] Exporting: %s -> %s.*%n",
            i + 1, args.length, className, baseName);
        final var clazz = Class.forName(className);
        final var icon = (javax.swing.Icon) clazz.getDeclaredConstructor().newInstance();
        IconExporterTest.exportIconToAllFormats(icon, baseName);
        ok++;
      } catch (Exception ex) {
        final var msg = "[" + (i + 1) + "/" + args.length + "] ERROR exporting " + className + ": " + ex.getMessage();
        System.err.println(msg);
        errors.add(msg);
      }
    }

    System.out.println("==================================================================");
    if (errors.isEmpty()) {
      System.out.println("  SUCCESS: all " + ok + " icons exported.");
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
