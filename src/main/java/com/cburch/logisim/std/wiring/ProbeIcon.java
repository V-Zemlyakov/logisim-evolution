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
public class ProbeIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw circle (fill: #FFF099, stroke: #C0C0C0)
    g2.setColor(new Color(255, 240, 153));
    g2.fill(new Ellipse2D.Double(scale(2.0000), scale(2.0000), scale(12.0000), scale(12.0000)));
    g2.setColor(new Color(192, 192, 192));
    g2.setStroke(new BasicStroke(scale(1.0000f)));
    g2.draw(new Ellipse2D.Double(scale(2.0000), scale(2.0000), scale(12.0000), scale(12.0000)));
    // Draw line (stroke: rgb(0, 3, 0))
    g2.setColor(new Color(0, 3, 0));
    g2.setStroke(new BasicStroke(scale(1.3333f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(5.3720), scale(7.9967), scale(10.6280), scale(8.0033)));
  }
}
