package processing.mode.p5js;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import processing.core.PApplet;
import processing.data.StringList;


public class ImportExamples extends PApplet {
  static final String WEB_MASTER =
    "https://github.com/processing/p5.js-website/archive/master.zip";
  static final String EXAMPLE_PREFIX =
    "p5.js-website-master/examples/examples_src/";
  static final String ASSETS_PREFIX =
    "p5.js-website-master/examples/examples/assets/";



  @Override
  public void setup() {
    File masterFile = sketchFile("website-master.zip");
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
      StringList exampleList = new StringList(exampleMap.keySet());
      StringList categories = new StringList();

      for (String item : exampleList) {
        String[] pieces = split(item, '/');
        categories.appendUnique(pieces[0]);
        // remove the number prefix when writing to the folder
        File categoryFolder = new File(examplesFolder, pieces[0].substring(3));
        // remove the .js from the end of the name
        String name = "ex" + pieces[1].substring(pieces[1].length() - 3);
        File exampleFolder = new File(categoryFolder, name);
        if (exampleFolder.mkdirs()) {
          File exampleFile = new File(exampleFolder, name + ".js");
          String[] lines =
            loadStrings(zip.getInputStream(exampleMap.get(item)));
          for (String line : lines) {
            String[] m = match(line, "\"assets/(.*)\"");
            if (m != null) {
              println(m[1]);
              if (assetMap.get(m[1]) == null) {
                System.err.println("could not find " + m[1]);
              }
            }
            saveStrings(exampleFile, lines);
          }
        } else {
          throw new RuntimeException("Couldn't make dir " + exampleFolder);
        }
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
  }


  static public void main(String[] args) {
    PApplet.main(ImportExamples.class);
  }
}