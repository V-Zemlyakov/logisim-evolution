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

  @Test
  void testPulseDetectionFromUnknownAndError() {
    final var file = com.cburch.logisim.file.LogisimFile.createNew(new com.cburch.logisim.file.Loader(null), null);
    final var project = new com.cburch.logisim.proj.Project(file);
    final var circuit = file.getMainCircuit();
    circuit.setProject(project);
    project.setCurrentCircuit(circuit);
    final var state = com.cburch.logisim.circuit.CircuitState.createRootState(project, circuit, Thread.currentThread());

    final var pinAttrs = Pin.FACTORY.createAttributeSet();
    final var pin = Pin.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        pinAttrs);

    final var probeAttrs = PulseProbe.FACTORY.createAttributeSet();
    probeAttrs.setValue(PulseProbe.ATTR_TRIGGER, PulseProbe.TRIG_LOW);
    final var probe = PulseProbe.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        probeAttrs);

    final var mutation = new com.cburch.logisim.circuit.CircuitMutation(circuit);
    mutation.add(pin);
    mutation.add(probe);
    mutation.execute();

    final var propagator = state.getPropagator();
    propagator.propagate();

    // Pulse ERROR -> FALSE -> ERROR
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.ERROR, pin, 1);
    propagator.propagate();
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.FALSE, pin, 1);
    propagator.propagate();
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.ERROR, pin, 1);
    propagator.propagate();

    final var probeState = (PulseProbe.PulseProbeState) state.getData(probe);
    assertNotNull(probeState);
    assertTrue(probeState.isHolding(), "Probe should hold after detecting 0 pulse from ERROR state");
    assertEquals(com.cburch.logisim.data.Value.FALSE, probeState.getCaughtValue());
  }

  @Test
  void testGlitchFromErrorInAnyMode() {
    final var file = com.cburch.logisim.file.LogisimFile.createNew(new com.cburch.logisim.file.Loader(null), null);
    final var project = new com.cburch.logisim.proj.Project(file);
    final var circuit = file.getMainCircuit();
    circuit.setProject(project);
    project.setCurrentCircuit(circuit);
    final var state = com.cburch.logisim.circuit.CircuitState.createRootState(project, circuit, Thread.currentThread());

    final var pin = Pin.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        Pin.FACTORY.createAttributeSet());

    final var probeAttrs = PulseProbe.FACTORY.createAttributeSet();
    probeAttrs.setValue(PulseProbe.ATTR_TRIGGER, PulseProbe.TRIG_ANY);
    final var probe = PulseProbe.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        probeAttrs);

    final var mutation = new com.cburch.logisim.circuit.CircuitMutation(circuit);
    mutation.add(pin);
    mutation.add(probe);
    mutation.execute();

    final var propagator = state.getPropagator();
    propagator.propagate();

    // Pulse ERR -> 0 -> ERR (transmission gate scenario)
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.ERROR, pin, 1);
    propagator.propagate();
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.FALSE, pin, 1);
    propagator.propagate();
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.ERROR, pin, 1);
    propagator.propagate();

    final var probeState = (PulseProbe.PulseProbeState) state.getData(probe);
    assertNotNull(probeState);
    assertTrue(probeState.isHolding());
    assertEquals(com.cburch.logisim.data.Value.FALSE, probeState.getCaughtValue(), "Should catch 0 pulse from ERROR baseline");
    assertFalse(probeState.isLastTransitionToTrue(), "Should indicate falling edge / dark green");
  }

  @Test
  void testGlitchFromUnknownInAnyMode() {
    final var file = com.cburch.logisim.file.LogisimFile.createNew(new com.cburch.logisim.file.Loader(null), null);
    final var project = new com.cburch.logisim.proj.Project(file);
    final var circuit = file.getMainCircuit();
    circuit.setProject(project);
    project.setCurrentCircuit(circuit);
    final var state = com.cburch.logisim.circuit.CircuitState.createRootState(project, circuit, Thread.currentThread());

    final var pinAttrs = Pin.FACTORY.createAttributeSet();
    pinAttrs.setValue(Pin.ATTR_BEHAVIOR, Pin.TRISTATE);
    final var pin = Pin.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        pinAttrs);

    final var probeAttrs = PulseProbe.FACTORY.createAttributeSet();
    probeAttrs.setValue(PulseProbe.ATTR_TRIGGER, PulseProbe.TRIG_ANY);
    final var probe = PulseProbe.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        probeAttrs);

    final var mutation = new com.cburch.logisim.circuit.CircuitMutation(circuit);
    mutation.add(pin);
    mutation.add(probe);
    mutation.execute();

    final var propagator = state.getPropagator();
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.UNKNOWN, pin, 1);
    propagator.propagate();

    final var probeState = (PulseProbe.PulseProbeState) state.getData(probe);
    assertNotNull(probeState);
    probeState.resetLatch();

    // Pulse 1 on Z bus: Z -> 1 -> Z
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.TRUE, pin, 1);
    propagator.propagate();
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.UNKNOWN, pin, 1);
    propagator.propagate();

    assertTrue(probeState.isHolding());
    assertEquals(com.cburch.logisim.data.Value.TRUE, probeState.getCaughtValue(), "Should catch 1 pulse from Z baseline");
    assertTrue(probeState.isLastTransitionToTrue(), "Should indicate rising edge / light green");
  }

  @Test
  void testHighAndLowTriggersWithZAndError() {
    final var file = com.cburch.logisim.file.LogisimFile.createNew(new com.cburch.logisim.file.Loader(null), null);
    final var project = new com.cburch.logisim.proj.Project(file);
    final var circuit = file.getMainCircuit();
    circuit.setProject(project);
    project.setCurrentCircuit(circuit);
    final var state = com.cburch.logisim.circuit.CircuitState.createRootState(project, circuit, Thread.currentThread());

    final var pinAttrs = Pin.FACTORY.createAttributeSet();
    pinAttrs.setValue(Pin.ATTR_BEHAVIOR, Pin.TRISTATE);
    final var pin = Pin.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        pinAttrs);

    // 1. HIGH trigger with Z -> 1 -> Z
    final var probeAttrsHigh = PulseProbe.FACTORY.createAttributeSet();
    probeAttrsHigh.setValue(PulseProbe.ATTR_TRIGGER, PulseProbe.TRIG_HIGH);
    final var probeHigh = PulseProbe.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(100, 100, true),
        probeAttrsHigh);

    final var mutation = new com.cburch.logisim.circuit.CircuitMutation(circuit);
    mutation.add(pin);
    mutation.add(probeHigh);
    mutation.execute();

    final var propagator = state.getPropagator();
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.UNKNOWN, pin, 1);
    propagator.propagate();

    final var probeStateHigh = (PulseProbe.PulseProbeState) state.getData(probeHigh);
    assertNotNull(probeStateHigh);
    probeStateHigh.resetLatch();

    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.TRUE, pin, 1);
    propagator.propagate();
    state.setValue(pin.getLocation(), com.cburch.logisim.data.Value.UNKNOWN, pin, 1);
    propagator.propagate();

    assertTrue(probeStateHigh.isHolding());
    assertEquals(com.cburch.logisim.data.Value.TRUE, probeStateHigh.getCaughtValue());

    // 2. LOW trigger with Z -> 0 -> Z
    final var probeAttrsLow = PulseProbe.FACTORY.createAttributeSet();
    probeAttrsLow.setValue(PulseProbe.ATTR_TRIGGER, PulseProbe.TRIG_LOW);
    final var probeLow = PulseProbe.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(200, 200, true),
        probeAttrsLow);
    final var pin2 = Pin.FACTORY.createComponent(
        com.cburch.logisim.data.Location.create(200, 200, true),
        pinAttrs);

    final var mutation2 = new com.cburch.logisim.circuit.CircuitMutation(circuit);
    mutation2.add(pin2);
    mutation2.add(probeLow);
    mutation2.execute();

    state.setValue(pin2.getLocation(), com.cburch.logisim.data.Value.UNKNOWN, pin2, 1);
    propagator.propagate();

    final var probeStateLow = (PulseProbe.PulseProbeState) state.getData(probeLow);
    assertNotNull(probeStateLow);
    probeStateLow.resetLatch();

    state.setValue(pin2.getLocation(), com.cburch.logisim.data.Value.FALSE, pin2, 1);
    propagator.propagate();
    state.setValue(pin2.getLocation(), com.cburch.logisim.data.Value.UNKNOWN, pin2, 1);
    propagator.propagate();

    assertTrue(probeStateLow.isHolding());
    assertEquals(com.cburch.logisim.data.Value.FALSE, probeStateLow.getCaughtValue());
  }
}
