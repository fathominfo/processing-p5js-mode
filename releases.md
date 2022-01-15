# p5jsMode 1.4

* TODO


# p5jsMode 1.3.1

* Another workaround for the JNA issues seen in the previous releases


# p5jsMode 1.3

Bug fixes and other updates

* Added workaround for JNA issues on Big Sur [#26](https://github.com/fathominfo/processing-p5js-mode/issues/26)
* Update to p5.js 1.2.0 (the December 19, 2020 release)
* Update the examples
* Added p5.js menu
* Added menu option to replace p5.js with the version included with p5jsMode
* Implemented menu option to replace `index.html`

Internal changes

* Add revision check to the build
* Fix up a few warnings by explicitly closing a few more streams
* Remove p5js.dom references


# p5jsMode 1.2.2

A few bug fixes and updates:

* Updated p5.js to version 0.10.2
* Update jshint to 2.11.0
* Remove warning about trailing commas in code


# p5jsMode 1.2

Significant improvements to syntax and error checking!

* Incorporates an entirely new linter, *much* better error-checking and now includes warnings.
* Supports newer JS syntax (more ES6+ features supported).
* Using just one server/port per editor window (rather than restarting a fresh server on each run).
* Fix coloring and Find in Reference for circle(), square(), push(), pop().


# p5jsMode 1.1.1

Same as 1.1 but with fixes to the build process.

## Features and Fixes
* Add a basic version of Find in Reference [22](https://github.com/fathominfo/processing-p5js-mode/issues/22)
* Updated to p5.js 0.7.3
* Also using the newest Sound and DOM libraries
* Update examples to the latest version available
* Allow use of let and const in p5js-mode [20](https://github.com/fathominfo/processing-p5js-mode/issues/20), [18](https://github.com/fathominfo/processing-p5js-mode/issues/18)
* Switch to using the minified version of p5.js (and the libraries)
* Allow JSON files to be viewed in the editor (though not from subfolders like `assets` or `data`) [16](https://github.com/fathominfo/processing-p5js-mode/issues/16)
* Better handling of URL opening on Linux systems [17](https://github.com/fathominfo/processing-p5js-mode/issues/17)
* Fix problems when opening a bare `.js` file as a sketch [7](https://github.com/fathominfo/processing-p5js-mode/issues/7)

## Fixes in Processing 3.5.3
* Prevent issues when editing CSS files [21](https://github.com/fathominfo/processing-p5js-mode/issues/21)
