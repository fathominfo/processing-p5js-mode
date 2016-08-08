package processing.mode.p5js;

import java.io.File;

import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ECMAException;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.options.Options;

import processing.app.Base;
import processing.app.Sketch;
import processing.app.SketchException;

import processing.core.PApplet;


public class p5jsBuild {
//  ScriptEngine engine;
//  static ScriptEngine engine =
//    new ScriptEngineManager().getEngineByName("javascript");


  public p5jsBuild(Sketch sketch) throws SketchException {
    //SketchCode[] code = sketch.getCode();
    //for (int i = 0; i < code.length; i++) {
    for (int i = 0; i < sketch.getCodeCount(); i++) {
      handleFile(sketch, i);
    }
  }


//  public p5jsBuild(final Editor editor, Sketch sketch) throws SketchException {
//    //engine = new ScriptEngineManager(null).getEngineByName("javascript");
//    //engine = new ScriptEngineManager(null).getEngineByName("nashorn");
//    //engine = new ScriptEngineManager(ClassLoader.getSystemClassLoader()).getEngineByName("javascript");
//    //ScriptEngineManager manager = new ScriptEngineManager();
//
//    /*
//    ScriptEngineManager manager = new ScriptEngineManager(Base.class.getClassLoader());
//    engine = manager.getEngineByName("js");
//    if (engine == null) {
//      throw new SketchException("script engine is null");
//    }
////    try {
////      engine.eval("load(\"nashorn:mozilla_compat.js\")");
////      //System.out.println(engine.eval("1+1"));
//////      System.out.println(engine.eval("void setup() {}"));
////    } catch (Exception e) {
////      e.printStackTrace();
////    }
//
//    String[] paths = Util.listFiles(sketch.getFolder(), false, ".js");
////    PApplet.println("files:");
////    PApplet.printArray(paths);
//    File mainFile = sketch.getMainFile();
////    System.out.println("main file is " + mainFile);
//    //File[] files = new File[paths.length];
//
////    for (int i = 0; i < paths.length; i++) {
////      //files[i] = new File(paths[i]);
////      File file = new File(paths[i]);
////      if (!file.equals(mainFile)) {
////        handleFile(file);
////      }
////    }
//
//    handleFile(new File(sketch.getFolder(), "libraries/p5.js"));
//    handleFile(new File(sketch.getFolder(), "libraries/p5.dom.js"));
//    handleFile(new File(sketch.getFolder(), "libraries/p5.sound.js"));
//    handleFile(mainFile);
//    */
//
//    Options options = new Options("nashorn");
//    options.set("anon.functions", true);
//    options.set("parse.only", true);
//    options.set("scripting", true);
//
//    ErrorManager errors = new ErrorManager();
//    /*
//    ErrorManager errors = new ErrorManager() {
//      // never seems to fire on parse errors
//      public void error(ParserException ex) {
//        //handleError(ex, true);
//        //sketch.getMode().
//        editor.statusError(new SketchException(ex.getMessage(), 0, ex.getLineNumber()));
//      }
////      @Override
////      public void warning(ParserException ex) {
////        throw new SketchException(ex.getMessage(), 0, ex.getLineNumber());
////      }
//    };
//    */
//    //Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
//    Context context = new Context(options, errors, Base.class.getClassLoader());
////    Source source = Source.sourceFor("test", "var a = 10; var b = a + 1;" +
////        "function someFunction() { return b + 1; }  ");
//    /*
//    try {
//      Source source = Source.sourceFor(sketch.getName(), sketch.getMainFile());
//      Parser parser = new Parser(context.getEnv(), source, errors);
//      FunctionNode functionNode = parser.parse();
//      Block block = functionNode.getBody();
//      List<Statement> statements = block.getStatements();
//      for (Statement s : statements) {
//        System.out.println(s);
//      }
//      System.out.println("errors = " + errors.getNumberOfErrors());
//      System.out.println("warnings = " + errors.getNumberOfWarnings());
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//       */
//    Context.setGlobal(context.createGlobal());
//    String code = PApplet.join(PApplet.loadStrings(sketch.getMainFile()), "\n");
//    try {
//      /*String json =*/ ScriptUtils.parse(code, sketch.getName(), true);
//      //System.out.println(json);
//    } catch (ECMAException ecma) {
//      // [0] "SyntaxError: test2:6:14 Expected ; but found !"
//      // [1] "SyntaxError"
//      // [2] " test2"
//      // [3] "6"
//      // [4] "14"
//      // [5] "Expected ; but found !"
//      String[] m = PApplet.match(ecma.getMessage(),
//                                 "(.*:\\s*)(.*):(\\d+):(\\d+)\\s+([^\n]*)");
//      //PApplet.printArray(m);
//
//      if (m == null) {
//        // not sure how to parse this one, just posted it as-is
//        throw new SketchException(ecma.getMessage(), false);
//      } else {
//        // Subtract 1 from the result because the lines are 1-indexed.
//        // If the parseInt fails, won't set the line or column number,
//        // because it'll return 0 and then subtract 1, and -1 passed to
//        // SketchException for line or column is "unknown".
//        throw new SketchException(m[1] + m[5], 0,
//                                  PApplet.parseInt(m[3]) - 1,
//                                  PApplet.parseInt(m[4]) - 1,
//                                  false);
//        /*
//        //System.out.println("err: " + ecma.getEcmaError());
//        String msg = ecma.getMessage();
//        System.out.println("full message is " + msg);
//        String[] parts = PApplet.split(msg, ':');
//        if (parts.length > 3) {
//          //        char[] c = msg.toCharArray();
//          //        for (char cc : c) {
//          //          System.out.println(cc + " " + (int)cc);
//          //        }
//          msg = parts[3];
//          msg = msg.substring(msg.indexOf(' '), msg.indexOf('\n'));
//        }
//        System.out.println("line number is " + ecma.getLineNumber());
//        throw new SketchException(msg, 0, ecma.getLineNumber(),
//                                  ecma.getColumnNumber(), false);
//         */
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//      throw new SketchException("Internal error: check console for details");
//      //System.out.println(e.getClass().getName());
//    }
//  }


