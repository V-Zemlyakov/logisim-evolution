/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.icons;

import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.std.io.extra.TwoPinLedIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

public class IconExporterTest {

  /**
   * Simple SVG Graphics2D interceptor to convert basic Java2D drawing calls into clean SVG XML.
   */
  private static class SvgGraphicsInterceptor {
    private final StringBuilder svg = new StringBuilder();
    private Color currentColor = Color.BLACK;
    private float strokeWidth = 1.0f;
    private final double targetWidth;
    private final double targetHeight;
    private double getScaleBack() {
      final double sc = AppPreferences.getScaled(1.0);
      return sc > 0 ? (1.0 / sc) : 1.0;
    }

    public SvgGraphicsInterceptor(double canvasW, double canvasH, double iconWidth) {
      this.targetWidth = canvasW > 0 ? canvasW : 16.0;
      this.targetHeight = canvasH > 0 ? canvasH : 16.0;

      svg.append(String.format(Locale.US,
          "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%.2fpx\" height=\"%.2fpx\" viewBox=\"0 0 %.2f %.2f\">\n",
          targetWidth, targetHeight, targetWidth, targetHeight));
    }

    public void setColor(Color c) {
      if (c != null) this.currentColor = c;
    }

    public void setStroke(Stroke s) {
      if (s instanceof BasicStroke bs) {
        this.strokeWidth = (float) (bs.getLineWidth() * getScaleBack());
      }
    }

