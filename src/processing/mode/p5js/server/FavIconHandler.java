package processing.mode.p5js.server;

import java.io.InputStream;

import processing.core.PApplet;


public class FavIconHandler extends Handler {

  /**
   * Send the Processing icon as a default favicon, avoiding incessant
   * 404s in the web browser console (to reduce unnecessary confusion).
   */
  public FavIconHandler(HttpServer server) {
    super(server);
  }


  @Override
  public void handle(String params, CarlOrff ps) {
    // Nah, don't bother: why deal with guessing the mime type?
    //File file = new File(server.getRoot(), "favicon.ico");

    final String ICON_PATH = "/icon/icon-32.png";
    InputStream input = null;
    try {
      input = PApplet.class.getResourceAsStream(ICON_PATH);
      if (input == null) {
        ps.status(HttpServer.HTTP_NOT_FOUND);
        ps.println("Content-Type: text/html");
        ps.println();
        ps.println("Not Found");
        ps.println("<PRE>" + ICON_PATH + " not found.</PRE>");

      } else {
        try {
          byte[] b = PApplet.loadBytes(input);

          ps.status(HttpServer.HTTP_OK);
          ps.println("Content-Type: image/png");
          ps.println("Content-Length: " + b.length);
          ps.println();
          ps.write(b);

        } catch (Exception e) {
          ps.status(HttpServer.HTTP_SERVER_ERROR);
          ps.println("Content-Type: text/html");
          ps.println();
          ps.println("Server Error");
          ps.println("<PRE>Error while reading " + ICON_PATH);
          ps.write(e);
        }
      }
    } finally {
      try {
        if (input != null) {
          input.close();
        }
      } catch (Exception e) { }
    }
  }
}