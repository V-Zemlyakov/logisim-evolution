/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io.extra;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.icons.NE555Icon;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceComponent;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.tools.key.DirectionConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import javax.swing.Timer;

public class NE555 extends InstanceFactory {

  public static final String _ID = "NE555";

  public static final int PIN_OUT = 0;
  public static final int PIN_TRIG = 1;

  public static final AttributeOption MODE_ASTABLE = new AttributeOption("astable",
      S.getter("ne555ModeAstable"));
  public static final AttributeOption MODE_MONOSTABLE = new AttributeOption("monostable",
      S.getter("ne555ModeMonostable"));
  public static final Attribute<AttributeOption> ATTR_MODE = Attributes.forOption(
      "mode",
      S.getter("ne555ModeAttr"),
      new AttributeOption[] { MODE_ASTABLE, MODE_MONOSTABLE });

  public static final Attribute<Integer> ATTR_R1 = Attributes.forInteger("r1",
      S.getter("ne555ResistorR1Attr"));
  public static final Attribute<Integer> ATTR_C1 = Attributes.forInteger("c1",
      S.getter("ne555CapacitorC1Attr"));

  public static final double FIXED_RFIXED = 1000.0; // 1 kOhm standard pull-up resistor from VCC

  public static final int MIN_R1_OHM = 100;          // 100 Ohm
  public static final int MAX_R1_OHM = 10_000_000;   // 10 MOhm
  public static final int DEFAULT_R1_OHM = 71_660;   // 71.66 kOhm gives T ≈ 1.00 s

  public static final int MIN_C1_UF = 1;             // 1 uF (minimum integer uF)
  public static final int MAX_C1_UF = 1000;          // 1000 uF
  public static final int DEFAULT_C1_UF = 10;        // 10 uF (gives T ≈ 1.00 s with R1 = 71.66 kOhm)

  private static final double DEFAULT_VCC = 5.0;
  private static final double TIMER_INTERVAL_SEC = 0.010;

  public static class NE555State implements InstanceData, Cloneable, ActionListener {
    private WeakReference<InstanceComponent> comp;
    private WeakReference<Simulator> simRef;
    private Timer timer;

    volatile double capVoltage;
    volatile double vccVoltage;
    volatile boolean output;
    volatile boolean disch;
    volatile boolean running;
    volatile boolean waiting;
    volatile long activeStartNanos;
    volatile long lastUpdateNanos;

    public NE555State(InstanceState state) {
      comp = new WeakReference<>(state.getInstance().getComponent());
      simRef = new WeakReference<>(state.getProject().getSimulator());
      capVoltage = 0.0;
      vccVoltage = DEFAULT_VCC;
      output = true;
      disch = true;
      running = false;
      waiting = false;
      activeStartNanos = System.nanoTime();
      lastUpdateNanos = System.nanoTime();
      timer = new Timer((int) (TIMER_INTERVAL_SEC * 1000), this);
      timer.setCoalesce(true);
      timer.start();
    }

    public void stop() {
      if (timer != null && timer.isRunning()) {
        timer.stop();
      }
    }

    @Override
    public Object clone() {
      try {
        final var s = (NE555State) super.clone();
        s.comp = new WeakReference<>(comp.get());
        s.simRef = new WeakReference<>(simRef.get());
        s.timer = new Timer((int) (TIMER_INTERVAL_SEC * 1000), s);
        s.timer.setCoalesce(true);
        s.timer.start();
        return s;
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final var c = comp.get();
      final var s = simRef.get();
      if (c == null || s == null) {
        stop();
        return;
      }
      c.fireInvalidated();
      s.nudge();
    }
  }

