/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.icons;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

public class SvgToBaseIconConverterTest {

  public static void convertAndWriteFile(File svgFile, File targetJavaFile, String packageName, String className) throws Exception {
    final var code = convertSvgToBaseIconClass(svgFile, packageName, className);
    if (targetJavaFile.getParentFile() != null) {
      targetJavaFile.getParentFile().mkdirs();
    }
    try (final var writer = new java.io.FileWriter(targetJavaFile)) {
      writer.write(code);
    }
    System.out.println("Generated BaseIcon class at: " + targetJavaFile.getAbsolutePath());
  }

  public static String convertSvgToBaseIconClass(File svgFile, String packageName, String className) throws Exception {
    final var dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    // Disable XXE to prevent XML injection when parsing arbitrary SVG files
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    dbf.setExpandEntityReferences(false);
    final var doc = dbf.newDocumentBuilder().parse(svgFile);
    final var root = doc.getDocumentElement();

    double svgWidth = 16.0;
    double svgHeight = 16.0;
    double minX = 0.0;
    double minY = 0.0;

    if (root.hasAttribute("viewBox")) {
      final var vb = root.getAttribute("viewBox").trim().split("[\\s,]+");
      if (vb.length == 4) {
        minX = Double.parseDouble(vb[0]);
        minY = Double.parseDouble(vb[1]);
        svgWidth = Double.parseDouble(vb[2]);
        svgHeight = Double.parseDouble(vb[3]);
      }
    } else {
      if (root.hasAttribute("width")) {
        final var wStr = root.getAttribute("width").replaceAll("[^0-9.]", "");
        if (!wStr.isEmpty()) svgWidth = Double.parseDouble(wStr);
      }
      if (root.hasAttribute("height")) {
        final var hStr = root.getAttribute("height").replaceAll("[^0-9.]", "");
        if (!hStr.isEmpty()) svgHeight = Double.parseDouble(hStr);
      }
    }

    final double effW = svgWidth > 0 ? svgWidth : 16.0;
    final double effH = svgHeight > 0 ? svgHeight : 16.0;
    final double scaleFactor = 16.0 / Math.max(effW, effH);
    final double offsetX = (16.0 - (effW * scaleFactor)) / 2.0;
    final double offsetY = (16.0 - (effH * scaleFactor)) / 2.0;

    final var baseAT = new AffineTransform();
    if (offsetX != 0 || offsetY != 0) {
      baseAT.translate(offsetX, offsetY);
    }
    if (scaleFactor != 1.0) {
      baseAT.scale(scaleFactor, scaleFactor);
    }
    if (minX != 0 || minY != 0) {
      baseAT.translate(-minX, -minY);
    }

    final var sb = new StringBuilder();
    sb.append("/*\n");
    sb.append(" * Logisim-evolution - digital logic design tool and simulator\n");
    sb.append(" * Copyright by the Logisim-evolution developers\n");
    sb.append(" *\n");
    sb.append(" * https://github.com/logisim-evolution/\n");
    sb.append(" *\n");
    sb.append(" * This is free software released under GNU GPLv3 license\n");
    sb.append(" */\n\n");
    sb.append("package ").append(packageName).append(";\n\n");
    sb.append("import com.cburch.logisim.gui.icons.BaseIcon;\n");
    sb.append("import com.cburch.logisim.prefs.AppPreferences;\n");
    sb.append("import java.awt.BasicStroke;\n");
    sb.append("import java.awt.Color;\n");
    sb.append("import java.awt.Graphics2D;\n");
    sb.append("import java.awt.geom.Ellipse2D;\n");
    sb.append("import java.awt.geom.Line2D;\n");
    sb.append("import java.awt.geom.Path2D;\n");
    sb.append("import java.awt.geom.Rectangle2D;\n");
    sb.append("import java.awt.geom.RoundRectangle2D;\n\n");
    sb.append("// Generated BaseIcon\n");
    sb.append("public class ").append(className).append(" extends BaseIcon {\n\n");
    sb.append("  @Override\n");
    sb.append("  protected void paintIcon(Graphics2D g2) {\n");
    sb.append("    final var currentColor = g2.getColor();\n\n");

    final var cssRules = parseCssStyles(doc);

    final int[] pathIdx = {0};
    processElementChildren(root, sb, baseAT, pathIdx, cssRules);

    sb.append("  }\n");
    sb.append("}\n");

    return sb.toString();
  }

  private static Map<String, Map<String, String>> parseCssStyles(org.w3c.dom.Document doc) {
    final var cssRules = new java.util.HashMap<String, Map<String, String>>();
    final var styleNodes = doc.getElementsByTagName("style");
    for (int i = 0; i < styleNodes.getLength(); i++) {
      final var styleText = styleNodes.item(i).getTextContent();
      if (styleText == null || styleText.isEmpty()) continue;
      final var cleaned = styleText.replaceAll("/\\*.*?\\*/", "");
      final var matcher = Pattern.compile("([^{]+)\\{([^}]+)\\}").matcher(cleaned);
      while (matcher.find()) {
        final var selectors = matcher.group(1).trim().split("[\\s,]+");
        final var declsStr = matcher.group(2).trim();
        final var propMap = new java.util.HashMap<String, String>();
        for (var decl : declsStr.split(";")) {
          final var kv = decl.split(":", 2);
          if (kv.length == 2) {
            propMap.put(kv[0].trim().toLowerCase(Locale.US), kv[1].trim());
          }
        }
        for (var sel : selectors) {
          final var s = sel.trim();
          if (!s.isEmpty()) {
            cssRules.computeIfAbsent(s, k -> new java.util.HashMap<>()).putAll(propMap);
          }
        }
      }
    }
    return cssRules;
  }

  private static String extractStyleProp(String style, String prop) {
    if (style == null || style.isEmpty()) return "";
    for (var part : style.split(";")) {
      final var kv = part.split(":", 2);
      if (kv.length == 2 && kv[0].trim().equalsIgnoreCase(prop)) {
        return kv[1].trim();
      }
    }
    return "";
  }

