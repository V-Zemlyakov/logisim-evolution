/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.wiring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.StdAttr;
import org.junit.jupiter.api.Test;

class PulseProbeTest {

  @Test
  void testFactoryRegistration() {
    final var library = new WiringLibrary();
    boolean found = false;
    for (final var tool : library.getTools()) {
      if (tool.getName().equals(PulseProbe._ID)) {
        found = true;
        break;
      }
    }
    assertTrue(found, "PulseProbe should be registered in WiringLibrary");
  }

  @Test
  void testDefaultAttributes() {
    final var factory = PulseProbe.FACTORY;
    final var attrs = factory.createAttributeSet();

    assertEquals(Direction.EAST, attrs.getValue(StdAttr.FACING));
    assertEquals(500, attrs.getValue(PulseProbe.ATTR_HOLD_DURATION));
    assertFalse(attrs.getValue(PulseProbe.ATTR_LATCH));
    assertNotNull(attrs.getValue(PulseProbe.ATTR_TRIGGER));
  }

  @Test
  void testBoundsFacing() {
    final var factory = PulseProbe.FACTORY;
    final var attrs = factory.createAttributeSet();

    attrs.setValue(StdAttr.FACING, Direction.WEST);
    final var bdsWest = factory.getOffsetBounds(attrs);
    assertEquals(0, bdsWest.getX());
    assertEquals(-10, bdsWest.getY());
    assertEquals(20, bdsWest.getWidth());
    assertEquals(20, bdsWest.getHeight());

    attrs.setValue(StdAttr.FACING, Direction.EAST);
    final var bdsEast = factory.getOffsetBounds(attrs);
    assertEquals(-20, bdsEast.getX());
    assertEquals(-10, bdsEast.getY());
    assertEquals(20, bdsEast.getWidth());
    assertEquals(20, bdsEast.getHeight());
  }
}
