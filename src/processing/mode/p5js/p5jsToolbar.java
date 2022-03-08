package processing.mode.p5js;

import processing.app.ui.Editor;
import processing.app.ui.EditorToolbar;


public class p5jsToolbar extends EditorToolbar {

  public p5jsToolbar(Editor editor) {
    super(editor);
  }


  @Override
  public void handleRun(int modifiers) {
    p5jsEditor jsEditor = (p5jsEditor) editor;
    jsEditor.handleRun();
  }


  @Override
  public void handleStop() {
    p5jsEditor jsEditor = (p5jsEditor) editor;
    jsEditor.handleStop();
  }
}
