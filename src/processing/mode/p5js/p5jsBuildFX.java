package processing.mode.p5js;

import java.awt.Dimension;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.w3c.dom.Document;

import processing.app.Sketch;
import processing.app.SketchException;
import processing.app.ui.Editor;


/**
 * Off into crazy-town, running inside an embedded WebKit browser.
 * This is not currently in use: just a misguided stretch of an attempt.
 */
public class p5jsBuildFX {
  static JFrame frame;


  public p5jsBuildFX(final Editor editor, Sketch sketch) throws SketchException {
    if (frame == null) {
      frame = new JFrame();
      final JFXPanel fxPanel = new JFXPanel();
      fxPanel.setSize(new Dimension(600, 600));
      frame.getContentPane().add(fxPanel);

      Platform.runLater(new Runnable() { // this will run initFX as JavaFX-Thread
        @Override
        public void run() {
          initFX(fxPanel);

          frame.pack();
          frame.setResizable(true);
          frame.setVisible(true);
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
//    webView.setMinSize(600, 600);
//    webView.setMaxSize(300, 300);

    // Obtain the webEngine to navigate
    final WebEngine engine = webView.getEngine();

    // http://stackoverflow.com/a/18396900
    engine.documentProperty().addListener(new ChangeListener<Document>() {
      @Override public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
        enableFirebug(engine);
      }
    });
    engine.load("http://www.google.com/");
  }


  /**
   * Enables Firebug Lite for debugging a webEngine.
   * @param engine the webEngine for which debugging is to be enabled.
   */
  private static void enableFirebug(final WebEngine engine) {
    engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
  }
}