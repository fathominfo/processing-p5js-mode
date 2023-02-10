package processing.mode.p5js;

import java.io.File;
import java.io.IOException;

import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.app.Util;

import processing.core.PApplet;
import processing.data.StringList;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;


public class p5jsBuild {
  static final String P5JS_UNMINIFIED = "p5.js";
  static final String P5JS_MINIFIED = "p5.min.js";

  static final String HTML_PREFIX =
    "<!-- PLEASE NO CHANGES BELOW THIS LINE (UNTIL I SAY SO) -->";
  static final String HTML_SUFFIX =
    "<!-- OK, YOU CAN MAKE CHANGES BELOW THIS LINE AGAIN -->";


  static void updateHtml(Sketch sketch) throws SketchException {
    File sketchFolder = sketch.getFolder();
    SketchCode indexHtmlCode = p5jsMode.findIndexHtml(sketch);
    if (indexHtmlCode == null) {
      System.err.println("Re-creating the missing index.html file.");
      // If the index.html file has been removed,
      // replace with a fresh copy from the template
      File templateFolder = sketch.getMode().getTemplateFolder();
      File htmlTemplate = new File(templateFolder, "index.html");

      /*
      File htmlFile = new File(sketchFolder, "index.html");
      try {
        Util.copyFile(htmlTemplate, htmlFile);
      } catch (IOException e) {
        throw new SketchException(e.getMessage());
      }
      */
      /*
      // Reload the sketch so that the index.html file shows up.
      // But *do not* reload if there are edits to other tabs,
      // otherwise the changes will be lost.
      if (!sketch.isModified()) {
        sketch.reload();
      }
      */
      // This will copy index.html from the template folder
      // and take care of creating a new tab for it.
      // This will include @@sketch@@, but that's ok,
      // because it will be rewritten immediately below.
      sketch.addFile(htmlTemplate);

      // Get the (formerly missing, now new) tab so we can work with it
      indexHtmlCode = p5jsMode.findIndexHtml(sketch);
    }

//    if (indexHtmlCode != null && indexHtmlCode.isModified()) {
//      // TODO can we throw an exception here? how often is this happening?
//      System.err.println("Could not update index.html because it has unsaved changes.");
//      return;
//    }

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
      //if (filename.endsWith(".js")) {
      if (code.isExtension("js")) {
        insert.append(scriptPath(filename));
      }
    }

    /*
    // write the HTML based on the template
    String[] lines = PApplet.loadStrings(htmlFile);
    if (lines == null) {
      System.err.println("Could not read " + htmlFile);
      lines = new String[0];  // recover; shows more useful error msg below
    }
    String html = PApplet.join(lines, "\n");
    */

    String html = null;
    if (indexHtmlCode.getDocument() != null) {
      try {
        // If actively editing, use the text from the Document object.
        html = indexHtmlCode.getDocumentText();
      } catch (BadLocationException ignored) { }
    }
    if (html == null) {
      // If there was an error, or the Document object does not yet exist
      // (because the tab has not been visited), fall back to getProgram().
      // It will have the last version loaded from disk, or from setProgram()
      // being called by prepareRun() or when switching away from a tab.
      html = indexHtmlCode.getProgram();
    }

    int start = html.indexOf(HTML_PREFIX);
    int stop = html.indexOf(HTML_SUFFIX);

    if (start == -1 || stop == -1) {
      System.out.println("p5jsMode uses a specially crafted index.html to work properly.");
      System.out.println("Use Sketch → Show Sketch Folder and rename index.html to something else.");
      System.out.println("The index.html file will be automatically re-created.");
      System.out.println("If there are edits you need from the renamed file,");
      System.out.println("copy and paste those parts into the new index.html.");
      throw new SketchException("The index.html file is damaged, " +
                                "please remove or rename it and try again.", false);
    }

    // stop point is after the suffix, but need it to be -1 up top there
    stop += HTML_SUFFIX.length();

    final String newInsert =
      HTML_PREFIX + "\n" + insert.join("\n") + "\n  " + HTML_SUFFIX;

    if (indexHtmlCode.getDocument() != null) {
      // If the tab has been opened at least once, work on the Document object
      try {
        Document doc = indexHtmlCode.getDocument();
        String oldInsert = doc.getText(start, stop - start);
        if (!oldInsert.equals(newInsert)) {
          doc.remove(start, stop - start);
          doc.insertString(start, newInsert, null);
          indexHtmlCode.setModified(true);
        }
      } catch (BadLocationException ble) {
        ble.printStackTrace();
        throw new SketchException("Error while updating index.html");
      }
    }
    // XX Tab has not been visited, so no Document object available yet.
    // XX Use setProgram() to set the text without doing a save.
    // Need to call setProgram() regardless, so that Save As will work.
    String newHtml =
      html.substring(0, start) + newInsert + html.substring(stop);
    if (!newHtml.equals(html)) {
      indexHtmlCode.setProgram(newHtml);
      indexHtmlCode.setModified(true);
    }

    /*
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
    */
  }


  static String scriptPath(String path) {
    return "  <script language=\"javascript\" type=\"text/javascript\" src=\"" + path + "\"></script>";
  }
}
