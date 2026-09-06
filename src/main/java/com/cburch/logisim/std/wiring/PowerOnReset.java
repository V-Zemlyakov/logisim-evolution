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

import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.generic.ComboBox;
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
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.Timer;

public class PowerOnReset extends InstanceFactory {
  /**
   * Unique identifier of the tool, used as reference in project files.
   * Do NOT change as it will prevent project files from loading.
   *
   * Identifier value must MUST be unique string among all tools.
   */
  public static final String _ID = "POR";

  private static final AttributeOption SIZE_WIDE =
      new AttributeOption(3, S.getter("porSizeWide"));
  private static final AttributeOption SIZE_MEDIUM =
      new AttributeOption(1, S.getter("porSizeMedium"));
  private static final AttributeOption SIZE_NARROW =
      new AttributeOption(2, S.getter("porSizeNarrow"));
  private static final Attribute<AttributeOption> PORSIZE =
      Attributes.forOption(
          "porsize", S.getter("PorSize"), new AttributeOption[] {SIZE_WIDE, SIZE_MEDIUM, SIZE_NARROW});

  private static final AttributeOption HTOL =
      new AttributeOption(1, S.getter("porHighToLow"));
  private static final AttributeOption LTOH =
      new AttributeOption(2, S.getter("porLowToHigh"));
  private static final Attribute<AttributeOption> PORTRANS =
      Attributes.forOption(
          "porTransition", S.getter("porTransition"), new AttributeOption[] {HTOL, LTOH});

  public static final AttributeOption UNIT_SECONDS =
      new AttributeOption(1, S.getter("realTimeClockSeconds"));
  public static final AttributeOption UNIT_MILLISECONDS =
      new AttributeOption(2, S.getter("realTimeClockMilliseconds"));
  public static final Attribute<AttributeOption> PORUNIT =
      Attributes.forOption(
          "porUnit", S.getter("porDurationUnit"), new AttributeOption[] {UNIT_SECONDS, UNIT_MILLISECONDS});

  private static final Integer[] PRESETS_SEC = new Integer[] {1, 2, 3, 4, 5, 10};
  private static final Integer[] PRESETS_MS = new Integer[] {10, 20, 50, 100, 200, 500, 1000, 2000, 5000};

  private static class PORDurationAttribute extends Attribute<Integer> {
    private final AttributeSet attrs;

    public PORDurationAttribute(String name, StringGetter disp, AttributeSet attrs) {
      super(name, disp);
      this.attrs = attrs;
    }

    @Override
    public Component getCellEditor(Window source, Integer value) {
      final var isMs = attrs != null && attrs.getValue(PORUNIT) == UNIT_MILLISECONDS;
      final var presets = isMs ? PRESETS_MS : PRESETS_SEC;
      final var combo = new ComboBox<>(presets);
      combo.setEditable(true);

      if (value != null) {
        combo.setSelectedItem(value);
        final var editorComp = combo.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField field) {
          field.setText(value.toString());
        }
      }
      return combo;
    }

    @Override
    public Integer parse(String value) {
      try {
        final var ret = Integer.parseInt(value.trim());
        final var isMs = attrs != null && attrs.getValue(PORUNIT) == UNIT_MILLISECONDS;
        final var min = 1;
        final var max = isMs ? 60000 : 60;
        if (ret < min) {
          throw new PORValidationException(S.get("durationSmallMessage", "" + min));
        } else if (ret > max) {
          throw new PORValidationException(S.get("durationLargeMessage", "" + max));
        }
        return ret;
      } catch (PORValidationException e) {
        throw e;
      } catch (NumberFormatException e) {
        throw new NumberFormatException(S.get("freqInvalidMessage"));
      }
    }

