package processing.mode.p5js.server;

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

    byte[] b = PApplet.loadBytes(PApplet.class.getResourceAsStream("/icon/icon-32.png"));

    ps.status(HttpServer.HTTP_OK);
    ps.println("Content-Type: image/png");
    ps.println("Content-Length: " + b.length);
    ps.println();
    ps.write(b);
  }
}