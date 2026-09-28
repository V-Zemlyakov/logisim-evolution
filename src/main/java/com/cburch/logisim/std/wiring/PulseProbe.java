/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.wiring;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceComponent;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.tools.key.DirectionConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.Timer;

public class PulseProbe extends InstanceFactory {
  /**
   * Unique identifier of the tool, used as reference in project files.
   * Do NOT change as it will prevent project files from loading.
   */
  public static final String _ID = "PulseProbe";

  public static final AttributeOption SIZE_SMALL =
      new AttributeOption("small", S.getter("pulseProbeSizeSmall"));
  public static final AttributeOption SIZE_MEDIUM =
      new AttributeOption("medium", S.getter("pulseProbeSizeMedium"));

  public static final Attribute<AttributeOption> ATTR_SIZE =
      Attributes.forOption(
          "size", S.getter("pulseProbeSizeAttr"), new AttributeOption[] {SIZE_SMALL, SIZE_MEDIUM});

  private static final AttributeOption TRIG_HIGH =
      new AttributeOption("high", S.getter("pulseProbeTriggerHigh"));
  private static final AttributeOption TRIG_LOW =
      new AttributeOption("low", S.getter("pulseProbeTriggerLow"));
  private static final AttributeOption TRIG_ANY =
      new AttributeOption("any", S.getter("pulseProbeTriggerAny"));

  public static final Attribute<AttributeOption> ATTR_TRIGGER =
      Attributes.forOption(
          "trigger", S.getter("pulseProbeTriggerAttr"), new AttributeOption[] {TRIG_HIGH, TRIG_LOW, TRIG_ANY});

  public static final Attribute<Integer> ATTR_HOLD_DURATION =
      Attributes.forIntegerRange("holdDuration", S.getter("pulseProbeHoldAttr"), 50, 5000);

  public static final Attribute<Boolean> ATTR_LATCH =
      Attributes.forBoolean("latch", S.getter("pulseProbeLatchAttr"));

  public static final PulseProbe FACTORY = new PulseProbe();

  public static class Poker extends InstancePoker {
    @Override
    public void mouseReleased(InstanceState state, MouseEvent e) {
      final var probeState = (PulseProbeState) state.getData();
      if (probeState != null) {
        probeState.resetLatch(state);
      }
    }
  }

  public static class PulseProbeState implements InstanceData, Cloneable, ActionListener {
    private InstanceComponent component;
    private Value lastVal = Value.UNKNOWN;
    private boolean active = false;
    private boolean holding = false;
    private boolean latched = false;
    private Timer holdTimer;

    public PulseProbeState(InstanceState state) {
      this.component = state.getInstance().getComponent();
      final var duration = state.getAttributeValue(ATTR_HOLD_DURATION);
      this.holdTimer = new Timer(duration, this);
      this.holdTimer.setRepeats(false);
    }

    public void update(Value curVal, InstanceState state) {
      this.component = state.getInstance().getComponent();
      final var trigger = state.getAttributeValue(ATTR_TRIGGER);
      final var isLatchMode = state.getAttributeValue(ATTR_LATCH);
      final var duration = state.getAttributeValue(ATTR_HOLD_DURATION);

      if (holdTimer.getDelay() != duration) {
        holdTimer.setInitialDelay(duration);
        holdTimer.setDelay(duration);
      }

      boolean triggerConditionMet = false;
      boolean currentIsActive = false;

      if (trigger == TRIG_HIGH) {
        currentIsActive = (curVal == Value.TRUE);
        if (lastVal != Value.TRUE && curVal == Value.TRUE) {
          triggerConditionMet = true;
        } else if (lastVal == Value.TRUE && curVal != Value.TRUE) {
          // Falling edge -> start hold
          startHold(isLatchMode);
        }
      } else if (trigger == TRIG_LOW) {
        currentIsActive = (curVal == Value.FALSE);
        if (lastVal != Value.FALSE && curVal == Value.FALSE) {
          triggerConditionMet = true;
        } else if (lastVal == Value.FALSE && curVal != Value.FALSE) {
          // Rising edge -> start hold
          startHold(isLatchMode);
        }
      } else { // TRIG_ANY
        if (lastVal != Value.UNKNOWN && curVal != lastVal) {
          triggerConditionMet = true;
          startHold(isLatchMode);
        }
      }

      if (triggerConditionMet) {
        if (isLatchMode) {
          latched = true;
        }
      }

      active = currentIsActive;
      lastVal = curVal;
    }