    @Override
    public String toDisplayString(Integer value) {
      if (value == null) return "";
      if (attrs != null && attrs.getValue(PORUNIT) == UNIT_MILLISECONDS) {
        return S.get("PORDurationMsValue", value.toString());
      } else {
        if (value.equals(1)) {
          return S.get("PORDurationOneValue");
        } else {
          return S.get("PORDurationValue", value.toString());
        }
      }
    }
  }

  private static class PORValidationException extends NumberFormatException {
    PORValidationException(String message) {
      super(message);
    }
  }

  public static final PowerOnReset FACTORY = new PowerOnReset();

  public static class Poker extends InstancePoker {
    @Override
    public void mouseReleased(InstanceState state, MouseEvent e) {
      PORState ret = (PORState) state.getData();
      ret.reset(state);
    }
  }

  private static class PowerOnResetAttributes extends AbstractAttributeSet {
    private Direction facing = Direction.EAST;
    private AttributeOption size = SIZE_WIDE;
    private AttributeOption trans = HTOL;
    private AttributeOption unit = UNIT_SECONDS;
    private int duration = 2;

    private final Attribute<Integer> durationAttr =
        new PORDurationAttribute("PorHighDuration", S.getter("porHighAttr"), this);

    private final List<Attribute<?>> attributes =
        Arrays.asList(StdAttr.FACING, PORSIZE, PORTRANS, PORUNIT, durationAttr);

    @Override
    protected void copyInto(AbstractAttributeSet destObj) {
      final var dest = (PowerOnResetAttributes) destObj;
      dest.facing = this.facing;
      dest.size = this.size;
      dest.trans = this.trans;
      dest.unit = this.unit;
      dest.duration = this.duration;
    }

    @Override
    public List<Attribute<?>> getAttributes() {
      return attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getValue(Attribute<V> attr) {
      if (attr == StdAttr.FACING) return (V) facing;
      if (attr == PORSIZE) return (V) size;
      if (attr == PORTRANS) return (V) trans;
      if (attr == PORUNIT) return (V) unit;
      if (attr == durationAttr || "PorHighDuration".equals(attr.getName())) return (V) Integer.valueOf(duration);
      return null;
    }

    @Override
    public <V> void setValue(Attribute<V> attr, V value) {
      if (attr == StdAttr.FACING) {
        final var newFacing = (Direction) value;
        if (!facing.equals(newFacing)) {
          facing = newFacing;
          fireAttributeValueChanged(attr, value, null);
        }
      } else if (attr == PORSIZE) {
        final var newSize = (AttributeOption) value;
        if (!size.equals(newSize)) {
          size = newSize;
          fireAttributeValueChanged(attr, value, null);
        }
      } else if (attr == PORTRANS) {
        final var newTrans = (AttributeOption) value;
        if (!trans.equals(newTrans)) {
          trans = newTrans;
          fireAttributeValueChanged(attr, value, null);
        }
      } else if (attr == PORUNIT) {
        final var newUnit = (AttributeOption) value;
        if (!unit.equals(newUnit)) {
          final var oldUnit = unit;
          unit = newUnit;
          if (oldUnit == UNIT_SECONDS && newUnit == UNIT_MILLISECONDS) {
            duration = Math.min(duration * 1000, 60000);
            fireAttributeValueChanged(durationAttr, Integer.valueOf(duration), null);
          } else if (oldUnit == UNIT_MILLISECONDS && newUnit == UNIT_SECONDS) {
            duration = Math.max(1, (duration + 999) / 1000);
            duration = Math.min(duration, 60);
            fireAttributeValueChanged(durationAttr, Integer.valueOf(duration), null);
          }
          fireAttributeValueChanged(attr, value, null);
        }
      } else if (attr == durationAttr || "PorHighDuration".equals(attr.getName())) {
        final var newDuration = (Integer) value;
        if (duration != newDuration) {
          duration = newDuration;
          fireAttributeValueChanged(attr, value, null);
        }
      }
    }

    @Override
    public <V> List<Attribute<?>> attributesMayAlsoBeChanged(Attribute<V> attr, V value) {
      if (attr == PORUNIT) {
        return List.of(durationAttr);
      }
      return null;
    }

    public int getDurationInMs() {
      return unit == UNIT_MILLISECONDS ? duration : duration * 1000;
    }
  }

  public PowerOnReset() {
    super(_ID, S.getter("PowerOnResetComponent"));
    setFacingAttribute(StdAttr.FACING);
    setIconName("por.png");
    setInstancePoker(Poker.class);
  }

  @Override
  public AttributeSet createAttributeSet() {
    return new PowerOnResetAttributes();
  }

  private static class PORState implements InstanceData, Cloneable, ActionListener {

    private boolean value;
    private InstanceComponent component;
    private Simulator simulator;
    private final Timer tim;
    private int tstart;
    private int tend;
    private int duration;

    public PORState(InstanceState state) {
      value = true;
      component = state.getInstance().getComponent();
      simulator = state.getProject().getSimulator();
      updateParameters(state);
      tim = new Timer(duration, this);
      state.setPort(0, Value.createKnown(BitWidth.ONE, tstart), 0);
      tim.start();
    }

    private void updateParameters(InstanceState state) {
      final var attrs = state.getAttributeSet();
      if (attrs instanceof PowerOnResetAttributes porAttrs) {
        duration = porAttrs.getDurationInMs();
      } else {
        // Backward compatibility
        final var unit = attrs.getValue(PORUNIT);
        final var val = (Integer) attrs.getValue(attrs.getAttribute("PorHighDuration"));
        final var durationVal = val != null ? val : 2;
        duration = (unit == UNIT_MILLISECONDS) ? durationVal : durationVal * 1000;
      }

      if (state.getAttributeValue(PORTRANS) == LTOH) {
        tstart = 0;
        tend = 1;
      } else {
        tstart = 1;
        tend = 0;
      }
    }

    public boolean getValue() {
      return value;
    }

    public int gettstart() {
      return tstart;
    }

    public int gettend() {
      return tend;
    }

    public void reset(InstanceState state) {
      if (value) {
        tim.stop();
        value = false;
      }
      value = true;
      updateParameters(state);
      state.setPort(0, Value.createKnown(BitWidth.ONE, tstart), 0);

      tim.setInitialDelay(duration);
      tim.start();
    }

    @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == tim) {
        if (value) {
          value = false;
          component.fireInvalidated();
          if (simulator != null) simulator.nudge();
          tim.stop();
        }
      }
    }
  }

  @Override
  protected void configureNewInstance(Instance instance) {
    instance.addAttributeListener();
    instance.setPorts(new Port[] {new Port(0, 0, Port.OUTPUT, BitWidth.ONE)});

  }

  @Override
  public Bounds getOffsetBounds(AttributeSet attrs) {
    Direction facing = attrs.getValue(StdAttr.FACING);

    final var psize = attrs.getValue(PORSIZE);
    if (psize == SIZE_MEDIUM) {
      return Bounds.create(0, -20, 40, 40).rotate(Direction.WEST, facing, 0, 0);
    } else if (psize == SIZE_NARROW) {
      return Bounds.create(0, -10, 20, 20).rotate(Direction.WEST, facing, 0, 0);
    } else {
      return Bounds.create(0, -20, 200, 40).rotate(Direction.WEST, facing, 0, 0);
    }
  }

  @Override
  protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
    if (attr == StdAttr.FACING || attr == PORSIZE) {
      instance.recomputeBounds();
    }

  }

  @Override
  public void paintInstance(InstancePainter painter) {

    java.awt.Graphics g = painter.getGraphics();
    Bounds bds = painter.getInstance().getBounds();
    int x = bds.getX();
    int y = bds.getY();
    int width =  bds.getWidth();
    int height = bds.getHeight();
    GraphicsUtil.switchToWidth(g, 2);
    g.setColor(Color.WHITE);
    g.fillRect(x, y, width, height);
    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.drawRect(x, y, width, height);

    final var psize = painter.getAttributeValue(PORSIZE);

    if (psize == SIZE_WIDE) {
      Font old = g.getFont();
      g.setFont(old.deriveFont(16.0f).deriveFont(Font.BOLD));
      String txt = S.get("porLongName");

      FontMetrics fm = g.getFontMetrics();
      int wide = Math.max(width, height);

      int offset = (wide - fm.stringWidth(txt)) / 2;
      Direction facing = painter.getAttributeValue(StdAttr.FACING);

      if (((facing == Direction.NORTH) || (facing == Direction.SOUTH)) && (g instanceof Graphics2D g2)) {
        int xpos = facing == Direction.NORTH ? x + 20 - fm.getDescent() : x + 20 + fm.getDescent();
        int ypos = facing == Direction.NORTH ? y + offset : y + height - offset;
        g.setColor(Color.BLACK);
        g2.translate(xpos, ypos);
        g2.rotate(facing.toRadians());
        g.drawString(txt, 0, 0);
        g2.rotate(-facing.toRadians());
        g2.translate(-xpos, -ypos);
      } else {
        g.setColor(Color.BLACK);
        g.drawString(txt, x + offset, y + fm.getDescent() + 20);
      }
    } else {
      int x1;
      int x2;
      int x3;
      int y1;
      int y2;
      int offset;

      Font old = g.getFont();
      if  (psize == SIZE_NARROW) {
        g.setFont(old.deriveFont(6.0f).deriveFont(Font.BOLD));
        offset = 7;
      } else {
        g.setFont(old.deriveFont(14.0f).deriveFont(Font.BOLD));
        offset = 13;
      }

      y1 = y + height - 4;
      y2 = y + offset;
      x1 = x + 3;
      x2 = x + width - 4;

      Graphics2D g2 = (Graphics2D) g;
      var oldStroke = g2.getStroke();
      g2.setStroke(new BasicStroke(1));
      g.setColor(Color.BLUE);
      g.drawLine(x1, y1, x2, y1);
      x1 = x1 + 1;
      y1 = y1 + 1;
      g.drawLine(x1, y2, x1, y1);
      g2.setStroke(oldStroke);

      x1 = x + 4;
      x2 = x + width / 2;
      x3 = x + width - 4;
      y1 = y + offset + 2;
      y2 = y + height - 5;

      final var pstat = painter.getAttributeValue(PORTRANS);
      if (pstat == LTOH) {
        var tmp = y1;
        y1 = y2;
        y2 = tmp;
      }

      g.setColor(Color.RED);
      g.drawLine(x1, y1, x2, y1);
      g.drawLine(x2, y1, x2, y2);
      g.drawLine(x2, y2, x3, y2);

      g.setColor(Color.BLACK);
      g.drawString(_ID, x + 2, y + offset - 1);
    }

    painter.drawPorts();
  }

  @Override
  public void propagate(InstanceState state) {
    PORState ret = (PORState) state.getData();
    if (ret == null) {
      ret = new PORState(state);
      state.setData(ret);
    }

    state.setPort(0, Value.createKnown(BitWidth.ONE, ret.getValue() ? ret.gettstart() : ret.gettend()), 0);

    // TODO Auto-generated method stub

  }
}
