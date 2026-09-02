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
public class PowerOnResetIcon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var currentColor = g2.getColor();

    // Draw path (black stroke)
    final var path0 = new Path2D.Double();
    path0.moveTo(scale(1.0000), scale(1.0000));
    path0.lineTo(scale(15.0000), scale(1.0000));
    path0.lineTo(scale(15.0000), scale(15.0000));
    path0.lineTo(scale(1.0000), scale(15.0000));
    path0.lineTo(scale(1.0000), scale(1.0000));
    path0.closePath();
    g2.setColor(currentColor);
    g2.setStroke(new BasicStroke(scale(1.0000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(path0);
    // Draw path (black fill)
    final var path1 = new Path2D.Double();
    path1.moveTo(scale(2.9410), scale(6.2000));
    path1.lineTo(scale(2.9410), scale(2.5870));
    path1.lineTo(scale(4.2010), scale(2.5870));
    path1.quadTo(scale(4.8600), scale(2.5870), scale(5.1420), scale(2.8080));
    path1.quadTo(scale(5.4240), scale(3.0290), scale(5.4240), scale(3.5460));
    path1.quadTo(scale(5.4240), scale(4.1320), scale(5.0470), scale(4.4580));
    path1.quadTo(scale(4.6700), scale(4.7840), scale(3.9910), scale(4.7840));
    path1.lineTo(scale(3.6790), scale(4.7840));
    path1.lineTo(scale(3.6790), scale(6.2000));
    path1.closePath();
    path1.moveTo(scale(3.6790), scale(4.2880));
    path1.lineTo(scale(3.8250), scale(4.2880));
    path1.quadTo(scale(4.2110), scale(4.2880), scale(4.4340), scale(4.1090));
    path1.quadTo(scale(4.6580), scale(3.9290), scale(4.6580), scale(3.6220));
    path1.quadTo(scale(4.6580), scale(3.0820), scale(3.9720), scale(3.0820));
    path1.lineTo(scale(3.6790), scale(3.0820));
    path1.closePath();
    path1.moveTo(scale(7.5920), scale(6.2900));
    path1.quadTo(scale(6.7690), scale(6.2900), scale(6.2760), scale(5.7730));
    path1.quadTo(scale(5.7830), scale(5.2550), scale(5.7830), scale(4.3930));
    path1.quadTo(scale(5.7830), scale(3.5220), scale(6.2790), scale(3.0090));
    path1.quadTo(scale(6.7740), scale(2.4960), scale(7.6170), scale(2.4960));
    path1.quadTo(scale(8.4540), scale(2.4960), scale(8.9510), scale(3.0090));
    path1.quadTo(scale(9.4480), scale(3.5220), scale(9.4480), scale(4.3860));
    path1.quadTo(scale(9.4480), scale(5.2700), scale(8.9510), scale(5.7800));
    path1.quadTo(scale(8.4540), scale(6.2900), scale(7.5920), scale(6.2900));
    path1.closePath();
    path1.moveTo(scale(7.6020), scale(5.7920));
    path1.quadTo(scale(8.0850), scale(5.7920), scale(8.3670), scale(5.4130));
    path1.quadTo(scale(8.6490), scale(5.0330), scale(8.6490), scale(4.3840));
    path1.quadTo(scale(8.6490), scale(3.7540), scale(8.3660), scale(3.3730));
    path1.quadTo(scale(8.0830), scale(2.9920), scale(7.6170), scale(2.9920));
    path1.quadTo(scale(7.1460), scale(2.9920), scale(6.8630), scale(3.3730));
    path1.quadTo(scale(6.5820), scale(3.7540), scale(6.5820), scale(4.3910));
    path1.quadTo(scale(6.5820), scale(5.0230), scale(6.8620), scale(5.4080));
    path1.quadTo(scale(7.1430), scale(5.7920), scale(7.6020), scale(5.7920));
    path1.closePath();
    path1.moveTo(scale(10.1240), scale(6.2000));
    path1.lineTo(scale(10.1240), scale(2.5870));
    path1.lineTo(scale(11.4770), scale(2.5870));
    path1.quadTo(scale(12.5780), scale(2.5870), scale(12.5780), scale(3.4830));
    path1.quadTo(scale(12.5780), scale(3.8170), scale(12.3910), scale(4.0920));
    path1.quadTo(scale(12.2040), scale(4.3670), scale(11.8770), scale(4.5130));
    path1.lineTo(scale(13.0590), scale(6.2000));
    path1.lineTo(scale(12.1430), scale(6.2000));
    path1.lineTo(scale(11.2470), scale(4.7230));
    path1.lineTo(scale(10.8320), scale(4.7230));
    path1.lineTo(scale(10.8320), scale(6.2000));
    path1.closePath();
    path1.moveTo(scale(10.8320), scale(4.2270));
    path1.lineTo(scale(11.0050), scale(4.2270));
    path1.quadTo(scale(11.8230), scale(4.2270), scale(11.8230), scale(3.5660));
    path1.quadTo(scale(11.8230), scale(3.0820), scale(11.0930), scale(3.0820));
    path1.lineTo(scale(10.8320), scale(3.0820));
    path1.closePath();
    g2.setColor(currentColor);
    g2.fill(path1);
    // Draw path (stroke: #0000ff)
    final var path2 = new Path2D.Double();
    path2.moveTo(scale(3.5000), scale(7.5000));
    path2.lineTo(scale(3.5000), scale(13.5000));
    path2.lineTo(scale(13.0000), scale(13.5000));
    g2.setColor(new Color(0, 0, 255));
    g2.setStroke(new BasicStroke(scale(1.0000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(path2);
    // Draw path (stroke: #ff0000)
    final var path3 = new Path2D.Double();
    path3.moveTo(scale(4.5000), scale(9.0000));
    path3.lineTo(scale(8.5000), scale(9.0000));
    path3.lineTo(scale(8.5000), scale(12.5000));
    path3.lineTo(scale(12.5000), scale(12.5000));
    g2.setColor(new Color(255, 0, 0));
    g2.setStroke(new BasicStroke(scale(1.0000f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(path3);
  }
}