  private static String getEffective(Element elem, String attr, Map<String, Map<String, String>> cssRules) {
    var curr = elem;
    while (curr != null) {
      final var styleStr = curr.getAttribute("style");
      final var fromStyle = extractStyleProp(styleStr, attr);
      if (!fromStyle.isEmpty()) return fromStyle;

      if (curr.hasAttribute("class")) {
        final var classes = curr.getAttribute("class").trim().split("[\\s,]+");
        for (var cls : classes) {
          if (!cls.isEmpty()) {
            final var rule = cssRules.get("." + cls);
            if (rule != null && rule.containsKey(attr)) {
              return rule.get(attr);
            }
          }
        }
      }

      if (curr.hasAttribute("id")) {
        final var id = curr.getAttribute("id").trim();
        final var rule = cssRules.get("#" + id);
        if (rule != null && rule.containsKey(attr)) {
          return rule.get(attr);
        }
      }

      final var tagRule = cssRules.get(curr.getTagName().toLowerCase(Locale.US));
      if (tagRule != null && tagRule.containsKey(attr)) {
        return tagRule.get(attr);
      }

      if (curr.hasAttribute(attr)) return curr.getAttribute(attr);
      final var parent = curr.getParentNode();
      if (parent instanceof Element parentElem) {
        curr = parentElem;
      } else {
        break;
      }
    }
    return "";
  }

  private static String getEffectiveFill(Element elem, Map<String, Map<String, String>> cssRules) {
    final var fillOpacity = getEffective(elem, "fill-opacity", cssRules);
    if ("0".equals(fillOpacity) || "0.0".equals(fillOpacity) || "none".equalsIgnoreCase(fillOpacity)) {
      return "none";
    }
    final var opacity = getEffective(elem, "opacity", cssRules);
    if ("0".equals(opacity) || "0.0".equals(opacity)) {
      return "none";
    }
    return getEffective(elem, "fill", cssRules);
  }

  private static String getEffectiveStroke(Element elem, Map<String, Map<String, String>> cssRules) {
    final var strokeOpacity = getEffective(elem, "stroke-opacity", cssRules);
    if ("0".equals(strokeOpacity) || "0.0".equals(strokeOpacity) || "none".equalsIgnoreCase(strokeOpacity)) {
      return "none";
    }
    final var opacity = getEffective(elem, "opacity", cssRules);
    if ("0".equals(opacity) || "0.0".equals(opacity)) {
      return "none";
    }
    return getEffective(elem, "stroke", cssRules);
  }

  private static AffineTransform parseTransform(String transformStr) {
    final var at = new AffineTransform();
    if (transformStr == null || transformStr.isEmpty()) return at;

    final var matcher = Pattern.compile("(matrix|translate|rotate|scale|skewX|skewY)\\(([^)]+)\\)").matcher(transformStr);
    while (matcher.find()) {
      final var type = matcher.group(1);
      final var args = matcher.group(2).split("[\\s,]+");
      try {
        switch (type) {
          case "matrix" -> {
            if (args.length == 6) {
              at.concatenate(new AffineTransform(
                  Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                  Double.parseDouble(args[2]), Double.parseDouble(args[3]),
                  Double.parseDouble(args[4]), Double.parseDouble(args[5])));
            }
          }
          case "translate" -> {
            double tx = args.length >= 1 ? Double.parseDouble(args[0]) : 0;
            double ty = args.length >= 2 ? Double.parseDouble(args[1]) : 0;
            at.translate(tx, ty);
          }
          case "rotate" -> {
            double angle = args.length >= 1 ? Math.toRadians(Double.parseDouble(args[0])) : 0;
            if (args.length == 3) {
              at.rotate(angle, Double.parseDouble(args[1]), Double.parseDouble(args[2]));
            } else {
              at.rotate(angle);
            }
          }
          case "scale" -> {
            double sx = args.length >= 1 ? Double.parseDouble(args[0]) : 1;
            double sy = args.length >= 2 ? Double.parseDouble(args[1]) : sx;
            at.scale(sx, sy);
          }
          case "skewX" -> {
            double angle = args.length >= 1 ? Math.toRadians(Double.parseDouble(args[0])) : 0;
            at.shear(Math.tan(angle), 0);
          }
          case "skewY" -> {
            double angle = args.length >= 1 ? Math.toRadians(Double.parseDouble(args[0])) : 0;
            at.shear(0, Math.tan(angle));
          }
          default -> { /* ignore */ }
        }
      } catch (Exception ignored) {
      }
    }
    return at;
  }

