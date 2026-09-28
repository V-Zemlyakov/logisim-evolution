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

    assertEquals(Direction.SOUTH, attrs.getValue(StdAttr.FACING));
    assertEquals(PulseProbe.SIZE_SMALL, attrs.getValue(PulseProbe.ATTR_SIZE));
    assertEquals(500, attrs.getValue(PulseProbe.ATTR_HOLD_DURATION));
    assertFalse(attrs.getValue(PulseProbe.ATTR_LATCH));
    assertNotNull(attrs.getValue(PulseProbe.ATTR_TRIGGER));
  }

  @Test
  void testBoundsFacing() {
    final var factory = PulseProbe.FACTORY;
    final var attrs = factory.createAttributeSet();

    // Default Small (10x10), facing SOUTH -> port is at bottom center (0, 0), body is x: [-5, 5], y: [-10, 0]
    final var bdsSouth = factory.getOffsetBounds(attrs);
    assertEquals(-5, bdsSouth.getX());
    assertEquals(-10, bdsSouth.getY());
    assertEquals(10, bdsSouth.getWidth());
    assertEquals(10, bdsSouth.getHeight());

    // Medium (20x20), facing SOUTH
    attrs.setValue(PulseProbe.ATTR_SIZE, PulseProbe.SIZE_MEDIUM);
    final var bdsSouthMed = factory.getOffsetBounds(attrs);
    assertEquals(-10, bdsSouthMed.getX());
    assertEquals(-20, bdsSouthMed.getY());
    assertEquals(20, bdsSouthMed.getWidth());
    assertEquals(20, bdsSouthMed.getHeight());

    // Medium (20x20), facing WEST
    attrs.setValue(StdAttr.FACING, Direction.WEST);
    final var bdsWest = factory.getOffsetBounds(attrs);
    assertEquals(0, bdsWest.getX());
    assertEquals(-10, bdsWest.getY());
    assertEquals(20, bdsWest.getWidth());
    assertEquals(20, bdsWest.getHeight());
  }

  @Test
  void testPropagationWithClockDoesNotOscillateOrFreeze() {
    final var file = com.cburch.logisim.file.LogisimFile.createNew(new com.cburch.logisim.file.Loader(null), null);
    final var project = new com.cburch.logisim.proj.Project(file);
    final var circuit = file.getMainCircuit();
    circuit.setProject(project);
    project.setCurrentCircuit(circuit);
    final var state = com.cburch.logisim.circuit.CircuitState.createRootState(project, circuit, Thread.currentThread());

    final var clock = Clock.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        Clock.FACTORY.createAttributeSet());
    final var probe = PulseProbe.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        PulseProbe.FACTORY.createAttributeSet());

    final var mutation = new com.cburch.logisim.circuit.CircuitMutation(circuit);
    mutation.add(clock);
    mutation.add(probe);
    mutation.execute();

    final var propagator = state.getPropagator();
    propagator.propagate();
    assertFalse(propagator.isOscillating(), "Simulation should not oscillate when PulseProbe is attached");

    state.setValue(clock.getLocation(), com.cburch.logisim.data.Value.TRUE, clock, 1);
    propagator.propagate();
    assertFalse(propagator.isOscillating(), "Simulation should not oscillate on HIGH");

    state.setValue(clock.getLocation(), com.cburch.logisim.data.Value.FALSE, clock, 1);
    propagator.propagate();
    assertFalse(propagator.isOscillating(), "Simulation should not oscillate on LOW");
  }
}
