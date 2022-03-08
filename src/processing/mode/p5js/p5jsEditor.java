package processing.mode.p5js;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import processing.app.Base;
import processing.app.Formatter;
import processing.app.Library;
import processing.app.Messages;
import processing.app.Mode;
import processing.app.Platform;
import processing.app.Problem;
import processing.app.SketchCode;
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

import processing.data.JSONArray;
import processing.data.JSONObject;


public class p5jsEditor extends Editor {
  // One server per Editor, same port used for the same Editor
  HttpServer server;
  // Try to maintain the same port for this Editor window
  int port;
  boolean showSizeWarning = true;

  static final boolean USE_LINTER = true;
  // object to handle linting, invoke once b/c heavyweight
  static Linter linter;

  ErrorWatcher watcher;


  protected p5jsEditor(Base base, String path,
                       EditorState state, Mode mode) throws EditorException {
    super(base, path, state, mode);

    if (linter == null) {
      linter = new Linter(this);
    }

    // if getting started with the template, get things cleaned up
    if (sketch.isUntitled()) {
      rebuildHtml();
    }

    // if the "libraries" folder doesn't exist, re-create it
    File sketchLibs = new File(sketch.getFolder(), "libraries");
    if (!sketchLibs.exists()) {
      File templateLibs = new File(mode.getTemplateFolder(), "libraries");
      try {
        Util.copyDir(templateLibs, sketchLibs);
      } catch (IOException e) {
        throw new EditorException("Could not re-create the libraries folder for this sketch.", e);
      }
    }
    // if the index.html file has gone missing, re-create it
    if (p5jsMode.findIndexHtml(sketch) == null) {
      rebuildHtml();
    }

    enableDisableModeMenu();

    initWatcher();
    startServer();
  }


