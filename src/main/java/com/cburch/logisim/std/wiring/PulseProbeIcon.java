/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.wiring;

import com.cburch.logisim.gui.icons.BaseIcon;
import com.cburch.logisim.prefs.AppPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class PulseProbeIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var isDark = AppPreferences.isDarkTheme(AppPreferences.LookAndFeel.get());
    final var borderColor = g2.getColor();
    final var bgColor = isDark ? new Color(45, 45, 45) : new Color(245, 245, 245);
    final var pulseColor = isDark ? new Color(80, 220, 100) : new Color(0, 160, 50);

    // Outer rounded square body
    final var body = new RoundRectangle2D.Double(scale(1.5), scale(1.5), scale(13.0), scale(13.0), scale(3.0), scale(3.0));
    g2.setColor(bgColor);
    g2.fill(body);
    g2.setColor(borderColor);
    g2.setStroke(new BasicStroke(scale(1.2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(body);

    // Pulse waveform inside: _П_
    final var pulse = new Path2D.Double();
    pulse.moveTo(scale(3.5), scale(10.5));
    pulse.lineTo(scale(6.0), scale(10.5));
    pulse.lineTo(scale(6.0), scale(4.5));
    pulse.lineTo(scale(10.0), scale(4.5));
    pulse.lineTo(scale(10.0), scale(10.5));
    pulse.lineTo(scale(12.5), scale(10.5));

    g2.setColor(pulseColor);
    g2.setStroke(new BasicStroke(scale(1.5f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(pulse);
  }
}
