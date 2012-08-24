//Copyright: (c) 2009-2012 KTH, Royal Institute of Technology, Stockholm, Sweden
//Author: Fredrik Jönsson <fjo@kth.se>
//
//Based on:
//Copyright: (c) 2007 The Open University, Milton Keynes, UK
//Author: Jonathan Fine <jfine@pytex.org>, <J.Fine@open.ac.uk>

//Javascript that uses WebTex to add images to your web page via
//TeX tips. See http://webtex-1.sys.kth.se/webtex/ for examples.

/* Typical use:
   ...
   <script type="text/javascript" src="/js/mathtran.js"></script>
   ...
   <p id="mathtran.textips.error">Hidden unless init() fails.</p>
   ...
   <div class="textips">
   <p>Click on formula to display its TeX source 
   <img alt="tex:x^2+y^2=1" />
   <img alt="tex:\sin^2\theta + \cos^2\theta = 1" />
   </p>
   </div>
   ...
 */

//Create a namespace, to hold variables and functions.
mathtran = new Object();

//Change this use a different MathTran server.
mathtran.imgSrc = "/webtex/WebTex?";

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

//==========================================================================

//Create a namespace, to hold variables and functions.
mathtran.textips = new Object(); 

//We place an input form at the each TEXTIPS DIV.
mathtran.textips.formStr = 
	'<form><p><input id="text_1" type="text" size="50" />\
	 <input type="button" value="Try it!" /></p>\
	 <p class="output"><span>Output area.</span></p>\
	 <pre class="log">Log area</pre></form>';

//We will store the input forms on this page, and their targets.
mathtran.textips.inputs = new Array();
mathtran.textips.targets = new Array();

//Submit value of input form, and place result in target.
mathtran.textips.submit = function (textip_no) {
	var tex_src = mathtran.textips.inputs[textip_no].value;
	var target = mathtran.textips.targets[textip_no];
	var img_src = mathtran.getImgSrc(tex_src, 1);
	target.innerHTML = '<img src="' + img_src + '" alt="" />';
	img =  target.getElementsByTagName('img')[0];
	mathtran.httpRequest(img, mathtran.textips.showLog);
};

mathtran.textips.showLog = function(img) {
	img.parentNode.parentNode.parentNode.lastChild.innerHTML = mathtran.htmlEscape(img.math.log);
};

//Copy TeX source from image to input form, and submit.
mathtran.textips.copyAlt = function (img, textip_no) {
	var input = mathtran.textips.inputs[textip_no];
	input.value = img.alt.substring(4);
	mathtran.textips.submit(textip_no);
};

//Initialise the document.
mathtran.textips.init = function () {

	// Process each DIV with class textips.
	var elems = mathtran.getElementsByClassName('textips');
	var textip_no = -1;

	for (var i=0; i < elems.length; i++) {
		div = elems[i];
		textip_no ++;

		// Add FORM at end of the DIV.
		var htmlStr = mathtran.textips.formStr;
		// Cross-platform based on http://www.faqts.com/knowledge_base/entry/edit/index.phtml?aid=5756&fid=195&return_url=%2Fknowledge_base%2Fview.phtml%2Faid%2F5756
		if (div.insertAdjacentHTML){ //For Internet Explorer.
			div.insertAdjacentHTML("BeforeEnd", htmlStr);
		} else { //For Mozilla, etc.
			var r = div.ownerDocument.createRange();
			r.setStartBefore(div);
			var parsedHTML = r.createContextualFragment(htmlStr);
			div.appendChild(parsedHTML);
		}

		// Find the form we just made.  Could be done better.
		var form = div.getElementsByTagName("form")[0];

		// Find in the FORM the other elements we need.
		var input = form.getElementsByTagName("input")[0];
		var button = form.getElementsByTagName("input")[1];
		var target = form.getElementsByTagName("span")[0];

		// Store the location of the input form and the image target.
		mathtran.textips.inputs.push(input);
		mathtran.textips.targets.push(target);

		// Add action to submit button and to form.
		var fn_string = "mathtran.textips.submit(" + textip_no + ")";
		var fn = new Function(fn_string);
		mathtran.addEvent(button, 'click', fn, false);
		form.action = "javascript:" + fn_string;

		// Create function to be called when image is clicked.
		// Cross-platform way of passing img to copyAlt function.
		var fn_string = "var img=this; mathtran.textips.copyAlt(img," + textip_no + ")";
		var fn = new Function(fn_string);

		// Process each IMG with tex ALT text.
		var images = div.getElementsByTagName("img");
		for (var j=0; j < images.length; j++) {
			var img = images[j];
			if (mathtran.isTexImage(img))  {
				img.onclick = fn;
			}
		}
	}
	mathtran.hideElementById("mathtran.textips.error");
};

//Initialise once the whole document is loaded.
mathtran.addEvent(window, 'load', mathtran.init, false);
mathtran.addEvent(window, 'load', mathtran.textips.init, false);
