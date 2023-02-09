package processing.mode.p5js.build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import processing.app.Platform;
import processing.app.Util;
import processing.core.PApplet;
import processing.data.StringList;
import processing.mode.p5js.p5jsMode;


// initial setup

// git clone git@github.com:processing/p5.js-website.git
// cd p5.js-website.git
// npm install
// npm run watch

// subsequent updates
// cd p5.js-website.git
// git pull
// additional modules may be in use
// npm install
// npm run watch

// the /examples subdirectory is ignored in git,
// so seeing changes with 'git status' won't be possible

public class ImportExamples extends PApplet {
  static final String EXAMPLE_PREFIX =
    "dist/assets/examples/en/";
  static final String ASSETS_PREFIX =
    "dist/assets/examples/assets/";


  @Override
  public void setup() {
    File siteFolder = sketchFile("p5.js-website");
    if (!siteFolder.exists()) {
      System.err.println("You must first clone p5.js-website before running");
      System.exit(1);
    }
    if (!new File(siteFolder, EXAMPLE_PREFIX).exists()) {
      System.err.println("First run 'npm run watch' inside 'p5.js-website'");
      System.exit(1);
    }

    Map<String, File> assetMap = new HashMap<>();
    Map<String, File> exampleMap = new HashMap<>();

    try {
      String[] paths =
        listPaths(siteFolder.getAbsolutePath(), "recursive", "relative");
      for (String path : paths) {
        if (path.startsWith(EXAMPLE_PREFIX) && path.endsWith(".js")) {
          String name = path.substring(EXAMPLE_PREFIX.length());
          exampleMap.put(name, new File(siteFolder, path));
        } else if (path.startsWith(ASSETS_PREFIX)) {
          String name = path.substring(ASSETS_PREFIX.length());
          assetMap.put(name, new File(siteFolder, path));
        }
      }

      File examplesFolder = sketchFile("examples");
      if (examplesFolder.exists()) {
        Platform.deleteFile(examplesFolder);  // move to trash
      }

      StringList exampleList = new StringList(exampleMap.keySet());
      exampleList.sort();
      StringList categories = new StringList();

      File templateFolder = sketchFile("template");

      for (String item : exampleList) {
        System.out.println(item);
        String[] pieces = PApplet.split(item, '/');
        // keep the numbers on here so that we can get the order
        categories.appendUnique(pieces[0]);
        // remove the number prefix when writing to the folder
        File categoryFolder = new File(examplesFolder, fixCategory(pieces[0]));
        // remove the .js from the end of the name
        String name = "ex" + pieces[1].substring(0, pieces[1].length() - 3);
        File exampleFolder = new File(categoryFolder, name);
        if (exampleFolder.mkdirs()) {
          File exampleFile = new File(exampleFolder, name + ".js");
          String[] lines = loadStrings(exampleMap.get(item));
          //for (String line : lines) {
          StringList libraries = new StringList();
          for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // sketches that need these libraries mention 'em in the comments
            if (line.contains("p5.dom")) {
              libraries.appendUnique("p5.dom");
            }
            if (line.contains("p5.sound")) {
              libraries.appendUnique("p5.sound");
            }

            //String[] m = PApplet.match(line, "[\"']assets/(.+)[\"']");
            String[] m = PApplet.match(line, "[\"']assets/([^\"']+)");
            if (m != null) {
              // Switch to using the data folder so that it
              // plays nicely with the PDE's "Add File" command.
              lines[i] = line.replace("assets/" + m[1], "data/" + m[1]);

              //ZipEntry entry = assetMap.get(m[1]);
              File entry = assetMap.get(m[1]);
              if (entry != null) {
                // saveStream() will create intermediate folders as necessary
                InputStream in = createInput(entry);
                PApplet.saveStream(new File(exampleFolder, "data/" + m[1]), in);
                in.close();
              } else {
                boolean found = false;
                for (String ext : new String[] { ".mp3", ".ogg" }) {
                  entry = assetMap.get(m[1] + ext);
                  if (entry != null) {
                    InputStream in = createInput(entry);
                    PApplet.saveStream(new File(exampleFolder, "data/" + m[1] + ext), in);
                    in.close();
                    found = true;
                  }
                }
                if (!found) {
                  System.err.println(item + " could not find " + m[1]);
                  System.err.println("  " + line);
                }
              }
            }
            PApplet.saveStrings(exampleFile, lines);

            // Add index.html and the p5js library itself
            Util.copyDir(templateFolder, exampleFolder);
            // now for some libraries
            File librariesFolder = sketchFile("libraries");
            for (String library : libraries) {
              // libraries/p5.dom/library/p5.dom.js
              if (!library.equals("p5.dom")) {
                Util.copyFile(new File(librariesFolder, library +
                                       "/library/" + library + ".min.js"),
                              new File(exampleFolder,
                                       "libraries/" + library + ".min.js"));
              }
            }
            // won't be needing this one
            new File(exampleFolder, "sketch.js").delete();
            p5jsMode.insertSketchName(exampleFolder, name);
          }
        } else {
          throw new RuntimeException("Couldn't make dir " + exampleFolder);
        }
      }

      System.out.println();
      System.out.println("Example categories for p5jsMode.java:");
      categories.sort();
      for (String category : categories) {
        System.out.print("\"" + fixCategory(category) + "\", ");
      }
      System.out.println();
      exit();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  static private String fixCategory(String what) {
    what = what.substring(3);
    if ("Dom".equals(what)) {
      // consistent with example title and other folder naming
      return "DOM";
    }
    return what.replace('_', ' ');
  }


  static public void main(String[] args) {
    PApplet.main(ImportExamples.class);
  }
}