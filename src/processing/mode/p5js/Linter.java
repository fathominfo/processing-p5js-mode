package ca.cutterslade.parnashjsh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

public final class ParallelNashornJSHint {
  private static final int REPEAT_COUNT = 1;
  private static final String OPTIONS = "{es3: false," +
      "boss: true," +
      "browser: true," +
      "curly: true," +
      "eqnull: true," +
      "evil: true," +
      "immed: true," +
      "jquery: true," +
      "loopfunc: true," +
      "multistr: true," +
      "newcap: true," +
      "noarg: true," +
      "noempty: true," +
      "onecase: true," +
      "sub: true," +
      "trailing: true}";
  private static final String FUNCTION_CALL = "JSHINT(lines, null, " + OPTIONS + ")";
  private static final AtomicLong COMPLETION_COUNT = new AtomicLong();

  public static void main(final String[] args) throws IOException, ScriptException {
    final List<String> lines = getScriptLines();
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

  private static List<String> getScriptLines() throws IOException {
    final URL resource = ParallelNashornJSHint.class.getResource("jshint.js");
    try (final InputStream stream = resource.openStream();
         final InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         final BufferedReader buffered = new BufferedReader(reader)) {
      final List<String> list = new ArrayList<>();
      for (String line = buffered.readLine(); null != line; line = buffered.readLine()) {
        list.add(line);
      }
      return Collections.unmodifiableList(list);
    }
  }

  private static CompiledScript getCompiledScript(final List<String> lines) throws ScriptException {
    final ScriptEngineManager manager = new ScriptEngineManager();
    final ScriptEngine engine = manager.getEngineByMimeType("text/javascript");

    final String fullScript = Stream.concat(lines.stream(), Stream.of(FUNCTION_CALL))
        .collect(Collectors.joining("\n"));
    return ((Compilable) engine).compile(fullScript);
  }
}
