package processing.mode.p5js;

import java.io.File;

import processing.app.*;
import processing.app.ui.*;


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
   *  Return the default extension for this mode, same as Java
   */
  @Override
  public String getDefaultExtension() {
    return "js";
  }


  /**
   *  Return allowed extensions
   */
  @Override
  public String[] getExtensions () {
    return new String[] { "pde", "js" };
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


  public File getTemplateLibrariesFolder() {
    return new File(getTemplateFolder(), "libraries");
  }


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
