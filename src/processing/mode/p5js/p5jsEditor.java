package processing.mode.p5js;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import processing.app.Base;
import processing.app.Formatter;
import processing.app.Mode;
import processing.app.Platform;
import processing.app.SketchException;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.PdeTextArea;
import processing.app.syntax.PdeTextAreaDefaults;
import processing.app.ui.Editor;
import processing.app.ui.EditorException;
import processing.app.ui.EditorState;
import processing.app.ui.EditorToolbar;
import processing.app.ui.Toolkit;
import processing.mode.java.AutoFormat;
import processing.mode.java.JavaInputHandler;
import processing.mode.p5js.server.HttpServer;


public class p5jsEditor extends Editor {
//  private p5jsMode jsMode;

  HttpServer server;
  boolean showSizeWarning = true;


  protected p5jsEditor(Base base, String path,
                       EditorState state, Mode mode) throws EditorException {
    super(base, path, state, mode);

//    jsMode = (p5jsMode) mode;
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
    JMenuItem exportItem = Toolkit.newJMenuItem("Export", 'E');
    exportItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleExport(true);
      }
    });
    return buildFileMenu(new JMenuItem[] { exportItem });
    //return buildFileMenu(null);
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


  /**
   *  Menu item callback, let's users set the server port number
   */
  /*
  private void handleSetServerPort ()
  {
  statusEmpty();

  boolean wasRunning = isServerRunning();
  if ( wasRunning )
  {
    statusNotice("Server was running, changing the port requires a restart.");
    stopServer();
  }

  setServerPort();
  saveSketchSettings();

  if ( wasRunning ) {
    startServer( getExportFolder() );
  }
  }
  */


  /**
   *  Menu item callback, copy basic template to sketch folder
   */
  /*
  private void handleCreateCustomTemplate() {
    Sketch sketch = getSketch();

    File ajs = sketch.getMode().getContentFile(p5jsBuild.TEMPLATE_FOLDER_NAME);
    File tjs = getCustomTemplateFolder();

    if (!tjs.exists()) {
      try {
        Util.copyDir(ajs, tjs);
        statusNotice("Default template copied.");
        Platform.openFolder( tjs );
      } catch ( java.io.IOException ioe ) {
        Messages.showWarning("Copy default template folder",
                             "Something went wrong when copying the template folder.", ioe);
      }
    } else {
      statusError( "You need to remove the current "+
          "\""+p5jsBuild.TEMPLATE_FOLDER_NAME+"\" "+
          "folder from the sketch." );
    }
  }
  */


  /**
   *  Menu item callback, open custom template folder from inside sketch folder
   */
  /*
  private void handleOpenCustomTemplateFolder ()
  {
    File tjs = getCustomTemplateFolder();
  if ( tjs.exists() )
  {
    Platform.openFolder( tjs );
  }
  else
  {
    // TODO: promt to create one?
    statusNotice( "You have no custom template with this sketch. Create one from the menu!" );
  }
  }
  */


  /**
   *  Menu item callback, copy server address to clipboard
   */
  /*
  private void handleCopyServerAddress() {
    String address = getServerAddress();
    if (address != null) {
      StringSelection stringSelection = new StringSelection(address);
      Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents( stringSelection, null );
    }
  }
  */

  /**
   *  Menu item callback, open the playback settings frontend
   */
  /*
  private void handleShowDirectivesEditor ()
  {
  if ( directivesEditor == null )
  {
    directivesEditor = new DirectivesEditor(this);
  }

  directivesEditor.show();
  }
  */


  /*
  public File getTemplateFolder() {
    return getMode().getContentFile("template");
  }
  */


  public File getLibrariesFolder() {
    return new File(mode.getTemplateFolder(), "libraries");
  }


  public void handleRun() {
    toolbar.activateRun();
    if (server == null || !server.isRunning()) {
      restartServer();
    }
    Platform.openURL(server.getAddress());
  }


  /**
   *  Menu item callback, replacement for STOP: stop server.
   */
  public void handleStop() {
    stopServer();
    toolbar.deactivateRun();
  }


  public boolean handleExport(boolean openFolder) {
    //return mode.handleExport(sketch);
    try {
      new p5jsBuild(sketch);
      return true;
    } catch (SketchException se) {
      statusError(se);
    }
    return false;
  }


  /**
   *  Menu item callback, call the export method of the sketch
   *  and handle the gui stuff
   */
  /*
  public boolean handleExport(boolean openFolder) {
    if (!handleExportCheckModifiedMod()) {
      return false;
    } else {
      try {
        boolean success = jsMode.handleExport(sketch);
        if (success && openFolder) {
          File exportFolder = new File(sketch.getFolder(),
                                       p5jsBuild.EXPORT_FOLDER_NAME);
          Platform.openFolder(exportFolder);
          statusNotice("Finished exporting.");

        } else if (!success) {
          // error message already displayed by handleExport
          return false;
        }
      } catch (Exception e) {
        statusError(e);

        return false;
      }
    }
    return true;
  }
  */


  /**
   *  Menu item callback, changed from Editor.java to automatically
   *  export and handle the server when it's running.
   *  Normal save ops otherwise.
   *
   *  @param immediately set false to run in a Swing optimized manner
   */
  /*
  @Override
  public boolean handleSave(boolean immediately) {
    if (sketch.isUntitled()) {
      return handleSaveAs();
    } else if (immediately) {
      handleSaveImpl();
      statusEmpty();
      if (isServerRunning()) {
        handleStartServer();
      }
    } else {
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          handleSaveImpl();
          statusEmpty();
          if (isServerRunning()) {
            handleStartServer();
          }
        }
      });
    }
    return true;
  }
  */


  /**
   *  Called from handleSave( true/false )
   */
  /*
  public void handleSave ()
  {
    // toolbar.activate(JavaScriptToolbar.SAVE);

    handleSaveImpl();

    // toolbar.deactivate(JavaScriptToolbar.SAVE);
  }
  */


  /**
   *  Called from handleExport()
   */
  /*
  private boolean handleExportCheckModifiedMod() {
    if (sketch.isModified()) {
      Object[] options = { "OK", "Cancel" };
      int result = JOptionPane.showOptionDialog(this,
                                                "Save changes before export?",
                                                "Save",
                                                JOptionPane.OK_CANCEL_OPTION,
                                                JOptionPane.QUESTION_MESSAGE,
                                                null,
                                                options,
                                                options[0]);

      if (result == JOptionPane.OK_OPTION) {
        handleSave(true);
      } else {
        statusNotice("Export canceled, changes must first be saved.");
        return false;
      }
    }
    return true;
  }
  */


  /**
   * Called from server thread after the server started (WebServerListener)
   */
  /*
  @Override
  public void serverStarted() {
    String location = server.getAddress();
    statusNotice("Server started: " + location);
    openBrowserForServer();
    if (!handleExport(false)) return;
    toolbar.activateRun();
  }
  */


  /**
   *  Return the current export folder in a sane way
   *
   *  @return the export folder as File
   */
  /*
  private File getExportFolder() {
    return new File(getSketch().getFolder(), p5jsBuild.EXPORT_FOLDER_NAME);
  }
  */


  /**
   *  Return the custom template folder
   *
   *  @return the custom template folder as File
   */
  /*
  private File getCustomTemplateFolder() {
    return new File(getSketch().getFolder(), p5jsBuild.TEMPLATE_FOLDER_NAME);
  }
  */


  /**
   *  Set the server port, shows an input dialog to enter a port number
   */
  /*
  protected void setServerPort() {
    String pString = null;
    String msg = "Set the server port (1024 < port < 65535)";
    int currentPort = -1;

    if (server != null) {
      currentPort = server.getPort();
    }

    if (currentPort > 0) {
      pString = JOptionPane.showInputDialog(msg, currentPort + "");
    } else {
      pString = JOptionPane.showInputDialog(msg);
    }

    if (pString == null) return;

    int port = -1;
    try {
      port = Integer.parseInt(pString);
    } catch (Exception e) {
      // sending foobar? you lil' hacker you ...
      statusError("That number was not okay ..");
      return;
    }

    if (port < WebServer.MIN_PORT || port > WebServer.MAX_PORT) {
      statusError("That port number is out of range");
      return;
    }

    if (server != null) {
      server.setPort(port);
    }

    File sketchProps = getSketchPropertiesFile();
    if (sketchProps.exists()) {
      try {
        Settings settings = new Settings(sketchProps);
        settings.set(PROP_KEY_SERVER_PORT, (port + ""));
        settings.save();
      } catch (IOException ioe) {
        statusError(ioe);
      }
    }
  }
  */


  /**
   *  @return the server port as int or -1
   */
  /*
  public int getServerPort() {
    return server != null ? server.getPort() : -1;
  }
  */


  /**
   *  @return the server address as URL string or null
   */
  /*
  public String getServerAddress() {
    if (server != null && server.isRunning()) {
      return server.getAddress();
    }
    return null;
  }
  */


  /**
   *  A toggle to start/stop the server
   *  @param root the root folder to start from if it needs to be started
   */
  /*
  protected void startStopServer(File root) {
    if (isServerRunning()) {
      stopServer();
    } else {
      restartServer();
    }
  }
  */


  /**
   *  Create a server to server from given root dir
   *  @param root the root folder to server from
   *  @return the BasicServer instance running or created
   */
  /*
  protected WebServer createServer(File root) {
    if (server != null) return server;

    if (!root.exists() && !root.mkdir()) {
      // bad .. let server handle the complaining ..
    }

    server = new WebServer(root);
    server.addListener(this);

    File sketchProps = getSketchPropertiesFile();
    if (sketchProps.exists()) {
      try {
        Settings props = new Settings(sketchProps);
        String portString = props.get(PROP_KEY_SERVER_PORT);
        if (portString != null && !portString.trim().isEmpty()) {
          int port = Integer.parseInt(portString);
          server.setPort(port);
        }
      } catch (IOException ioe) {
        statusError(ioe);
      }
    }
    return server;
  }
  */


  /**
   *  Start the internal server for this sketch.
   *  @param root the root folder for the server to serve from
   *  @return true if it was started anew, false if it was running
   */
  protected void restartServer() {
    if (server != null && !server.isRunning()) {
      // if server hung or something else went wrong .. stop it.
      server.stop();
      server = null;
    }

    if (server == null) {
      server = new HttpServer(this);
    }

    server.start();
    statusNotice("Server running at " + server.getAddress());
  }


  protected boolean isServerRunning() {
    return server != null && server.isRunning();
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


  /**
   *  Open a new browser window or tab with the server address
   */
  /*
  protected void openBrowserForServer() {
    if (isServerRunning()) {
      Platform.openURL(server.getAddress());
    }
  }
  */


  /**
   * Called from server thread after the server stopped (WebServerListener)
   */
  /*
  @Override
  public void serverStopped() {
    statusNotice("Server stopped.");
  }
  */


  @Override
  public void handleImportLibrary(String name) { }
}