    private String toHexColor(Color c) {
      if (c.getRGB() == com.cburch.logisim.prefs.AppPreferences.COMPONENT_ICON_COLOR.get() 
          || c.equals(Color.BLACK)) {
        return "#000000";
      }
      return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public void fillRect(int x, int y, int w, int h) {
      final var sb = getScaleBack();
      svg.append(String.format(Locale.US,
          "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\" />\n",
          x * sb, y * sb, w * sb, h * sb, toHexColor(currentColor)));
    }

    public void drawRect(int x, int y, int w, int h) {
      final var sb = getScaleBack();
      svg.append(String.format(Locale.US,
          "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"none\" stroke=\"%s\" stroke-width=\"%.2f\" />\n",
          x * sb, y * sb, w * sb, h * sb, toHexColor(currentColor), strokeWidth));
    }

    public void fillRoundRect(int x, int y, int w, int h, int rx, int ry) {
      final var sb = getScaleBack();
      svg.append(String.format(Locale.US,
          "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"%.2f\" ry=\"%.2f\" fill=\"%s\" />\n",
          x * sb, y * sb, w * sb, h * sb, (rx / 2.0) * sb, (ry / 2.0) * sb, toHexColor(currentColor)));
    }

    public void drawRoundRect(int x, int y, int w, int h, int rx, int ry) {
      final var sb = getScaleBack();
      svg.append(String.format(Locale.US,
          "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"%.2f\" ry=\"%.2f\" fill=\"none\" stroke=\"%s\" stroke-width=\"%.2f\" />\n",
          x * sb, y * sb, w * sb, h * sb, (rx / 2.0) * sb, (ry / 2.0) * sb, toHexColor(currentColor), strokeWidth));
    }

    public void fillOval(int x, int y, int w, int h) {
      final var sb = getScaleBack();
      final var rx = (w / 2.0) * sb;
      final var ry = (h / 2.0) * sb;
      final var cx = (x * sb) + rx;
      final var cy = (y * sb) + ry;
      svg.append(String.format(Locale.US, "  <ellipse cx=\"%.2f\" cy=\"%.2f\" rx=\"%.2f\" ry=\"%.2f\" fill=\"%s\" />\n",
          cx, cy, rx, ry, toHexColor(currentColor)));
    }

    public void drawOval(int x, int y, int w, int h) {
      final var sb = getScaleBack();
      final var rx = (w / 2.0) * sb;
      final var ry = (h / 2.0) * sb;
      final var cx = (x * sb) + rx;
      final var cy = (y * sb) + ry;
      svg.append(String.format(Locale.US,
          "  <ellipse cx=\"%.2f\" cy=\"%.2f\" rx=\"%.2f\" ry=\"%.2f\" fill=\"none\" stroke=\"%s\" stroke-width=\"%.2f\" />\n",
          cx, cy, rx, ry, toHexColor(currentColor), strokeWidth));
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
      final var sb = getScaleBack();
      svg.append(String.format(Locale.US,
          "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"%s\" stroke-width=\"%.2f\" stroke-linecap=\"round\" />\n",
          x1 * sb, y1 * sb, x2 * sb, y2 * sb, toHexColor(currentColor), strokeWidth));
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
      final var sb = getScaleBack();
      final var points = new StringBuilder();
      for (int i = 0; i < nPoints; i++) {
        if (i > 0) points.append(" ");
        points.append(String.format(Locale.US, "%.2f,%.2f", xPoints[i] * sb, yPoints[i] * sb));
      }
      svg.append(String.format("  <polygon points=\"%s\" fill=\"%s\" />\n", points, toHexColor(currentColor)));
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
      final var sb = getScaleBack();
      final var points = new StringBuilder();
      for (int i = 0; i < nPoints; i++) {
        if (i > 0) points.append(" ");
        points.append(String.format(Locale.US, "%.2f,%.2f", xPoints[i] * sb, yPoints[i] * sb));
      }
      svg.append(String.format(Locale.US, "  <polygon points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"%.2f\" />\n", points, toHexColor(currentColor), strokeWidth));
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
      final var sb = getScaleBack();
      final var points = new StringBuilder();
      for (int i = 0; i < nPoints; i++) {
        if (i > 0) points.append(" ");
        points.append(String.format(Locale.US, "%.2f,%.2f", xPoints[i] * sb, yPoints[i] * sb));
      }
      svg.append(String.format(Locale.US, "  <polyline points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"%.2f\" />\n", points, toHexColor(currentColor), strokeWidth));
    }

    public void fillShape(java.awt.Shape s) {
      final var d = shapeToSvgPathData(s);
      if (!d.isEmpty()) {
        svg.append(String.format("  <path d=\"%s\" fill=\"%s\" />\n", d, toHexColor(currentColor)));
      }
    }

    public void drawShape(java.awt.Shape s) {
      final var d = shapeToSvgPathData(s);
      if (!d.isEmpty()) {
        svg.append(String.format(Locale.US,
            "  <path d=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"%.2f\" stroke-linecap=\"round\" stroke-linejoin=\"round\" />\n",
            d, toHexColor(currentColor), strokeWidth));
      }
    }

    private String shapeToSvgPathData(java.awt.Shape s) {
      final var pi = s.getPathIterator(null);
      final var coords = new double[6];
      final var d = new StringBuilder();
      final var sb = getScaleBack();
      while (!pi.isDone()) {
        final var type = pi.currentSegment(coords);
        switch (type) {
          case java.awt.geom.PathIterator.SEG_MOVETO ->
              d.append(String.format(Locale.US, "M%.3f,%.3f ", coords[0] * sb, coords[1] * sb));
          case java.awt.geom.PathIterator.SEG_LINETO ->
              d.append(String.format(Locale.US, "L%.3f,%.3f ", coords[0] * sb, coords[1] * sb));
          case java.awt.geom.PathIterator.SEG_QUADTO ->
              d.append(String.format(Locale.US, "Q%.3f,%.3f %.3f,%.3f ",
                  coords[0] * sb, coords[1] * sb, coords[2] * sb, coords[3] * sb));
          case java.awt.geom.PathIterator.SEG_CUBICTO ->
              d.append(String.format(Locale.US, "C%.3f,%.3f %.3f,%.3f %.3f,%.3f ",
                  coords[0] * sb, coords[1] * sb, coords[2] * sb, coords[3] * sb, coords[4] * sb, coords[5] * sb));
          case java.awt.geom.PathIterator.SEG_CLOSE -> d.append("Z ");
          default -> { /* ignore */ }
        }
        pi.next();
      }
      return d.toString().trim();
    }

    public void drawString(String str, float x, float y) {
      final var sb = getScaleBack();
      svg.append(String.format(Locale.US,
          "  <text x=\"%.2f\" y=\"%.2f\" fill=\"%s\" font-family=\"SansSerif\">%s</text>\n",
          x * sb, y * sb, toHexColor(currentColor), str));
    }

    public String build() {
      return svg.toString() + "</svg>\n";
    }
  }

  /**
   * Dummy Graphics2D proxy for capturing SVG commands during icon rendering.
   */
  private static class SvgCapturingGraphics extends Graphics2D {
    private final SvgGraphicsInterceptor interceptor;

    public SvgCapturingGraphics(SvgGraphicsInterceptor interceptor) {
      this.interceptor = interceptor;
    }

    @Override
    public void setColor(Color c) {
      interceptor.setColor(c);
    }

    @Override
    public Color getColor() {
      return interceptor.currentColor;
    }

    @Override
    public void setStroke(Stroke s) {
      interceptor.setStroke(s);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
      interceptor.fillRect(x, y, width, height);
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
      interceptor.drawRect(x, y, width, height);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
      interceptor.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
      interceptor.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
      interceptor.fillOval(x, y, width, height);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
      interceptor.drawOval(x, y, width, height);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
      interceptor.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
      interceptor.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
      interceptor.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
      interceptor.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void fill(java.awt.Shape s) {
      interceptor.fillShape(s);
    }

    @Override
    public void draw(java.awt.Shape s) {
      interceptor.drawShape(s);
    }

    @Override
    public java.awt.Graphics create() {
      return this;
    }

    @Override
    public void dispose() {}

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {}

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
      return null;
    }

