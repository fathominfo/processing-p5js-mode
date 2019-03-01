package processing.mode.p5js;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

// Nashorn invocation based on code from: https://github.com/wfhartford/parallel-nashorn-jshint/blob/master/src/main/java/ca/cutterslade/parnashjsh/ParallelNashornJSHint.java
public final class Linter {
//  private static final int REPEAT_COUNT = 1;
  //static final String OPTIONS = "{es3: false," +
  static final String OPTIONS = "{" +
      "esversion: 6," +
//      "es3: false," +
//      "boss: true," +  // this would suppress "a = a" when "a == a" was intended
      "browser: true," +  // enable browser globals (document, navigator, FileReader)
      "curly: true," +  // require curly braces around blocks
      "eqnull: true," +  // ok to check if something == null
      "evil: true," +   // allows eval()
//      "immed: true," +  // this is a style issue and has been deprecated
      "jquery: true," +  // turn on jquery globals
      "loopfunc: true," +  // warn about functions defined inside loops
//      "multistr: true," +  // another style issue
//      "newcap: true," +  // yet another style issue
      "noarg: true," +  // prohibits the use of arguments.caller and arguments.callee
//      "noempty: true," +  // warns on empty blocks (deprecated)
//      "onecase: true," +  // not in the current reference?
//      "sub: true," +  // suppress warnings about using person['name'] when person.name would work (deprecated)
//      "trailing: true" +  // doesn't appear to exist, replacing with trailingcomma
      "trailingcomma: true" +
  "}";
  //private static final String FUNCTION_CALL = "JSHINT(lines, null, " + OPTIONS + ")";

  // or use polyfill from 4.1.2: https://www.n-k.de/riding-the-nashorn/
  //private static final String FUNCTION_CALL = "JSHINT(lines, null, " + OPTIONS + "); console.log(JSHINT.errors);";
  //private static final String FUNCTION_CALL = "JSHINT(lines, null, " + OPTIONS + "); print(JSHINT.errors);";
  //private static final String FUNCTION_CALL = "JSHINT(lines, null, " + OPTIONS + "); JSHINT.errors;";
//  private static final String FUNCTION_CALL = "JSHINT(lines, null, " + OPTIONS + "); print(JSON.stringify(JSHINT.errors));";
//  private static final String FUNCTION_CALL = "JSHINT(lines, null, " + OPTIONS + "); JSON.stringify(JSHINT.errors);";
  private static final String FUNCTION_CALL = "JSHINT(lines, " + OPTIONS + "); JSON.stringify(JSHINT.errors);";
//  private static final AtomicLong COMPLETION_COUNT = new AtomicLong();

//  static public void test(String code, String jshint) throws ScriptException {
//    final String scriptLines = jshint +
//    result = script.eval(new SimpleBindings(new HashMap<>(Collections.singletonMap("lines", scriptLines))));
//  }

  static Object test(final String scriptLines) throws ScriptException {
    //final List<String> lines = getHintLines();
    //final CompiledScript script = getCompiledScript(lines);
    long t1 = System.currentTimeMillis();
    final CompiledScript script = getCompiledScript(getHintLines());
    long t2 = System.currentTimeMillis();
//    final List<String> hugeLines = new ArrayList<>(lines.size() * REPEAT_COUNT);
//    for (int i = 0; i < REPEAT_COUNT; i++) {
//      hugeLines.addAll(lines);
//    }
//    final List<String> hundredLines = Collections.unmodifiableList(hugeLines);
//    final long startTime = System.nanoTime();
//    Stream.generate(() -> hundredLines).parallel()
//        .map(scriptLines -> {
//          Object result = null;
//          try {
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
//          }
//          catch (ScriptException e) {
//            System.out.println("!!! Exception Thrown");
//          }
//          return result;
//        })
//        .forEach(ignore -> {
//          final long count = COMPLETION_COUNT.incrementAndGet();
//          final long sinceStart = System.nanoTime() - startTime;
//          final double secondsSinceStart = sinceStart / (double) TimeUnit.SECONDS.toNanos(1);
//          final double rate = count / secondsSinceStart;
//          System.out.println("Completed execution number " + count + " at " + rate + "/sec");
//        });
  }

  /*
  public static void main(final String[] args) throws IOException, ScriptException {
    final List<String> lines = getHintLines();
    final CompiledScript script = getCompiledScript(lines);
    final List<String> hugeLines = new ArrayList<>(lines.size() * REPEAT_COUNT);
    for (int i = 0; i < REPEAT_COUNT; i++) {
      hugeLines.addAll(lines);
    }
    final List<String> hundredLines = Collections.unmodifiableList(hugeLines);
    final long startTime = System.nanoTime();
    Stream.generate(() -> hundredLines).parallel()
        .map(scriptLines -> {
          Object result = null;
          try {
            result = script.eval(new SimpleBindings(new HashMap<>(Collections.singletonMap("lines", scriptLines))));
          }
          catch (ScriptException e) {
            System.out.println("!!! Exception Thrown");
          }
          return result;
        })
        .forEach(ignore -> {
          final long count = COMPLETION_COUNT.incrementAndGet();
          final long sinceStart = System.nanoTime() - startTime;
          final double secondsSinceStart = sinceStart / (double) TimeUnit.SECONDS.toNanos(1);
          final double rate = count / secondsSinceStart;
          System.out.println("Completed execution number " + count + " at " + rate + "/sec");
        });
  }
  */

  private static List<String> getHintLines() {  //throws IOException {
    File file = new File("/Users/fry/coconut/processing-other/p5js-mode/jshint.js");
    String[] lines = PApplet.loadStrings(file);
    return Arrays.asList(lines);
    /*
    final URL resource = HintTest.class.getResource("jshint.js");
    try (final InputStream stream = resource.openStream();
         final InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         final BufferedReader buffered = new BufferedReader(reader)) {
      final List<String> list = new ArrayList<>();
      for (String line = buffered.readLine(); null != line; line = buffered.readLine()) {
        list.add(line);
      }
      return Collections.unmodifiableList(list);
    }
    */
  }

  private static CompiledScript getCompiledScript(final List<String> lines) throws ScriptException {
    final ScriptEngineManager manager = new ScriptEngineManager();
    final ScriptEngine engine = manager.getEngineByMimeType("text/javascript");

    final String fullScript = Stream.concat(lines.stream(), Stream.of(FUNCTION_CALL))
        .collect(Collectors.joining("\n"));
    return ((Compilable) engine).compile(fullScript);
  }
}