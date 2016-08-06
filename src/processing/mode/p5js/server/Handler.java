package processing.mode.p5js.server;


abstract public class Handler {
  HttpServer server;


  public Handler(HttpServer server) {
    this.server = server;
  }


  abstract public void handle(String params, CarlOrff ps);
}