/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.tools;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.std.gates.GatesLibrary;
import com.cburch.logisim.std.plexers.PlexersLibrary;
import com.cburch.logisim.std.ttl.TtlLibrary;
import com.cburch.logisim.std.wiring.WiringLibrary;
import org.junit.jupiter.api.Test;

class SearchAliasesTest {

  @Test
  void testTtlAliases() {
    final var ttlLib = new TtlLibrary();
    var found7424 = false;
    var found7400 = false;

    for (final var tool : ttlLib.getTools()) {
      final var aliases = tool.getSearchAliases();
      if ("7424".equals(tool.getName())) {
        found7424 = true;
        assertTrue(aliases.contains("74132"), "7424 must have 74132 alias");
        assertTrue(aliases.contains("74LS132"), "7424 must have 74LS132 alias");
        assertTrue(aliases.contains("К155ТЛ3"), "7424 must have К155ТЛ3 alias");
      }
      if ("7400".equals(tool.getName())) {
        found7400 = true;
        assertTrue(aliases.contains("74LS00"), "7400 must have 74LS00 alias");
        assertTrue(aliases.contains("К155ЛА3"), "7400 must have К155ЛА3 alias");
      }
    }

    assertTrue(found7424, "Ttl7424 tool must exist");
    assertTrue(found7400, "Ttl7400 tool must exist");
  }

  @Test
  void testPlexersAliases() {
    final var plexersLib = new PlexersLibrary();
    var foundMux = false;
    var foundDemux = false;

    for (final var tool : plexersLib.getTools()) {
      final var aliases = tool.getSearchAliases();
      if ("Multiplexer".equalsIgnoreCase(tool.getName())) {
        foundMux = true;
        assertTrue(aliases.contains("mux"), "Multiplexer must have 'mux' alias");
        assertTrue(aliases.contains("мукс"), "Multiplexer must have 'мукс' alias");
      }
      if ("Demultiplexer".equalsIgnoreCase(tool.getName())) {
        foundDemux = true;
        assertTrue(aliases.contains("demux"), "Demultiplexer must have 'demux' alias");
        assertTrue(aliases.contains("демукс"), "Demultiplexer must have 'демукс' alias");
      }
    }

    assertTrue(foundMux, "Multiplexer tool must exist");
    assertTrue(foundDemux, "Demultiplexer tool must exist");
  }

  @Test
  void testWiringAliases() {
    final var wiringLib = new WiringLibrary();
    var foundTunnel = false;

    for (final var tool : wiringLib.getTools()) {
      final var aliases = tool.getSearchAliases();
      if (tool.getName().toLowerCase().contains("tunnel")) {
        foundTunnel = true;
        assertTrue(aliases.contains("туннель"), "Tunnel must have 'туннель' alias");
        assertTrue(aliases.contains("net"), "Tunnel must have 'net' alias");
      }
    }

    assertTrue(foundTunnel, "Tunnel tool must exist");
  }

  @Test
  void testGatesAliases() {
    final var gatesLib = new GatesLibrary();
    var foundXor = false;
    var foundNot = false;

    for (final var tool : gatesLib.getTools()) {
      final var aliases = tool.getSearchAliases();
      if (tool.getName().toLowerCase().contains("xor")) {
        foundXor = true;
        assertTrue(aliases.contains("ксор"), "XOR Gate must have 'ксор' alias");
      }
      if (tool.getName().toLowerCase().contains("not")) {
        foundNot = true;
        assertTrue(aliases.contains("inverter"), "NOT Gate must have 'inverter' alias");
        assertTrue(aliases.contains("инвертор"), "NOT Gate must have 'инвертор' alias");
      }
    }

    assertTrue(foundXor, "XOR Gate tool must exist");
    assertTrue(foundNot, "NOT Gate tool must exist");
  }
}
