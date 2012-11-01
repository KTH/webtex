<?xml version="1.0" encoding="utf-8"?>
<%@page import="java.util.ResourceBundle"%>
<%@page contentType="text/html; charset=utf-8"%>
<%

ResourceBundle resources = ResourceBundle.getBundle("webtex");

%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Online translation of mathematical content</title>
    <link rel="stylesheet" type="text/css" href="css/master.css" />
    <link rel="stylesheet" type="text/css" href="css/webtex.css" />
    <script type="text/javascript" src="js/webtex.js"></script>
    <script type="text/javascript" src="js/textips.js"></script>
  </head>
  <body>
    <h1>
      WebTex
      <%=resources.getString("webtex.version")%>
      - Online translation of mathematical content
    </h1>

    <h2>Demo</h2>

    <p id="webtex.textips.error">Sorry, either your browser doesn't
    have Javascript enabled or there's a problem somewhere - so TeX tips
    won't work for you.</p>

    <div class="textips">
      <p>
	This is an example of TeX tips. Try clicking on one of the boxed
	images below. The rendered image and the TeX log will appear below
	the data entry form.<br /> <img
	alt="tex:1 + 1/2 + 1/4 + \ldots + 1/2^n" /> <img
	alt="tex:\sum_0^n 2^{-n}" /> <img alt="tex:\sum_0^\infty x^n/n!" />
	<img alt="tex:\sum_0^\infty (-1)^nx^n/n!" /> <img
	alt="tex:\sum_0^\infty x^n/n" />
      </p>
    </div>

    <h2>Adding math to your webpage</h2>

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
        An optional scaling 1-10 of the image size, defaults to 2.
      </dd>
    </dl>

    <p>
      Cache control HTTP headers are set in the response to indicate the life span of the 
      image in the cache. In addition to this, a number of custom headers are set in the 
      response indicating some properties of the image which can be used:
    </p>
    <dl>
      <dt><code>X-MathImage-tex</code></dt>
      <dd>Contains the tex expression used to generate the image.</dd>
      <dt><code>X-MathImage-depth</code></dt>
      <dd>Contains a numeric value indicating the base line position of the image which can be used for positioning.</dd>
      <dt><code>X-MathImage-log</code></dt>
      <dd>The error message from LaTeX or 'OK' if no errors where encountered.</dd>
    </dl>

    <p>
      For example, the img tag <code>&lt;img src="<%=request.getRequestURL()%>WebTex?D=4&amp;tex=%5Cdisplaystyle%7B%7B%7Be%7D%7D%5E%7B%7B%7Bi%7D%5Cpi%7D%7D%2B%7B1%7D%3D%7B0%7D%7D"/&gt; </code> generates the image:
    </p>
    <p>
      <img src="<%=request.getRequestURL()%>WebTex?D=4&amp;tex=%5Cdisplaystyle%7B%7B%7Be%7D%7D%5E%7B%7B%7Bi%7D%5Cpi%7D%7D%2B%7B1%7D%3D%7B0%7D%7D"/>
    </p>

    <h3>Using the utility JavaScript webtex.js</h3>
    <p>
      The other method to include mathematical bitmaps in your web pages is to use the utility
      JavaScript webtex.js. This will also align the bitmaps properly in the text.
      First, put the magic URLs<br />
      <code>
	&lt;link rel="stylesheet" type="text/css" href="<%=request.getRequestURL()%>css/webtex.css"/&gt;<br/>
	&lt;script type="text/javascript" src="<%=request.getRequestURL()%>js/webtex.js"/&gt;
      </code>
      <br /> in the HEAD of your web page.
    </p>

    <p>
      Next, to get <img alt="tex:a^2+b^2=c^2" /> in your web page, put
      <code>&lt;img alt="tex:a^2+b^2=c^2"&gt;</code>
      in its HTML. That's all you need to do. More complicated equations are
      done in the same way, of course. WebTex will vertically align the
      formula for you, like this <img alt="tex:\int_0^1 x\, dx" /> and this
      <img alt="tex:{}^2" />
    </p>

    <p>
      Note that you have to consider regular javascript cross site issues if you want to use this at some
      other domain since the script will fetch images in order to align them properly. 
      You should probably host your own webtex service at your site to avoid them.
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
