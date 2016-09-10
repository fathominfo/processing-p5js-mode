package processing.mode.p5js.build;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import processing.app.Platform;
import processing.app.Util;
import processing.core.PApplet;
import processing.data.StringList;
import processing.mode.p5js.p5jsMode;


public class ImportExamples extends PApplet {
  static final String WEB_MASTER =
    "https://github.com/processing/p5.js-website/archive/master.zip";
  static final String EXAMPLE_PREFIX =
    "p5.js-website-master/examples/examples_src/";
  static final String ASSETS_PREFIX =
    "p5.js-website-master/examples/examples/assets/";



  @Override
  public void setup() {
    File masterFile = sketchFile("examples-master.zip");
    if (!masterFile.exists()) {
      println("Downloading " + WEB_MASTER);
      if (!saveStream(masterFile, WEB_MASTER)) {
        System.err.println("Error while downloading");
        masterFile.delete();
        System.exit(0);
      }
      println("Finished.");
    }

    Map<String, ZipEntry> assetMap = new HashMap<>();
    Map<String, ZipEntry> exampleMap = new HashMap<>();

    // p5.js-website-master/examples/examples_src/33_Sound/00_Load_and_Play_Sound.js
    ZipFile zip = null;
    try {
      zip = new ZipFile(masterFile);
      assetMap = new HashMap<String, ZipEntry>();
      Enumeration<?> en = zip.entries();
      while (en.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) en.nextElement();
        String path = entry.getName();
        if (path.startsWith(EXAMPLE_PREFIX) && path.endsWith(".js")) {
          //entries.put(entry.getName(), entry);
          //exampleList.append(path);
          String name = path.substring(EXAMPLE_PREFIX.length());
          exampleMap.put(name, entry);
        } else if (path.startsWith(ASSETS_PREFIX)) {
          String name = path.substring(ASSETS_PREFIX.length());
          assetMap.put(name, entry);
        }
      }

      File examplesFolder = sketchFile("examples");
      if (examplesFolder.exists()) {
        Platform.deleteFile(examplesFolder);  // move to trash
      }

      StringList exampleList = new StringList(exampleMap.keySet());
      StringList categories = new StringList();

      File templateFolder = sketchFile("template");

      for (String item : exampleList) {
        println(item);
        String[] pieces = split(item, '/');
        // keep the numbers on here so that we can get the order
        categories.appendUnique(pieces[0]);
        // remove the number prefix when writing to the folder
        File categoryFolder = new File(examplesFolder, fixCategory(pieces[0]));
        // remove the .js from the end of the name
        String name = "ex" + pieces[1].substring(0, pieces[1].length() - 3);
        File exampleFolder = new File(categoryFolder, name);
        if (exampleFolder.mkdirs()) {
          File exampleFile = new File(exampleFolder, name + ".js");
          String[] lines =
            loadStrings(zip.getInputStream(exampleMap.get(item)));
          //for (String line : lines) {
          StringList libraries = new StringList();
          for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // the sketches that need these libraries mention them in the comments
            if (line.contains("p5.dom")) {
              libraries.appendUnique("p5.dom.js");
            }
            if (line.contains("p5.sound")) {
              libraries.appendUnique("p5.sound.js");
            }

            String[] m = match(line, "[\"']assets/(.+)[\"']");
            if (m != null) {
//              println(m[1].length());
              // Switch to using the data folder so that it
              // plays nicely with the PDE's "Add File" command.
              lines[i] = line.replace("assets/" + m[1], "data/" + m[1]);

              ZipEntry entry = assetMap.get(m[1]);
              if (entry != null) {
                // saveStream() will create intermediate folders as necessary
                saveStream(new File(exampleFolder, "data/" + m[1]),
                           zip.getInputStream(entry));
              } else {
                boolean found = false;
                for (String ext : new String[] { ".mp3", ".ogg" }) {
                  entry = assetMap.get(m[1] + ext);
                  if (entry != null) {
                    saveStream(new File(exampleFolder, "data/" + m[1] + ext),
                               zip.getInputStream(entry));
                    found = true;
                  }
                }
                if (!found) {
                  System.err.println(item + " could not find " + m[1]);
                  System.err.println("  " + line);
                }
              }
            }
            saveStrings(exampleFile, lines);

            // Add index.html and the p5js library itself
            Util.copyDir(templateFolder, exampleFolder);
            // now for some libraries
            File librariesFolder = sketchFile("libraries");
            for (String library : libraries) {
              Util.copyFile(new File(librariesFolder, library),
                            new File(exampleFolder, "libraries/" + library));
            }
            // won't be needing this one
            new File(exampleFolder, "sketch.js").delete();
            p5jsMode.buildIndex(exampleFolder, name);
          }
        } else {
          throw new RuntimeException("Couldn't make dir " + exampleFolder);
        }
      }

      println();
      println("Example categories to be added to p5jsMode.java:");
      categories.sort();
      //println("\"" + categories.join("\", \"") + "\"");
      for (String category : categories) {
        println("\"" + fixCategory(category) + "\", ");
      }

    } catch (IOException e) {
      e.printStackTrace();

    } finally {
      try {
        zip.close();
      } catch (Exception e) {
        // might be an NPE if zip not initialized
        // or an IOException from calling close()
      }
    }
    exit();
  }


  private String fixCategory(String what) {
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