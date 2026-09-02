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
public class ConstantIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw rectangle (fill: #E6E6E6)
    g2.setColor(new Color(230, 230, 230));
    g2.fill(new Rectangle2D.Double(scale(2.4000), scale(1.6000), scale(11.2000), scale(12.8000)));
    // Draw shape (fill: #00AA00)
    final var path0 = new Path2D.Double();
    path0.moveTo(scale(6.1484), scale(11.5555));
    path0.lineTo(scale(6.1484), scale(10.8617));
    path0.lineTo(scale(7.5359), scale(10.8617));
    path0.lineTo(scale(7.5359), scale(5.3867));
    path0.lineTo(scale(6.1484), scale(5.7336));
    path0.lineTo(scale(6.1484), scale(5.0211));
    path0.lineTo(scale(8.4641), scale(4.4445));
    path0.lineTo(scale(8.4641), scale(10.8617));
    path0.lineTo(scale(9.8516), scale(10.8617));
    path0.lineTo(scale(9.8516), scale(11.5555));
    path0.closePath();
    g2.setColor(new Color(0, 170, 0));
    g2.fill(path0);
    // Draw circle (fill: #00AA00)
    g2.setColor(new Color(0, 170, 0));
    g2.fill(new Ellipse2D.Double(scale(12.4000), scale(6.8000), scale(2.4000), scale(2.4000)));
  }
}
