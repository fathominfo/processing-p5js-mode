package processing.mode.p5js;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import processing.app.Platform;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.app.Util;

import processing.core.PApplet;
import processing.data.StringList;


public class p5jsBuild {
  static final String P5JS_UNMINIFIED = "p5.js";
  static final String P5JS_MINIFIED = "p5.min.js";

  static final String HTML_PREFIX =
    "<!-- PLEASE NO CHANGES BELOW THIS LINE (UNTIL I SAY SO) -->";
  static final String HTML_SUFFIX =
    "<!-- OK, YOU CAN MAKE CHANGES BELOW THIS LINE AGAIN -->";

  static final boolean USE_SOUP = false;
  static final boolean USE_LINTER = true;

  static final String TEMP_PREFIX = "p5js-temp-";
//  ScriptEngine engine;
//  static ScriptEngine engine =
//    new ScriptEngineManager().getEngineByName("javascript");


  public p5jsBuild(Sketch sketch) throws SketchException {
    if (USE_LINTER) {

    } else {
      // otherwise just use the basic Nashorn parser as in 1.1 and earlier
      NashornParse.handle(sketch);
    }
  }


  static void updateHtml(Sketch sketch) throws SketchException, IOException {
//    Mode mode = sketch.getMode();

    SketchCode indexHtmlCode = findIndexHtml(sketch);
    if (indexHtmlCode != null && indexHtmlCode.isModified()) {
      // TODO can we throw an exception here? how often is this happening?
      System.err.println("Could not update index.html because it has unsaved changes.");
      return;
    }
//    if (indexCode == null) {
//      throw new SketchException("Could not find index.html file");
//    }

    File sketchFolder = sketch.getFolder();
    StringList insert;
    if (!USE_SOUP) {
      insert = new StringList();
    }

    cleanTempFiles(sketch);

    // load p5.js first
    if (!USE_SOUP) {
      insert.append(scriptPath("libraries/" + P5JS_MINIFIED));
      //insert.append(scriptPath("libraries/" + P5JS_UNMINIFIED));
    }

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
          if (!name.equals(P5JS_MINIFIED) &&  // already loaded first
              !name.equals(P5JS_UNMINIFIED) &&  // don't double-add
              name.toLowerCase().endsWith(".js")) {
            return true;
          }
        }
        return false;
      }
    });

    if (!USE_SOUP) {
      for (File file : libraryList) {
        insert.append(scriptPath("libraries/" + file.getName()));
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
          insert.append(scriptPath(filename));
        }
      }
    }
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

    Document htmlDoc;
    if (USE_SOUP) {
      htmlDoc = Jsoup.parse(htmlFile, "UTF-8");
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
          htmlDoc.head().appendElement("script")
                        .attr("language", "javascript")
                        .attr("type", "text/javascript")
                        .attr("src", filename);
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
    }

    if (!USE_SOUP) {
      // write the HTML based on the template
      String html = PApplet.join(PApplet.loadStrings(htmlFile), "\n");
      int start = html.indexOf(HTML_PREFIX);
      int stop = html.indexOf(HTML_SUFFIX);

      if (start == -1 || stop == -1) {
        System.out.println("p5jsMode uses a specially crafted index.html to work properly.");
        System.out.println("Use Sketch > Show Sketch Folder and rename index.html to something else.");
        System.out.println("The index.html file will be re-created to work with p5jsMode,");
        System.out.println("and if necessary, copy parts of your old index.html to the new one.");
        throw new SketchException("The index.html file is damaged, " +
                                  "please remove or rename it and try again.", false);
      }
      /*
      if (stop == -1) {
        System.err.println("HTML suffix is missing in the index.html file.");
        throw new SketchException("Somebody broke the index.html file, " +
                                  "please remove it and try again.", false);
      }
      */
      html = html.substring(0, start) +
        HTML_PREFIX + "\n" + insert.join("\n") + "\n  " + HTML_SUFFIX +
        html.substring(stop + HTML_SUFFIX.length());
      PApplet.saveStrings(htmlFile, PApplet.split(html, '\n'));
    }

    if (USE_SOUP) {
      // https://github.com/fathominfo/processing-p5js-mode/issues/13
      htmlDoc.outputSettings().prettyPrint(false);

      // Replace CRLF with just LF, then split so saveStrings() works
      String html = htmlDoc.toString().replace("\r\n", "\n");
      PApplet.saveStrings(htmlFile, PApplet.split(html, '\n'));
    }

    // reload in the Editor after modifications
    if (indexHtmlCode != null) {
      indexHtmlCode.load();
      // unfortunate hack that seems necessary at the moment?
      indexHtmlCode.setDocument(null);

    } else {
      // TODO when the index.html file has been removed (and now, rewritten),
      //      add its tab to the PDE, and mark it as opened so that we don't
      //      get the message about the file being modified externally.
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


  static SketchCode findIndexHtml(Sketch sketch) {
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
}
