/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.generic;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.ttl.TtlLibrary;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.std.wiring.WiringLibrary;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.LocaleManager;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import javax.swing.tree.TreePath;
import org.junit.jupiter.api.Test;

class ProjectExplorerTest {

  @Test
  void filtersToolsByAliases() {
    final var ttlLib = new TtlLibrary();
    final var file = LogisimFile.createNew(new Loader(null), null);
    file.addLibrary(ttlLib);

    try {
      final var project = new Project(file);
      final var explorer = new ProjectExplorer(project, false);
      final var model = (ProjectExplorerModel) explorer.getModel();

      explorer.setFilterText("132");
      assertTrue(model.isFiltering());

      final var root = (ProjectExplorerLibraryNode) model.getRoot();
      final var ttlNode = findLibraryNode(root, ttlLib);
      assertNotNull(ttlNode);
      assertTrue(model.getChildCount(ttlNode) > 0);

      var found7424 = false;
      for (var i = 0; i < model.getChildCount(ttlNode); i++) {
        final var child = model.getChild(ttlNode, i);
        if (child instanceof ProjectExplorerToolNode toolNode
            && "7424".equals(toolNode.getValue().getName())) {
          found7424 = true;
          break;
        }
      }
      assertTrue(found7424, "TTL 7424 must remain visible when filtering by alias '132'");
    } finally {
      file.stopAutosaveThread(false);
    }
  }

  @Test
  void filtersToolsByEnglishNameUnderRussianLocale() {
    final var prevLocale = LocaleManager.getLocale();
    try {
      LocaleManager.setLocale(Locale.forLanguageTag("ru"));
      final var wiringLib = new WiringLibrary();
      final var file = LogisimFile.createNew(new Loader(null), null);
      file.addLibrary(wiringLib);

      try {
        final var project = new Project(file);
        final var explorer = new ProjectExplorer(project, false);
        final var model = (ProjectExplorerModel) explorer.getModel();

        explorer.setFilterText("probe");
        assertTrue(model.isFiltering());

        final var root = (ProjectExplorerLibraryNode) model.getRoot();
        final var wiringNode = findLibraryNode(root, wiringLib);
        assertNotNull(wiringNode);
        assertTrue(model.getChildCount(wiringNode) > 0);

        var foundProbe = false;
        for (var i = 0; i < model.getChildCount(wiringNode); i++) {
          final var child = model.getChild(wiringNode, i);
          if (child instanceof ProjectExplorerToolNode toolNode
              && "Probe".equals(toolNode.getValue().getName())) {
            foundProbe = true;
            break;
          }
        }
        assertTrue(foundProbe, "Probe must remain visible in Russian locale when filtering by 'probe'");
      } finally {
        file.stopAutosaveThread(false);
      }
    } finally {
      LocaleManager.setLocale(prevLocale);
    }
  }

  @Test
  void selectsToolsFromNestedLibraries() {
    final var leafTool = new AddTool(Pin.FACTORY);
    final var leafLibrary = new TestLibrary("leaf", List.of(leafTool), List.of());
    final var parentLibrary = new TestLibrary("parent", List.of(), List.of(leafLibrary));
    final var file = LogisimFile.createNew(new Loader(null), null);
    file.addLibrary(parentLibrary);

    try {
      final var project = new Project(file);
      final var explorer = new ProjectExplorer(project, false);
      final var root = (ProjectExplorerLibraryNode) explorer.getModel().getRoot();
      final var parentNode = findLibraryNode(root, parentLibrary);
      final var leafNode = findLibraryNode(parentNode, leafLibrary);
      final var toolNode = findToolNode(leafNode, leafTool);

      explorer.setSelectionPath(new TreePath(new Object[] {root, parentNode, leafNode, toolNode}));

      assertSame(leafTool, explorer.getSelectedTool());
    } finally {
      file.stopAutosaveThread(false);
    }
  }

  private static ProjectExplorerLibraryNode findLibraryNode(
      ProjectExplorerLibraryNode parent, Library library) {
    for (final var children = parent.children(); children.hasMoreElements(); ) {
      final var child = children.nextElement();
      if (child instanceof ProjectExplorerLibraryNode node && node.getValue() == library) {
        return node;
      }
    }
    return fail("library node not found: " + library.getName());
  }

  private static ProjectExplorerToolNode findToolNode(ProjectExplorerLibraryNode parent, Tool tool) {
    for (final Enumeration<?> children = parent.children(); children.hasMoreElements(); ) {
      final var child = children.nextElement();
      if (child instanceof ProjectExplorerToolNode node && node.getValue() == tool) {
        return node;
      }
    }
    return fail("tool node not found: " + tool.getName());
  }

  private static class TestLibrary extends Library {
    private final String name;
    private final List<Tool> tools;
    private final List<Library> libraries;

    TestLibrary(String name, List<Tool> tools, List<Library> libraries) {
      this.name = name;
      this.tools = tools;
      this.libraries = libraries;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public List<Library> getLibraries() {
      return libraries;
    }

    @Override
    public List<? extends Tool> getTools() {
      return tools;
    }
  }
}
