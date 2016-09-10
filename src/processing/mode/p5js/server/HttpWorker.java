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


  /**
   * Set the Socket for this worker. The Socket will be closed by this class
   * once finished.
   */
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
            ps.status(HttpServer.HTTP_BAD_METHOD);
            ps.println("Content-type: text/plain");
            ps.println();
            ps.println("500 So Misunderstood");
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
          ps.status(HttpServer.HTTP_BAD_METHOD);
          ps.println("Content-type: text/plain");
          ps.println();
          ps.println("405 Unsupported Method: " + pieces[0]);
          ps.println();
        }
        ps.flush();
      }
    } catch (SocketTimeoutException ste) {
      // Ignoring these for now... Seems to be extra sockets opened
      // by the browser, but not quite clear yet.
      // https://github.com/fathominfo/processing-p5js-mode/issues/5
      System.out.println("Socket timed out.");

    } catch (IOException e) {
      e.printStackTrace();
    }

    // clean up, but ignore any errors coming through
    try {
      reader.close();
    } catch (IOException e) { }
    try {
      socket.close();
    } catch (IOException e) { }
  }
}
