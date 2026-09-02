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
public class ClockIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw rectangle (black stroke)
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.3243f)));
    g2.draw(new Rectangle2D.Double(scale(1.3813), scale(1.3359), scale(13.2434), scale(13.2434)));
    // Draw polyline (stroke: rgb(0, 170, 0))
    final var path0 = new Path2D.Double();
    path0.moveTo(scale(4.0300), scale(7.9576));
    path0.lineTo(scale(4.0300), scale(5.3089));
    path0.lineTo(scale(8.0030), scale(5.3089));
    path0.lineTo(scale(8.0030), scale(10.6063));
    path0.lineTo(scale(11.9760), scale(10.6063));
    path0.lineTo(scale(11.9760), scale(7.9576));
    g2.setColor(new Color(0, 170, 0));
    g2.setStroke(new BasicStroke(scale(1.3243f)));
    g2.draw(path0);
  }
}
