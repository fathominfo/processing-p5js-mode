package processing.mode.p5js;

import java.io.File;
import java.io.IOException;

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
   *  Fetch and return examples
   */
  @Override
  public File[] getExampleCategoryFolders() {
    // find included example subdirs
    File[] inclExamples = examplesFolder.listFiles(new java.io.FileFilter() {
      @Override
      public boolean accept (File f) {
        return f.isDirectory();
      }
    });
    java.util.Arrays.sort(inclExamples);

    // add JavaMode examples as these are supposed to run in JSMode
//    JavaMode jMode = getJavaMode();
//    if ( jMode == null )
      return inclExamples; // js examples only

      /*
    File jExamples = jMode.getContentFile("examples");
    File[] jModeExamples = new File[] {
      new File(jExamples, "Basics"),
      //new File(jExamples, "Topics"),
      //new File(jExamples, "3D") ,
      //new File(jExamples, "Books")
    };

    // merge them all
    File[] finalExamples = new File[inclExamples.length + jModeExamples.length];
    for ( int i = 0; i < inclExamples.length; i++ )
      finalExamples[i] = inclExamples[i];
    for ( int i = 0; i < jModeExamples.length; i++ )
      finalExamples[inclExamples.length+i] = jModeExamples[i];

    java.util.Arrays.sort(finalExamples);

    return finalExamples;
    */
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
    return new String[] { "js", "txt", "html", "css" };
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


  /** Override handles rewriting index.html with the sketch name. */
  @Override
  public File addTemplateFiles(File sketchFolder,
                               String sketchName) throws IOException {
    File mainFile = super.addTemplateFiles(sketchFolder, sketchName);
    File indexFile = new File(sketchFolder, "index.html");
    String program = PApplet.join(PApplet.loadStrings(indexFile), "\n");
    program = program.replaceAll("@@sketch@@", sketchName + ".js");
    PApplet.saveStrings(indexFile, PApplet.split(program, '\n'));
    return mainFile;
  }



  /*
  public File getTemplateLibrariesFolder() {
    return new File(getTemplateFolder(), "libraries");
  }
  */


//  /**
//   *  Build and export a sketch
//   */
//  public boolean handleExport(Sketch sketch) throws IOException, SketchException {
//    new p5jsBuild(sketch);
//    //return new p5jsBuild(sketch).export();
//    return true;
//  }


  //public boolean handleExportApplet(Sketch sketch) throws SketchException, IOException { }

  //public boolean handleExportApplication(Sketch sketch) throws SketchException, IOException { }
}