  @Override
  protected void handleOpenInternal(String path) throws EditorException {
    if (path.endsWith("sketch.js")) {
      // Because of poor Exception handling by me, there isn't a good way to
      // recover from this situation without throwing this ridiculous blob of
      // text into the Exception message itself. Ben 1, Software Engineering 0.
      throw new EditorException("Cannot open this type of sketch.\n" +
                                "This version of p5jsMode does not play nicely with sketches created by other editors.\n" +
                                "To use this code, please use File > New to create a new sketch and copy your code into it.\n" +
                                "See https://github.com/fathominfo/processing-p5js-mode/issues/14 for updates or details.");
    }
    super.handleOpenInternal(path);
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


  JMenuItem resetPeaFiveItem;
  JMenuItem resetIndexItem;

  @Override
  public JMenu buildModeMenu() {
    JMenu menu = new JMenu("p5.js");
//    JMenuItem item;

    resetPeaFiveItem = new JMenuItem("Replace p5.js library");
    resetPeaFiveItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // copy p5.min.js to the libraries folder
        File sourceFile =
          new File(mode.getTemplateFolder(), "libraries/p5.min.js");
        File targetFile =
          new File(sketch.getFolder(), "libraries/p5.min.js");
        try {
          Util.copyFile(sourceFile, targetFile);
          enableDisableModeMenu();  // disable the menu item

        } catch (IOException ioe) {
          Messages.showTrace("Could not update the p5.js library",
                             "An error occurred while trying to replace\n" +
                             targetFile.getAbsolutePath(), ioe, false);
        }
      }
    });
    menu.add(resetPeaFiveItem);

    resetIndexItem = new JMenuItem("Re-create index.html");
    resetIndexItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          // get a fresh index.html from the template
          File sourceFile =
            new File(mode.getTemplateFolder(), "index.html");
          File targetFile =
            new File(sketch.getFolder(), "index.html");
          Util.copyFile(sourceFile, targetFile);

          // swap @@ entries from the template with the sketch name
          p5jsMode.buildIndex(sketch.getFolder(), sketch.getName());

          // load the new one back into the editor
          SketchCode indexHtmlCode = p5jsMode.findIndexHtml(sketch);

          if (indexHtmlCode != null) {
            indexHtmlCode.load();
            // unfortunate hack that seems necessary at the moment?
            indexHtmlCode.setDocument(null);
            // this gets the editor text area to update
            setCode(indexHtmlCode);
          }
        } catch (Exception ex) {
          Messages.showTrace("Error", "Could not write index.html.", ex, false);
        }
      }
    });
    menu.add(resetIndexItem);

    enableDisableModeMenu();
    return menu;
  }


  private void enableDisableModeMenu() {
    if (sketch != null) {
      File sourceFile =
        new File(mode.getTemplateFolder(), "libraries/p5.min.js");
      File targetFile =
        new File(sketch.getFolder(), "libraries/p5.min.js");

      // not a perfect check, but good enough for this usage
      boolean p5jsDifferent =
        (sourceFile.lastModified() != targetFile.lastModified()) ||
        (sourceFile.length() != targetFile.length());
      resetPeaFiveItem.setEnabled(p5jsDifferent);
    }
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
        Platform.openURL("https://p5js.org/get-started/");
      }
    });
    menu.add(item);

    item = new JMenuItem("Reference");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.openURL("https://p5js.org/reference/");
      }
    });
    menu.add(item);

    item = Toolkit.newJMenuItemShift("Find in Reference", 'F');
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleFindReference();
      }
    });
    menu.add(item);

    menu.addSeparator();

    item = new JMenuItem("Visit p5js.org");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Platform.openURL("https://p5js.org/");
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

      if (checkErrors()) {
        toolbar.deactivateRun();

      } else {
        if (server == null || server.isDead()) {
          startServer();
        }
        statusNotice("Server running at " + server.getAddress());
        String local = server.getLocalAddress();
        if (local != null) {
          System.out.println("To connect from another device on the local network, try:");
          System.out.println(local);
        }

        /*
        if (Desktop.isDesktopSupported()) {
          // use this version so that errors pop out as exceptions,
          // and we can show them as errors in the PDE (otherwise weird
          // URL opening problems are confusing for new users)
          Desktop.getDesktop().browse(new URI(server.getAddress()));

        } else {
          // if Desktop not available, try the Platform version,
          // which will try platform tricks (but is wrapped so the Exception
          // doesn't propagate, and therefore we're not using this by default)
          // https://github.com/fathominfo/processing-p5js-mode/issues/17
          Platform.openURL(server.getAddress());
        }
        */
        // in 4.0 beta 5, some fixes to how openURL() works
        Platform.openURL(server.getAddress());
      }
    } catch (Exception e) {
      statusError(e);
    }
  }


  public void handleStop() {
    try {
      p5jsBuild.cleanTempFiles(sketch);
    } catch (IOException e) {
      e.printStackTrace();  // TODO ignore?
    }
//    stopServer();
//    statusNotice("Server stopped.");
    toolbar.deactivateRun();
  }


  /** Find the first entry in the list of Problem objects that's an error. */
  private Problem findError() {
    for (Problem p : problems) {
      if (p.isError()) return p;
    }
    return null;
  }


  /**
   * Check for errors before launching
   * @return true if fatal errors found
   */
  private boolean checkErrors() {
    // if using the linter and there's an error, fail with that
    if (USE_LINTER) {
      Problem p = findError();
      if (p != null) {
        int line = p.getLineNumber();
        int column = p.getStartOffset() - getLineStartOffset(line);
        statusError(new SketchException(p.getMessage(),
                                        p.getTabIndex(),
                                        p.getLineNumber(),
                                        column,
                                        false));
        return true;
      }
    } else {  // otherwise just use the basic parser as in 1.1 and earlier
      try {
        NashornParse.handle(sketch);

      } catch (SketchException se) {
        statusError(se);
        return true;
      }
    }
    return false;
  }


  @Override
  public void showReference(String filename) {
    // this will give us "blah_.html" or "blah.html"
    // first remove the .html from the end
    String term = filename.substring(0, filename.length() - 5);
    // p5js doesn't do the underscore thing (functions and vars
    // can't have the same name in JS anyway, those lucky ducks)
    if (term.endsWith("_")) {
      term = term.substring(0, term.length() - 1);
    }
    Platform.openURL("https://p5js.org/reference/#/p5/" + term);
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  private void initWatcher() {
    // window will fire an activated event, so don't call startWatcher()
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowActivated(WindowEvent e) {
        startWatcher();
      }

      @Override
      public void windowDeactivated(WindowEvent e) {
        stopWatcher();
      }
    });

    // install the ErrorListener on all js and json tabs
    for (SketchCode sc : sketch.getCode()) {
      checkDocumentListener(sc);
    }
  }


  private void startWatcher() {
    watcher = new ErrorWatcher();
    watcher.start();
  }


  public void stopWatcher() {
    watcher = null;
  }


  class ErrorWatcher extends Thread {

    @Override
    public void run() {
//      while (Thread.currentThread() == this) {
      while (watcher == this) {
        if (System.currentTimeMillis() > nextUpdate) {
//          System.out.println("gonna check errors");
          checkLint();
        }
        try {
//          System.out.println("gonna sleep " + ErrorWatcher.this);
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }


  private void checkLint() {
    try {
      if (linter != null) {
        //String code = sketch.getMainProgram();
        try {
          // grab the code from the current tab (the one that changed)
          String code = sketch.getCurrentCode().getDocumentText();
          JSONArray result = linter.lint(code);
          if (result != null) {
            parseErrors(result);
            // no more updates until this is reset by a document change
            nextUpdate = Long.MAX_VALUE;
          }
        } catch (BadLocationException ble) { }  // ignore for now
      }
    } catch (ScriptException e1) {
      e1.printStackTrace();
    }
  }


  private void parseErrors(JSONArray result) {
//    System.out.println(result.format(2));
    final int tabIndex = sketch.getCurrentCodeIndex();

    List<Problem> problems = new ArrayList<>();

    //for (JSONObject obj : result.)
    for (int i = 0; i < result.size(); i++) {
      JSONObject obj = result.getJSONObject(i);

      String errorCode = obj.getString("code", null);
      final boolean error = errorCode != null && errorCode.startsWith("E");

      final String message = obj.getString("reason");

      final int line = obj.getInt("line", 1) - 1;
      final int start = getLineStartOffset(line);
      // use the evidence or the entire line
      String evidence = obj.getString("evidence", getLineText(line));
      final int stop = start + evidence.length();

      problems.add(new Problem() {

        @Override
        public boolean isWarning() {
          return !error;
        }

        @Override
        public boolean isError() {
          return error;
        }

        @Override
        public int getTabIndex() {
          return tabIndex;
        }

        @Override
        public int getStartOffset() {
          return start;
        }

        @Override
        public int getStopOffset() {
          return stop;
        }

        @Override
        public String getMessage() {
          return message;
        }

        @Override
        public int getLineNumber() {
          return line;
        }
      });
    }
    setProblemList(problems);
  }


  static final int DELAY_BEFORE_UPDATE = 650;
  long nextUpdate;


  @Override
  public void sketchChanged() {
    nextUpdate = System.currentTimeMillis() + DELAY_BEFORE_UPDATE;
  }


  final DocumentListener sketchChangedListener = new DocumentListener() {
    @Override
    public void insertUpdate(DocumentEvent e) {
      sketchChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      sketchChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      sketchChanged();
    }
  };


  private boolean hasListener(Document doc) {
    for (DocumentListener dl : ((AbstractDocument) doc).getDocumentListeners()) {
      if (dl == sketchChangedListener) {
        return true;
      }
    }
    return false;
  }


//  private void checkDocumentListeners() {
//    for (SketchCode sc : sketch.getCode()) {
//      checkDocumentListener(sc);
//    }
//  }


  private void checkDocumentListener(SketchCode sketchCode) {
    if (sketchCode.isExtension("js") || sketchCode.isExtension("json")) {
      Document doc = sketchCode.getDocument();
      if (doc != null) {
//      if (listeningDocuments == null) {
//        listeningDocuments = new HashSet<>();
//      }
      //System.out.println("listening docs: " + listeningDocuments);
      //System.out.println("this doc: " + doc + ", listening docs:");
//      System.out.println("listener: " + sketchChangedListener + ", doc listeners:");
//      DocumentListener[] dl = ((AbstractDocument) doc).getDocumentListeners();
//      for (int i = 0; i < dl.length; i++) {
//        System.out.println(dl[i]);
//      }
//      if (!listeningDocuments.contains(doc)) {
//        doc.addDocumentListener(sketchChangedListener);
//        listeningDocuments.add(doc);
//      }
        if (!hasListener(doc)) {
          doc.addDocumentListener(sketchChangedListener);
        }
      }
    }
  }


  /**
   * Event handler called when switching between tabs.
   * @param code tab to switch to
   */
  @Override
  public void setCode(SketchCode code) {
    /*
    Document oldDoc = code.getDocument();

    super.setCode(code);

    Document newDoc = code.getDocument();
    if (oldDoc != newDoc) {
      addDocumentListener(newDoc);
    }
    */
    super.setCode(code);
    checkDocumentListener(code);
  }


  @Override
  public void dispose() {
    // set the error thread to null when closing
    stopWatcher();
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


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
   */
  protected void startServer() {
//    System.out.println("restarting server? " + server + " " + (server != null && server.isDead()));
    if (server != null && server.isDead()) {
      // if server hung or something else went wrong .. stop it.
      server.stop();
      server = null;
    }

    if (port == 0) {
      resetPort();
    }
    try {
      server = new HttpServer(this, port);

    } catch (BindException be) {
      // If the port is in use, try another. Only do this once,
      // because it may be due to a firewall or other circumstances.
      resetPort();
      try {
        server = new HttpServer(this, port);
      } catch (IOException ioe) {
        statusError(ioe);  // error out here if still trouble
      }
    } catch (IOException e) {  // other unknown type of exception
      statusError(e);
    }

    if (server != null) {  // actually kick off the listening threads
      server.start();
    }
  }


  void resetPort() {
    port = (int) (8000 + Math.random() * 1000);
  }


  // method is still here, though we're never gonna stop the server
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
