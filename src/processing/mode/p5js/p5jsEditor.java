package processing.mode.p5js;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import processing.app.Base;
import processing.app.Formatter;
import processing.app.Library;
import processing.app.Mode;
import processing.app.Platform;
import processing.app.Problem;
import processing.app.SketchException;
import processing.app.Util;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.PdeTextArea;
import processing.app.syntax.PdeTextAreaDefaults;
import processing.app.ui.Editor;
import processing.app.ui.EditorException;
import processing.app.ui.EditorFooter;
import processing.app.ui.EditorState;
import processing.app.ui.EditorToolbar;
import processing.app.ui.Toolkit;
import processing.mode.java.AutoFormat;
import processing.mode.java.JavaInputHandler;
import processing.mode.p5js.server.HttpServer;


public class p5jsEditor extends Editor {
  HttpServer server;
  boolean showSizeWarning = true;


  protected p5jsEditor(Base base, String path,
                       EditorState state, Mode mode) throws EditorException {
    super(base, path, state, mode);

    if (sketch.isUntitled()) {
      rebuildHtml();
    }
  }


  @Override
  protected JEditTextArea createTextArea() {
    return new PdeTextArea(new PdeTextAreaDefaults(mode),
                           new JavaInputHandler(this), this);
  }


  /**
   *  Create and return the toolbar (tools above text area),
   *  implements abstract Editor.createToolbar(),
   *  called in Editor constructor to add the toolbar to the window.
   *
   *  @return an EditorToolbar, in our case a JavaScriptToolbar
   *  @see processing.mode.p5js.p5jsToolbar
   */
  @Override
  public EditorToolbar createToolbar() {
    return new p5jsToolbar(this);
  }


  @Override
  public EditorFooter createFooter() {
    EditorFooter footer = super.createFooter();
    addErrorTable(footer);
    return footer;
  }


  /**
   *  Create a formatter to prettify code,
   *  implements abstract Editor.createFormatter(),
   *  called by Editor.handleAutoFormat() to handle menu item or shortcut
   *
   *  @return the formatter to handle formatting of code.
   */
  @Override
  public Formatter createFormatter() {
    return new AutoFormat();
  }


  /**
   *  Build the "File" menu,
   *  implements abstract Editor.buildFileMenu(),
   *  called by Editor.buildMenuBar() to generate the app menu for the editor window
   *
   *  @return JMenu containing the menu items for "File" menu
   */
  @Override
  public JMenu buildFileMenu() {
    /*
    JMenuItem exportItem = Toolkit.newJMenuItem("Export", 'E');
    exportItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleExport(true);
      }
    });
    return buildFileMenu(new JMenuItem[] { exportItem });
    */
    return buildFileMenu(null);
  }