  public NE555() {
    super(_ID, S.getter("ne555Component"));
    setAttributes(
        new Attribute[] {
            StdAttr.LABEL,
            StdAttr.LABEL_LOC,
            StdAttr.LABEL_FONT,
            StdAttr.LABEL_COLOR,
            StdAttr.LABEL_VISIBILITY,
            ATTR_MODE,
            ATTR_R1,
            ATTR_C1,
        },
        new Object[] {
            "",
            Direction.NORTH,
            StdAttr.DEFAULT_LABEL_FONT,
            StdAttr.DEFAULT_LABEL_COLOR,
            true,
            MODE_ASTABLE,
            DEFAULT_R1_OHM,
            DEFAULT_C1_UF,
        });
    setIcon(new NE555Icon());
    setKeyConfigurator(new DirectionConfigurator(StdAttr.LABEL_LOC, KeyEvent.ALT_DOWN_MASK));
  }

  private static AttributeOption getMode(AttributeSet attrs) {
    final var m = attrs.getValue(ATTR_MODE);
    return m != null ? m : MODE_ASTABLE;
  }

  public static double getResistanceOhm(AttributeSet attrs) {
    final var rOhm = attrs.getValue(ATTR_R1);
    final var val = (rOhm == null) ? DEFAULT_R1_OHM : Math.max(MIN_R1_OHM, Math.min(MAX_R1_OHM, rOhm));
    return (double) val;
  }

  public static double getCapFarad(AttributeSet attrs) {
    final var cUf = attrs.getValue(ATTR_C1);
    final var val = (cUf == null) ? DEFAULT_C1_UF : Math.max(MIN_C1_UF, Math.min(MAX_C1_UF, cUf));
    return val * 1e-6;
  }

  private static double getVcc() {
    return DEFAULT_VCC;
  }

  private static double getCtrlVoltage(NE555State s) {
    return s.vccVoltage * 2.0 / 3.0;
  }

  @Override
  protected Object getInstanceFeature(Instance instance, Object key) {
    if (key == com.cburch.logisim.tools.ToolTipMaker.class) {
      return (com.cburch.logisim.tools.ToolTipMaker) e -> {
        final var attrs = instance.getAttributeSet();
        final var mode = getMode(attrs);
        if (mode == MODE_ASTABLE) {
          return S.get("ne555FormulaAstableTip");
        } else {
          return S.get("ne555FormulaMonostableTip");
        }
      };
    }
    return super.getInstanceFeature(instance, key);
  }

  @Override
  protected void configureNewInstance(Instance instance) {
    instance.addAttributeListener();
    configurePorts(instance);
  }

  private void configurePorts(Instance instance) {
    final var attrs = instance.getAttributeSet();
    final var mode = getMode(attrs);
    final var edgeX = 80;

    if (mode == MODE_MONOSTABLE) {
      final var ports = new Port[2];
      ports[PIN_OUT] = new Port(edgeX, 0, Port.OUTPUT, BitWidth.ONE);
      ports[PIN_OUT].setToolTip(S.getter("ne555PortOutTip"));
      ports[PIN_TRIG] = new Port(-edgeX, 10, Port.INPUT, BitWidth.ONE);
      ports[PIN_TRIG].setToolTip(S.getter("ne555PortTrigTip"));
      instance.setPorts(ports);
    } else {
      final var ports = new Port[1];
      ports[PIN_OUT] = new Port(edgeX, 0, Port.OUTPUT, BitWidth.ONE);
      ports[PIN_OUT].setToolTip(S.getter("ne555PortOutTip"));
      instance.setPorts(ports);
    }
  }

  @Override
  public Bounds getOffsetBounds(AttributeSet attrs) {
    return Bounds.create(-80, -60, 160, 120);
  }

