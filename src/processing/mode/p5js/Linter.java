package processing.mode.p5js;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import processing.app.ui.Editor;
import processing.core.PApplet;
import processing.data.JSONArray;


// Nashorn invocation based on ParallelNashornJSHint.java from this repo:
// https://github.com/wfhartford/parallel-nashorn-jshint/

// other useful polyfills in section 4.1.2: https://www.n-k.de/riding-the-nashorn/

public class Linter {
  static private final String OPTIONS = "{" +
      "esversion: 6," +
      "browser: true," +  // enable browser globals (document, navigator, FileReader)
      "curly: true," +  // require curly braces around blocks
      "eqnull: true," +  // ok to check if something == null
      "evil: true," +   // allows eval()
      "jquery: true," +  // turn on jquery globals
      "loopfunc: true," +  // warn about functions defined inside loops
      "noarg: true," +  // prohibits the use of arguments.caller and arguments.callee

      // https://jshint.com/docs/options/#sub
      "sub: true," +  // don't complain about person['name'] vs. person.name

      // https://jshint.com/docs/options/#trailingcomma
      "trailingcomma: false," +  // too confusing for users

      // https://jshint.com/docs/options/#asi
      // https://github.com/fathominfo/processing-p5js-mode/issues/25
      "asi: true" +  // allow no semicolons
  "}";


  // Call jshint on array named 'lines' which we'll replace with the sketch.
  static private final String FUNCTION_CALL =
    "JSHINT(lines, " + OPTIONS + "); JSON.stringify(JSHINT.errors);";

  CompiledScript compiled;


  public Linter(Editor editor) {
    new Thread(() -> {
      final ScriptEngine engine =
        new NashornScriptEngineFactory().getScriptEngine();

      File jshintFile = editor.getMode().getContentFile("jshint.js");
      String[] lines = PApplet.loadStrings(jshintFile);
      String preamble = PApplet.join(lines, '\n') + "\n" + FUNCTION_CALL;
      try {
        compiled = ((Compilable) engine).compile(preamble);
        lint("");
      } catch (ScriptException e) {
        e.printStackTrace();  // need better error handling, but...
      }
    }).start();
  }


  JSONArray lint(String code) throws ScriptException {
    if (compiled != null) {
      Object result = compiled.eval(new SimpleBindings(new HashMap<>(Collections.singletonMap("lines", code))));
      if (result instanceof String) {
        return JSONArray.parse((String) result);
      } else {
        System.err.println("result not a string: " + result.getClass().getName());
        System.err.println("result toString() is " + result);
      }
    }
    return null;
  }


  /*
  static Object test(final String scriptLines) throws ScriptException {
    long t1 = System.currentTimeMillis();
    final CompiledScript script = getCompiledScript(getHintLines());
    long t2 = System.currentTimeMillis();
    final String dumbLines = "";
    Object result = script.eval(new SimpleBindings(new HashMap<>(Collections.singletonMap("lines", dumbLines))));
    long t3 = System.currentTimeMillis();
    result = script.eval(new SimpleBindings(new HashMap<>(Collections.singletonMap("lines", dumbLines))));
    long t4 = System.currentTimeMillis();
    result = script.eval(new SimpleBindings(new HashMap<>(Collections.singletonMap("lines", scriptLines))));
    long t5 = System.currentTimeMillis();
    // 554 1078 70 484
    // half second to get set up, one second for first run
    // for p5 sketch, maybe half a second to run
    System.out.println((t2 - t1) + " " + (t3-t2) + " " + (t4-t3) + " " + (t5-t4));

    if (result instanceof String) {
      //JSONObject obj = JSONObject.parse((String) result);
      JSONArray obj = JSONArray.parse((String) result);
      System.out.println(obj.format(2));
    }

    return result;
  }


  private static List<String> getHintLines() {  //throws IOException {
    File file = new File("/Users/fry/coconut/processing-other/p5js-mode/jshint.js");
    String[] lines = PApplet.loadStrings(file);
    return Arrays.asList(lines);
  }


  private static CompiledScript getCompiledScript(final List<String> lines) throws ScriptException {
    final ScriptEngineManager manager = new ScriptEngineManager();
    final ScriptEngine engine = manager.getEngineByMimeType("text/javascript");

    final String fullScript = Stream.concat(lines.stream(), Stream.of(FUNCTION_CALL))
        .collect(Collectors.joining("\n"));
    return ((Compilable) engine).compile(fullScript);
  }
  */
}