package processing.mode.p5js;

import java.io.File;

import javax.script.*;

import processing.app.Sketch;
import processing.app.SketchException;
import processing.app.Util;
import processing.core.PApplet;


public class p5jsBuild {
  static ScriptEngine engine =
    new ScriptEngineManager().getEngineByName("javascript");


  public p5jsBuild(Sketch sketch) throws SketchException {
//    engine = new ScriptEngineManager().getEngineByName("javascript");
    String[] paths = Util.listFiles(sketch.getFolder(), false, ".js");
    File mainFile = sketch.getMainFile();
    //File[] files = new File[paths.length];
    for (int i = 0; i < paths.length; i++) {
      //files[i] = new File(paths[i]);
      File file = new File(paths[i]);
      if (!file.equals(mainFile)) {
        handleFile(file);
      }
    }
    handleFile(mainFile);
  }


  protected void handleFile(File file) throws SketchException {
    try {
      engine.eval(PApplet.createReader(file));
    } catch (ScriptException se) {
      int line = se.getLineNumber();
      throw new SketchException(se.getMessage(), 0, line);
    }
  }


  public boolean export() {
    return false;
  }
}