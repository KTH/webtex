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
webtex = new Object();

webtex.MAX_D = 6;
webtex.allowResize = false;

//Set the path to the service script relative.
webtex.imgSrc = document.getElementsByTagName('script');
webtex.imgSrc = webtex.imgSrc[webtex.imgSrc.length-1].src.replace("js/webtex.js", "WebTex?");

//Function to transform the whole document.  Add SRC to each IMG with
//ALT text starting with "tex:".  However, skip if element already
//has a SRC.  Note that 'src=""' may be a non-empty src, because the
//browser will prefix the base URL.
webtex.init = function () {
    if (! document.getElementsByTagName) {
	return;
    }

    var objs = document.getElementsByTagName("img"), 
        len = objs.length, 
        i, img, tex_src;

    for (i = 0; i<len; i++) {
	img = objs[i];
	if (webtex.isTexImage(img)) {
	    
	    if (!img.src) {
		tex_src = img.alt.substring(4);
		size = webtex.contextSize(img);
		img.src = webtex.getImgSrc(tex_src, size);
	    }
	    // Append TEX to the class of the IMG.
	    img.className +=' tex';
            // Fetch to get scaling.
            webtex.httpRequest(img);
	}
    }
    webtex.hideElementById("webtex.error");
};

//Utility function. Could define webtex.getTexImages=function.
webtex.isTexImage = function(img) {
    return (img.alt.substring(0,4) == 'tex:'); //img.alt.substring(4);
};

//Utility function.
webtex.getImgSrc = function(tex_src, size) {
    var size_frag;

    if (size == null) {
	size_frag = '';
    } else {
	size_frag = 'D=' + size + '&';
    }

    // See http://xkr.us/articles/javascript/encode-compare/
    return webtex.imgSrc + size_frag + 'tex=' + encodeURIComponent(tex_src);
};

webtex.contextSize = function(img) {
    if (!webtex.allowResize) {
	return;
    }
    
    var fontSize = webtex.getStyle(img, 'font-size'),
        pos = fontSize.indexOf('p'), //Mozilla Opera px | MSIE pt.
        unit = fontSize.substr(pos),
        divide = 11,
        resize;

    if (unit == 'pt') {
	divide=12;
    }

    resize = Math.round(fontSize.substr(0, pos)/divide);
    
    if (resize > webtex.MAX_D) {
	resize = webtex.MAX_D;
    }

    img.title = 'Debug: '+ fontSize +' | D='+resize;
    return resize;
};

//Utility function.
webtex.hideElementById = function (id) {
    var obj = document.getElementById(id);
    if (obj) {
	obj.style.display = 'none';
    }
    return obj;
};

//Utility function, should be a prototype on 'document' or 'node'.
webtex.getElementsByClassName = function(classname) {
    var a = [],
        re = new RegExp('\\b' + classname + '\\b'),
        els = document.getElementsByTagName("*"), //node
        i, j;

    for (i=0, j=els.length; i<j; i++) {
	if(re.test(els[i].className))a.push(els[i]);
    }
    return a;
};

webtex.getStyle = function(elem, styleProp) {
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
webtex.addEvent = function (obj, evType, fn, useCapture) {
    if (obj.addEventListener) { //For Mozilla.
	obj.addEventListener(evType, fn, useCapture);
	return true;
    } else if (obj.attachEvent) { //For Internet Explorer.
	var r = obj.attachEvent("on"+evType, fn);
	return r;
    }
};

webtex.htmlEscape = function(s) {
    s = s.replace(/&/g,'&amp;');
    s = s.replace(/>/g,'&gt;');
    s = s.replace(/</g,'&lt;');
    return s;
};

//CAUTION: for development ONLY.
webtex.security = new Object();
webtex.enableCrossDomain = false;

webtex.security.enableCrossDomain = function() {
	webtex.enableCrossDomain = true;
};


//Adapted from: http://www.w3schools.com/xml/xml_http.asp
webtex.httpRequest = function(img, post_fn) {
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
	    webtex.httpCallback(xmlhttp, img, post_fn);
	};
	try {
	    if (webtex.enableCrossDomain && typeof(netscape) != "undefined") {
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

webtex.httpCallback = function(xmlhttp, img, post_fn) {
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

webtex.addEvent(window, 'load', webtex.init, false);
