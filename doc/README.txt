
WebTeX
======

WebTeX is a simple servlet that creates PNG images for publishing on the web
given TeX expressions as input.


Installation
============

Prerequisites

WebTeX (as well as mathtran) needs a TeX (plain tex) installation. 
It also needs the dvipng software to create images from the TeX DVI 
output. Both the tex and dvipng binaries must be executable using the 
system path of the servlet container.

Deployment

Apart from the prerequisites, installation should be the simple matter of 
copying the WAR file to the application folder in the servlet container. 
The created image files will be cached in a sub directory tmp. Redeploying 
the application will expire the cache and cause the files to be regenerated
on demand.


RATIONALE
=========

The servlet is intended to be an almost drop-in replacement for the Python 
script used by www.mathtran.org which is available as free source under GPL. 
Mathtran uses a pre-forked TeX binary that expressions is piped through. This 
makes it fast, but only one TeX binary is used, creating a bottle neck, and 
the produced images are not stored anywhere, but recreated every time unless
a particular browser caches the image.

The mathtran software was intended to use as the backend for a WYSIWYG web 
editor for mathematical content. However, the TeX pipe (or IPC) mode seem to 
be unpredictable and the startup of the mathtran daemon process has been 
difficult to stabilize. A new implementation of the same idea while avoiding 
the TeX pipe mode would be rather easy to implement. 


WebTeX
======

WebTeX uses some parts of mathtran. In particular the JavaScript is almost
identical, and the secplain TeX format is identical, apart from a couple of
additions required by math.se.

All the Python code is re-written as a simple Java servlet. WebTeX operates 
differently, the created images are cached for future reference, so that an 
image does not need to be regenerated if asked for again. TeX is run in an 
ordinary manner whenever necessary. Each servlet thread may run it's own 
TeX binary, so several instances of TeX may be running simultaneously.
