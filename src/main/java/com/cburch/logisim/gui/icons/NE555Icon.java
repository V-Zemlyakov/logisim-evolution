/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.icons;

import java.awt.Graphics2D;
import java.awt.font.TextLayout;

public class NE555Icon extends BaseIcon {

  @Override
  protected void paintIcon(Graphics2D g2) {
    g2.drawRect(scale(2), 0, scale(12), scale(16));
    g2.drawLine(scale(1), scale(3), scale(2), scale(3));
    g2.drawLine(scale(1), scale(6), scale(2), scale(6));
    g2.drawLine(scale(1), scale(9), scale(2), scale(9));
    g2.drawLine(scale(1), scale(12), scale(2), scale(12));
    g2.drawLine(scale(14), scale(3), scale(15), scale(3));
    g2.drawLine(scale(14), scale(6), scale(15), scale(6));
    g2.drawLine(scale(14), scale(9), scale(15), scale(9));
    g2.drawLine(scale(14), scale(12), scale(15), scale(12));
    g2.drawLine(scale(4), 0, scale(4), scale(16));
    final var f = g2.getFont().deriveFont(scale((float) 4));
    final var t5 = new TextLayout("5", f, g2.getFontRenderContext());
    final var advance = t5.getAdvance();
    final var x = scale(8) - advance / 2f;
    t5.draw(g2, x, scale(4.5f));
    t5.draw(g2, x, scale(8.5f));
    t5.draw(g2, x, scale(12.5f));
  }
}
