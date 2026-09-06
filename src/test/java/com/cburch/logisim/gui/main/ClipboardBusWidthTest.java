/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Pin;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClipboardBusWidthTest {

  @Test
  void testClipboardPreservesBusWidthPos() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    final var project = new Project(file);
    final var circuit = file.getMainCircuit();
    circuit.setProject(project);
    project.setCurrentCircuit(circuit);

    // Create 3-bit input & output pins connected by a wire
    final var pinInLoc = Location.create(100, 100, false);
    final var pinOutLoc = Location.create(200, 100, false);
    final var pinIn = Pin.FACTORY.createComponent(pinInLoc, Pin.FACTORY.createAttributeSet());
    pinIn.getAttributeSet().setValue(StdAttr.WIDTH, BitWidth.create(3));
    pinIn.getAttributeSet().setValue(Pin.ATTR_TYPE, Pin.INPUT);

    final var pinOut = Pin.FACTORY.createComponent(pinOutLoc, Pin.FACTORY.createAttributeSet());
    pinOut.getAttributeSet().setValue(StdAttr.WIDTH, BitWidth.create(3));
    pinOut.getAttributeSet().setValue(Pin.ATTR_TYPE, Pin.OUTPUT);

    final var wire = Wire.create(pinInLoc, pinOutLoc);

    final var addXn = new CircuitMutation(circuit);
    addXn.add(pinIn);
    addXn.add(pinOut);
    addXn.add(wire);
    addXn.execute();

    // Enable Show Bus Width on the wire (set to START option)
    final var setPosXn = new CircuitMutation(circuit);
    setPosXn.set(wire, Wire.BUS_WIDTH_POS_ATTR, Wire.BUS_WIDTH_POS_START);
    setPosXn.execute();

    assertEquals(Wire.BUS_WIDTH_POS_START, circuit.getWireBusWidthPos(wire));

    // Select the circuit elements
    final var selection = new Selection(project, null);
    selection.addAll(List.of(pinIn, pinOut, wire));

    // Copy to clipboard
    Clipboard.set(selection, selection.getAttributeSet());
    final var clip = Clipboard.get();
    assertNotNull(clip);

    // Paste copied circuit
    final var pasteXn = new CircuitMutation(circuit);
    selection.pasteHelper(pasteXn, clip.getComponents());
    pasteXn.execute();

    final var dropXn = new CircuitMutation(circuit);
    selection.dropAll(dropXn);
    dropXn.execute();

    // Find the pasted wire and verify that its bus width pos was retained
    Wire pastedWire = null;
    for (final var w : circuit.getWires()) {
      if (!w.equals(wire)) {
        pastedWire = w;
        break;
      }
    }

    assertNotNull(pastedWire, "Pasted wire should exist in circuit");
    assertEquals(
        Wire.BUS_WIDTH_POS_START,
        circuit.getWireBusWidthPos(pastedWire),
        "Pasted wire should retain BUS_WIDTH_POS attribute");
  }
}
