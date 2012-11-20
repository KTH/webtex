WebTeX
------

WebTeX is a simple servlet that creates PNG images for publishing on the web
given TeX expressions as input. It is used to show nicely formatted math
expressions on web pages and as a back end for a sort-of wysiwyg tinymce 
plugin math editor to make such images at a site at KTH, the Royal institute
of technology, Stockholm, Sweden, http://www.kth.se/.


## API

Once the WAR is deployed, see Installation below, there is a fully working
demo page available at the root of the context which also contains brief 
documentation of the two available interfaces. The interfaces are made up 
of the supplied JavaScript and the servlet interface.

### JavaScript

The included JavaScript webtex.js encapsulates a lot of the interaction
with the servlet and additionally provides for automatic vertical alignment
of formulas.

A full example is given below. You write the LaTeX expression as an alt attribute
prefixed by the string `tex:`. You notify the webtex.js script that it is 
a math image by adding `webtex` to the `class` attribute of the image.

Full example, paths have to be amended according to your configuation:
```
<html>
<head>
   ...
   <link rel="stylesheet" type="text/css" href=".../webtex/css/webtex.css"/>
   <script type="text/javascript" src=".../webtex/js/jquery.js"/>
   <script type="text/javascript" src=".../webtex/js/webtex.js"/>
   ...
</head>
<body>
   <p>
      <img class="webtex" alt="tex:a^2+b^2=c^2">
   </p>
</body>
</html>
```

NOTE: The only change from the original Mathtran behaviour is that the images 
are identified by the `webtex` class, rather than a non-existing `src` attribute. 
This makes it possible to use WebTex while still having validating pages, since
the `src` attribute is mandatory for `img` tags in validating pages.

### Servlet interface

The servlet request interface is identical to the original Mathtran interface. 
A couple of headers are added with more information about the generated image.

#### Request

HEAD and GET requests are supported. Both include the same headers but the 
HEAD request does not contain the image data (obviously).

`tex` A URLencoded LaTeX expression.

`D` An integer 1-10 specifying the size of the image.

NOTE: It is identical with the difference that the expressions are all expressions
supported by LaTeX with the mathtools package enabled, which should be a super
set of the expressions supported by the mathtran package this code was built to
replace.

#### Response

The servlet response contains non-standard headers with information about the 
generated picture, both in HEAD and GET requests.

`X-MathImage-tex` JavaScript encodeURIComponent-encoded string with the requested
TeX expression. This is currently equivalent to the `tex` request parameter but may
in a future release be a normalized version of the TeX expression.

`X-MathImage-log` JS encodeURIComponent-encoded string with error information or
'OK' if successful. 

`X-MathImage-depth` integer with base line offset information in approximate pixels.

`X-MathImage-width` integer with image width in pixels.

`X-MathImage-height` integer with image height in pixels.

Apart from these headers standard headers with cache control information are sent.


## Installation

### Prerequisites

WebTeX needs a LaTeX installation. It also needs the dvipng software to
create images from the TeX DVI output. Both the latex and dvipng 
binaries must be executable using the system path of the servlet container.

This require, e.g, at least the texlive-latex, dvipng and tomcat6 packages 
to be installed on RHEL6. WebTex uses the mathtools package from LaTeX which
is included in modern LaTeX distributions.

### Deployment

Apart from the prerequisites, installation should be the simple matter of 
copying the WAR file to the application folder in the servlet container. 
The created image files will be cached in a sub directory tmp. Redeploying 
the application will expire the cache and cause all files to be regenerated
on demand.

The WAR file supports deployment in sub-contexts, it can hence be named 
something like foo#bar.war in order to be reachable on the path foo/bar/
on the server.

### Security Considerations

There is no limit on the cache size. It will continue to fill the available
disk and memory spaces until out of resources. It is hence currently
possible to achieve a DoS-attack by generating lots of images.

It is thus recommended to run WebTex on a dedicated server. It is also 
recommended to use a separate partition for the tomcat folder serving
WebTex in order not to bring the hosting system down if the disk gets full,
and size it properly.

A future version of WebTex may provide customizable max settings for 
number of items and disk size of cache if necessary, but so far this 
requirement has not been a high priority.


## Rationale and history

### Mathtran

The servlet was originally intended to be an almost drop-in replacement for 
the Python script used by www.mathtran.org which is available as free source
under GPL.

Mathtran uses a pre-forked TeX binary that expressions are piped 
through. This was one way to increase speed of the service, but used only
one or a pool of TeX binaries. The produced images were not stored anywhere,
but recreated every time the image was requested. Since many requests
shared a TeX instance, the secsty format was necessary and made it difficult
to support newer constructs users normally use in documents. The service
also suffered a bit from somewhat weird behaviour of the TeX IPC mechanism.


### WebTeX

WebTex was written as a servlet dropping the IPC mechanism and simply
running TeX for each requests, but caching the output. In reality this proved
to be as fast or faster for our use cases and the new service was much more
reliable and less complicated. Each servlet thread runs its own LaTeX and 
multiple requests are handled by the servlet container threading up as necessary.

Since then, also the JavaScripts have been rewritten using jQuery and
optimized and very little of the original code is left (mostly some css).

The fact that LaTeX is not run in IPC mode means that we can get rid of the
TeX secsty format and the limitations it brings, and use a full LaTeX with
mathtools enabled providing many more math features.


## Contact

WebTex is created and maintained by the Infosys group <infosys@kth.se> 
at KTH, Kungliga tekniska högskolan, http://www.kth.se/.


## License and acknowledgements

The WebTex source is released under GPLv3, see COPYING.txt. The distribution
also contains a library, infosysutil.jar the source for which currently
is not available as free software, but you are free to use it as it is. Source
can be provided on demand.

The WebTex service is inspired by and to some extent based on the Mathtran 
service, http://www.mathtran.org/, the source for which is available as 
free software under GPL at http://sourceforge.net/projects/mathtran/.

There is not much left of the original code by now apart from some css,
but the interfaces are much the same.

A copy of yuicompressor is included to minify JavaScript and CSS.
Copyright © 2012 Yahoo! Inc. All rights reserved.
http://yuilibrary.com/license/

A copy of servlet-api is included to build the servlet.
Copyright 1999-2012 The Apache Software Foundation
http://www.apache.org/licenses/LICENSE-2.0
