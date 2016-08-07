package processing.mode.p5js;

import java.io.File;

import javax.script.*;

import processing.app.Base;
import processing.app.Sketch;
import processing.app.SketchException;
import processing.app.Util;
import processing.core.PApplet;


public class p5jsBuild {
  ScriptEngine engine;
//  static ScriptEngine engine =
//    new ScriptEngineManager().getEngineByName("javascript");


  public p5jsBuild(Sketch sketch) throws SketchException {
    //engine = new ScriptEngineManager(null).getEngineByName("javascript");
    //engine = new ScriptEngineManager(null).getEngineByName("nashorn");
    //engine = new ScriptEngineManager(ClassLoader.getSystemClassLoader()).getEngineByName("javascript");
    //ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngineManager manager = new ScriptEngineManager(Base.class.getClassLoader());
    engine = manager.getEngineByName("js");
    if (engine == null) {
      throw new SketchException("script engine is null");
    }
    try {
      System.out.println(engine.eval("1+1"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    String[] paths = Util.listFiles(sketch.getFolder(), false, ".js");
    PApplet.println("files:");
    PApplet.printArray(paths);
    File mainFile = sketch.getMainFile();
    System.out.println("main file is " + mainFile);
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
      System.out.println("handling " + file);
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