Release notes
-------------

## WebTex 1.5.0

* Convert to a fixed size FIFO cache in order to avoid potential DDOS
  situations when running in a common infrastructure. Cache size may
  be configurable in a later release, currently it is not.
* Minor fix in _about.

## WebTex 1.4.3

* Smaller image.

## WebTex 1.4.2

* Bunyan log format.
* Optional SSL support.

## WebTex 1.4.0

* Dockerized.

## WebTex 1.3.7

* Get the first and consequent error line. That's where the good stuff is.

## WebTex 1.3.5

* Get all lines of error into the tex error log output.

## WebTex 1.3.4

* Halt on error to avoid looping LaTeX-processes on some input.

## WebTex 1.3.3

* Return 400 rather than 500 and stack trace when dvipng returns error.

## WebTex 1.3.2

* Enable amssymb package to get mathbb et al.

## WebTex 1.3.1

* Updated documentation with instructions on how to install the standalone
  LaTeX document class since it is not yet standard in common distributions.
  See README.md. No functional changes.

## WebTex 1.3.0

* Added X-MathImage-width and X-MathImage-height headers. This is a 
  backwards compatible API change adding to the previously sent headers.
* Use documentclass standalone to avoid too narrow typesetting.

## WebTex 1.2.2

* The X-MathImage-tex header should be encoded.
* The X-MathImage-log header should be encoded.

## WebTex 1.2.0

* Change behaviour of webtex.js so pages can use WebTex and still validate.
* Rewritten JavaScripts using jQuery.
* Minified javascript and css.
* Stop cache thread properly when servlet container destroys servlet.
* Maintain disk size of cache and report in monitor page. This could be
  eventually be used to put a limit on the cache size.
* General cleanup and code review.
* Prepared for public release.

## WebTex 1.1.2

* Fix bug with servlet running at 100% when cache is empty.
* Remove unused stuff from webtex.js.

## WebTex 1.1.1

* Use single cache for servlet, not one per thread.
* Added _monitor and _about pages.
* Use mathtools rather than amsmath.

## WebTex 1.0.1

* Use latex instead of plain tex with secsty format. Most of the cases
  secsty handles are not valid with our way of running tex. The remaining
  known issues to handle are taken care of with a simple validation of
  the user input.
* The project is moved to gitolite@social-git.sys.kth.se:webtex.

## WebTex 0.3.8-3

* Add "Expires" header and increase http cache recommendation to 24 hours.
* Several other fixes.

## WebTex 0.3.2

* Fix critical bug in 0.3.1. 

## WebTex 0.3.1

* Transparent backgrounds in generated images.
* Make \text synonym of \rm
* Make it possible to use \\ instead of \cr in matrix.
* Add version number to test page.

## WebTex 0.3.0

* Add some TeX commands used by Virtuellt Campus. 
* Add version number to service front page.
* Minor syntax fixes in demo page.

## WebTex 0.2.1

* Bug fix in the cache.
* Hudson support.

## WebTex 0.2.0

* Cache data in webapp folder.
* Remove data that has not been read for one week.