    @Override
    public void setRenderingHints(java.util.Map<?, ?> hints) {}

    @Override
    public void addRenderingHints(java.util.Map<?, ?> hints) {}

    @Override
    public RenderingHints getRenderingHints() {
      return null;
    }

    @Override
    public void translate(int x, int y) {}

    @Override
    public void translate(double tx, double ty) {}

    @Override
    public void rotate(double theta) {}

    @Override
    public void rotate(double theta, double x, double y) {}

    @Override
    public void scale(double sx, double sy) {}

    @Override
    public void shear(double shx, double shy) {}

    @Override
    public void transform(java.awt.geom.AffineTransform Tx) {}

    @Override
    public void setTransform(java.awt.geom.AffineTransform Tx) {}

    @Override
    public java.awt.geom.AffineTransform getTransform() {
      return new java.awt.geom.AffineTransform();
    }

    @Override
    public java.awt.Paint getPaint() {
      return null;
    }

    @Override
    public void setPaint(java.awt.Paint paint) {}

    @Override
    public Stroke getStroke() {
      return new BasicStroke(1.0f);
    }

    @Override
    public void clip(java.awt.Shape s) {}

    @Override
    public java.awt.Font getFont() {
      return null;
    }

    @Override
    public void setFont(java.awt.Font font) {}

    @Override
    public java.awt.FontMetrics getFontMetrics(java.awt.Font f) {
      return null;
    }

