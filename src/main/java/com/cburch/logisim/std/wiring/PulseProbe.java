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
import java.util.Objects;
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

  public static final AttributeOption TRIG_HIGH =
      new AttributeOption("high", S.getter("pulseProbeTriggerHigh"));
  public static final AttributeOption TRIG_LOW =
      new AttributeOption("low", S.getter("pulseProbeTriggerLow"));
  public static final AttributeOption TRIG_ANY =
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
    private Value baselineVal = Value.UNKNOWN;
    private boolean initialized = false;
    private Value caughtVal = null;
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

      if (!initialized) {
        initialized = true;
        lastVal = curVal;
        baselineVal = curVal;
        return;
      }

      if (Objects.equals(curVal, lastVal)) {
        return;
      }

      final var prevVal = lastVal;
      lastVal = curVal;
      boolean triggerConditionMet = false;
      boolean currentIsActive = false;

      if (trigger == TRIG_HIGH) {
        currentIsActive = Objects.equals(curVal, Value.TRUE);
        if (!Objects.equals(prevVal, Value.TRUE) && Objects.equals(curVal, Value.TRUE)) {
          // Transition into 1 from 0, Z, or ERR
          triggerConditionMet = true;
          caughtVal = Value.TRUE;
        } else if (Objects.equals(prevVal, Value.TRUE) && !Objects.equals(curVal, Value.TRUE)) {
          // 1 pulse completed: returning to 0, Z, or ERR -> start hold
          caughtVal = Value.TRUE;
          startHold(isLatchMode);
        }
      } else if (trigger == TRIG_LOW) {
        currentIsActive = Objects.equals(curVal, Value.FALSE);
        if (!Objects.equals(prevVal, Value.FALSE) && Objects.equals(curVal, Value.FALSE)) {
          // Transition into 0 from 1, Z, or ERR
          triggerConditionMet = true;
          caughtVal = Value.FALSE;
        } else if (Objects.equals(prevVal, Value.FALSE) && !Objects.equals(curVal, Value.FALSE)) {
          // 0 pulse completed: returning to 1, Z, or ERR -> start hold
          caughtVal = Value.FALSE;
          startHold(isLatchMode);
        }
      } else { // TRIG_ANY
        triggerConditionMet = true;
        if (!holding && !latched) {
          baselineVal = prevVal;
          currentIsActive = true;
          caughtVal = curVal;
          startHold(isLatchMode);
        } else {
          if (Objects.equals(curVal, baselineVal)) {
            // Returning to baseline after pulse/glitch: maintain caughtVal and extend hold
            currentIsActive = false;
            startHold(isLatchMode);
          } else {
            // New pulse or change during hold
            currentIsActive = true;
            caughtVal = curVal;
            startHold(isLatchMode);
          }
        }
      }

      if (triggerConditionMet && isLatchMode) {
        latched = true;
      }

      active = currentIsActive;
    }

    private void startHold(boolean isLatchMode) {
      holding = true;
      if (isLatchMode) {
        latched = true;
      }
      holdTimer.restart();
    }

    public void resetLatch() {
      latched = false;
      holding = false;
      caughtVal = null;
      baselineVal = lastVal;
      holdTimer.stop();
      if (component != null) {
        component.fireInvalidated();
      }
    }

    public void resetLatch(InstanceState state) {
      resetLatch();
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

    public Value getCaughtValue() {
      return caughtVal;
    }

    public boolean isLastTransitionToTrue() {
      return caughtVal == Value.TRUE;
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
        if (!latched) {
          caughtVal = null;
          baselineVal = lastVal;
        }
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
    final var caughtVal = probeState == null ? null : probeState.getCaughtValue();

    // Determine colors based on digital logic states (bright green trueColor / dark green falseColor)
    Color fillColor;
    Color strokeColor;
    Color pulseColor;

    final var isPulseActive = isActive || isHolding || isLatched;

    if (isPulseActive) {
      final var displayVal = caughtVal != null ? caughtVal : (trigger == TRIG_LOW ? Value.FALSE : Value.TRUE);
      if (displayVal == Value.TRUE) {
        // High pulse / Rising edge (* -> 1 -> *)
        fillColor = isDark ? new Color(10, 55, 20) : new Color(205, 255, 215);
        strokeColor = Value.trueColor;
        pulseColor = isDark ? Color.WHITE : Color.BLACK;
      } else if (displayVal == Value.FALSE) {
        // Low pulse / Falling edge (* -> 0 -> *)
        fillColor = isDark ? new Color(15, 45, 22) : new Color(210, 245, 215);
        strokeColor = Value.falseColor;
        pulseColor = isDark ? Color.WHITE : Color.BLACK;
      } else if (displayVal == Value.UNKNOWN) {
        // Floating / Z glitch (* -> Z -> *)
        fillColor = isDark ? new Color(15, 30, 50) : new Color(225, 235, 250);
        strokeColor = Value.unknownColor;
        pulseColor = strokeColor;
      } else {
        // Error / Conflict glitch (* -> ERR -> *)
        fillColor = isDark ? new Color(60, 10, 10) : new Color(255, 220, 220);
        strokeColor = Value.errorColor;
        pulseColor = strokeColor;
      }
    } else if (curVal == Value.UNKNOWN) {
      // Floating / Z-state in idle
      fillColor = isDark ? new Color(15, 30, 50) : new Color(225, 235, 250);
      strokeColor = Value.unknownColor;
      pulseColor = strokeColor;
    } else if (curVal == Value.ERROR) {
      // Error / Conflict in idle
      fillColor = isDark ? new Color(60, 10, 10) : new Color(255, 220, 220);
      strokeColor = Value.errorColor;
      pulseColor = strokeColor;
    } else {
      // Idle state
      fillColor = isDark ? new Color(30, 30, 30) : Color.WHITE;
      strokeColor = Color.GRAY;
      pulseColor = Color.GRAY;
    }

    final int cornerRadius = isSmall ? 2 : 4;

    // Draw background body
    g.setColor(fillColor);
    g.fillRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), cornerRadius, cornerRadius);

    // Draw pulse symbol inside square
    final var displayVal = isPulseActive ? (caughtVal != null ? caughtVal : (trigger == TRIG_LOW ? Value.FALSE : Value.TRUE)) : curVal;
    drawPulseSymbol(g2, bds, trigger, pulseColor, curVal, displayVal, isPulseActive);

    // Draw outer frame
    g.setColor(strokeColor);
    final int borderStroke = isSmall ? 1 : ((isPulseActive || curVal == Value.ERROR) ? 2 : 1);
    GraphicsUtil.switchToWidth(g, borderStroke);
    g.drawRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), cornerRadius, cornerRadius);
    GraphicsUtil.switchToWidth(g, 1);

    painter.drawLabel();
    painter.drawPorts();
  }

  private void drawPulseSymbol(
      Graphics2D g2,
      Bounds bds,
      AttributeOption trigger,
      Color color,
      Value curVal,
      Value displayVal,
      boolean isPulseActive) {
    final var x = bds.getX();
    final var y = bds.getY();
    final var w = bds.getWidth();
    final var h = bds.getHeight();

    g2.setColor(color);
    final var strokeW = (w <= 10) ? 1.0f : 1.6f;
    g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    if (!isPulseActive && curVal == Value.UNKNOWN) {
      final var fontSize = Math.max(7, (int) (h * 0.7));
      g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
      GraphicsUtil.drawCenteredText(g2, "Z", x + w / 2, y + h / 2 - 1);
    } else if (!isPulseActive && curVal == Value.ERROR) {
      final var fontSize = Math.max(8, (int) (h * 0.75));
      g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
      GraphicsUtil.drawCenteredText(g2, "!", x + w / 2, y + h / 2 - 1);
    } else if (isPulseActive && displayVal == Value.UNKNOWN) {
      final var fontSize = Math.max(7, (int) (h * 0.7));
      g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
      GraphicsUtil.drawCenteredText(g2, "Z", x + w / 2, y + h / 2 - 1);
    } else if (isPulseActive && displayVal == Value.ERROR) {
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
