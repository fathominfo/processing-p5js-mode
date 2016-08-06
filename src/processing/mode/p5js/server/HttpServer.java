package processing.mode.p5js.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import processing.mode.p5js.p5jsEditor;


/**
 * This is a very simple, multi-threaded HTTP server, originally based on
 * <a href="http://j.mp/6BQwpI">this</a> article on java.sun.com.
 */
public class HttpServer {
  // Where worker threads stand idle
  static List<HttpWorker> threads = new ArrayList<>();

  // timeout on client connections
  static final int TIMEOUT = 10000;

  // max # worker threads
  static final int WORKERS = 5;

  Map<String, Handler> handlerMap = new ConcurrentHashMap<>();
  Handler genericHandler;

  Thread thread;
  int port;
  boolean running;

  p5jsEditor editor;
  //File root;


  public HttpServer(p5jsEditor editor) {
    this(editor, (int) (8000 + Math.random() * 1000));
    //root = editor.getSketch().getFolder();
  }


  public HttpServer(p5jsEditor editor, final int port) {
    this.editor = editor;
    this.port = port;

    /*
    handlerMap.put("details", new DetailsHandler(this));
    handlerMap.put("header", new HeaderHandler(this));
    handlerMap.put("page", new PageHandler(this));
    handlerMap.put("sample", new SampleHandler(this));
    handlerMap.put("skip", new SkipHandler(this));
    handlerMap.put("status", new StatusHandler(this));
    handlerMap.put("types", new TypesHandler(this));
    */
    genericHandler = new GenericHandler(this);

    for (int i = 0; i < WORKERS; i++) {
      HttpWorker w = new HttpWorker(this);
      new Thread(w, "HttpServer Worker " + (i+1)).start();
      threads.add(w);
    }

    /*
    String url = "http://localhost:" + port + "/index.html";
    System.out.println(url);
    PApplet.launch(url);
    */
  }


  public void start() {
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        running = true;
        ServerSocket socket = null;
        try {
          socket = new ServerSocket(port);
          while (Thread.currentThread() == thread) {
            Socket s = socket.accept();
//            Worker w = null;
            synchronized (threads) {
              if (threads.isEmpty()) {
                HttpWorker worker = new HttpWorker(HttpServer.this);
                worker.setSocket(s);
                (new Thread(worker, "additional worker")).start();
              } else {
//                w = threads.get(0);
//                threads.remove(0);
                HttpWorker w = threads.remove(0);
                w.setSocket(s);
              }
            }
          }

        } catch (IOException e) {
          e.printStackTrace();

        } finally {
          if (socket != null) {
            try {
              socket.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
        running = false;
      }
    });
    thread.start();
  }


  public void stop() {
    thread = null;
  }


  public boolean isRunning () {
    return running;
  }


  public String getAddress() {
    return "http://127.0.0.1:" + port + "/";
  }


  File getRoot() {
    // Makes it persist properly even as the sketch is saved to new locations.
    // A sketch from a different editor will be running its own server.
    return editor.getSketch().getFolder();
    //return root;
  }


  Handler getHandler(String prefix) {
    return handlerMap.get(prefix);
  }


  Handler getGenericHandler() {
    return genericHandler;
  }


  /*
  static void log(String s) {
    if (false) {
      System.out.println(s);
    }
  }
  */


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


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


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


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
  static public final int HTTP_FOUND = 302;
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


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  static Map<Integer, String> statusMessages = new HashMap<>();


  static String getStatusMessage(int code) {
    return statusMessages.get(code);
  }


  static {
    statusMessages.put(HTTP_OK, "OK");
    statusMessages.put(HTTP_FOUND, "Found");
    statusMessages.put(HTTP_UNAUTHORIZED, "Found");
    statusMessages.put(HTTP_NOT_FOUND, "Not Found");
    statusMessages.put(HTTP_BAD_METHOD, "Bad Method");
    statusMessages.put(HTTP_SERVER_ERROR, "Server Error");
  }
}