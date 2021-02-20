package processing.mode.p5js;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import processing.app.*;
import processing.app.syntax.TokenMarker;
import processing.app.ui.*;
import processing.core.PApplet;


public class p5jsMode extends Mode {
  private p5jsEditor jsEditor;


  public p5jsMode (Base base, File folder) {
    super(base, folder);
  }


  /**
   *  Called to create the actual editor when needed (once per Sketch)
   */
  @Override
  public Editor createEditor(Base base, String path,
                             EditorState state) throws EditorException {
    jsEditor = new p5jsEditor(base, path, state, this);
    return jsEditor;
  }


  /**
   *  Called from Base to get the Editor for this mode.
   */
  public Editor getEditor() {
    return jsEditor;
  }


  /*
  public JavaMode getJavaMode() {
    if (defaultJavaMode == null) {
      for (Mode m : base.getModeList() ) {
        if (m.getClass() == JavaMode.class) {
          defaultJavaMode = (JavaMode) m;
          break;
        }
      }
    }
    return defaultJavaMode;
  }
  */


  @Override
  public File[] getKeywordFiles() {
    return new File[] {
      Platform.getContentFile("modes/java/keywords.txt"),
      new File(folder, "keywords.txt")
    };
  }


  @Override
  public TokenMarker getTokenMarker(SketchCode code) {
    String ext = code.getExtension();

    if (ext.equals("js") || ext.equals("json")) {
      return tokenMarker;

    } else if (code.isExtension("html")) {
      return new HtmlTokenMarker();

//    } else if (code.isExtension("css")) {
//      System.out.println("no highlight for " + code.getFile());
//      return null;
    }
    return null;  // no styling
  }


  /**
   *  Return pretty title of this mode for menu listing and such
   */
  @Override
  public String getTitle() {
    return "p5.js";
  }


  // public EditorToolbar createToolbar(Editor editor) { }


  // public Formatter createFormatter() { }


  // public Editor createEditor(Base ibase, String path, int[] location) { }


  /**
   *  Get a list of folders that contain examples, ordered by the way they
   *  should show up in the window or menu.
   */
  @Override
  public File[] getExampleCategoryFolders() {
    final String[] titles = {
      "Structure", "Form", "Data", "Arrays", "Control", "Image", "Color",
      "Math", "Simulate", "Interaction", "Objects", "Lights", "Motion",
      "Instance Mode", "DOM", "Drawing", "Transform", "Typography",
      "3D", "Input", "Advanced Data", "Sound", "Mobile", "Hello P5"
    };

    File[] outgoing = new File[titles.length];
    for (int i = 0; i < titles.length; i++) {
      outgoing[i] = new File(examplesFolder, titles[i]);
    }
    return outgoing;
  }


  @Override
  public void rebuildLibraryList() {
    //super.rebuildLibraryList();

    coreLibraries = new ArrayList<>();
//    Library domLibrary =
//      new p5jsLibrary(new File(getLibrariesFolder(), "p5.dom"));
//    coreLibraries.add(domLibrary);
    Library soundLibrary =
      new p5jsLibrary(new File(getLibrariesFolder(), "p5.sound"));
    coreLibraries.add(soundLibrary);

    // no contribs for now, figure this out later
    contribLibraries = new ArrayList<>();
  }


  @Override
  public boolean requireExampleCompatibility() {
    return true;
  }


  /**
   * Return the default extension for this mode.
   */
  @Override
  public String getDefaultExtension() {
    return "js";
  }


  /**
   * The list of extensions that should show up as tabs.
   */
  @Override
  public String[] getExtensions () {
    return new String[] { "js", "html", "css", "json" };
  }


  /**
   * Return list of file and folder names that should be ignored on Save As.
   * Starting in Processing 3.2, this can return null (otherwise it should be
   * a zero length String array if there's nothing to ignore).
   */
  @Override
  public String[] getIgnorable() {
    return null;
  }


  /**
   * Override handles rewriting index.html with the sketch name.
   */
  @Override
  public File addTemplateFiles(File sketchFolder,
                               String sketchName) throws IOException {
    File mainFile = super.addTemplateFiles(sketchFolder, sketchName);
    buildIndex(sketchFolder, sketchName);
    return mainFile;
  }


  /**
   * Write the index.html file. Broken out for ImportExamples.
   */
  static public void buildIndex(File sketchFolder,
                                String sketchName) throws IOException {
    File indexFile = new File(sketchFolder, "index.html");
    String program = PApplet.join(PApplet.loadStrings(indexFile), "\n");
    program = program.replaceAll("@@sketch@@", sketchName + ".js");
    PApplet.saveStrings(indexFile, PApplet.split(program, '\n'));
  }


  static SketchCode findIndexHtml(Sketch sketch) {
    for (SketchCode code : sketch.getCode()) {
      if (code.getFileName().equals("index.html")) {
        return code;
      }
    }
    return null;
  }


  /*
  public File getTemplateLibrariesFolder() {
    return new File(getTemplateFolder(), "libraries");
  }
  */
}
