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
public class SplitterIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw path (black fill)
    final var path0 = new Path2D.Double();
    path0.moveTo(scale(7.3206), scale(1.6011));
    path0.lineTo(scale(12.5274), scale(1.6011));
    path0.lineTo(scale(12.5274), scale(2.9758));
    path0.lineTo(scale(7.3206), scale(2.9758));
    path0.lineTo(scale(7.3206), scale(1.6011));
    g2.setColor(currentColor);
    g2.fill(path0);
    // Draw path (black fill)
    final var path1 = new Path2D.Double();
    path1.moveTo(scale(6.7801), scale(5.5965));
    path1.lineTo(scale(11.9870), scale(5.5965));
    path1.lineTo(scale(11.9870), scale(6.9877));
    path1.lineTo(scale(6.7801), scale(6.9877));
    path1.lineTo(scale(6.7801), scale(5.5965));
    g2.setColor(currentColor);
    g2.fill(path1);
    // Draw path (black fill)
    final var path2 = new Path2D.Double();
    path2.moveTo(scale(6.9367), scale(9.7271));
    path2.lineTo(scale(12.1435), scale(9.7271));
    path2.lineTo(scale(12.1435), scale(11.1019));
    path2.lineTo(scale(6.9367), scale(11.1019));
    path2.lineTo(scale(6.9367), scale(9.7271));
    g2.setColor(currentColor);
    g2.fill(path2);
    // Draw ellipse (black fill)
    g2.setColor(currentColor);
    g2.fill(new Ellipse2D.Double(scale(10.9953), scale(5.1933), scale(2.2247), scale(2.2678)));
    // Draw ellipse (black fill)
    g2.setColor(currentColor);
    g2.fill(new Ellipse2D.Double(scale(11.0543), scale(1.1933), scale(2.2247), scale(2.2678)));
    // Draw ellipse (black fill)
    g2.setColor(currentColor);
    g2.fill(new Ellipse2D.Double(scale(10.9583), scale(9.3123), scale(2.2247), scale(2.2678)));
    // Draw path (fill: #1b531c)
    final var path3 = new Path2D.Double();
    path3.moveTo(scale(2.7018), scale(1.2158));
    path3.lineTo(scale(2.7018), scale(12.6311));
    g2.setColor(new Color(27, 83, 28));
    g2.fill(path3);
    // Draw path (fill: #1b531c, black stroke)
    final var path4 = new Path2D.Double();
    path4.moveTo(scale(6.5069), scale(1.7787));
    path4.lineTo(scale(6.5294), scale(12.2033));
    g2.setColor(new Color(27, 83, 28));
    g2.fill(path4);
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(2.0000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(path4);
    // Draw path (fill: #1b531c, black stroke)
    final var path5 = new Path2D.Double();
    path5.moveTo(scale(3.9177), scale(14.6124));
    path5.lineTo(scale(6.4619), scale(12.2708));
    g2.setColor(new Color(27, 83, 28));
    g2.fill(path5);
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(2.0000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    g2.draw(path5);
  }
}
