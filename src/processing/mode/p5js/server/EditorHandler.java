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
          String program;
          if (code.getDocument() != null) {
            try {
              // if there are changes, update the program text internally
              program = code.getDocumentText();
            } catch (BadLocationException ignored) { }
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