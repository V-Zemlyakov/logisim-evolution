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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

// Generated BaseIcon
public class PullResistorIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.0000f)));
    g2.draw(new Line2D.Double(scale(8.5698), scale(1.7846), scale(8.5698), scale(4.3876)));
    // Draw rectangle (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.0000f)));
    g2.draw(new Rectangle2D.Double(scale(6.5142), scale(4.5193), scale(4.0032), scale(7.9825)));
    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.0000f)));
    g2.draw(new Line2D.Double(scale(8.4859), scale(12.9364), scale(8.4859), scale(14.9974)));
    // Draw circle (fill: #259530, stroke: #3e9f2e)
    g2.setColor(new Color(37, 149, 48));
    g2.fill(new Ellipse2D.Double(scale(7.5678), scale(0.5613), scale(2.0000), scale(2.0000)));
    g2.setColor(new Color(62, 159, 46));
    g2.setStroke(new BasicStroke(scale(0.5000f)));
    g2.draw(new Ellipse2D.Double(scale(7.5678), scale(0.5613), scale(2.0000), scale(2.0000)));
  }
}
