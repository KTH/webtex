Release notes
-------------

## WebTex x.x.x

* The X-MathImage-tex header should be URL encoded.

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
