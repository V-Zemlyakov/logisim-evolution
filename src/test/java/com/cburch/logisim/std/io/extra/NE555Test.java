/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io.extra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class NE555Test {

  @Test
  void testComponentExists() {
    final var ne555 = new NE555();
    assertNotNull(ne555);
  }

  @Test
  void testAttributes() {
    final var ne555 = new NE555();
    final var attrs = ne555.createAttributeSet();
    assertNotNull(NE555.ATTR_MODE);
    assertEquals("astable", NE555.MODE_ASTABLE.getValue());
    assertEquals("monostable", NE555.MODE_MONOSTABLE.getValue());
    assertNotNull(NE555.ATTR_R1);
    assertNotNull(NE555.ATTR_C1);
  }

  @Test
  void testDefaultAttributeValues() {
    final var ne555 = new NE555();
    final var attrs = ne555.createAttributeSet();
    assertEquals(NE555.MODE_ASTABLE, attrs.getValue(NE555.ATTR_MODE));
  }

  @Test
  void testDefault1SecondPeriod() {
    final var ne555 = new NE555();
    final var attrs = ne555.createAttributeSet();

    // Default R1 = 71,660 Ohm, C1 = 10 uF -> T ≈ 1.00 s, f ≈ 1.00 Hz
    final var period = NE555.getPeriodSeconds(attrs);
    assertEquals(1.00, period, 0.01);
    assertEquals("1.00 s", NE555.formatTime(period));
    assertEquals("1.00 Hz", NE555.formatFrequency(NE555.getFrequencyHz(attrs)));
  }

  @Test
  void testTimingCalculationsAstable() {
    final var ne555 = new NE555();
    final var attrs = ne555.createAttributeSet();
    attrs.setValue(NE555.ATTR_MODE, NE555.MODE_ASTABLE);
    attrs.setValue(NE555.ATTR_R1, 100_000); // R1 = 100k Ohm
    attrs.setValue(NE555.ATTR_C1, 10);       // C1 = 10 uF

    // T = 0.693 * (1000 + 200000) * 10e-6 = 1.39293 s
    final var period = NE555.getPeriodSeconds(attrs);
    assertEquals(1.39293, period, 1e-3);

    final var freq = NE555.getFrequencyHz(attrs);
    assertEquals(0.7179, freq, 1e-3);

    assertEquals("1.39 s", NE555.formatTime(period));
    assertEquals("0.72 Hz", NE555.formatFrequency(freq));
  }

  @Test
  void testTimingCalculationsMonostable() {
    final var ne555 = new NE555();
    final var attrs = ne555.createAttributeSet();
    attrs.setValue(NE555.ATTR_MODE, NE555.MODE_MONOSTABLE);
    attrs.setValue(NE555.ATTR_R1, 100_000); // R1 = 100k Ohm
    attrs.setValue(NE555.ATTR_C1, 10);       // C1 = 10 uF

    // T = 1.1 * 100000 * 10e-6 = 1.1 s
    final var period = NE555.getPeriodSeconds(attrs);
    assertEquals(1.1, period, 1e-3);
    assertEquals("1.10 s", NE555.formatTime(period));
    assertEquals("N/A", NE555.formatFrequency(NE555.getFrequencyHz(attrs)));
  }

  @Test
  void testAttributeClamping() {
    final var ne555 = new NE555();
    final var attrs = ne555.createAttributeSet();

    // Test underflow clamping (below min)
    attrs.setValue(NE555.ATTR_R1, -50);
    attrs.setValue(NE555.ATTR_C1, 0);
    assertEquals((double) NE555.MIN_R1_OHM, NE555.getResistanceOhm(attrs));
    assertEquals(NE555.MIN_C1_UF * 1e-6, NE555.getCapFarad(attrs));

    // Test overflow clamping (above max)
    attrs.setValue(NE555.ATTR_R1, 50_000_000);
    attrs.setValue(NE555.ATTR_C1, 20_000);
    assertEquals((double) NE555.MAX_R1_OHM, NE555.getResistanceOhm(attrs));
    assertEquals(NE555.MAX_C1_UF * 1e-6, NE555.getCapFarad(attrs));
  }
}
