package processing.mode.p5js.server;

import java.io.*;
import java.net.*;
import java.util.*;

import processing.core.PApplet;


public class HttpWorker implements Runnable {
  private HttpServer server;
  private Socket socket;


  HttpWorker(HttpServer server) {
    this.server = server;
    socket = null;
  }


  synchronized void setSocket(Socket s) {
    this.socket = s;
    notify();
  }


  @Override
  public synchronized void run() {
    while (true) {
      if (socket == null) {
        // nothing to do
        try {
          wait();
        } catch (InterruptedException e) {
          // should not happen
          continue;
        }
      }
      try {
        handleClient();
      } catch (Exception e) {
        e.printStackTrace();
      }
      // go back in wait queue if there's fewer than numHandler connections.
      socket = null;
      List<HttpWorker> pool = HttpServer.threads;
      synchronized (pool) {
        if (pool.size() >= HttpServer.WORKERS) {
          // too many threads, exit this one
          return;
        } else {
          pool.add(this);
        }
      }
    }
  }


  void handleClient() throws IOException {
    BufferedReader reader = PApplet.createReader(socket.getInputStream());
    CarlOrff ps = new CarlOrff(socket.getOutputStream());

    // we will only block in read for this many milliseconds
    // before we fail with java.io.InterruptedIOException,
    // at which point we will abandon the connection.
    socket.setSoTimeout(HttpServer.TIMEOUT);
    socket.setTcpNoDelay(true);

    try {
      String line = reader.readLine();
      if (line != null) {
        String[] pieces = line.split(" ");
        if ("GET".equals(pieces[0])) {
          String path = pieces[1];
          if (path.length() < 1 || path.charAt(0) != '/') {
            ps.println("HTTP/1.0 500 So Misunderstood");
            ps.println();

          } else {
            path = path.substring(1);
            int slash = path.indexOf('/');
            String params = null;
            if (slash != -1) {
              params = path.substring(slash + 1);
            } else {
              slash = path.length();
            }
            String command = path.substring(0, slash);
            Handler handler = server.getHandler(command);
            if (handler != null) {
              handler.handle(params, ps);
            } else {
              server.getGenericHandler().handle(path, ps);
            }
          }

        } else if ("HEAD".equals(pieces[0])) {
          System.err.println("HEAD request ignored");

        } else {
          ps.println("HTTP/1.0 " + HTTP_BAD_METHOD + " Unsupported Method: " + pieces[0]);
          ps.println();
        }
        ps.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();

    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        // don't fuss about this, just doing out best
      }
    }
  }


  /** mapping of file extensions to content-types */
  static Map<String, String> mimeTypes = new HashMap<>();


  static String getMimeType(String extension) {
    return mimeTypes.get(extension);
  }


  static {
    mimeTypes.put("", "content/unknown");

    mimeTypes.put(".uu", "application/octet-stream");
    mimeTypes.put(".exe", "application/octet-stream");
    mimeTypes.put(".ps", "application/postscript");
    mimeTypes.put(".zip", "application/zip");
    mimeTypes.put(".sh", "application/x-shar");
    mimeTypes.put(".tar", "application/x-tar");
    mimeTypes.put(".snd", "audio/basic");
    mimeTypes.put(".au", "audio/basic");
    mimeTypes.put(".wav", "audio/x-wav");

    mimeTypes.put(".gif", "image/gif");
    mimeTypes.put(".jpg", "image/jpeg");
    mimeTypes.put(".jpeg", "image/jpeg");
    mimeTypes.put(".png", "image/png");

    mimeTypes.put(".htm", "text/html");
    mimeTypes.put(".html", "text/html");
    mimeTypes.put(".css", "text/css");
    mimeTypes.put(".js", "text/javascript");

    mimeTypes.put(".json", "application/json");
    mimeTypes.put(".jsonp", "application/javascript");

    mimeTypes.put(".txt", "text/plain");
    mimeTypes.put(".java", "text/plain");  // x-java-source -> plain is better

    mimeTypes.put(".c", "text/plain");
    mimeTypes.put(".cc", "text/plain");
    mimeTypes.put(".c++", "text/plain");
    mimeTypes.put(".h", "text/plain");
    mimeTypes.put(".pl", "text/plain");
  }


  /** 2XX: generally "OK" */
  static public final int HTTP_OK = 200;
//  static private final int HTTP_CREATED = 201;
//  static private final int HTTP_ACCEPTED = 202;
//  static private final int HTTP_NOT_AUTHORITATIVE = 203;
//  static private final int HTTP_NO_CONTENT = 204;
//  static private final int HTTP_RESET = 205;
//  static private final int HTTP_PARTIAL = 206;

  /** 3XX: relocation/redirect */
//  static private final int HTTP_MULT_CHOICE = 300;
//  static private final int HTTP_MOVED_PERM = 301;
//  static private final int HTTP_MOVED_TEMP = 302;
//  static private final int HTTP_SEE_OTHER = 303;
//  static private final int HTTP_NOT_MODIFIED = 304;
//  static private final int HTTP_USE_PROXY = 305;

  /** 4XX: client error */
//  static private final int HTTP_BAD_REQUEST = 400;
  static public final int HTTP_UNAUTHORIZED = 401;
//  static private final int HTTP_PAYMENT_REQUIRED = 402;
//  static private final int HTTP_FORBIDDEN = 403;
  static public final int HTTP_NOT_FOUND = 404;
  static public final int HTTP_BAD_METHOD = 405;
//  static private final int HTTP_NOT_ACCEPTABLE = 406;
//  static private final int HTTP_PROXY_AUTH = 407;
//  static private final int HTTP_CLIENT_TIMEOUT = 408;
//  static private final int HTTP_CONFLICT = 409;
//  static private final int HTTP_GONE = 410;
//  static private final int HTTP_LENGTH_REQUIRED = 411;
//  static private final int HTTP_PRECON_FAILED = 412;
//  static private final int HTTP_ENTITY_TOO_LARGE = 413;
//  static private final int HTTP_REQ_TOO_LONG = 414;
//  static private final int HTTP_UNSUPPORTED_TYPE = 415;

  /** 5XX: server error */
  static public final int HTTP_SERVER_ERROR = 500;
//  static private final int HTTP_INTERNAL_ERROR = 501;
//  static private final int HTTP_BAD_GATEWAY = 502;
//  static private final int HTTP_UNAVAILABLE = 503;
//  static private final int HTTP_GATEWAY_TIMEOUT = 504;
//  static private final int HTTP_VERSION = 505;
}
