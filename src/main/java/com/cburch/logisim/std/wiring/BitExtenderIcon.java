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
public class BitExtenderIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw path (black stroke)
    final var path0 = new Path2D.Double();
    path0.moveTo(scale(3.5000), scale(5.5000));
    path0.lineTo(scale(7.0000), scale(5.5000));
    path0.lineTo(scale(11.5000), scale(2.5000));
    path0.lineTo(scale(11.5000), scale(13.5000));
    path0.lineTo(scale(7.0000), scale(10.5000));
    path0.lineTo(scale(3.5000), scale(10.5000));
    path0.closePath();
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.2000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(path0);
  }
}
