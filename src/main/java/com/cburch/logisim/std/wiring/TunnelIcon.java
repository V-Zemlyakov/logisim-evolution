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
public class TunnelIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw path (fill: #808080)
    final var path0 = new Path2D.Double();
    path0.moveTo(scale(4.4790), scale(8.0000));
    path0.curveTo(scale(4.4790), scale(8.4280), scale(4.1440), scale(8.7760), scale(3.7300), scale(8.7760));
    path0.curveTo(scale(3.3170), scale(8.7760), scale(2.9810), scale(8.4280), scale(2.9810), scale(8.0000));
    path0.curveTo(scale(2.9810), scale(7.5710), scale(3.3170), scale(7.2240), scale(3.7300), scale(7.2240));
    path0.curveTo(scale(4.1440), scale(7.2240), scale(4.4790), scale(7.5710), scale(4.4790), scale(8.0000));
    path0.closePath();
    g2.setColor(new Color(128, 128, 128));
    g2.fill(path0);
    // Draw path (black stroke)
    final var path1 = new Path2D.Double();
    path1.moveTo(scale(6.0640), scale(2.4510));
    path1.lineTo(scale(11.4190), scale(2.4510));
    path1.lineTo(scale(11.4190), scale(13.5490));
    path1.lineTo(scale(6.0640), scale(13.5490));
    path1.lineTo(scale(6.0640), scale(11.4680));
    path1.lineTo(scale(4.0560), scale(8.0000));
    path1.lineTo(scale(6.0640), scale(4.5320));
    path1.closePath();
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(0.8500f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(path1);
  }
}
