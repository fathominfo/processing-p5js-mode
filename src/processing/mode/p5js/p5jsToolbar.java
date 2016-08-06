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


  static public String getTitle ( int index, boolean shift )
  {
    switch (index)
    {
	    case RUN:    return "Start server";
      case STOP:   return "Stop server";
      case NEW:    return "New";
      case OPEN:   return "Open";
      case SAVE:   return "Save";
      case EXPORT: return "Export for Web";
    }
    return null;
  }


  public p5jsToolbar ( Editor editor )
  {
    super(editor);
  }


  public void init ()
  {
    // Image[][] images = loadImages();
    // for (int i = 0; i < 6; i++)
    // {
    //   addButton( getTitle(i, false), getTitle(i, true), images[i], i == NEW );
    // }
  }


  @Override
  public void handleRun(int modifiers) {
    p5jsEditor jsEditor = (p5jsEditor) editor;
    jsEditor.handleStartServer();
  }


  @Override
  public void handleStop() {
    p5jsEditor jsEditor = (p5jsEditor) editor;
    jsEditor.handleStopServer();
  }


  public void handlePressed (MouseEvent e, int index) {
    p5jsEditor jsEditor = (p5jsEditor) editor;

    switch (index) {

	case RUN:
		jsEditor.handleStartServer();
		break;

	case STOP:
		jsEditor.handleStopServer();
		break;

    case OPEN:
      JPopupMenu popup = editor.getMode().getToolbarMenu().getPopupMenu();
      popup.show(this, e.getX(), e.getY());
      break;

    case NEW:
//      if (shift) {
      base.handleNew();
//      } else {
//        base.handleNewReplace();
//      }
      break;

    case SAVE:
      jsEditor.handleSave(false);
      break;

    case EXPORT:
      jsEditor.handleExport( true );
      break;
    }
  }
}
