package processing.mode.p5js;

import java.io.File;

import processing.app.Library;


public class p5jsLibrary extends Library {

  public p5jsLibrary(File folder) {
    super(folder);
  }


  @Override
  protected void handle() {
    // no platform-specific stuff to do here; clear out the superclass
    // parsing of the .jar file and whatnot
  }
}