  @Override
  protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
    final var attrs = instance.getAttributeSet();
    if (attr == ATTR_MODE) {
      instance.recomputeBounds();
      configurePorts(instance);
      instance.fireInvalidated();
    } else if (attr == ATTR_R1) {
      final var val = attrs.getValue(ATTR_R1);
      if (val != null) {
        final var clamped = Math.max(MIN_R1_OHM, Math.min(MAX_R1_OHM, val));
        if (!val.equals(clamped)) {
          attrs.setValue(ATTR_R1, clamped);
        }
      }
      instance.fireInvalidated();
    } else if (attr == ATTR_C1) {
      final var val = attrs.getValue(ATTR_C1);
      if (val != null) {
        final var clamped = Math.max(MIN_C1_UF, Math.min(MAX_C1_UF, val));
        if (!val.equals(clamped)) {
          attrs.setValue(ATTR_C1, clamped);
        }
      }
      instance.fireInvalidated();
    } else if (attr == StdAttr.LABEL_LOC) {
      instance.computeLabelTextField(Instance.AVOID_CENTER | Instance.AVOID_LEFT);
    }
  }

  @Override
  public void paintGhost(InstancePainter painter) {
    final var g = painter.getGraphics();
    final var bds = painter.getBounds();
    GraphicsUtil.switchToWidth(g, 2);
    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.drawRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), 8, 8);
    GraphicsUtil.switchToWidth(g, 1);
  }

  @Override
  public void paintInstance(InstancePainter painter) {
    final var g = painter.getGraphics();
    final var bds = painter.getBounds();
    final var showState = painter.getShowState();
    final var s = (NE555State) painter.getData();
    final var attrs = painter.getAttributeSet();
    final var mode = getMode(attrs);

    final var x = bds.getX();
    final var y = bds.getY();
    final var w = bds.getWidth();
    final var h = bds.getHeight();

    // Outer background box with theme support
    g.setColor(new Color(AppPreferences.CANVAS_BG_COLOR.get()));
    g.fillRoundRect(x, y, w, h, 8, 8);
    g.setColor(new Color(AppPreferences.COMPONENT_SECONDARY_COLOR.get()));
    g.drawRoundRect(x, y, w, h, 8, 8);

    final var rOhm = attrs.getValue(ATTR_R1);
    final var cUf = attrs.getValue(ATTR_C1);

    final var rVal = (rOhm == null) ? DEFAULT_R1_OHM : Math.max(MIN_R1_OHM, Math.min(MAX_R1_OHM, rOhm));
    final var cVal = (cUf == null) ? DEFAULT_C1_UF : Math.max(MIN_C1_UF, Math.min(MAX_C1_UF, cUf));

    final var rStr = (rVal >= 1000) ? (rVal / 1000 + "k") : (rVal + "Ω");
    final var cStr = cVal + "µF";

    final var cx = x + w / 2;
    final var cy = y + h / 2;

    g.translate(cx, cy);

    if (mode == MODE_ASTABLE) {
      drawAstableSchematic(painter, g, s, rStr, cStr, showState);
    } else {
      drawMonostableSchematic(painter, g, s, rStr, cStr, showState);
    }

    drawTimingInfo(g, attrs, mode);

    g.translate(-cx, -cy);

    painter.drawPorts();
    painter.drawLabel();
  }

  // ---------------------------------------------------------------------------
  // Отрисовка внутреннего корпуса микросхемы NE555 и подписей её пинов
  // ---------------------------------------------------------------------------
  private void drawInnerChip(Graphics g, boolean showState, NE555State s) {
    // Координаты корпуса микросхемы (сдвинуты влево на 10px: от x=10 до x=70):
    final var icX = 10;   // Левый край микросхемы
    final var icY = -35;  // Верхний край микросхемы
    final var icW = 60;   // Ширина микросхемы (от x=10 до x=70)
    final var icH = 70;   // Высота микросхемы

    // 1. Заливка корпуса и рамка (адаптируется к теме)
    g.setColor(new Color(AppPreferences.CANVAS_BG_COLOR.get()));
    g.fillRect(icX, icY, icW, icH);
    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.drawRect(icX, icY, icW, icH);

    // 2. Название микросхемы NE555 в верхней части корпуса
    g.setFont(g.getFont().deriveFont(7.0f).deriveFont(java.awt.Font.BOLD));
    g.drawString("NE555", icX + 19, icY + 57);

    // 3. Обозначения выводов (пинов) внутри корпуса
    g.setFont(g.getFont().deriveFont(5.5f).deriveFont(java.awt.Font.PLAIN));

    // Верхние пины
    g.drawString("4 RST",  icX + 2,  icY + 9);
    g.drawString("8 VCC",  icX + 34, icY + 9);

    // Левые пины
    g.drawString("7 DIS",  icX + 2, icY + 20);
    g.drawString("6 THR",  icX + 2, icY + 34);
    g.drawString("2 TRIG", icX + 2, icY + 47);
    g.drawString("1 GND",  icX + 2, icY + icH - 2);

    // Правый пин
    g.drawString("3 OUT",  icX + 40, icY + 37);

    // Нижний пин
    g.drawString("5 CTRL", icX + 35, icY + icH - 2);

    // 4. Светодиодный индикатор состояния (светло-зеленый при 1, темно-зеленый при 0)
    if (showState && s != null) {
      final var ledSize = 5;
      g.setColor(s.output ? com.cburch.logisim.data.Value.trueColor : com.cburch.logisim.data.Value.falseColor);
      g.fillOval(icX + 30, icY + 32, ledSize, ledSize);
      g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    }
  }

  // ---------------------------------------------------------------------------
  // Общий каркас схемы для обоих режимов: шины Vcc/GND, чип, питание, CTRL и OUT
  // ---------------------------------------------------------------------------
  private void drawCommonFrame(InstancePainter painter, Graphics g, NE555State s, boolean showState) {
    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.setFont(g.getFont().deriveFont(5.5f).deriveFont(java.awt.Font.PLAIN));

    // 1. Верхняя шина Vcc и нижняя шина GND (от x=-60 до x=45)
    g.drawLine(-60, -48, 45, -48);
    g.drawString("Vcc", -12, -40);
    g.drawLine(-60, 48, 45, 48);
    g.drawString("GND", -12, 45);

    // 2. Внутренний чип NE555 (icX = 10, от x=10 до x=70)
    drawInnerChip(g, showState, s);

    // 3. Подключение пина 4 (RST, x=25) и пина 8 (VCC, x=45) к шине Vcc
    drawDot(g, 25, -48);
    g.drawLine(25, -48, 25, -35);
    drawDot(g, 45, -48);
    g.drawLine(45, -48, 45, -35);

    // 4. Фильтрующий конденсатор 10nF от пина 5 (CTRL, x=45) к шине GND
    drawCapacitor(g, 45, 35, 48, "10nF", -1.0, 3);
    drawDot(g, 45, 48);

    // 5. Подключение пина 1 (GND, x=18) к шине GND
    g.drawLine(18, 35, 18, 48);
    drawDot(g, 18, 48);

    // 6. Выходной провод от пина 3 (OUT, x=70) к правому краю элемента (x=80)
    final var outVal = painter.getPortValue(PIN_OUT);
    final var outColor = (showState && outVal != null) ? outVal.getColor() : new Color(AppPreferences.COMPONENT_COLOR.get());
    GraphicsUtil.switchToWidth(g, 3);
    g.setColor(outColor);
    g.drawLine(70, 0, 80, 0);
    GraphicsUtil.switchToWidth(g, 1);
  }

  // ---------------------------------------------------------------------------
  // Обвязка только автоколебательного режима (Astable)
  // ---------------------------------------------------------------------------
  private void drawAstableSchematic(InstancePainter painter, Graphics g, NE555State s, String rStr, String cStr, boolean showState) {
    drawCommonFrame(painter, g, s, showState);

    // Верхний фиксированный резистор R2 (1k): от Vcc (y=-48) до узла DIS (y=-20) на x=-60
    drawResistor(g, -60, -48, -20, "R2 (1k)");
    drawDot(g, -60, -20);
    g.drawLine(-60, -20, 10, -20); // провод к пину 7 (DIS)

    // Нижний резистор R1: от узла DIS (y=-20) до узла TRIG (y=12) на x=-60
    drawResistor(g, -60, -20, 12, "R1 (" + rStr + ")");
    drawDot(g, -60, 12);
    g.drawLine(-60, 12, 10, 12);  // провод к пину 2 (TRIG)

    // Перемычка между пином 6 (THR) и пином 2 (TRIG)
    drawDot(g, 0, 12);
    g.drawLine(0, 12, 0, -3);
    g.drawLine(0, -3, 10, -3);

    // Конденсатор C1: от узла TRIG (y=12) к шине GND (y=48) на x=-60
    final var capV = (showState && s != null) ? Math.max(0.0, s.capVoltage) : 0.0;
    drawCapacitor(g, -60, 12, 48, "C1 (" + cStr + ")", capV, 0);
  }

  // ---------------------------------------------------------------------------
  // Обвязка только ждущего режима (Monostable)
  // ---------------------------------------------------------------------------
  private void drawMonostableSchematic(InstancePainter painter, Graphics g, NE555State s, String rStr, String cStr, boolean showState) {
    drawCommonFrame(painter, g, s, showState);

    // В моновибраторе резистор R1: от Vcc к пину 7 DIS
    drawResistor(g, -60, -48, -20, "R1 (" + rStr + ")");
    drawDot(g, -60, -20);
    g.drawLine(-60, -20, 10, -20); // провод к пину 7 DIS

    // Перемычка от пина 7 DIS к пину 6 THR
    drawDot(g, 0, -20);
    g.drawLine(0, -20, 0, -3);
    g.drawLine(0, -3, 10, -3);   // к пину 6 THR

    // Соединительный провод от узла (-60, -20) вниз к конденсатору C1 (y=12)
    g.drawLine(-60, -20, -60, 12);

    // Конденсатор C1: от узла y=12 к шине GND (y=48) на x=-60
    final var capV = (showState && s != null) ? Math.max(0.0, s.capVoltage) : 0.0;
    drawCapacitor(g, -60, 12, 48, "C1 (" + cStr + ")", capV, 0);

    // Внешний вход Trigger к пину 2 (TRIG, y=10) от левого края (x = -80)
    final var trigVal = painter.getPortValue(PIN_TRIG);
    final var trigColor = (showState && trigVal != null) ? trigVal.getColor() : new Color(AppPreferences.COMPONENT_COLOR.get());
    GraphicsUtil.switchToWidth(g, 3);
    g.setColor(trigColor);
    g.drawLine(-80, 10, 10, 10);
    GraphicsUtil.switchToWidth(g, 1);
    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.drawString("Trigger", -55, 6);
  }

  private void drawResistor(Graphics g, int cx, int y1, int y2, String label) {
    final var w = 8;
    final var h = Math.abs(y2 - y1);
    final var topY = Math.min(y1, y2);
    final var boxY = topY + (h - 18) / 2;
    g.setColor(new Color(AppPreferences.CANVAS_BG_COLOR.get()));
    g.fillRect(cx - w / 2, boxY, w, 18);
    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.drawRect(cx - w / 2, boxY, w, 18);
    g.drawLine(cx, topY, cx, boxY);
    g.drawLine(cx, boxY + 18, cx, topY + h);
    if (label != null && !label.isEmpty()) {
      g.setFont(g.getFont().deriveFont(5.5f).deriveFont(java.awt.Font.PLAIN));
      g.drawString(label, cx + w / 2 + 2, boxY + 11);
    }
  }

  private void drawCapacitor(Graphics g, int cx, int y1, int y2, String label, double voltage, int labelYOffset) {
    final var topY = Math.min(y1, y2);
    final var h = Math.abs(y2 - y1);
    final var midY = topY + h / 2;
    final var gap = 4;
    final var plateW = 10;

    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.drawLine(cx, topY, cx, midY - gap / 2);
    g.drawLine(cx, midY + gap / 2, cx, topY + h);
    g.drawLine(cx - plateW / 2, midY - gap / 2, cx + plateW / 2, midY - gap / 2);
    g.drawLine(cx - plateW / 2, midY + gap / 2, cx + plateW / 2, midY + gap / 2);
    if (label != null && !label.isEmpty()) {
      g.setFont(g.getFont().deriveFont(5.5f).deriveFont(java.awt.Font.PLAIN));
      g.drawString(label, cx + plateW / 2 + 2, midY + 4 + labelYOffset);
      if (voltage >= 0.0) {
        final var vStr = String.format(java.util.Locale.US, "%.1fV", voltage);
        g.setFont(g.getFont().deriveFont(5.0f).deriveFont(java.awt.Font.BOLD));
        g.drawString(vStr, cx + plateW / 2 + 2, midY - gap / 2 - 1);
      }
    }
  }

  private void drawDot(Graphics g, int x, int y) {
    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.fillOval(x - 2, y - 2, 4, 4);
  }

  public static double getPeriodSeconds(AttributeSet attrs) {
    final var mode = getMode(attrs);
    final var cFarad = getCapFarad(attrs);
    if (mode == MODE_ASTABLE) {
      final var rfixed = FIXED_RFIXED;
      final var r1 = getResistanceOhm(attrs);
      return 0.693 * (rfixed + 2.0 * r1) * cFarad;
    } else {
      final var r1 = getResistanceOhm(attrs);
      return 1.1 * r1 * cFarad;
    }
  }

  public static double getFrequencyHz(AttributeSet attrs) {
    final var mode = getMode(attrs);
    if (mode != MODE_ASTABLE) return 0.0;
    final var period = getPeriodSeconds(attrs);
    return (period > 0) ? (1.0 / period) : 0.0;
  }

  public static String formatTime(double seconds) {
    if (seconds <= 0) return "0s";
    if (seconds < 1e-6) return String.format(java.util.Locale.US, "%.2f ns", seconds * 1e9);
    if (seconds < 1e-3) return String.format(java.util.Locale.US, "%.2f µs", seconds * 1e6);
    if (seconds < 1.0) return String.format(java.util.Locale.US, "%.2f ms", seconds * 1e3);
    return String.format(java.util.Locale.US, "%.2f s", seconds);
  }

  public static String formatFrequency(double hz) {
    if (hz <= 0) return "N/A";
    if (hz >= 1e6) return String.format(java.util.Locale.US, "%.2f MHz", hz / 1e6);
    if (hz >= 1e3) return String.format(java.util.Locale.US, "%.2f kHz", hz / 1e3);
    return String.format(java.util.Locale.US, "%.2f Hz", hz);
  }

  private void drawTimingInfo(Graphics g, AttributeSet attrs, AttributeOption mode) {
    g.setColor(new Color(AppPreferences.COMPONENT_COLOR.get()));
    g.setFont(g.getFont().deriveFont(5.5f).deriveFont(java.awt.Font.BOLD));
    final var period = getPeriodSeconds(attrs);
    if (mode == MODE_ASTABLE) {
      final var freq = getFrequencyHz(attrs);
      final var str = "T=" + formatTime(period) + "  f=" + formatFrequency(freq);
      g.drawString(str, -70, 56);
    } else {
      final var str = "T=" + formatTime(period);
      g.drawString(str, -70, 56);
    }
  }

  private static NE555State getOrCreateState(InstanceState state) {
    var s = (NE555State) state.getData();
    if (s == null) {
      s = new NE555State(state);
      state.setData(s);
    }
    return s;
  }

  private static void applyVcc(double vcc, NE555State s) {
    s.vccVoltage = Math.max(0.1, vcc);
  }

  private static void updateCap(AttributeSet attrs, NE555State s, double dt,
      double targetV, double rCharge) {
    if (rCharge > 0) {
      final var c = getCapFarad(attrs);
      final var tau = rCharge * c;
      if (tau > 1e-18) {
        final var alpha = 1.0 - Math.exp(-dt / tau);
        final var clamped = Math.min(alpha, 1.0 - 1e-6);
        s.capVoltage += (targetV - s.capVoltage) * clamped;
      } else {
        s.capVoltage = targetV;
      }
    }
  }

  @Override
  public void propagate(InstanceState state) {
    final var s = getOrCreateState(state);
    final var attrs = state.getAttributeSet();
    final var mode = getMode(attrs);
    final var vcc = getVcc();
    applyVcc(vcc, s);

    final var trigVal = (mode == MODE_MONOSTABLE) ? state.getPortValue(PIN_TRIG) : Value.UNKNOWN;
    final var trigLow = (trigVal == Value.FALSE);

    final var thresV = getCtrlVoltage(s);

    final var now = System.nanoTime();
    final var dt = Math.min((now - s.lastUpdateNanos) / 1e9, 0.1);
    s.lastUpdateNanos = now;

    if (mode == MODE_ASTABLE) {
      final var trigV = s.vccVoltage / 3.0;
      doAstable(state, s, trigV, thresV, dt);
    } else {
      doMonostable(state, s, trigLow, thresV, dt);
    }

    setOutputs(state, s, vcc);
  }

  private static void doAstable(InstanceState state, NE555State s,
      double trigV, double thresV, double dt) {
    final var rfixed = FIXED_RFIXED;
    final var r1 = getResistanceOhm(state.getAttributeSet());
    final var vcc = s.vccVoltage;

    if (!s.running && !s.waiting) {
      s.capVoltage = trigV;
      s.output = true;
      s.disch = false;
      s.running = true;
    }

    if (s.output && !s.disch) {
      updateCap(state.getAttributeSet(), s, dt, vcc, rfixed + r1);
      if (s.capVoltage >= thresV) {
        s.output = false;
        s.disch = true;
      }
    } else if (!s.output && s.disch) {
      updateCap(state.getAttributeSet(), s, dt, 0.0, r1);
      final var thr = Math.min(trigV * 0.95, s.capVoltage * 0.99 + 0.005);
      if (s.capVoltage <= thr) {
        s.output = true;
        s.disch = false;
      }
    }
  }

  private static void doMonostable(InstanceState state, NE555State s,
      boolean trigLow, double thresV, double dt) {
    final var r = getResistanceOhm(state.getAttributeSet());
    final var c = getCapFarad(state.getAttributeSet());

    if (!s.running && !s.waiting) {
      s.output = false;
      s.disch = true;
      s.capVoltage = 0.0;
      if (trigLow) {
        s.running = true;
        s.output = true;
        s.disch = false;
        s.activeStartNanos = System.nanoTime();
      }
    } else if (s.running) {
      s.output = true;
      s.disch = false;
      final var targetT = 1.1 * r * c;
      final var elapsed = (System.nanoTime() - s.activeStartNanos) / 1e9;
      final var vcc = s.vccVoltage;
      final var tau = r * c;
      if (tau > 1e-18) {
        final var alpha = 1.0 - Math.exp(-dt / tau);
        s.capVoltage += (vcc - s.capVoltage) * alpha;
      } else {
        s.capVoltage = vcc;
      }
      if (s.capVoltage >= thresV || elapsed >= targetT) {
        s.output = false;
        s.disch = true;
        s.running = false;
        s.waiting = true;
      }
    } else if (s.waiting) {
      s.output = false;
      s.disch = true;
      updateCap(state.getAttributeSet(), s, dt, 0.0, 1000.0);
      if (!trigLow && s.capVoltage < 0.1) {
        s.waiting = false;
      }
    }
  }

  private static void setOutputs(InstanceState state, NE555State s, double vcc) {
    final var outV = s.output ? Value.TRUE : Value.FALSE;
    state.setPort(PIN_OUT, outV, 0);
  }
}
