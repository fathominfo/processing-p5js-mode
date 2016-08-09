## p5.js Mode for Processing

A simple editor for p5.js code that runs inside the PDE. 


### Goals/Criteria

* Have a simple way to get started with p5.js, while making use of the editing facilities of the PDE and its Sketchbook.
* Provide a bridge until the official p5.js Web Editor project is complete. (With any luck, this project will have a lifespan of just a few months (or maybe the 2016-2017 school year).
* Have a simple editor that allows offline use.
* Make use of the PDE being installed in many schools and labs, and have a Mode that’s easy to install from inside the PDE (no download, unzip, install process).
* We need a way to support courses we're teaching during Fall 2016.
* Sketches should always be runnable directly from the folder: no need to Export or otherwise prepare a Sketch. 
* This is not an official [Processing Foundation](https://github.com/processing) project, no matter what you might know about its [primary author](https://github.com/benfry). 


### Details

This Mode is not designed for flexibility. It's also not a way to teach people how to do "proper" JavaScript development. Like all things in the PDE, we're removing features in an attempt to greatly simplify the process of getting started and/or creating projects at the simpler end of the scale. And like the PDE, if you outgrow this setup, you should use another IDE or development solution (like a full-featured programmer's text editor and similar tools).

The tradeoffs here represent the best solution based on the goals and criteria above. Of course, these are subject to change if we find that our assumptions were foolish.

* Like the usual Java Mode in Processing, the main code is found in a file with the same name as the sketch. If you need more flexibility, this Mode isn't for you.
* An `index.html` file is created in each new sketch folder. It contains a section where each .js file from the sketch is added automatically. Removing this block of code (it’s clearly marked in the file) will cause the sketch to no longer run inside the PDE.
* If you run into trouble, remove the `index.html` file, which will reset it to the version from the template.
* Add library files or additional code to the `libraries` subfolder of the sketch. That code will be included in the HTML file, though they won’t be visible as tabs in the Editor.
* Like everything else in the PDE, this uses the `data` folder (unlike many p5js examples which use an `assets` folder). I'm guessing this is ok for now because sketches must specify `assets` in the path, so it's just as easy to do that as to specify `data` instead. 


#### todo

- [ ] add single file library support
	- [ ] get p5.dom.js and p5.sound.js into the menu
	- [ ] contributed libraries into the menu?
- [ ] update libraries subfolder
- [ ] import all the examples
- [ ] basic syntax highlighting for .css?


#### done

- [x] get templates working
- [x] make sure toolbar buttons are behaving
- [x] move the side panels from the editor out of Java and into Editor
- [x] remove DirectivesEditor
- [x] edit .html, .txt, .json


#### nope

- [ ] make the server provide dummy /libraries folder
- [ ] implement Export as separate option that creates web folder