  private static void processElementChildren(Element parent, StringBuilder sb, AffineTransform currentAT, int[] pathIdx, Map<String, Map<String, String>> cssRules) {
    final var children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element elem) {
        final var tag = elem.getTagName().toLowerCase(Locale.US);

        final var elementAT = (AffineTransform) currentAT.clone();
        if (elem.hasAttribute("transform")) {
          elementAT.concatenate(parseTransform(elem.getAttribute("transform")));
        }

        final var fill = getEffectiveFill(elem, cssRules);
        final var stroke = getEffectiveStroke(elem, cssRules);
        final var strokeWidthStr = getEffective(elem, "stroke-width", cssRules);
        final var capStr = getEffective(elem, "stroke-linecap", cssRules);
        final var joinStr = getEffective(elem, "stroke-linejoin", cssRules);

        final var isBlackFill = fill.isEmpty() || fill.equals("#000000") || fill.equals("#000") || fill.equalsIgnoreCase("black") || fill.equalsIgnoreCase("currentColor");
        final var isWhiteFill = fill.equals("#ffffff") || fill.equals("#fff") || fill.equalsIgnoreCase("white");
        final var isBlackStroke = !stroke.isEmpty() && (stroke.equals("#000000") || stroke.equals("#000") || stroke.equalsIgnoreCase("black") || stroke.equalsIgnoreCase("currentColor"));
        final var isWhiteStroke = stroke.equals("#ffffff") || stroke.equals("#fff") || stroke.equalsIgnoreCase("white");

        final var scaleX = Math.hypot(elementAT.getScaleX(), elementAT.getShearY());
        final var scaleY = Math.hypot(elementAT.getScaleY(), elementAT.getShearX());
        final var avgScale = (scaleX + scaleY) / 2.0;

        final var hasComplexTransform = (elementAT.getShearX() != 0 || elementAT.getShearY() != 0 || (elementAT.getScaleX() < 0) || (elementAT.getScaleY() < 0));

        switch (tag) {
          case "rect" -> {
            final var rawX = parseDouble(elem, "x");
            final var rawY = parseDouble(elem, "y");
            final var rawW = parseDouble(elem, "width");
            final var rawH = parseDouble(elem, "height");
            final var rawRx = parseDouble(elem, "rx");
            final var rawRy = parseDouble(elem, "ry");

            if (hasComplexTransform) {
              final var rectShape = new RoundRectangle2D.Double(rawX, rawY, rawW, rawH, rawRx * 2, rawRy * 2);
              emitTransformedShape(sb, rectShape, elementAT, fill, stroke, strokeWidthStr, capStr, joinStr, avgScale, isBlackFill, isWhiteFill, isBlackStroke, isWhiteStroke, pathIdx);
            } else {
              final var p1 = transformPoint(elementAT, rawX, rawY);
              final var p2 = transformPoint(elementAT, rawX + rawW, rawY + rawH);
              final var rx = rawRx * scaleX;
              final var ry = rawRy * scaleY;

              final var rw = Math.abs(p2.x - p1.x);
              final var rh = Math.abs(p2.y - p1.y);

              final var isRounded = (rx > 0 || ry > 0);

              final var rectShapeExpr = isRounded
                  ? String.format(Locale.US,
                      "new RoundRectangle2D.Double(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f))",
                      p1.x, p1.y, rw, rh, rx * 2.0, ry * 2.0)
                  : String.format(Locale.US,
                      "new Rectangle2D.Double(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f))",
                      p1.x, p1.y, rw, rh);
              final var comment = buildStyleComment(isRounded ? "rounded rectangle" : "rectangle", fill, isBlackFill, isWhiteFill, stroke, isBlackStroke, isWhiteStroke);
              emitFillAndDraw(sb, comment, rectShapeExpr, fill, isBlackFill, isWhiteFill,
                  stroke, isBlackStroke, isWhiteStroke, strokeWidthStr, capStr, joinStr, avgScale);
            }
          }

          case "circle", "ellipse" -> {
            final var rawCx = parseDouble(elem, "cx");
            final var rawCy = parseDouble(elem, "cy");
            final var rawRx = elem.hasAttribute("r") ? parseDouble(elem, "r") : parseDouble(elem, "rx");
            final var rawRy = elem.hasAttribute("r") ? parseDouble(elem, "r") : parseDouble(elem, "ry");

            if (hasComplexTransform) {
              final var ellipseShape = new Ellipse2D.Double(rawCx - rawRx, rawCy - rawRy, rawRx * 2, rawRy * 2);
              emitTransformedShape(sb, ellipseShape, elementAT, fill, stroke, strokeWidthStr, capStr, joinStr, avgScale, isBlackFill, isWhiteFill, isBlackStroke, isWhiteStroke, pathIdx);
            } else {
              final var center = new Point2D.Double(rawCx, rawCy);
              elementAT.transform(center, center);

              final var scaledRx = rawRx * scaleX;
              final var scaledRy = rawRy * scaleY;
              final var ox = center.x - scaledRx;
              final var oy = center.y - scaledRy;
              final var ow = scaledRx * 2;
              final var oh = scaledRy * 2;

              final var ellipseExpr = String.format(Locale.US,
                  "new Ellipse2D.Double(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f))",
                  ox, oy, ow, oh);
              final var comment = buildStyleComment(tag, fill, isBlackFill, isWhiteFill, stroke, isBlackStroke, isWhiteStroke);
              emitFillAndDraw(sb, comment, ellipseExpr, fill, isBlackFill, isWhiteFill,
                  stroke, isBlackStroke, isWhiteStroke, strokeWidthStr, capStr, joinStr, avgScale);
            }
          }

          case "line" -> {
            final var pt1 = new Point2D.Double(parseDouble(elem, "x1"), parseDouble(elem, "y1"));
            final var pt2 = new Point2D.Double(parseDouble(elem, "x2"), parseDouble(elem, "y2"));
            elementAT.transform(pt1, pt1);
            elementAT.transform(pt2, pt2);

            sb.append("    ").append(buildStyleComment("line", "none", false, false, stroke, isBlackStroke || stroke.isEmpty(), isWhiteStroke)).append("\n");
            emitSetStrokeColor(sb, isBlackStroke || stroke.isEmpty(), isWhiteStroke, stroke);
            sb.append(String.format(Locale.US, "    g2.setStroke(%s);\n",
                formatBasicStroke(resolveStrokeWidth(strokeWidthStr, avgScale), capStr, joinStr)));
            sb.append(String.format(Locale.US,
                "    g2.draw(new Line2D.Double(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f)));\n",
                pt1.x, pt1.y, pt2.x, pt2.y));
          }

          case "polygon", "polyline" -> {
            final var pointsStr = elem.getAttribute("points").trim();
            if (!pointsStr.isEmpty()) {
              final var tokens = pointsStr.split("[\\s,]+");
              final var transformedPts = new ArrayList<Point2D.Double>();
              for (int k = 0; k + 1 < tokens.length; k += 2) {
                if (!tokens[k].isEmpty() && !tokens[k + 1].isEmpty()) {
                  final var rawX = Double.parseDouble(tokens[k]);
                  final var rawY = Double.parseDouble(tokens[k + 1]);
                  final var dest = new Point2D.Double();
                  elementAT.transform(new Point2D.Double(rawX, rawY), dest);
                  transformedPts.add(dest);
                }
              }

              if (!transformedPts.isEmpty()) {
                final var varName = "path" + pathIdx[0]++;
                sb.append("    ").append(buildStyleComment(tag, fill, isBlackFill, isWhiteFill, stroke, isBlackStroke, isWhiteStroke)).append("\n");
                sb.append(String.format(Locale.US, "    final var %s = new Path2D.Double();\n", varName));
                for (int j = 0; j < transformedPts.size(); j++) {
                  final var p = transformedPts.get(j);
                  if (j == 0) {
                    sb.append(String.format(Locale.US, "    %s.moveTo(scale(%.4f), scale(%.4f));\n", varName, p.x, p.y));
                  } else {
                    sb.append(String.format(Locale.US, "    %s.lineTo(scale(%.4f), scale(%.4f));\n", varName, p.x, p.y));
                  }
                }
                if (tag.equals("polygon")) {
                  sb.append(String.format(Locale.US, "    %s.closePath();\n", varName));
                }

                emitFillAndDraw(sb, null, varName, fill, isBlackFill, isWhiteFill,
                    stroke, isBlackStroke, isWhiteStroke, strokeWidthStr, capStr, joinStr, avgScale);
              }
            }
          }

          case "path" -> {
            final var d = elem.getAttribute("d").trim();
            if (!d.isEmpty()) {
              final var varName = "path" + pathIdx[0]++;
              final var pathCode = new StringBuilder();
              final boolean hasDrawing = parseAndEmitSvgPath(d, elementAT, varName, pathCode);

              if (hasDrawing) {
                sb.append("    ").append(buildStyleComment("path", fill, isBlackFill, isWhiteFill, stroke, isBlackStroke, isWhiteStroke)).append("\n");
                sb.append(String.format(Locale.US, "    final var %s = new Path2D.Double();\n", varName));
                sb.append(pathCode);

                emitFillAndDraw(sb, null, varName, fill, isBlackFill, isWhiteFill,
                    stroke, isBlackStroke, isWhiteStroke, strokeWidthStr, capStr, joinStr, avgScale);
              }
            }
          }

          case "text" -> {
            final var textContent = elem.getTextContent().trim();
            if (!textContent.isEmpty()) {
              var rawX = parseDouble(elem, "x");
              var rawY = parseDouble(elem, "y");

              final var childNodes = elem.getChildNodes();
              for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j) instanceof Element childElem && childElem.getTagName().equalsIgnoreCase("tspan")) {
                  if (childElem.hasAttribute("x")) rawX = parseDouble(childElem, "x");
                  if (childElem.hasAttribute("y")) rawY = parseDouble(childElem, "y");
                  break;
                }
              }

              final var fontSizeRaw = getEffective(elem, "font-size", cssRules);
              double fontSize = 10.0;
              if (!fontSizeRaw.isEmpty()) {
                final var cleaned = fontSizeRaw.replaceAll("[^0-9.]", "");
                if (!cleaned.isEmpty()) {
                  try {
                    fontSize = Double.parseDouble(cleaned);
                  } catch (Exception ignored) {
                  }
                }
              }

              final var fontWeight = getEffective(elem, "font-weight", cssRules);
              final boolean isBold = fontWeight.equalsIgnoreCase("bold") || fontWeight.equals("700") || fontWeight.equals("800") || fontWeight.equals("900");
              final int fontStyle = isBold ? java.awt.Font.BOLD : java.awt.Font.PLAIN;

              final var font = new java.awt.Font("SansSerif", fontStyle, (int) Math.round(fontSize));
              final var frc = new java.awt.font.FontRenderContext(null, true, true);
              final var gv = font.createGlyphVector(frc, textContent);

              final var anchor = getEffective(elem, "text-anchor", cssRules);
              final var dominantBaseline = getEffective(elem, "dominant-baseline", cssRules);
              final var alignmentBaseline = getEffective(elem, "alignment-baseline", cssRules);
              final var baseline = !dominantBaseline.isEmpty() ? dominantBaseline : alignmentBaseline;

              final var bounds = gv.getVisualBounds();
              double fontOffsetX = 0.0;
              if (anchor.equalsIgnoreCase("middle")) {
                fontOffsetX = -bounds.getWidth() / 2.0 - bounds.getX();
              } else if (anchor.equalsIgnoreCase("end") || anchor.equalsIgnoreCase("right")) {
                fontOffsetX = -bounds.getWidth() - bounds.getX();
              }

              double fontOffsetY = 0.0;
              if (baseline.equalsIgnoreCase("central") || baseline.equalsIgnoreCase("middle")) {
                fontOffsetY = -(bounds.getY() + bounds.getHeight() / 2.0);
              } else if (baseline.equalsIgnoreCase("hanging") || baseline.equalsIgnoreCase("text-before-edge")) {
                fontOffsetY = -bounds.getY();
              }

              final var textShape = gv.getOutline((float) (rawX + fontOffsetX), (float) (rawY + fontOffsetY));

              emitTransformedShape(sb, textShape, elementAT, fill, stroke, strokeWidthStr, capStr, joinStr, avgScale, isBlackFill, isWhiteFill, isBlackStroke, isWhiteStroke, pathIdx);
            }
          }

          case "g", "svg" -> processElementChildren(elem, sb, elementAT, pathIdx, cssRules);

          default -> processElementChildren(elem, sb, elementAT, pathIdx, cssRules);
        }
      }
    }
  }

  private static void emitSetFillColor(StringBuilder sb, boolean isBlack, boolean isWhite, String hex) {
    if (isBlack) {
      sb.append("    g2.setColor(currentColor);\n");
    } else if (isWhite) {
      sb.append("    g2.setColor(AppPreferences.isDarkTheme(AppPreferences.LookAndFeel.get()) ? new Color(40, 40, 40) : Color.WHITE);\n");
    } else {
      sb.append(String.format(Locale.US, "    g2.setColor(new Color(%s));\n", parseColorToJava(hex)));
    }
  }

  private static void emitSetStrokeColor(StringBuilder sb, boolean isBlack, boolean isWhite, String hex) {
    if (isBlack) {
      sb.append("    g2.setColor(currentColor);\n");
    } else if (isWhite) {
      sb.append("    g2.setColor(AppPreferences.isDarkTheme(AppPreferences.LookAndFeel.get()) ? Color.BLACK : Color.WHITE);\n");
    } else {
      sb.append(String.format(Locale.US, "    g2.setColor(new Color(%s));\n", parseColorToJava(hex)));
    }
  }

  private static double resolveStrokeWidth(String strokeWidthStr, double avgScale) {
    if (strokeWidthStr == null || strokeWidthStr.isEmpty()) return 1.0;
    final var val = strokeWidthStr.replaceAll("[^0-9.]", "");
    if (val.isEmpty()) return 1.0;
    try {
      final double rawSW = Double.parseDouble(val);
      final double scaledSW = rawSW * avgScale;
      return Math.max(scaledSW, 0.5);
    } catch (Exception e) {
      return 1.0;
    }
  }

  private static String buildStyleComment(String elementName, String fill, boolean isBlackFill, boolean isWhiteFill,
      String stroke, boolean isBlackStroke, boolean isWhiteStroke) {
    final var details = new ArrayList<String>();
    if (!fill.equals("none")) {
      if (isBlackFill) details.add("black fill");
      else if (isWhiteFill) details.add("white fill");
      else details.add("fill: " + fill);
    }
    if (!stroke.isEmpty() && !stroke.equals("none")) {
      if (isBlackStroke) details.add("black stroke");
      else if (isWhiteStroke) details.add("white stroke");
      else details.add("stroke: " + stroke);
    }
    if (details.isEmpty()) {
      return "// Draw " + elementName;
    }
    return "// Draw " + elementName + " (" + String.join(", ", details) + ")";
  }

  private static void emitFillAndDraw(StringBuilder sb, String comment, String shapeExpr,
      String fill, boolean isBlackFill, boolean isWhiteFill,
      String stroke, boolean isBlackStroke, boolean isWhiteStroke,
      String strokeWidthStr, String capStr, String joinStr, double avgScale) {
    final boolean hasFill = !fill.equals("none");
    final boolean hasStroke = !stroke.isEmpty() && !stroke.equals("none");

    if ((hasFill || hasStroke) && comment != null && !comment.isEmpty()) {
      sb.append("    ").append(comment).append("\n");
    }

    if (hasFill) {
      emitSetFillColor(sb, isBlackFill, isWhiteFill, fill);
      sb.append(String.format(Locale.US, "    g2.fill(%s);\n", shapeExpr));
    }

    if (hasStroke) {
      final boolean sameColor = hasFill && (fill.trim().equalsIgnoreCase(stroke.trim()) || (isBlackFill && isBlackStroke) || (isWhiteFill && isWhiteStroke));
      if (!sameColor) {
        final double sw = resolveStrokeWidth(strokeWidthStr, avgScale);
        emitSetStrokeColor(sb, isBlackStroke, isWhiteStroke, stroke);
        sb.append(String.format(Locale.US, "    g2.setStroke(%s);\n",
            formatBasicStroke(sw, capStr, joinStr)));
        sb.append(String.format(Locale.US, "    g2.draw(%s);\n", shapeExpr));
      }
    }
  }

  private static void emitTransformedShape(StringBuilder sb, java.awt.Shape shape, AffineTransform at, String fill, String stroke, String strokeWidthStr, String capStr, String joinStr, double avgScale, boolean isBlackFill, boolean isWhiteFill, boolean isBlackStroke, boolean isWhiteStroke, int[] pathIdx) {
    final var pi = shape.getPathIterator(at);
    final var coords = new double[6];
    final var varName = "path" + pathIdx[0]++;
    sb.append("    ").append(buildStyleComment("shape", fill, isBlackFill, isWhiteFill, stroke, isBlackStroke, isWhiteStroke)).append("\n");
    sb.append(String.format(Locale.US, "    final var %s = new Path2D.Double();\n", varName));

    while (!pi.isDone()) {
      final var type = pi.currentSegment(coords);
      switch (type) {
        case java.awt.geom.PathIterator.SEG_MOVETO ->
            sb.append(String.format(Locale.US, "    %s.moveTo(scale(%.4f), scale(%.4f));\n", varName, coords[0], coords[1]));
        case java.awt.geom.PathIterator.SEG_LINETO ->
            sb.append(String.format(Locale.US, "    %s.lineTo(scale(%.4f), scale(%.4f));\n", varName, coords[0], coords[1]));
        case java.awt.geom.PathIterator.SEG_QUADTO ->
            sb.append(String.format(Locale.US, "    %s.quadTo(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f));\n", varName, coords[0], coords[1], coords[2], coords[3]));
        case java.awt.geom.PathIterator.SEG_CUBICTO ->
            sb.append(String.format(Locale.US, "    %s.curveTo(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f));\n", varName, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
        case java.awt.geom.PathIterator.SEG_CLOSE ->
            sb.append(String.format(Locale.US, "    %s.closePath();\n", varName));
      }
      pi.next();
    }

    emitFillAndDraw(sb, null, varName, fill, isBlackFill, isWhiteFill,
        stroke, isBlackStroke, isWhiteStroke, strokeWidthStr, capStr, joinStr, avgScale);
  }

  private static boolean parseAndEmitSvgPath(String d, AffineTransform at, String varName, StringBuilder sb) {
    final var matcher = Pattern.compile("([MmLlHhVvCcSsQqTtAaZz])|([-+]?(?:\\d+\\.\\d+|\\d+|\\.\\d+)(?:[eE][-+]?\\d+)?)").matcher(d);
    final var tokens = new ArrayList<String>();
    while (matcher.find()) {
      tokens.add(matcher.group());
    }

    double curX = 0, curY = 0;
    double startX = 0, startY = 0;
    double lastCpX = 0, lastCpY = 0;
    int idx = 0;
    boolean emitted = false;

    while (idx < tokens.size()) {
      final var tok = tokens.get(idx);
      if (!tok.matches("[MmLlHhVvCcSsQqTtAaZz]")) {
        idx++;
        continue;
      }
      final var cmd = tok.charAt(0);
      idx++;

      switch (cmd) {
        case 'M', 'm' -> {
          boolean first = true;
          while (idx + 1 < tokens.size() && isNumber(tokens.get(idx)) && isNumber(tokens.get(idx + 1))) {
            double x = Double.parseDouble(tokens.get(idx++));
            double y = Double.parseDouble(tokens.get(idx++));
            if (cmd == 'm') {
              x += curX;
              y += curY;
            }
            curX = x;
            curY = y;
            lastCpX = curX;
            lastCpY = curY;
            final var pt = transformPoint(at, curX, curY);
            if (first) {
              startX = curX;
              startY = curY;
              sb.append(String.format(Locale.US, "    %s.moveTo(scale(%.4f), scale(%.4f));\n", varName, pt.x, pt.y));
              first = false;
            } else {
              sb.append(String.format(Locale.US, "    %s.lineTo(scale(%.4f), scale(%.4f));\n", varName, pt.x, pt.y));
            }
            emitted = true;
          }
        }
        case 'L', 'l' -> {
          while (idx + 1 < tokens.size() && isNumber(tokens.get(idx)) && isNumber(tokens.get(idx + 1))) {
            double x = Double.parseDouble(tokens.get(idx++));
            double y = Double.parseDouble(tokens.get(idx++));
            if (cmd == 'l') {
              x += curX;
              y += curY;
            }
            curX = x;
            curY = y;
            lastCpX = curX;
            lastCpY = curY;
            final var pt = transformPoint(at, curX, curY);
            sb.append(String.format(Locale.US, "    %s.lineTo(scale(%.4f), scale(%.4f));\n", varName, pt.x, pt.y));
            emitted = true;
          }
        }
        case 'H', 'h' -> {
          while (idx < tokens.size() && isNumber(tokens.get(idx))) {
            double x = Double.parseDouble(tokens.get(idx++));
            if (cmd == 'h') x += curX;
            curX = x;
            lastCpX = curX;
            lastCpY = curY;
            final var pt = transformPoint(at, curX, curY);
            sb.append(String.format(Locale.US, "    %s.lineTo(scale(%.4f), scale(%.4f));\n", varName, pt.x, pt.y));
            emitted = true;
          }
        }
        case 'V', 'v' -> {
          while (idx < tokens.size() && isNumber(tokens.get(idx))) {
            double y = Double.parseDouble(tokens.get(idx++));
            if (cmd == 'v') y += curY;
            curY = y;
            lastCpX = curX;
            lastCpY = curY;
            final var pt = transformPoint(at, curX, curY);
            sb.append(String.format(Locale.US, "    %s.lineTo(scale(%.4f), scale(%.4f));\n", varName, pt.x, pt.y));
            emitted = true;
          }
        }
        case 'C', 'c' -> {
          while (idx + 5 < tokens.size() && isNumber(tokens.get(idx)) && isNumber(tokens.get(idx + 1))
              && isNumber(tokens.get(idx + 2)) && isNumber(tokens.get(idx + 3))
              && isNumber(tokens.get(idx + 4)) && isNumber(tokens.get(idx + 5))) {
            double x1 = Double.parseDouble(tokens.get(idx++));
            double y1 = Double.parseDouble(tokens.get(idx++));
            double x2 = Double.parseDouble(tokens.get(idx++));
            double y2 = Double.parseDouble(tokens.get(idx++));
            double x = Double.parseDouble(tokens.get(idx++));
            double y = Double.parseDouble(tokens.get(idx++));
            if (cmd == 'c') {
              x1 += curX;
              y1 += curY;
              x2 += curX;
              y2 += curY;
              x += curX;
              y += curY;
            }
            lastCpX = x2;
            lastCpY = y2;
            curX = x;
            curY = y;
            final var p1 = transformPoint(at, x1, y1);
            final var p2 = transformPoint(at, x2, y2);
            final var p = transformPoint(at, x, y);
            sb.append(String.format(Locale.US,
                "    %s.curveTo(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f));\n",
                varName, p1.x, p1.y, p2.x, p2.y, p.x, p.y));
            emitted = true;
          }
        }
        case 'S', 's' -> {
          while (idx + 3 < tokens.size() && isNumber(tokens.get(idx)) && isNumber(tokens.get(idx + 1))
              && isNumber(tokens.get(idx + 2)) && isNumber(tokens.get(idx + 3))) {
            double x2 = Double.parseDouble(tokens.get(idx++));
            double y2 = Double.parseDouble(tokens.get(idx++));
            double x = Double.parseDouble(tokens.get(idx++));
            double y = Double.parseDouble(tokens.get(idx++));
            if (cmd == 's') {
              x2 += curX;
              y2 += curY;
              x += curX;
              y += curY;
            }
            double x1 = 2 * curX - lastCpX;
            double y1 = 2 * curY - lastCpY;
            lastCpX = x2;
            lastCpY = y2;
            curX = x;
            curY = y;
            final var p1 = transformPoint(at, x1, y1);
            final var p2 = transformPoint(at, x2, y2);
            final var p = transformPoint(at, x, y);
            sb.append(String.format(Locale.US,
                "    %s.curveTo(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f));\n",
                varName, p1.x, p1.y, p2.x, p2.y, p.x, p.y));
            emitted = true;
          }
        }
        case 'Q', 'q' -> {
          while (idx + 3 < tokens.size() && isNumber(tokens.get(idx)) && isNumber(tokens.get(idx + 1))
              && isNumber(tokens.get(idx + 2)) && isNumber(tokens.get(idx + 3))) {
            double x1 = Double.parseDouble(tokens.get(idx++));
            double y1 = Double.parseDouble(tokens.get(idx++));
            double x = Double.parseDouble(tokens.get(idx++));
            double y = Double.parseDouble(tokens.get(idx++));
            if (cmd == 'q') {
              x1 += curX;
              y1 += curY;
              x += curX;
              y += curY;
            }
            lastCpX = x1;
            lastCpY = y1;
            curX = x;
            curY = y;
            final var p1 = transformPoint(at, x1, y1);
            final var p = transformPoint(at, x, y);
            sb.append(String.format(Locale.US,
                "    %s.quadTo(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f));\n",
                varName, p1.x, p1.y, p.x, p.y));
            emitted = true;
          }
        }
        case 'A', 'a' -> {
          while (idx + 6 < tokens.size() && isNumber(tokens.get(idx)) && isNumber(tokens.get(idx + 1))
              && isNumber(tokens.get(idx + 2)) && isNumber(tokens.get(idx + 3))
              && isNumber(tokens.get(idx + 4)) && isNumber(tokens.get(idx + 5))
              && isNumber(tokens.get(idx + 6))) {
            double rx = Double.parseDouble(tokens.get(idx++));
            double ry = Double.parseDouble(tokens.get(idx++));
            double rot = Double.parseDouble(tokens.get(idx++));
            double largeArc = Double.parseDouble(tokens.get(idx++));
            double sweep = Double.parseDouble(tokens.get(idx++));
            double x = Double.parseDouble(tokens.get(idx++));
            double y = Double.parseDouble(tokens.get(idx++));
            if (cmd == 'a') {
              x += curX;
              y += curY;
            }
            emitSvgArc(curX, curY, rx, ry, rot, largeArc != 0, sweep != 0, x, y, at, varName, sb);
            curX = x;
            curY = y;
            lastCpX = curX;
            lastCpY = curY;
            emitted = true;
          }
        }
        case 'Z', 'z' -> {
          curX = startX;
          curY = startY;
          lastCpX = curX;
          lastCpY = curY;
          sb.append(String.format(Locale.US, "    %s.closePath();\n", varName));
          emitted = true;
        }
        default -> {
          while (idx < tokens.size() && isNumber(tokens.get(idx))) {
            idx++;
          }
        }
      }
    }
    return emitted;
  }

  private static Point2D.Double transformPoint(AffineTransform at, double x, double y) {
    final var pt = new Point2D.Double();
    at.transform(new Point2D.Double(x, y), pt);
    return pt;
  }

  private static boolean isNumber(String str) {
    return str.matches("[-+]?(?:\\d+\\.\\d+|\\d+|\\.\\d+)(?:[eE][-+]?\\d+)?");
  }

  private static double parseDouble(Element elem, String attr) {
    if (!elem.hasAttribute(attr)) return 0.0;
    try {
      final var val = elem.getAttribute(attr).replaceAll("[^0-9.-]", "");
      return val.isEmpty() ? 0.0 : Double.parseDouble(val);
    } catch (Exception e) {
      return 0.0;
    }
  }

  private static boolean isVisuallySimilarColor(String c1Hex, String c2Hex) {
    if (c1Hex == null || c2Hex == null || c1Hex.isEmpty() || c2Hex.isEmpty()) return true;
    try {
      final var p1 = parseColorToJava(c1Hex).split(",");
      final var p2 = parseColorToJava(c2Hex).split(",");
      if (p1.length == 3 && p2.length == 3) {
        int r1 = Integer.parseInt(p1[0].trim());
        int g1 = Integer.parseInt(p1[1].trim());
        int b1 = Integer.parseInt(p1[2].trim());
        int r2 = Integer.parseInt(p2[0].trim());
        int g2 = Integer.parseInt(p2[1].trim());
        int b2 = Integer.parseInt(p2[2].trim());
        int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
        return diff < 40;
      }
    } catch (Exception ignored) {
    }
    return false;
  }
  private static String parseColorToJava(String hex) {
    if (hex == null || hex.isEmpty()) return "0, 0, 0";
    final var val = hex.trim().toLowerCase(Locale.US);
    if (val.startsWith("rgb")) {
      final var matcher = Pattern.compile("rgba?\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)").matcher(val);
      if (matcher.find()) {
        final var r = Integer.parseInt(matcher.group(1));
        final var g = Integer.parseInt(matcher.group(2));
        final var b = Integer.parseInt(matcher.group(3));
        return String.format("%d, %d, %d", r, g, b);
      }
    }
    if (val.startsWith("#")) {
      if (val.length() == 7) {
        final var r = Integer.parseInt(val.substring(1, 3), 16);
        final var g = Integer.parseInt(val.substring(3, 5), 16);
        final var b = Integer.parseInt(val.substring(5, 7), 16);
        return String.format("%d, %d, %d", r, g, b);
      }
      if (val.length() == 4) {
        final var r = Integer.parseInt(val.substring(1, 2) + val.substring(1, 2), 16);
        final var g = Integer.parseInt(val.substring(2, 3) + val.substring(2, 3), 16);
        final var b = Integer.parseInt(val.substring(3, 4) + val.substring(3, 4), 16);
        return String.format("%d, %d, %d", r, g, b);
      }
    }
    return switch (val) {
      case "white" -> "255, 255, 255";
      case "black" -> "0, 0, 0";
      case "red" -> "255, 0, 0";
      case "green" -> "0, 128, 0";
      case "lime" -> "0, 255, 0";
      case "blue" -> "0, 0, 255";
      case "yellow" -> "255, 255, 0";
      case "cyan" -> "0, 255, 255";
      case "magenta", "fuchsia" -> "255, 0, 255";
      case "orange" -> "255, 165, 0";
      case "purple" -> "128, 0, 128";
      case "gray", "grey" -> "128, 128, 128";
      case "lightgray", "lightgrey" -> "211, 211, 211";
      case "darkgray", "darkgrey" -> "169, 169, 169";
      default -> "0, 0, 0";
    };
  }

  private static String formatBasicStroke(double strokeW, String capStr, String joinStr) {
    int cap = -1;
    if ("round".equalsIgnoreCase(capStr)) cap = 1;
    else if ("butt".equalsIgnoreCase(capStr)) cap = 0;
    else if ("square".equalsIgnoreCase(capStr)) cap = 2;

    int join = -1;
    if ("round".equalsIgnoreCase(joinStr)) join = 1;
    else if ("bevel".equalsIgnoreCase(joinStr)) join = 2;
    else if ("miter".equalsIgnoreCase(joinStr)) join = 0;

    if (cap != -1 || join != -1) {
      String capJava = switch (cap) {
        case 0 -> "BasicStroke.CAP_BUTT";
        case 1 -> "BasicStroke.CAP_ROUND";
        default -> "BasicStroke.CAP_SQUARE";
      };
      String joinJava = switch (join) {
        case 1 -> "BasicStroke.JOIN_ROUND";
        case 2 -> "BasicStroke.JOIN_BEVEL";
        default -> "BasicStroke.JOIN_MITER";
      };
      return String.format(Locale.US, "new BasicStroke(scale(%.4ff), %s, %s)", strokeW, capJava, joinJava);
    } else {
      return String.format(Locale.US, "new BasicStroke(scale(%.4ff))", strokeW);
    }
  }

  private static void emitSvgArc(double x0, double y0, double rx, double ry, double angleDeg, boolean largeArc, boolean sweep, double x, double y, AffineTransform at, String varName, StringBuilder sb) {
    if (x0 == x && y0 == y) return;
    if (rx == 0 || ry == 0) {
      final var pt = transformPoint(at, x, y);
      sb.append(String.format(Locale.US, "    %s.lineTo(scale(%.4f), scale(%.4f));\n", varName, pt.x, pt.y));
      return;
    }

    rx = Math.abs(rx);
    ry = Math.abs(ry);
    double angleRad = Math.toRadians(angleDeg % 360.0);
    double cosAngle = Math.cos(angleRad);
    double sinAngle = Math.sin(angleRad);

    double dx2 = (x0 - x) / 2.0;
    double dy2 = (y0 - y) / 2.0;
    double x1 = cosAngle * dx2 + sinAngle * dy2;
    double y1 = -sinAngle * dx2 + cosAngle * dy2;

    double rx2 = rx * rx;
    double ry2 = ry * ry;
    double x12 = x1 * x1;
    double y12 = y1 * y1;

    double check = x12 / rx2 + y12 / ry2;
    if (check > 1) {
      rx *= Math.sqrt(check);
      ry *= Math.sqrt(check);
      rx2 = rx * rx;
      ry2 = ry * ry;
    }

    double sign = (largeArc == sweep) ? -1.0 : 1.0;
    double sq = ((rx2 * ry2) - (rx2 * y12) - (ry2 * x12)) / ((rx2 * y12) + (ry2 * x12));
    if (sq < 0) sq = 0;
    double coef = sign * Math.sqrt(sq);
    double cx1 = coef * ((rx * y1) / ry);
    double cy1 = coef * (-(ry * x1) / rx);

    double cx = cosAngle * cx1 - sinAngle * cy1 + (x0 + x) / 2.0;
    double cy = sinAngle * cx1 + cosAngle * cy1 + (y0 + y) / 2.0;

    double ux = (x1 - cx1) / rx;
    double uy = (y1 - cy1) / ry;
    double vx = (-x1 - cx1) / rx;
    double vy = (-y1 - cy1) / ry;

    double startAngle = angleBetween(1, 0, ux, uy);
    double dAngle = angleBetween(ux, uy, vx, vy);

    if (!sweep && dAngle > 0) {
      dAngle -= 2 * Math.PI;
    } else if (sweep && dAngle < 0) {
      dAngle += 2 * Math.PI;
    }

    int segments = (int) Math.ceil(Math.abs(dAngle) / (Math.PI / 2.0));
    double delta = dAngle / segments;
    double t = (8.0 / 3.0) * Math.sin(delta / 4.0) * Math.sin(delta / 4.0) / Math.sin(delta / 2.0);

    double th1 = startAngle;
    for (int i = 0; i < segments; i++) {
      double th2 = th1 + delta;
      double cosTh1 = Math.cos(th1);
      double sinTh1 = Math.sin(th1);
      double cosTh2 = Math.cos(th2);
      double sinTh2 = Math.sin(th2);

      double ep1x = cx + rx * cosTh1 * cosAngle - ry * sinTh1 * sinAngle;
      double ep1y = cy + rx * cosTh1 * sinAngle + ry * sinTh1 * cosAngle;

      double cp1x = ep1x - t * (rx * sinTh1 * cosAngle + ry * cosTh1 * sinAngle);
      double cp1y = ep1y - t * (rx * sinTh1 * sinAngle - ry * cosTh1 * cosAngle);

      double ep2x = cx + rx * cosTh2 * cosAngle - ry * sinTh2 * sinAngle;
      double ep2y = cy + rx * cosTh2 * sinAngle + ry * sinTh2 * cosAngle;

      double cp2x = ep2x + t * (rx * sinTh2 * cosAngle + ry * cosTh2 * sinAngle);
      double cp2y = ep2y + t * (rx * sinTh2 * sinAngle - ry * cosTh2 * cosAngle);

      final var p1 = transformPoint(at, cp1x, cp1y);
      final var p2 = transformPoint(at, cp2x, cp2y);
      final var p = transformPoint(at, ep2x, ep2y);

      sb.append(String.format(Locale.US,
          "    %s.curveTo(scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f), scale(%.4f));\n",
          varName, p1.x, p1.y, p2.x, p2.y, p.x, p.y));

      th1 = th2;
    }
  }

  private static double angleBetween(double ux, double uy, double vx, double vy) {
    double uLen = Math.hypot(ux, uy);
    double vLen = Math.hypot(vx, vy);
    if (uLen == 0 || vLen == 0) return 0;
    double dp = (ux * vx + uy * vy) / (uLen * vLen);
    if (dp < -1.0) dp = -1.0;
    if (dp > 1.0) dp = 1.0;
    double angle = Math.acos(dp);
    if (ux * vy - uy * vx < 0) angle = -angle;
    return angle;
  }

  /**
   * Dev-tool test: converts an SVG to a BaseIcon Java class.
   *
   * <p>Reads parameters from environment variables set by {@code svg2baseicon.sh}:
   * <ul>
   *   <li>{@code CONVERT_SVG_PATH}  — path to the source SVG file
   *   <li>{@code CONVERT_JAVA_PATH} — path to the output Java file
   *   <li>{@code CONVERT_PKG_NAME}  — Java package name
   *   <li>{@code CONVERT_CLS_NAME}  — Java class name
   * </ul>
   * Skipped silently when variables are not set (normal CI/test runs).
   */
  @Test
  void testConvertSvgToBaseIcon() throws Exception {
    final var svgPath = System.getenv("CONVERT_SVG_PATH");
    final var javaPath = System.getenv("CONVERT_JAVA_PATH");
    final var packageName = System.getenv("CONVERT_PKG_NAME");
    final var className = System.getenv("CONVERT_CLS_NAME");

    if (svgPath == null || javaPath == null || packageName == null || className == null) {
      return; // not a conversion run — skip silently
    }
    final var svgFile = new File(svgPath);
    final var targetJavaFile = new File(javaPath);
    System.out.println("Converting SVG [" + svgFile.getAbsolutePath() + "] -> Java [" + targetJavaFile.getAbsolutePath() + "]");
    convertAndWriteFile(svgFile, targetJavaFile, packageName, className);
  }
}
