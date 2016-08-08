package processing.mode.p5js;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import processing.app.Sketch;
import processing.app.SketchException;
import processing.app.ui.Editor;

public class p5jsBuildFX {
  static Frame frame;


  public p5jsBuildFX(final Editor editor, Sketch sketch) throws SketchException {
    if (frame == null) {
      frame = new JFrame();
      final JFXPanel fxPanel = new JFXPanel();
      fxPanel.setSize(new Dimension(300, 300));
      frame.add(fxPanel);
      frame.pack();
      frame.setResizable(true);
      frame.setVisible(true);

      Platform.runLater(new Runnable() { // this will run initFX as JavaFX-Thread
        @Override
        public void run() {
          initFX(fxPanel);
        }
      });
    }
  }


  private static void initFX(final JFXPanel fxPanel) {
    Group group = new Group();
    Scene scene = new Scene(group);
    fxPanel.setScene(scene);

    WebView webView = new WebView();

    group.getChildren().add(webView);
    webView.setMinSize(300, 300);
    webView.setMaxSize(300, 300);

    // Obtain the webEngine to navigate
    WebEngine webEngine = webView.getEngine();
    webEngine.load("http://www.google.com/");
  }
}