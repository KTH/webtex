//Copyright: (c) 2009-2012 KTH, Royal Institute of Technology, Stockholm, Sweden
//Author: Fredrik Jönsson <fjo@kth.se>
//
//Based on:
//Copyright: (c) 2007 The Open University, Milton Keynes, UK
//Author: Jonathan Fine <jfine@pytex.org>, <J.Fine@open.ac.uk>

//Javascript that uses WebTex to add images to your web page.
//See http://webtex-1.sys.kth.se/webtex/ for examples.

/* Typical use:
   ...
   <script type="text/javascript" src=".../js/webtex.js"></script>
   ...
   <p>
   <img alt="tex:x^2+y^2=1" />
   <img alt="tex:\sin^2\theta + \cos^2\theta = 1" />
   </p>
   ...
 */

//Create a namespace, to hold variables and functions.
mathtran = new Object();

//Change this use a different MathTran server.
mathtran.imgSrc = "../webtex/WebTex?";

mathtran.MAX_D = 6;
mathtran.allowResize = false;

//CAUTION: for development ONLY.
mathtran.security = new Object();
mathtran.enableCrossDomain = false;

mathtran.security.enableCrossDomain = function() {
	mathtran.enableCrossDomain = true;
};

//Function to transform the whole document.  Add SRC to each IMG with
//ALT text starting with "tex:".  However, skip if element already
//has a SRC.  Note that 'src=""' may be a non-empty src, because the
//browser will prefix the base URL.
mathtran.init = function () {
	if (! document.getElementsByTagName) {
		return;
	}

	var objs = document.getElementsByTagName("img");
	var len = objs.length;
	var tex_count = 0;

	for (var i = 0; i<len; i++) {
		var img = objs[i];
		if (mathtran.isTexImage(img)) {

			if (!img.src) {
				var tex_src = img.alt.substring(4);
				size = mathtran.contextSize(img);
				img.src = mathtran.getImgSrc(tex_src, size);
			}

			// Append TEX to the class of the IMG.
			img.className +=' tex';
			tex_count++;
			mathtran.httpRequest(img);
		}
	}
	mathtran.hideElementById("mathtran.error");
};

//Utility function. Could define mathtran.getTexImages=function.
mathtran.isTexImage = function(img) {
	return (img.alt.substring(0,4) == 'tex:'); //img.alt.substring(4);
};

//Utility function.
mathtran.getImgSrc = function(tex_src, size) {
	var size_frag;

	if (size == null) {
		size_frag = '';
	} else {
		size_frag = 'D=' + size + '&';
	}

	// See http://xkr.us/articles/javascript/encode-compare/
	return mathtran.imgSrc + size_frag + 'tex=' + encodeURIComponent(tex_src);
};

mathtran.contextSize = function(img) {

	if (!mathtran.allowResize) {
		return
	}

	var fontSize = mathtran.getStyle(img, 'font-size');
	var pos = fontSize.indexOf('p'); //Mozilla Opera px | MSIE pt.
	var unit = fontSize.substr(pos);
	var divide = 11;
	if (unit == 'pt') divide=12;
	var resize = Math.round(fontSize.substr(0, pos)/divide);
	if (resize > mathtran.MAX_D) resize = mathtran.MAX_D;

	img.title = 'Debug: '+ fontSize +' | D='+resize;
	return resize;
};

//Utility function.
mathtran.hideElementById = function (id) {
	var obj = document.getElementById(id);
	if (obj) {
		obj.style.display = 'none';
	}
	return obj;
};

//Utility function, should be a prototype on 'document' or 'node'.
mathtran.getElementsByClassName = function(classname) {
	var a = [];
	var re = new RegExp('\\b' + classname + '\\b');
	var els = document.getElementsByTagName("*"); //node
	for (var i=0,j=els.length; i<j; i++) {
		if(re.test(els[i].className))a.push(els[i]);
	}
	return a;
};

mathtran.getStyle = function(elem, styleProp) {
	var y = "";
	if (window.getComputedStyle) { //For Mozilla.
		y = document.defaultView.getComputedStyle(elem, null).getPropertyValue(styleProp);
	}
	else if (elem.currentStyle) { //For Internet Explorer.
		styleProp = styleProp.replace(/\-(\w)/,
				function(str, p1, offset, s){ return p1.toUpperCase(); } );
		y = elem.currentStyle[styleProp];
	}
	return y;
};

//Thanks to Scott Andrew, we resolve a cross-browser issue.
//http://scottandrew.com/weblog/articles/cbs-events
mathtran.addEvent = function (obj, evType, fn, useCapture) {
	if (obj.addEventListener) { //For Mozilla.
		obj.addEventListener(evType, fn, useCapture);
		return true;
	} else if (obj.attachEvent) { //For Internet Explorer.
		var r = obj.attachEvent("on"+evType, fn);
		return r;
	}
};

mathtran.htmlEscape = function(s) {
	s = s.replace(/&/g,'&amp;');
	s = s.replace(/>/g,'&gt;');
	s = s.replace(/</g,'&lt;');
	return s;
};

//Adapted from: http://www.w3schools.com/xml/xml_http.asp
mathtran.httpRequest = function(img, post_fn) {
	var xmlhttp = null;

	if (window.XMLHttpRequest) { //For Mozilla, etc
		xmlhttp = new XMLHttpRequest();
	}
	else if (window.ActiveXObject) { //For Internet Explorer.
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	this.xmlhttp = xmlhttp;

	if (xmlhttp != null) {

		xmlhttp.onreadystatechange = function() {
			mathtran.httpCallback(xmlhttp, img, post_fn);
		};
		try {
			if (mathtran.enableCrossDomain && typeof(netscape) != "undefined") {
				netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserRead");
			}
		} catch (e) {
			alert("Permission UniversalBrowserRead denied.");
		}
		xmlhttp.open("GET", img.src, true);
		xmlhttp.send(null);
	} else {
		alert("Your browser does not support XMLHTTP.");
	}
};

mathtran.httpCallback = function(xmlhttp, img, post_fn) {
	if (xmlhttp.readyState==4) {
		if (xmlhttp.status==200) { //"OK"

			img.math = new Object();
			img.math.log = decodeURIComponent(xmlhttp.getResponseHeader("X-MathImage-log"));
			img.math.depth = xmlhttp.getResponseHeader("X-MathImage-depth");

			//img.style.verticalAlign = -img.depth+'px';
			if (img.math.depth[0] != '-') {
				img.className +=' dp' + img.math.depth;
			} else {
				img.className +=' dp_' + img.math.depth.substring(1);
			}

			if (post_fn) {
				post_fn(img);
			}
		} else {
			alert("Problem retrieving XML data");
		}
	}
};

mathtran.addEvent(window, 'load', mathtran.init, false);
