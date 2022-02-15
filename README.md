## p5.js Mode for Processing

A simple editor for [p5.js](https://p5js.org) code that runs inside the PDE.

Starting in 2022, Processing 4.0 is required. Major changes in Java 17 make backwards compatibility difficult, and Processing 3 was last updated in January 2020.


### Goals/Criteria

* A simple way to get started with p5.js, while making use of the editing facilities of the PDE and its Sketchbook for organizing projects.
* A simple editor that allows offline use.
* Make use of the PDE being installed in many schools and labs, and have a Mode that’s easy to install from inside the PDE (no download, unzip, install process).
* A way for [us](https://fathom.info) to support [courses](https://infodesign.mit.edu) we are [teaching](https://learn.fathom.info).
* Sketches should always be runnable directly from the folder: no need to Export or otherwise prepare a Sketch.
* This is not an official [Processing Foundation](https://github.com/processing) project, no matter what you might know about its [primary author](https://github.com/benfry).


### Details

This Mode is not designed for flexibility. It's also not a way to teach people how to do “proper” JavaScript development any more than the PDE is a way to teach people Java. Like all things in the PDE, we're removing features in an attempt to simplify the process of getting started quickly, creating projects at the simpler end of the scale, and maximizing the ability to try ideas rapidly.

The tradeoffs below represent the best solution based on the goals and criteria above. Of course, these are subject to change if/when we realize mistakes.

* Like the usual Java Mode in Processing, the main code is found in a file with the same name as the sketch. If you need more flexibility, this Mode isn't for you.
* An `index.html` file is created in each new sketch folder. It contains a section where each `.js` file from the sketch is added automatically. Removing this block of code (it’s clearly marked in the file) will cause the sketch to no longer run inside the PDE.
* If you run into trouble, remove the `index.html` file, which will reset it to the version from the template.
* Add library files or additional code to the `libraries` subfolder of the sketch. References to that code will automatically be added to the HTML file, though the libraries won’t be visible as tabs in the Editor.
* Like everything else in the PDE, this uses the `data` folder (unlike many p5js examples which use an `assets` folder). Because sketches must specify `assets` in the path, it's just as easy to do that as to specify `data` instead (rather than rewrite file handling in the PDE).
* As with the rest of Processing, if you outgrow this setup, you should use another IDE or development solution (like a full-featured programmer's text editor and similar tools). We have no interest in creating a JavaScript IDE. Also like the rest of Processing, we *want* people to outgrow this setup.

This project started from Florian Jenett's [JavaScript Mode](https://github.com/fjenett/javascript-mode-processing) that was previously included (way back!) in Processing 1.5, but I’m not sure if any of his original code remains.
