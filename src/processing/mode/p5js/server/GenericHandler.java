package processing.mode.p5js.server;

import java.io.*;

import processing.core.PApplet;


public class GenericHandler extends Handler {
//  File root;


  public GenericHandler(HttpServer server) {
    super(server);
//    root = server.getRoot();
  }


//  public GenericHandler(HttpServer server, File root) {
//    super(server);
//    this.root = root;
//  }


//  public void setRoot(File root) {
//    this.root = root;
//  }


  @Override
  public void handle(String path, CarlOrff ps) {
//    System.out.println("root is " + root);
//    System.out.println("path is " + path);
    //File target = new File(root, path.substring(1));  // remove leading slash
    File target = new File(server.getRoot(), path);  // leading slash already removed

//    if (page == null || page.trim().length() == 0) {
//      ps.status(404);
//    }
    handleFile(path, ps, target);
  }


  void handleFile(String path, CarlOrff ps, File target) {
    if (target.exists()) {
      if (target.isDirectory()) {
        File indexFile = new File(target, "index.html");
        if (indexFile.exists()) {
          if (path.length() > 0 && !path.endsWith("/")) {
            ps.status(HttpServer.HTTP_FOUND);
            ps.println("Location: " + path + "/");
            ps.println();

          } else {
            path += "index.html";
            handle(path, ps);
          }
        } else {
          ps.status(HttpServer.HTTP_UNAUTHORIZED);
          ps.println("Content-type: text/html");
          ps.println();
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

          ps.status(HttpServer.HTTP_OK);
          String contentType = "content/unknown";
          int dot = localPath.lastIndexOf('.');
          if (dot != -1) {
            String extension = localPath.substring(dot);
            String found = HttpServer.getMimeType(extension);
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
          ps.status(HttpServer.HTTP_SERVER_ERROR);
          ps.println("Content-type: text/html");
          ps.println();
          ps.println("<html><body>");
          ps.println("<h1>" + HttpServer.HTTP_SERVER_ERROR + " Exception</h1>");
          ps.println("<pre>");
          e.printStackTrace(ps.ps);
          ps.println("</pre>");
          ps.println("</body</html>");
        }
      }
    } else {
      ps.status(HttpServer.HTTP_NOT_FOUND);
      ps.println("Content-type: text/html");
      ps.println();
      ps.println("<html><body>");
      ps.println("<h1>" + HttpServer.HTTP_NOT_FOUND + " Not Found</h1>");
      ps.println("<pre>" + path + "</pre>");
      ps.println("<pre>" + target.getAbsolutePath() + "</pre>");
      ps.println("</body></html>");
    }
  }
}
