package processing.mode.p5js;

import java.io.File;
import java.io.IOException;

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


  static void updateHtml(Sketch sketch) throws SketchException, IOException {
    SketchCode indexHtmlCode = p5jsMode.findIndexHtml(sketch);
    if (indexHtmlCode != null && indexHtmlCode.isModified()) {
      // TODO can we throw an exception here? how often is this happening?
      System.err.println("Could not update index.html because it has unsaved changes.");
      return;
    }

    File sketchFolder = sketch.getFolder();
    StringList insert;
    insert = new StringList();

    // load p5.js first
    insert.append(scriptPath("libraries/" + P5JS_MINIFIED));
    //insert.append(scriptPath("libraries/" + P5JS_UNMINIFIED));

    // then other entries from /libraries
    File librariesFolder = new File(sketchFolder, "libraries");
    File[] libraryList = librariesFolder.listFiles(file -> {
      if (!file.isDirectory()) {  // not doing subdirectories
        String name = file.getName();
        //noinspection RedundantIfStatement
        if (!name.equals(P5JS_MINIFIED) &&  // already loaded first
            !name.equals(P5JS_UNMINIFIED) &&  // don't double-add
            name.toLowerCase().endsWith(".js")) {
          return true;
        }
      }
      return false;
    });

    // if folder is unreadable, show a warning and recover
    if (libraryList == null) {
      System.err.println("Could not read " + librariesFolder);
      libraryList = new File[0];
    }

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
        insert.append(scriptPath(filename));
      }
    }

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
      // Reload the sketch so that the index.html file shows up.
      // But *do not* reload if there are edits to other tabs,
      // otherwise the changes will be lost.
      if (!sketch.isModified()) {
        sketch.reload();
      }
    }

    // write the HTML based on the template
    String[] lines = PApplet.loadStrings(htmlFile);
    if (lines == null) {
      System.err.println("Could not read " + htmlFile);
      lines = new String[0];  // recover; shows more useful error msg below
    }
    String html = PApplet.join(lines, "\n");
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
    html = html.substring(0, start) +
      HTML_PREFIX + "\n" + insert.join("\n") + "\n  " + HTML_SUFFIX +
      html.substring(stop + HTML_SUFFIX.length());
    PApplet.saveStrings(htmlFile, PApplet.split(html, '\n'));


    // reload in the Editor after modifications
    if (indexHtmlCode != null) {
      indexHtmlCode.load();
      // unfortunate hack that seems necessary at the moment?
      indexHtmlCode.setDocument(null);

    //} else {
      // TODO when the index.html file has been removed (and now, rewritten),
      //      add its tab to the PDE, and mark it as opened so that we don't
      //      get the message about the file being modified externally.
    }
  }


  static String scriptPath(String path) {
    return "  <script language=\"javascript\" type=\"text/javascript\" src=\"" + path + "\"></script>";
  }
}
