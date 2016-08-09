## p5.js Mode for Processing

A simple editor for p5.js code that runs inside the PDE. 


### Goals/Criteria

* Have a simple way to get started with p5.js, while making use of the (reasonably mature) editing facilities of the PDE and its Sketchbook.
* Provide a bridge until the official p5.js Web Editor project is complete.
* Make use of the PDE being installed in many schools and labs, and have a Mode that’s simple to install.
* A simple editor that allows offline use.
* Sketches should always be runnable directly from the folder: no need to Export or otherwise prepare a Sketch. 


### Details

* Like the usual Java Mode in Processing, the main code is found in a file with the same name as the sketch.
* An `index.html` file is created in each new sketch folder. It contains a section where each .js file from the sketch is added automatically. Removing this block of code (it’s clearly marked in the file) will cause the sketch to no longer run inside the PDE.
* If you run into trouble, remove the `index.html` file, which will reset it to the version from the template.
* Add library files or additional code to the `libraries` subfolder of the sketch. That code will be included in the HTML file, though they won’t be visible as tabs in the Editor.


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
