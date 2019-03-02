package processing.mode.p5js;

import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ECMAException;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.options.Options;

import processing.app.Base;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.core.PApplet;


public class NashornParse {

  static void handle(Sketch sketch) throws SketchException {
    for (int i = 0; i < sketch.getCodeCount(); i++) {
      handleFile(sketch, i);
    }
  }

  static void handleFile(Sketch sketch, int codeIndex) throws SketchException {
    SketchCode sketchCode = sketch.getCode(codeIndex);
    if (sketchCode.isExtension("js") || sketchCode.isExtension("json")) {
      Options options = new Options("nashorn");
      options.set("anon.functions", true);
      options.set("parse.only", true);
      options.set("scripting", true);
      options.set("language", "es6");

      ErrorManager errors = new ErrorManager();
      Context context = new Context(options, errors, Base.class.getClassLoader());
      Context.setGlobal(context.createGlobal());
      //String code = PApplet.join(PApplet.loadStrings(file), "\n");
      //String code = sketchCode.getProgram();

      try {
        String code = sketchCode.getDocumentText();

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
          ecma.printStackTrace();
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
  }
}