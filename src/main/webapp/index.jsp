<?xml version="1.0" encoding="utf-8"?>
<%@page session="false" contentType="text/html; charset=utf-8"%>
<%@page import="java.util.ResourceBundle"%>
<%

ResourceBundle resources = ResourceBundle.getBundle("webtex");

%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Online translation of mathematical content</title>
    <link rel="stylesheet" type="text/css" href="css/master-min.css" />
    <link rel="stylesheet" type="text/css" href="css/webtex-min.css" />
    <script type="text/javascript" src="js/jquery.js"></script>
    <script type="text/javascript" src="js/webtex-min.js"></script>
    <script type="text/javascript" src="js/textips-min.js"></script>
  </head>
  <body>
    <h1>
      WebTex
      <%=resources.getString("webtex.version")%>
      - Online translation of mathematical content
    </h1>

    <h2>Demo</h2>

    <p id="not_initialized">Sorry, either your browser doesn't
    have Javascript enabled or there's a problem somewhere - so TeX tips
    won't work for you.</p>

    <div class="textips">
      <p>
    This is an example of TeX tips. Try clicking on one of the boxed
    images below. The rendered image and the TeX log will appear below
    the data entry form.<br />
        <img class="webtex" src="" alt="tex:1 + 1/2 + 1/4 + \ldots + 1/2^n"/>
        <img class="webtex" src="" alt="tex:\sum_0^n 2^{-n}"/>
        <img class="webtex" src="" alt="tex:\sum_0^\infty x^n/n!"/>
        <img class="webtex" src="" alt="tex:\sum_0^\infty (-1)^nx^n/n!"/>
        <img class="webtex" src="" alt="tex:\sum_0^\infty x^n/n"/>
      </p>
    </div>
    <form id="form" action="">
      <p>
        <input id="tex_src" type="text" size="50" />
        <input id="button" type="button" value="Try it!" />
      </p>
      <p class="output">
        <span id="output">Output area.</span>
      </p>
      <pre class="log" id="log">Log area.</pre>
    </form>

    <h2>Adding math to your webpage</h2>
    <p>
      There are two major ways to use the service, either directly linking 
      to the service or using a utility JavaScript which takes care of 
      much of the work, but suffers from cross site scripting limitations
      you may have to consider. A common limitation for both methods is that
      the length of the URL limits the size of the LaTeX expressions you can
      render. However, in modern browsers this limit is quite high, in the order
      of 2000 characters.
    </p>

    <h3>Using the utility JavaScript webtex.js</h3>
    <p>
      This method has the advantage that it will also align the bitmaps properly in the text.
      First, put these magic URLs in the head section of the web page. You can use your own
      copy of jQuery if you are already using jquery, provided it is reasonably new, and you
      should probably host and use your own copy.
    </p>
    <pre>
      &lt;head&gt;
      ...
      &lt;link rel="stylesheet" type="text/css" href="<%=request.getRequestURL()%>css/webtex-min.css"/&gt;
      &lt;script type="text/javascript" src="<%=request.getRequestURL()%>js/jquery.js"/&gt;
      &lt;script type="text/javascript" src="<%=request.getRequestURL()%>js/webtex-min.js"/&gt;
      ...
      &lt;/head&gt;
    </pre>

    <p>
      Next, to get <img class="webtex" src="" alt="tex:a^2+b^2=c^2" /> in your web page, put
      <code>&lt;img class="webtex" alt="tex:a^2+b^2=c^2"&gt;</code>
      in its HTML. That's all you need to do. The 'webtex' class indicates to the script 
      that it is a math image it should render from the backend service. WebTex will vertically
      align the formula for you, like this <img class="webtex" src="" alt="tex:\int_0^1 x\, dx" />
      and this <img class="webtex" src="" alt="tex:{}^2" />.
    </p>

    <p>
      Note that you have to consider regular javascript cross site issues if you want to use this
      at some other domain since the script will fetch images in order to align them properly. 
      You should probably host your own webtex service at your site to avoid them.
    </p>
    <p>
      There is an uncompressed version of the webtex-min.js script available as webtex-min.js
      from the same place but this is mostly unnecessary with modern browsers.
    </p>

    <h3>Direct link to image generating service</h3>
    <p>
      The image generating service is located at <code><%=request.getRequestURL()%>WebTex</code>
      and takes two parameters.
    </p>
    <dl>
      <dt><code>tex</code></dt>
      <dd>
        An URL encoded LaTeX math expression without the surrounding delimiters you would 
        use to identify a math block in a LaTeX document.
      </dd>
      <dt><code>D</code></dt>
      <dd>
        An optional scaling 1-10 of the image size, defaults to 1.
      </dd>
    </dl>

    <p>
      Cache control HTTP headers are set in the response to indicate the life span of the 
      image in the cache. In addition to this, a number of custom headers are set in the 
      response indicating some properties of the image which can be used:
    </p>
    <dl>
      <dt><code>X-MathImage-tex</code></dt>
      <dd>
        Contains the TeX expression used to generate the image, encoded for
        decoding with JavaScript decodeURIComponent.
      </dd>
      <dt><code>X-MathImage-depth</code></dt>
      <dd>
        Contains a numeric value indicating the base line position of the image which
        can be used for positioning.
      </dd>
      <dt><code>X-MathImage-width</code></dt>
      <dd>
        Integer containing the width of the image in pixels.
      </dd>
      <dt><code>X-MathImage-height</code></dt>
      <dd>
        Integer containing the height of the image in pixels.
      </dd>
      <dt><code>X-MathImage-depth</code></dt>
      <dd>
        Contains a numeric value indicating the base line position of the image which
        can be used for positioning.
      </dd>
      <dt><code>X-MathImage-log</code></dt>
      <dd>
        The error message from LaTeX or 'OK' if no errors where encountered, encoded for
      	decoding with JavaScript decodeURIComponent.
      </dd>
    </dl>

    <p>
      For example, the img tag <code>&lt;img src="<%=request.getRequestURL()%>WebTex?D=4&amp;tex=%5Cdisplaystyle%7B%7B%7Be%7D%7D%5E%7B%7B%7Bi%7D%5Cpi%7D%7D%2B%7B1%7D%3D%7B0%7D%7D"/&gt; </code> generates the image:
    </p>
    <p>
      <img alt="" src="<%=request.getRequestURL()%>WebTex?D=4&amp;tex=%5Cdisplaystyle%7B%7B%7Be%7D%7D%5E%7B%7B%7Bi%7D%5Cpi%7D%7D%2B%7B1%7D%3D%7B0%7D%7D"/>
    </p>

    <h2>Download WebTex</h2>
    <p>
      WebTex is available as open source at GitHub at 
      <a href="http://github.com/KTHse/webtex/">github.com/KTHse/webtex/</a>, 
      please feel free to fork and improve and send a pull request so we may
      integrate your patches.
    </p>

    <h2>Acknowledgements</h2>
    <p>
      This software is based on the open source software Mathtran, Copyright
      &copy; 2007 <a href="http://www.open.ac.uk">The Open University</a>.
      More information can be found on the <a
      href="http://www.mathtran.org/">mathtran.org</a> home page.
    </p>
    <p>The source code for the mathtran project is released under the
    GPL. The resources are available here:</p>
    <ul>
      <li><a href="http://sourceforge.net/projects/mathtran">http://sourceforge.net/projects/mathtran</a></li>
      <li><a href="http://sourceforge.net/projects/texd">http://sourceforge.net/projects/texd</a></li>
    </ul>
  </body>
</html>
