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
public class TransistorIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(8.0182), scale(3.1409), scale(8.0182), scale(4.8409)));
    // Draw circle (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.0000f)));
    g2.draw(new Ellipse2D.Double(scale(6.7000), scale(5.0591), scale(2.6000), scale(2.6000)));
    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(4.5000), scale(7.8591), scale(11.5000), scale(7.8591)));
    // Draw line (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(new Line2D.Double(scale(4.0000), scale(10.3591), scale(12.0000), scale(10.3591)));
    // Draw path (black stroke)
    final var path0 = new Path2D.Double();
    path0.moveTo(scale(1.0000), scale(12.8591));
    path0.lineTo(scale(5.5000), scale(12.8591));
    path0.lineTo(scale(5.5000), scale(10.3591));
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(path0);
    // Draw path (black stroke)
    final var path1 = new Path2D.Double();
    path1.moveTo(scale(15.0000), scale(12.8591));
    path1.lineTo(scale(10.5000), scale(12.8591));
    path1.lineTo(scale(10.5000), scale(10.3591));
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(path1);
  }
}
