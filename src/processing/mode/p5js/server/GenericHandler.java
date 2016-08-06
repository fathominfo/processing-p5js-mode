package processing.mode.p5js.server;

import java.io.*;

import processing.core.PApplet;


public class GenericHandler extends Handler {

  public GenericHandler(HttpServer server) {
    super(server);
  }


  @Override
  public void handle(String path, CarlOrff ps) {
    File root = server.getRoot();
//    System.out.println("root is " + root);
//    System.out.println("path is " + path);
    //File target = new File(root, path.substring(1));  // remove leading slash
    File target = new File(root, path);  // leading slash already removed

//    if (page == null || page.trim().length() == 0) {
//      ps.status(404);
//    }

    if (target.exists()) {
      if (target.isDirectory()) {
        File indexFile = new File(target, "index.html");
        if (indexFile.exists()) {
          if (!path.endsWith("/")) {
            path += '/';
          }
          path += "index.html";
          handle(path, ps);
        } else {
          ps.status(HttpWorker.HTTP_UNAUTHORIZED);
          ps.println("<html><body>");
          ps.println("<h1>Directory Listing Denied</h1>");
          ps.println("<pre>" + path + "</pre>");
          ps.println("<pre>" + target.getAbsolutePath() + "</pre>");
          ps.println("</body></html>");
        }
      } else {
        byte[] b = null;
        try {
          String localPath = target.getAbsolutePath();
          InputStream input = new BufferedInputStream(new FileInputStream(target));
          b = PApplet.loadBytes(input);

          ps.status(HttpWorker.HTTP_OK);
          String contentType = "content/unknown";
          int dot = localPath.lastIndexOf('.');
          if (dot != -1) {
            String extension = localPath.substring(dot);
            String found = HttpWorker.getMimeType(extension);
            if (found != null) {
              contentType = found;
            } else {
              System.err.println("no content type found for " + extension);
            }
          }
          ps.println("Content-Type: " + contentType);
          ps.println("Content-Length: " + b.length);
          ps.println();
          ps.write(b);

        } catch (Exception e) {
          ps.status(HttpWorker.HTTP_SERVER_ERROR);
          ps.println("<html><body>");
          ps.println("<h1>" + HttpWorker.HTTP_SERVER_ERROR + " Exception</h1>");
          ps.println("<pre>");
          e.printStackTrace(ps.ps);
          ps.println("</pre>");
          ps.println("</body</html>");
        }
      }
    } else {
      ps.status(HttpWorker.HTTP_NOT_FOUND);
      ps.println("<html><body>");
      ps.println("<h1>" + HttpWorker.HTTP_NOT_FOUND + " Not Found</h1>");
      ps.println("<pre>" + path + "</pre>");
      ps.println("<pre>" + target.getAbsolutePath() + "</pre>");
      ps.println("</body></html>");
    }
  }
}
