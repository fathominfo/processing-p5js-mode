# p5jsMode 1.4.2

A few bug fixes and improvements.

* After hitting Run, the local server address is shown in the console, which helps with cross-device testing.

* Fixes for how openURL() is handled. [#29](https://github.com/fathominfo/processing-p5js-mode/pull/29)

* Fix up outdated URLs, change a few from http to https.

* Require use of 4.0 beta 5 for `PdeTextAreaDefaults` constructor deprecation.

* Cleaning up some old code, using lambdas, clearing up compile warnings.


# p5jsMode 1.4.1

All the things from the previous release (1.4) actually work in this one.


# p5jsMode 1.4

Latest p5.js (1.4) and support for Processing 4.0 beta 3 (which uses Java 17).

* Update p5.js from 1.2.0 to 1.4.0.

* Switch to Nashorn scripting engine [15.3](https://search.maven.org/artifact/org.openjdk.nashorn/nashorn-core/15.3/jar) from OpenJDK built by Attila Szegedi. The original Nashorn engine was removed prior to the Java 17 release, so this was necessary to keep things compatible with Processing 4.0 beta 3.

* Update jshint from 2.12 to [2.13.3](https://github.com/jshint/jshint/releases/tag/2.13.3)

* Change `authorList` to `authors` in `mode.properties` (responding to a change from, uh, Processing 3.0 alpha 11, which brings us in line with the spec as of 2015).


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
