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
public class DoNotConnectIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw line (stroke: #F44336)
    g2.setColor(new Color(244, 67, 54));
    g2.setStroke(new BasicStroke(scale(1.6000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(3.5000), scale(3.5000), scale(12.5000), scale(12.5000)));
    // Draw line (stroke: #F44336)
    g2.setColor(new Color(244, 67, 54));
    g2.setStroke(new BasicStroke(scale(1.6000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(3.5000), scale(12.5000), scale(12.5000), scale(3.5000)));
    // Draw circle (fill: #2196F3, stroke: #1565C0)
    g2.setColor(new Color(33, 150, 243));
    g2.fill(new Ellipse2D.Double(scale(6.2000), scale(6.2000), scale(3.6000), scale(3.6000)));
    g2.setColor(new Color(21, 101, 192));
    g2.setStroke(new BasicStroke(scale(0.6000f)));
    g2.draw(new Ellipse2D.Double(scale(6.2000), scale(6.2000), scale(3.6000), scale(3.6000)));
  }
}
