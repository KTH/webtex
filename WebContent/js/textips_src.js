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
   <script type="text/javascript" src=".../js/webtex.js"></script>
   <script type="text/javascript" src=".../js/textips.js"></script>
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
mathtran.textips = new Object(); 

//We place an input form at the each TEXTIPS DIV.
mathtran.textips.formStr = 
    '<form><p><input id="text_1" type="text" size="50" />\
     <input type="button" value="Try it!" /></p>		 \
     <p class="output"><span>Output area.</span></p> \
     <pre class="log">Log area</pre></form>';

//We will store the input forms on this page, and their targets.
mathtran.textips.inputs = new Array();
mathtran.textips.targets = new Array();

//Submit value of input form, and place result in target.
mathtran.textips.submit = function (textip_no) {
    var tex_src = mathtran.textips.inputs[textip_no].value,
        target = mathtran.textips.targets[textip_no],
        img_src = mathtran.getImgSrc(tex_src, 1);
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
    var elems = mathtran.getElementsByClassName('textips'),
        textip_no = -1,
        i, j, htmlStr, r, parsedHTML, form, fn, fn_string, img, images;
    
    for (i=0; i < elems.length; i++) {
	div = elems[i];
	textip_no ++;
	
	// Add FORM at end of the DIV.
	htmlStr = mathtran.textips.formStr;
	// Cross-platform based on http://www.faqts.com/knowledge_base/entry/edit/index.phtml?aid=5756&fid=195&return_url=%2Fknowledge_base%2Fview.phtml%2Faid%2F5756
	if (div.insertAdjacentHTML){ //For Internet Explorer.
	    div.insertAdjacentHTML("BeforeEnd", htmlStr);
	} else { //For Mozilla, etc.
	    r = div.ownerDocument.createRange();
	    r.setStartBefore(div);
	    parsedHTML = r.createContextualFragment(htmlStr);
	    div.appendChild(parsedHTML);
	}
	
	// Find the form we just made.  Could be done better.
	form = div.getElementsByTagName("form")[0];
	
	// Store the location of the input form and the image target.
	mathtran.textips.inputs.push(form.getElementsByTagName("input")[0]);
	mathtran.textips.targets.push(form.getElementsByTagName("span")[0]);
	
	// Add action to submit button and to form.
	fn_string = "mathtran.textips.submit(" + textip_no + ")";
	fn = new Function(fn_string);
	mathtran.addEvent(form.getElementsByTagName("input")[1], 'click', fn, false);
	form.action = "javascript:" + fn_string;
	
	// Create function to be called when image is clicked.
	// Cross-platform way of passing img to copyAlt function.
	fn_string = "var img=this; mathtran.textips.copyAlt(img," + textip_no + ")";
	fn = new Function(fn_string);
	
	// Process each IMG with tex ALT text.
	images = div.getElementsByTagName("img");
	for (j=0; j < images.length; j++) {
	    img = images[j];
	    if (mathtran.isTexImage(img))  {
		img.onclick = fn;
	    }
	}
    }
    mathtran.hideElementById("mathtran.textips.error");
};

mathtran.addEvent(window, 'load', mathtran.textips.init, false);

