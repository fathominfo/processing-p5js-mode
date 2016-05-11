package processing.mode.p5js;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import processing.app.Base;
import processing.app.Mode;
import processing.app.Platform;
import processing.app.Settings;
import processing.app.ui.Editor;
import processing.app.ui.EditorException;
import processing.app.ui.EditorState;


public abstract class ServingEditor extends Editor implements WebServerListener {
	static final String PROP_KEY_SERVER_PORT = "basicserver.port";

	WebServer server;
	boolean showSizeWarning = true;


	protected ServingEditor(Base base, String path, EditorState state, Mode mode)
		throws EditorException {
		super(base, path, state, mode);
	}


	/**
	 *	Set the server port, shows an input dialog to enter a port number
	 */
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

		if ( pString == null ) return;

		int port = -1;
		try {
			port = Integer.parseInt(pString);
		} catch (Exception e) {
			// sending foobar? you lil' hacker you ...
			statusError("That number was not okay ..");
			return;
		}

		if (port < WebServer.MIN_PORT || port > WebServer.MAX_PORT) {
			statusError( "That port number is out of range" );
			return;
		}

		if (server != null) {
			server.setPort(port);
		}

		File sketchProps = getSketchPropertiesFile();
		if (sketchProps.exists()) {
		  try {
		    Settings settings = new Settings(sketchProps);
		    settings.set( PROP_KEY_SERVER_PORT, (port + "") );
		    settings.save();
		  } catch ( IOException ioe ) {
		    statusError(ioe);
		  }
		}
	}


	/**
	 *	@return the server port as int or -1
	 */
	public int getServerPort() {
	  return server != null ? server.getPort() : -1;
	}


	/**
	 *	@return the server address as URL string or null
	 */
	public String getServerAddress() {
		if (server != null && server.isRunning()) {
		  return server.getAddress();
		}
		return null;
	}


	public WebServer getServer() {
		return server;
	}


	/**
	 *	A toggle to start/stop the server
	 *	@param root the root folder to start from if it needs to be started
	 */
	protected void startStopServer(File root) {
	  if (isServerRunning()) {
	    stopServer();
	  } else {
	    startServer(root);
	  }
	}


	/**
	 *	Create a server to server from given root dir
	 *	@param root the root folder to server from
	 *	@return the BasicServer instance running or created
	 */
	protected WebServer createServer(File root) {
		if ( server != null ) return server;

		if (!root.exists() && !root.mkdir()) {
			// bad .. let server handle the complaining ..
		}

		server = new WebServer(root);
		server.addListener( this );

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


	/**
	 *	Start the internal server for this sketch.
	 *	@param root the root folder for the server to serve from
	 *	@return true if it was started anew, false if it was running
	 */
	protected boolean startServer(File root) {
		if (server != null &&
		    (!server.isRunning() || !server.getRoot().equals(root))) {
		  // if server hung or something else went wrong .. stop it.
			server.shutDown();
			server = null;
		}

		if (server == null) {
			server = createServer( root );
		}

		if (!server.isRunning()) {
			server.setRoot(root);
			server.start();
			statusNotice( "Waiting for server to start ..." );

		} else if (server.isRunning()) {
			statusNotice("Server running ( " + server.getAddress() + "), " +
			             "reload your browser window." );
			return false;
		}
		return true;
	}


	protected boolean isServerRunning() {
		return server != null && server.isRunning();
	}


	protected void stopServer() {
		if (isServerRunning()) {
		  server.shutDown();
		}
	}


	/**
	 *	Create or get the sketch's properties file
	 *	@return the sketch properties file or null
	 */
	protected File getSketchPropertiesFile() {
		File sketchPropsFile =
		  new File(getSketch().getFolder(), "sketch.properties");
		if (!sketchPropsFile.exists()) {
			try {
				sketchPropsFile.createNewFile();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				statusError( "Unable to create sketch properties file!" );
				return null;
			}
		}
		return sketchPropsFile;
	}


	/**
	 *	Open a new browser window or tab with the server address
	 */
	protected void openBrowserForServer() {
		if (isServerRunning()) {
			Platform.openURL(server.getAddress());
		}
	}


	/**
   * Called from server thread after the server started (WebServerListener)
	 */
	public void serverStarted() {
		String location = server.getAddress();
		statusNotice("Server started: " + location);
		openBrowserForServer();
	}


	/**
	 * Called from server thread after the server stopped (WebServerListener)
	 */
	public void serverStopped () {
		statusNotice("Server stopped.");
	}
}