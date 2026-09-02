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
public class GroundIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(8.0000), scale(1.0000), scale(8.0000), scale(6.0000)));
    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(2.0000), scale(6.0000), scale(14.0000), scale(6.0000)));
    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(4.5000), scale(9.5000), scale(11.5000), scale(9.5000)));
    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(7.0000), scale(13.0000), scale(9.0000), scale(13.0000)));
  }
}