  /*
  protected void handleFile(File file) throws SketchException {
    try {
      System.out.println("handling " + file);
      engine.eval(PApplet.createReader(file));
    } catch (ScriptException se) {
      int line = se.getLineNumber();
      throw new SketchException(se.getMessage(), 0, line);
    }
  }
  */


  static void handleFile(Sketch sketch, int codeIndex) throws SketchException {
    Options options = new Options("nashorn");
    options.set("anon.functions", true);
    options.set("parse.only", true);
    options.set("scripting", true);

    ErrorManager errors = new ErrorManager();
    Context context = new Context(options, errors, Base.class.getClassLoader());
    Context.setGlobal(context.createGlobal());
    File file = sketch.getCode(codeIndex).getFile();
    String code = PApplet.join(PApplet.loadStrings(file), "\n");
    try {
      //String json = ScriptUtils.parse(code, sketch.getName(), true);
      ScriptUtils.parse(code, sketch.getName(), true);

    } catch (ECMAException ecma) {
      // [0] "SyntaxError: test2:6:14 Expected ; but found !"
      // [1] "SyntaxError"
      // [2] " test2"
      // [3] "6"
      // [4] "14"
      // [5] "Expected ; but found !"
      String[] m = PApplet.match(ecma.getMessage(),
                                 "(.*:\\s*)(.*):(\\d+):(\\d+)\\s+([^\n]*)");
      //PApplet.printArray(m);

      if (m == null) {
        // not sure how to parse this one, just posted it as-is
        throw new SketchException(ecma.getMessage(), false);
      } else {
        // Subtract 1 from the result because the lines are 1-indexed.
        // If the parseInt fails, won't set the line or column number,
        // because it'll return 0 and then subtract 1, and -1 passed to
        // SketchException for line or column is "unknown".
        throw new SketchException(m[1] + m[5], codeIndex,
                                  PApplet.parseInt(m[3]) - 1,
                                  PApplet.parseInt(m[4]) - 1,
                                  false);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new SketchException("Internal error: check console for details");
      //System.out.println(e.getClass().getName());
    }
  }


  /*
  public boolean export() {
    return false;
  }
  */
}