  /**
   *  Build the "Sketch" menu, implements abstract Editor.buildSketchMenu(),
   *  called by Editor.buildMenuBar().
   *  @return JMenu containing the menu items for "Sketch" menu
   */
  @Override
  public JMenu buildSketchMenu() {
    JMenuItem runItem = Toolkit.newJMenuItem("Run", 'R');
    runItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleRun();
      }
    });

    JMenuItem stopItem = new JMenuItem("Stop");
    stopItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleStop();
      }
    });

    return buildSketchMenu(new JMenuItem[] { runItem, stopItem });
  }


  /**
   *  Build the "Help" menu, implements abstract Editor.buildHelpMenu()
   *  @return JMenu containing the menu items for "Help" menu
   */
  @Override
  public JMenu buildHelpMenu() {
    JMenu menu = new JMenu("Help");
    JMenuItem item;

    item = new JMenuItem("Getting Started");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.openURL("http://p5js.org/get-started/#your-first-sketch");
      }
    });
    menu.add(item);

    item = new JMenuItem("Reference");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.openURL("http://p5js.org/reference/");
      }
    });
    menu.add(item);

    item = Toolkit.newJMenuItemShift("Find in Reference", 'F');
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        //handleFindReferenceImpl();
        handleFindReference();
      }
    });
    menu.add(item);

    menu.addSeparator();

    item = new JMenuItem("Visit p5js.org");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.openURL("http://p5js.org/");
      }
    });
    menu.add(item);

    item = new JMenuItem("Visit the Forum");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.openURL("https://forum.processing.org/");
      }
    });
    menu.add(item);

    item = new JMenuItem("View p5js on Github");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.openURL("https://github.com/processing/p5.js");
      }
    });
    menu.add(item);

    return menu;
  }


  /**
   * Returns the default commenting prefix for comment/uncomment command,
   * called from Editor.handleCommentUncomment()
   */
  @Override
  public String getCommentPrefix() {
    return "//";
  }


  /**
   * The EditorHeader is rebuilt when tabs are renamed, added, or removed.
   * Use this as a callback to rewrite the HTML file from the template.
   */
  @Override
  public void rebuildHeader() {
    super.rebuildHeader();
    rebuildHtml();
  }


  /**
   *  Stop the runner, in our case this is the server,
   *  implements abstract Editor.internalCloseRunner(),
   *  called from Editor.prepareRun()
   *
   *  Called when the window is going to be reused for another sketch.
   */
  @Override
  public void internalCloseRunner() {
    handleStop();
  }


  /**
   *  Implements abstract Editor.deactivateRun()
   */
  @Override
  public void deactivateRun() {
    toolbar.deactivateRun();
  }


  /*
  public File getTemplateFolder() {
    return getMode().getContentFile("template");
  }


  public File getLibrariesFolder() {
    return new File(mode.getTemplateFolder(), "libraries");
  }
   */


  public void handleRun() {
    toolbar.activateRun();

    try {
      // Make sure the sketch folder still exists, and the SketchCode objects
      // are updated to include any text changes from the Editor.
      prepareRun();
      // write the HTML here in case we need temp files
      p5jsBuild.updateHtml(sketch);
    } catch (Exception e) {
      statusError(e);
    }
    if (checkErrors(true)) {
      toolbar.deactivateRun();

    } else {
      if (server == null || server.isDead()) {
        restartServer();
      }
      statusNotice("Server running at " + server.getAddress());
      //Platform.openURL(server.getAddress());

      try {
        Desktop.getDesktop().browse(new URI(server.getAddress()));
      } catch (Exception e) {
        statusError(e);
      }
    }
  }


  /**
   *  Menu item callback, replacement for STOP: stop server.
   */
  public void handleStop() {
    try {
      p5jsBuild.cleanTempFiles(sketch);
    } catch (IOException e) {
      e.printStackTrace();  // TODO ignore?
    }
    stopServer();
    statusNotice("Server stopped.");
    toolbar.deactivateRun();
  }


  //public boolean handleExport(boolean openFolder) {
  protected boolean checkErrors(boolean fatal) {
    //return mode.handleExport(sketch);
//    long t = System.currentTimeMillis();
    try {
      new p5jsBuild(sketch);
//      new p5jsBuildFX(this, sketch);
//      System.out.println("elapsed: " + (System.currentTimeMillis() - t));
//      return true;
    } catch (SketchException se) {
      if (fatal) {
        statusError(se);
      } else {
        setProblemList(Arrays.asList(new Problem() {

          @Override
          public boolean isError() {
            return true;
          }

          @Override
          public boolean isWarning() {
            return false;
          }

          @Override
          public int getTabIndex() {
            return se.getCodeIndex();
          }

          @Override
          public int getLineNumber() {
            return se.getCodeLine();
          }

          @Override
          public String getMessage() {
            return se.getMessage();
          }

          @Override
          public int getStartOffset() {
            return se.getCodeColumn();
          }

          @Override
          public int getStopOffset() {
            return se.getCodeColumn() + 10;
          }
        }));
      }
      return true;
    }
//    System.out.println("elapsed: " + (System.currentTimeMillis() - t));
    return false;
  }


  protected boolean rebuildHtml() {
    try {
      p5jsBuild.updateHtml(sketch);
      return true;
    } catch (Exception e) {
      statusError(e);
    }
    return false;
  }


  @Override
  public boolean handleSaveAs() {
    if (super.handleSaveAs()) {
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          while (sketch.isSaving()) {  // wait until Save As completes
            try {
              Thread.sleep(5);
            } catch (InterruptedException e) { }
          }
          rebuildHtml();
        }
      });
      return true;  // kind of a farce
    }
    return false;
  }


  /**
   * Start the internal server for this sketch.
   * @param root the root folder for the server to serve from
   * @return true if it was started anew, false if it was running
   */
  protected void restartServer() {
    if (server != null && server.isDead()) {
      // if server hung or something else went wrong .. stop it.
      server.stop();
      server = null;
    }

    if (server == null) {
      server = new HttpServer(this);
    }

    server.start();
  }


  protected void stopServer() {
    if (server != null) {
      server.stop();
    }
  }


  /**
   *  Create or get the sketch's properties file
   *  @return the sketch properties file or null
   */
  protected File getSketchPropertiesFile() {
    File sketchPropsFile =
      new File(getSketch().getFolder(), "sketch.properties");
    if (!sketchPropsFile.exists()) {
      try {
        sketchPropsFile.createNewFile();
      } catch (IOException ioe) {
        ioe.printStackTrace();
        statusError("Unable to create sketch properties file!");
        return null;
      }
    }
    return sketchPropsFile;
  }


  @Override
  public void handleImportLibrary(String name) {
    // unlike the other Modes, this is actually adding the library code
    //System.out.println("import library " + name);
    Library library = mode.findLibraryByName(name);
    File folder = new File(library.getFolder(), "library");
    try {
      Util.copyDir(folder, new File(sketch.getFolder(), "libraries"));
      statusNotice("Copied " + name + " to the libraries folder of this sketch.");

      /*
      try {
        // write the HTML here in case we need temp files
        p5jsBuild.updateHtml(sketch);
      } catch (Exception e) {
        statusError(e);
      }
      */

    } catch (IOException e) {
      statusError(e);
    }
  }
}