    private void startHold(boolean isLatchMode) {
      holding = true;
      if (isLatchMode) {
        latched = true;
      }
      holdTimer.restart();
    }

    public void resetLatch(InstanceState state) {
      latched = false;
      holding = false;
      holdTimer.stop();
      if (component != null) {
        component.fireInvalidated();
      }
    }

    public boolean isActive() {
      return active;
    }

    public boolean isHolding() {
      return holding;
    }

    public boolean isLatched() {
      return latched;
    }

    public Value getLastValue() {
      return lastVal;
    }

    @Override
    public Object clone() {
      try {
        final var copy = (PulseProbeState) super.clone();
        copy.holdTimer = new Timer(this.holdTimer.getDelay(), copy);
        copy.holdTimer.setRepeats(false);
        return copy;
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (holding) {
        holding = false;
        if (component != null) {
          component.fireInvalidated();
        }
      }
    }
  }

  public PulseProbe() {
    super(_ID, S.getter("pulseProbeComponent"));
    setAttributes(
        new Attribute<?>[] {
          StdAttr.FACING,
          ATTR_SIZE,
          ATTR_TRIGGER,
          ATTR_HOLD_DURATION,
          ATTR_LATCH,
          StdAttr.LABEL,
          StdAttr.LABEL_LOC,
          StdAttr.LABEL_FONT,
          StdAttr.LABEL_COLOR,
          StdAttr.LABEL_VISIBILITY
        },
        new Object[] {
          Direction.SOUTH,
          SIZE_SMALL,
          TRIG_HIGH,
          500,
          Boolean.FALSE,
          "",
          Direction.NORTH,
          StdAttr.DEFAULT_LABEL_FONT,
          StdAttr.DEFAULT_LABEL_COLOR,
          Boolean.TRUE
        });
    setFacingAttribute(StdAttr.FACING);
    setIcon(new PulseProbeIcon());
    setKeyConfigurator(new DirectionConfigurator(StdAttr.LABEL_LOC, KeyEvent.ALT_DOWN_MASK));
    setPorts(new Port[] {new Port(0, 0, Port.INPUT, 1)});
    setInstancePoker(Poker.class);
  }

  @Override
  protected void configureNewInstance(Instance instance) {
    instance.addAttributeListener();
    instance.computeLabelTextField(Instance.AVOID_LEFT);
  }

  @Override
  public Bounds getOffsetBounds(AttributeSet attrs) {
    final var facing = attrs.getValue(StdAttr.FACING);
    final var size = attrs.getValue(ATTR_SIZE);
    final int w = (size == SIZE_MEDIUM) ? 20 : 10;
    final int h = (size == SIZE_MEDIUM) ? 20 : 10;
    return Bounds.create(-w / 2, -h, w, h).rotate(Direction.SOUTH, facing, 0, 0);
  }

  @Override
  protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
    if (attr == StdAttr.FACING || attr == ATTR_SIZE) {
      instance.recomputeBounds();
      instance.computeLabelTextField(Instance.AVOID_LEFT);
    } else if (attr == StdAttr.LABEL_LOC) {
      instance.computeLabelTextField(Instance.AVOID_LEFT);
    }
    instance.fireInvalidated();
  }

  @Override
  public void paintGhost(InstancePainter painter) {
    final var g = painter.getGraphics();
    final var bds = painter.getBounds();
    final var cornerRadius = (bds.getWidth() <= 10) ? 2 : 4;
    GraphicsUtil.switchToWidth(g, 2);
    g.drawRoundRect(bds.getX() + 1, bds.getY() + 1, bds.getWidth() - 2, bds.getHeight() - 2, cornerRadius, cornerRadius);
  }

