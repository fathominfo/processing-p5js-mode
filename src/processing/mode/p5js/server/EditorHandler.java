package processing.mode.p5js.server;

import processing.app.SketchCode;

import javax.swing.text.BadLocationException;
import java.io.*;
import java.nio.charset.StandardCharsets;


public class EditorHandler extends GenericHandler {

  public EditorHandler(HttpServer server) {
    super(server);
  }


  byte[] loadBytes(String path, File target) throws IOException {
    // if this is one of the editor files, send the version on-screen
    // (with any edits) rather than the version on disk.
    for (SketchCode code : server.editor.getSketch().getCode()) {
      if (code.getFileName().equals(path)) {
        if (code.isModified()) {
          String program = null;
          if (code.getDocument() != null) {
            try {
              // if actively editing, use the text from the Document object
              program = code.getDocumentText();
            } catch (BadLocationException ignored) { }
          }
          if (program == null) {
            // fall back to just getting the last code (setProgram() is
            // called in prepareRun() or whenever switching away from a tab)
            program = code.getProgram();
          }
          //code.setProgram(program);
          return program.getBytes(StandardCharsets.UTF_8);
        }
      }
    }

    // no changes, or an error, send the file from disk
    return super.loadBytes(path, target);
  }
}