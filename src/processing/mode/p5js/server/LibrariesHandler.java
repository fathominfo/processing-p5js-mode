package processing.mode.p5js.server;

import java.io.*;


/**
 * If no libraries folder exists, just pull them from the /libraries folder.
 */
public class LibrariesHandler extends GenericHandler {

  public LibrariesHandler(HttpServer server) {
    super(server);
  }

  @Override
  public void handle(String path, CarlOrff ps) {
    File target = new File(server.getRoot(), path);
    if (!target.exists()) {
      target = new File(server.getLibrariesFolder(), path);
    }
    handleFile(path, ps, target);
  }
}