    @Override
    public java.awt.Rectangle getClipBounds() {
      return null;
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {}

    @Override
    public void setClip(int x, int y, int width, int height) {}

    @Override
    public java.awt.Shape getClip() {
      return null;
    }

    @Override
    public void setClip(java.awt.Shape clip) {}

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {}

    @Override
    public void clearRect(int x, int y, int width, int height) {}

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}

    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, java.awt.image.ImageObserver observer) {
      return false;
    }

    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.image.ImageObserver observer) {
      return false;
    }

    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, Color bgcolor, java.awt.image.ImageObserver observer) {
      return false;
    }

    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, Color bgcolor, java.awt.image.ImageObserver observer) {
      return false;
    }

    @Override
    public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.image.ImageObserver observer) {
      return false;
    }

    @Override
    public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, java.awt.image.ImageObserver observer) {
      return false;
    }

    @Override
    public void drawString(String str, int x, int y) {
      interceptor.drawString(str, (float) x, (float) y);
    }

    @Override
    public void drawString(String str, float x, float y) {
      interceptor.drawString(str, x, y);
    }

    @Override
    public void drawString(java.text.AttributedCharacterIterator iterator, int x, int y) {}

    @Override
    public void drawString(java.text.AttributedCharacterIterator iterator, float x, float y) {}

    @Override
    public void drawGlyphVector(java.awt.font.GlyphVector g, float x, float y) {}

    @Override
    public void drawImage(java.awt.image.BufferedImage img, java.awt.image.BufferedImageOp op, int x, int y) {}

    @Override
    public boolean drawImage(java.awt.Image img, java.awt.geom.AffineTransform xform, java.awt.image.ImageObserver obs) {
      return false;
    }

    @Override
    public boolean hit(java.awt.Rectangle rect, java.awt.Shape s, boolean onStroke) {
      return false;
    }

    @Override
    public java.awt.GraphicsConfiguration getDeviceConfiguration() {
      return null;
    }

    @Override
    public void setComposite(java.awt.Composite comp) {}

    @Override
    public void setPaintMode() {}

    @Override
    public void setXORMode(Color c1) {}

    @Override
    public void drawRenderedImage(java.awt.image.RenderedImage img, java.awt.geom.AffineTransform xform) {}

    @Override
    public void drawRenderableImage(java.awt.image.renderable.RenderableImage img, java.awt.geom.AffineTransform xform) {}

    @Override
    public java.awt.Composite getComposite() {
      return null;
    }

    @Override
    public void setBackground(Color color) {}

    @Override
    public Color getBackground() {
      return null;
    }

    @Override
    public java.awt.font.FontRenderContext getFontRenderContext() {
      return new java.awt.font.FontRenderContext(null, true, true);
    }
  }

  public static void exportIconToPng(javax.swing.Icon icon, int width, int height, File outputFile)
      throws IOException {
    final var oldIconColor = com.cburch.logisim.prefs.AppPreferences.COMPONENT_ICON_COLOR.get();
    final var oldLaf = com.cburch.logisim.prefs.AppPreferences.LookAndFeel.get();
    com.cburch.logisim.prefs.AppPreferences.COMPONENT_ICON_COLOR.set(java.awt.Color.BLACK.getRGB());
    com.cburch.logisim.prefs.AppPreferences.LookAndFeel.set("com.formdev.flatlaf.FlatIntelliJLaf");
    try {
      final var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      final var g2 = image.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

      final var scaleX = (double) width / icon.getIconWidth();
      final var scaleY = (double) height / icon.getIconHeight();
      g2.scale(scaleX, scaleY);

      g2.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(1.0f));
      icon.paintIcon(null, g2, 0, 0);
      g2.dispose();

      if (outputFile.getParentFile() != null) {
        outputFile.getParentFile().mkdirs();
      }
      ImageIO.write(image, "PNG", outputFile);
    } finally {
      com.cburch.logisim.prefs.AppPreferences.COMPONENT_ICON_COLOR.set(oldIconColor);
      com.cburch.logisim.prefs.AppPreferences.LookAndFeel.set(oldLaf);
    }
  }

  public static void exportIconToSvg(javax.swing.Icon icon, File outputFile) throws IOException {
    final var oldIconColor = com.cburch.logisim.prefs.AppPreferences.COMPONENT_ICON_COLOR.get();
    final var oldLaf = com.cburch.logisim.prefs.AppPreferences.LookAndFeel.get();
    com.cburch.logisim.prefs.AppPreferences.COMPONENT_ICON_COLOR.set(java.awt.Color.BLACK.getRGB());
    com.cburch.logisim.prefs.AppPreferences.LookAndFeel.set("com.formdev.flatlaf.FlatIntelliJLaf");
    try {
      double originalW = 16.0;
      double originalH = 16.0;
      try {
        final var fW = icon.getClass().getField("ORIGINAL_CANVAS_WIDTH");
        final var fH = icon.getClass().getField("ORIGINAL_CANVAS_HEIGHT");
        originalW = fW.getDouble(icon);
        originalH = fH.getDouble(icon);
      } catch (Exception ignored) {
      }

      final var interceptor = new SvgGraphicsInterceptor(originalW, originalH, icon.getIconWidth());
      final var capturingG2 = new SvgCapturingGraphics(interceptor);
      icon.paintIcon(null, capturingG2, 0, 0);

      if (outputFile.getParentFile() != null) {
        outputFile.getParentFile().mkdirs();
      }
      try (final var writer = new FileWriter(outputFile)) {
        writer.write(interceptor.build());
      }
    } finally {
      com.cburch.logisim.prefs.AppPreferences.COMPONENT_ICON_COLOR.set(oldIconColor);
      com.cburch.logisim.prefs.AppPreferences.LookAndFeel.set(oldLaf);
    }
  }

  public static void exportIconToAllFormats(javax.swing.Icon icon, String baseName) throws IOException {
    final var svgDir = new File("src/main/resources/doc/icons/svgwithoutbackground");
    final var png16Dir = new File("src/main/resources/doc/icons/1616");
    final var png64Dir = new File("src/main/resources/doc/icons/6464");

    final var svgFile = new File(svgDir, baseName + ".svg");
    final var png16 = new File(png16Dir, baseName + ".png");
    final var png64 = new File(png64Dir, baseName + ".png");

    exportIconToSvg(icon, svgFile);
    exportIconToPng(icon, 16, 16, png16);
    exportIconToPng(icon, 64, 64, png64);

    System.out.println("Exported " + baseName + " to:");
    System.out.println(" - SVG (JavaHelp without bg): " + svgFile.getAbsolutePath());
    System.out.println(" - PNG 16x16 (JavaHelp 1616): " + png16.getAbsolutePath());
    System.out.println(" - PNG 64x64 (JavaHelp 6464): " + png64.getAbsolutePath());
  }

  /**
   * Dev-tool test: exports a BaseIcon to SVG and PNG assets.
   *
   * <p>Reads parameters from environment variables set by {@code baseicon2svg.sh}:
   * <ul>
   *   <li>{@code ICON_CLASS_NAME} — fully-qualified class name
   *   <li>{@code ICON_BASE_NAME}  — base file name without extension
   * </ul>
   * When the variables are not set (normal CI run), exports a default icon
   * for a quick sanity check.
   */
  @Test
  void testExportIconToAllFormats() throws Exception {
    final var className = System.getenv("ICON_CLASS_NAME");
    final var baseName = System.getenv("ICON_BASE_NAME");

    if (className != null && baseName != null) {
      final var clazz = Class.forName(className);
      final var icon = (javax.swing.Icon) clazz.getDeclaredConstructor().newInstance();
      exportIconToAllFormats(icon, baseName);
    } else {
      exportIconToAllFormats(new TwoPinLedIcon(), "twopinled");
    }
  }
}
