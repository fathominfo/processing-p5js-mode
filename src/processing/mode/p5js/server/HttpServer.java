package processing.mode.p5js.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

  p5jsEditor editor;
  int port;


  public HttpServer(p5jsEditor editor) {
    this(editor, (int) (8000 + Math.random() * 1000));
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

    new Thread(new Runnable() {
      @Override
      public void run() {
        ServerSocket ss = null;
        try {
          ss = new ServerSocket(port);
          while (true) {
            Socket s = ss.accept();
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
          if (ss != null) {
            try {
              ss.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }).start();
  }


  public String getAddress() {
    return "http://127.0.0.1:" + port + "/";
  }


  File getRoot() {
    return editor.getSketch().getFolder();
  }


  Handler getHandler(String prefix) {
    return handlerMap.get(prefix);
  }


  Handler getGenericHandler() {
    return genericHandler;
  }


  /** print to the log file */
  static void log(String s) {
    if (false) {
      System.out.println(s);
    }
  }
}