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
public class PinIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw path (black stroke)
    final var path0 = new Path2D.Double();
    path0.moveTo(scale(1.5076), scale(3.9600));
    path0.lineTo(scale(11.0040), scale(3.9600));
    path0.lineTo(scale(15.0290), scale(7.9840));
    path0.lineTo(scale(11.0040), scale(12.0080));
    path0.lineTo(scale(1.4895), scale(12.0080));
    path0.closePath();
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.0000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(path0);
    // Draw path (fill: #129d2a)
    final var path1 = new Path2D.Double();
    path1.moveTo(scale(5.2866), scale(10.4592));
    path1.lineTo(scale(5.2866), scale(9.8982));
    path1.lineTo(scale(6.4759), scale(9.8982));
    path1.lineTo(scale(6.4759), scale(6.3576));
    path1.lineTo(scale(5.2866), scale(6.5980));
    path1.lineTo(scale(5.2866), scale(6.0205));
    path1.lineTo(scale(7.6696), scale(5.5408));
    path1.lineTo(scale(7.6696), scale(9.8982));
    path1.lineTo(scale(8.8589), scale(9.8982));
    path1.lineTo(scale(8.8589), scale(10.4592));
    path1.closePath();
    g2.setColor(new Color(18, 157, 42));
    g2.fill(path1);
  }
}
