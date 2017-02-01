package processing.mode.p5js;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ECMAException;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.options.Options;
import processing.app.Base;
import processing.app.Platform;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.app.Util;
import processing.core.PApplet;
import processing.data.StringList;


public class p5jsBuild {
  /*
  static final String HTML_PREFIX =
    "<!-- PLEASE NO CHANGES BELOW THIS LINE (UNTIL I SAY SO) -->";
  static final String HTML_SUFFIX =
    "<!-- OK, YOU CAN MAKE CHANGES BELOW THIS LINE AGAIN -->";
  */

  static final String TEMP_PREFIX = "p5js-temp-";
//  ScriptEngine engine;
//  static ScriptEngine engine =
//    new ScriptEngineManager().getEngineByName("javascript");


  public p5jsBuild(Sketch sketch) throws SketchException {
//    updateHTML(sketch);
    //SketchCode[] code = sketch.getCode();
    //for (int i = 0; i < code.length; i++) {
    for (int i = 0; i < sketch.getCodeCount(); i++) {
      handleFile(sketch, i);
    }
  }


  static void updateHtml(Sketch sketch) throws SketchException, IOException {
//    Mode mode = sketch.getMode();

    SketchCode indexCode = findIndex(sketch);
    if (indexCode != null && indexCode.isModified()) {
      // TODO can we throw an exception here? how often is this happening?
      System.err.println("Could not update index.html because it has unsaved changes.");
      return;
    }
//    if (indexCode == null) {
//      throw new SketchException("Could not find index.html file");
//    }

    File sketchFolder = sketch.getFolder();

    cleanTempFiles(sketch);

    // load p5.js first
    //insert.append(scriptPath("libraries/p5.js"));

    // then other entries from /libraries
    File librariesFolder = new File(sketchFolder, "libraries");
    //System.out.println(librariesFolder + " " + librariesFolder.exists());
    //PApplet.launch(librariesFolder.getAbsolutePath());
    //PApplet.printArray(Util.listFiles(librariesFolder, false));
    File[] libraryList = librariesFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        //System.out.println("checking " + file);
        if (!file.isDirectory()) {  // not doing subdirectories
          String name = file.getName();
          if (!name.equals("p5.js") &&  // already loaded first
              name.toLowerCase().endsWith(".js")) {
            return true;
          }
        }
        return false;
      }
    });

    // different ways to do this; let's not be presumptuous that it's
    // sketchName + ".js" and leave ourselves some room for the future.
    //insert.append(scriptPath(sketch.getCode(0).getFileName()));

    File htmlFile = new File(sketchFolder, "index.html");

    // if the template has been removed, rewrite it (simplest fix)
    if (!htmlFile.exists()) {
      // replace with a fresh copy from the template
      File templateFolder = sketch.getMode().getTemplateFolder();
      File htmlTemplate = new File(templateFolder, "index.html");
      try {
        Util.copyFile(htmlTemplate, htmlFile);
      } catch (IOException e) {
        throw new SketchException(e.getMessage());
      }
      // Can't risk breaking the user's sketch if there are edits,
      // but with no modifications, handle the reload to bring back index.html
      if (!sketch.isModified()) {
        sketch.reload();
      }
    }

    Document htmlDoc = Jsoup.parse(htmlFile, "UTF-8");
    StringList scriptsInDoc = new StringList();

    Elements scriptElements = htmlDoc.getElementsByTag("script");
    for (Element scriptElement : scriptElements) {
      if (scriptElement.className().trim().equals("temp")) {
        scriptElement.remove();
      } else {
        scriptsInDoc.append(scriptElement.attr("src"));
      }
    }

    for (File file : libraryList) {
      String filename = "libraries/" + file.getName();
      if (!scriptsInDoc.hasValue(filename)) {
        htmlDoc.head().appendElement("script").attr("language", "javascript").
          attr("type", "text/javascript").attr("src", filename);
      }
    }

    // now the sketch code
    for (int ii = 0; ii < sketch.getCodeCount(); ii++) {
      // start at [1], and write [0] (the main code) to the file last
      int i = (ii + 1) % sketch.getCodeCount();
      SketchCode code =  sketch.getCode(i);
      String filename = code.getFileName();
      if (filename.endsWith(".js")) {
        if (code.isModified()) {
          // write a temporary file instead of the actual one
          String tempPrefix = TEMP_PREFIX + code.getPrettyName();
          File tempFile = File.createTempFile(tempPrefix, ".js", sketchFolder);
          Util.saveFile(code.getProgram(), tempFile);
          filename = tempFile.getName();
        }
        if (!scriptsInDoc.hasValue(filename)) {
          Element e = htmlDoc.head().appendElement("script").
            attr("language", "javascript").
            attr("type", "text/javascript").attr("src", filename);
          if (code.isModified()) {
            e.addClass("temp");
          }
        }
      }
    }

    /*
    // write the HTML based on the template
    String html = PApplet.join(PApplet.loadStrings(htmlFile), "\n");
    int start = html.indexOf(HTML_PREFIX);
    if (start == -1) {
      System.err.println("HTML prefix is missing in the index.html file.");
      throw new SketchException("The index.html file is damaged, " +
                                "please remove it and try again.", false);
    }
    int stop = html.indexOf(HTML_SUFFIX);
    if (stop == -1) {
      System.err.println("HTML suffix is missing in the index.html file.");
      throw new SketchException("Somebody broke the index.html file, " +
                                "please remove it and try again.", false);
    }
    html = html.substring(0, start) +
      HTML_PREFIX + "\n" + insert.join("\n") + "\n  " + HTML_SUFFIX +
      html.substring(stop + HTML_SUFFIX.length());
    PApplet.saveStrings(htmlFile, PApplet.split(html, '\n'));
    */

    //PApplet.saveStrings(htmlFile, PApplet.split(htmlDoc.toString(), '\n'));

    // https://github.com/fathominfo/processing-p5js-mode/issues/13
    htmlDoc.outputSettings().prettyPrint(false);
    String html = htmlDoc.toString().replace("\r\n", "\n");
    PApplet.saveStrings(htmlFile, PApplet.split(html, '\n'));

    // reload in the Editor
    if (indexCode != null) {
      indexCode.load();
      // unfortunate hack that seems necessary at the moment?
      indexCode.setDocument(null);
    }
  }


  static void cleanTempFiles(Sketch sketch) throws IOException {
    // remove any temp files from the last run
    File[] tempList = sketch.getFolder().listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return (!file.isDirectory() &&
                file.getName().startsWith(TEMP_PREFIX));
      }
    });
    // remove these files
    if (tempList != null) {
      for (File tempItem : tempList) {
        try {
          Platform.deleteFile(tempItem);  // move to trash, hopefully
        } catch (IOException e) {
          // Try to just silently catch these and move on
          // https://github.com/fathominfo/processing-p5js-mode/issues/6
          System.err.println(e.getMessage());
        }
      }
    }
  }


  static private SketchCode findIndex(Sketch sketch) {
    for (SketchCode code : sketch.getCode()) {
      if (code.getFileName().equals("index.html")) {
        return code;
      }
    }
    return null;
  }


  static String scriptPath(String path) {
    return "  <script language=\"javascript\" type=\"text/javascript\" src=\"" + path + "\"></script>";
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
    File file = sketch.getCode(codeIndex).getFile();
    if (!file.getName().endsWith(".js") &&
        !file.getName().endsWith(".json")) {
      return;  // nope, not this one
    }

    Options options = new Options("nashorn");
    options.set("anon.functions", true);
    options.set("parse.only", true);
    options.set("scripting", true);

    ErrorManager errors = new ErrorManager();
    Context context = new Context(options, errors, Base.class.getClassLoader());
    Context.setGlobal(context.createGlobal());
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
