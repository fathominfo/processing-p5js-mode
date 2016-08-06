/**
 *	JS Mode for Processing based on Processing.js. Comes with a server as
 *	replacement for the normal runner.
 *
 *	This used to be part of Processing 2.0 beta and was
 *	moved out on 2013-02-25
 */

package processing.mode.p5js;

import java.io.File;
import java.io.IOException;

import processing.app.*;
import processing.app.ui.*;


public class p5jsMode extends Mode {

	private p5jsEditor jsEditor;
//	private JavaMode defaultJavaMode;


	public p5jsMode (Base base, File folder) {
		super(base, folder);
	}


	/**
	 *	Called to create the actual editor when needed (once per Sketch)
	 */
	public Editor createEditor (Base base, String path, EditorState state)
		throws EditorException {

		jsEditor = new p5jsEditor( base, path, state, this );
		return jsEditor;
	}


	/**
	 *	Called from Base to get the Editor for this mode.
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


  public File[] getKeywordFiles() {
    return new File[] {
      Platform.getContentFile("modes/java/keywords.txt"),
      new File(folder, "keywords.txt")
    };
  }


	/**
	 *	Return pretty title of this mode for menu listing and such
	 */
	public String getTitle() {
		return "p5.js";
	}


  // public EditorToolbar createToolbar(Editor editor) { }


  // public Formatter createFormatter() { }


  // public Editor createEditor(Base ibase, String path, int[] location) { }


  /**
   *	Fetch and return examples
   */
	public File[] getExampleCategoryFolders() {
	  // find included example subdirs
	  File[] inclExamples = examplesFolder.listFiles(new java.io.FileFilter() {
	    public boolean accept (File f) {
	      // only the subfolders
	      return f.isDirectory();
	    }
	  });
	  java.util.Arrays.sort(inclExamples);

	  // add JavaMode examples as these are supposed to run in JSMode
//	  JavaMode jMode = getJavaMode();
//	  if ( jMode == null )
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
	 *	Return the default extension for this mode, same as Java
	 */
	public String getDefaultExtension() {
		//return "pde";  // [fry 160510]
	  return "js";
	}


	/**
	 *	Return allowed extensions
	 */
	public String[] getExtensions () {
		return new String[] { "pde", "js" };
	}


	/**
	 *	Return list of file- / folder-names that should be ignored when
	 *	sketch is being copied or saved as
	 */
	public String[] getIgnorable() {
		return new String[] {
			"applet",
			"applet_js",
			p5jsBuild.EXPORTED_FOLDER_NAME
		};
	}


	/**
	 *	Override Mode.getLibrary to add our own discovery of JS-only libraries.
	 *	fjenett 20121202
	 */
	public Library getLibrary(String pkgName) throws SketchException {
		return super.getLibrary(pkgName);
	}


	/**
	 *	Build and export a sketch
	 */
	public boolean handleExport(Sketch sketch) throws IOException, SketchException {
		return new p5jsBuild(sketch).export();
	}


	//public boolean handleExportApplet(Sketch sketch) throws SketchException, IOException { }

	//public boolean handleExportApplication(Sketch sketch) throws SketchException, IOException { }
}
