package processing.mode.p5js;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import processing.app.ui.Editor;
import processing.app.ui.EditorToolbar;


public class p5jsToolbar extends EditorToolbar {
  static protected final int RUN    = 0;
  static protected final int STOP   = 1;

  static protected final int NEW    = 2;
  static protected final int OPEN   = 3;
  static protected final int SAVE   = 4;
  static protected final int EXPORT = 5;


  public p5jsToolbar(Editor editor) {
    super(editor);
  }


  public void init() { }


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


  public void handlePressed(MouseEvent e, int index) {
    switch (index) {

    case RUN:
      handleRun(e.getModifiers());
      break;

    case STOP:
      handleStop();
      break;

    case OPEN:
      JPopupMenu popup = editor.getMode().getToolbarMenu().getPopupMenu();
      popup.show(this, e.getX(), e.getY());
      break;

    case NEW:
      base.handleNew();
      break;

    case SAVE:
      editor.handleSave(false);
      break;

      /*
    case EXPORT:
      jsEditor.handleExport( true );
      break;
      */
    }
  }


  static public String getTitle (int index, boolean shift) {
    switch (index) {
    case RUN:    return "Start server";
    case STOP:   return "Stop server";
    case NEW:    return "New";
    case OPEN:   return "Open";
    case SAVE:   return "Save";
    case EXPORT: return "Export for Web";
    }
    return null;
  }
}