  @Override
  public void paintInstance(InstancePainter painter) {
    final var g = painter.getGraphics();
    final var g2 = (Graphics2D) g;
    final var isSmall = painter.getBounds().getWidth() <= 10;
    final var bds = isSmall ? painter.getBounds() : painter.getBounds().expand(-1);
    final var isDark = AppPreferences.isDarkTheme(AppPreferences.LookAndFeel.get());

    final var probeState = (PulseProbeState) painter.getData();
    final var curVal = probeState == null ? Value.UNKNOWN : probeState.getLastValue();
    final var isActive = probeState != null && probeState.isActive();
    final var isHolding = probeState != null && probeState.isHolding();
    final var isLatched = probeState != null && probeState.isLatched();

    final var trigger = painter.getAttributeValue(ATTR_TRIGGER);

    // Determine colors based on digital logic states (bright green trueColor / dark green falseColor)
    Color fillColor;
    Color strokeColor;
    Color pulseColor;

    if (curVal == Value.UNKNOWN) {
      // Floating / Z-state
      fillColor = isDark ? new Color(15, 30, 50) : new Color(225, 235, 250);
      strokeColor = Value.unknownColor;
      pulseColor = strokeColor;
    } else if (curVal == Value.ERROR) {
      // Error / Conflict
      fillColor = isDark ? new Color(60, 10, 10) : new Color(255, 220, 220);
      strokeColor = Value.errorColor;
      pulseColor = strokeColor;
    } else {
      final var isPulseActive = isActive || isHolding || isLatched;

      if (trigger == TRIG_HIGH) {
        // High pulse trigger (_П_)
        if (isPulseActive) {
          // Captured / holding / active high pulse (glitch to '1')
          fillColor = isDark ? new Color(10, 55, 20) : new Color(205, 255, 215);
          strokeColor = Value.trueColor;
          pulseColor = isDark ? Color.WHITE : Color.BLACK;
        } else {
          // Idle state at '0' (dark green base)
          fillColor = isDark ? new Color(12, 35, 18) : new Color(230, 245, 232);
          strokeColor = Value.falseColor;
          pulseColor = isDark ? new Color(90, 150, 100) : new Color(60, 120, 70);
        }
      } else if (trigger == TRIG_LOW) {
        // Low pulse trigger (‾|_|‾)
        if (isPulseActive) {
          // Captured / holding / active low pulse (glitch to '0')
          fillColor = isDark ? new Color(15, 45, 22) : new Color(210, 245, 215);
          strokeColor = Value.falseColor;
          pulseColor = isDark ? Color.WHITE : Color.BLACK;
        } else {
          // Idle state at '1' (bright green base)
          fillColor = isDark ? new Color(10, 55, 20) : new Color(205, 255, 215);
          strokeColor = Value.trueColor;
          pulseColor = isDark ? new Color(180, 255, 190) : new Color(0, 130, 35);
        }
      } else { // TRIG_ANY (∿)
        if (isPulseActive) {
          final var targetColor = (curVal == Value.TRUE) ? Value.trueColor : Value.falseColor;
          fillColor = (curVal == Value.TRUE)
              ? (isDark ? new Color(10, 55, 20) : new Color(205, 255, 215))
              : (isDark ? new Color(12, 35, 18) : new Color(230, 245, 232));
          strokeColor = targetColor;
          pulseColor = isDark ? Color.WHITE : Color.BLACK;
        } else {
          final var isHigh = (curVal == Value.TRUE);
          fillColor = isHigh
              ? (isDark ? new Color(10, 55, 20) : new Color(205, 255, 215))
              : (isDark ? new Color(12, 35, 18) : new Color(230, 245, 232));
          strokeColor = isHigh ? Value.trueColor : Value.falseColor;
          pulseColor = isHigh
              ? (isDark ? new Color(180, 255, 190) : new Color(0, 130, 35))
              : (isDark ? new Color(90, 150, 100) : new Color(60, 120, 70));
        }
      }
    }

    final int cornerRadius = isSmall ? 2 : 4;

    // Draw background body
    g.setColor(fillColor);
    g.fillRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), cornerRadius, cornerRadius);

    // Draw pulse symbol inside square
    drawPulseSymbol(g2, bds, trigger, pulseColor, curVal);

    // Draw outer frame
    g.setColor(strokeColor);
    final int borderStroke = isSmall ? 1 : ((isActive || isHolding || isLatched || curVal == Value.ERROR) ? 2 : 1);
    GraphicsUtil.switchToWidth(g, borderStroke);
    g.drawRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), cornerRadius, cornerRadius);
    GraphicsUtil.switchToWidth(g, 1);

    painter.drawLabel();
    painter.drawPorts();
  }

  private void drawPulseSymbol(Graphics2D g2, Bounds bds, AttributeOption trigger, Color color, Value curVal) {
    final var x = bds.getX();
    final var y = bds.getY();
    final var w = bds.getWidth();
    final var h = bds.getHeight();

    g2.setColor(color);
    final var strokeW = (w <= 10) ? 1.0f : 1.6f;
    g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    if (curVal == Value.UNKNOWN) {
      final var fontSize = Math.max(7, (int) (h * 0.7));
      g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
      GraphicsUtil.drawCenteredText(g2, "Z", x + w / 2, y + h / 2 - 1);
    } else if (curVal == Value.ERROR) {
      final var fontSize = Math.max(8, (int) (h * 0.75));
      g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
      GraphicsUtil.drawCenteredText(g2, "!", x + w / 2, y + h / 2 - 1);
    } else if (trigger == TRIG_HIGH) {
      // Pulse _П_
      final var padX = Math.max(1.5, w * 0.15);
      final var padY = Math.max(1.5, h * 0.2);
      final var stepX = (w - 2 * padX) / 4.0;
      final var path = new Path2D.Double();
      path.moveTo(x + padX, y + h - padY);
      path.lineTo(x + padX + stepX, y + h - padY);
      path.lineTo(x + padX + stepX, y + padY);
      path.lineTo(x + padX + 3 * stepX, y + padY);
      path.lineTo(x + padX + 3 * stepX, y + h - padY);
      path.lineTo(x + w - padX, y + h - padY);
      g2.draw(path);
    } else if (trigger == TRIG_LOW) {
      // Negative pulse ‾|_|‾
      final var padX = Math.max(1.5, w * 0.15);
      final var padY = Math.max(1.5, h * 0.2);
      final var stepX = (w - 2 * padX) / 4.0;
      final var path = new Path2D.Double();
      path.moveTo(x + padX, y + padY);
      path.lineTo(x + padX + stepX, y + padY);
      path.lineTo(x + padX + stepX, y + h - padY);
      path.lineTo(x + padX + 3 * stepX, y + h - padY);
      path.lineTo(x + padX + 3 * stepX, y + padY);
      path.lineTo(x + w - padX, y + padY);
      g2.draw(path);
    } else { // TRIG_ANY
      // Pulse wave ∿
      final var padX = Math.max(1.5, w * 0.15);
      final var padY = Math.max(1.5, h * 0.2);
      final var stepX = (w - 2 * padX) / 3.0;
      final var path = new Path2D.Double();
      path.moveTo(x + padX, y + h / 2.0);
      path.lineTo(x + padX + stepX, y + padY);
      path.lineTo(x + padX + 2 * stepX, y + h - padY);
      path.lineTo(x + w - padX, y + h / 2.0);
      g2.draw(path);
    }
  }

  @Override
  public void propagate(InstanceState state) {
    final var curVal = state.getPortValue(0);
    var probeState = (PulseProbeState) state.getData();
    if (probeState == null) {
      probeState = new PulseProbeState(state);
      state.setData(probeState);
    }
    probeState.update(curVal, state);
  }
}
