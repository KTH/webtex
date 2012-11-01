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
webtex.imgSrc = webtex.imgSrc[webtex.imgSrc.length-1].src.replace("js/webtex_src.js", "WebTex?");

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
    $('#webtex.error').hide();
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
    
    var fontSize = $(img).css('font-size'),
        pos = fontSize.indexOf('p'), //Mozilla Opera px | MSIE pt.
        unit = fontSize.substr(pos),
        divide = 11,
        resize;

    alert(fontSize);
    
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

webtex.httpRequest = function(img, post_fn) {
    var res = $.ajax(img.src)
        .done(function() {
            img.math = new Object();
		    img.math.log = decodeURIComponent(res.getResponseHeader("X-MathImage-log"));
		    img.math.depth = res.getResponseHeader("X-MathImage-depth");
            
		    if (img.math.depth[0] != '-') {
		    	img.className +=' dp' + img.math.depth;
		    } else {
		    	img.className +=' dp_' + img.math.depth.substring(1);
		    }
            if (post_fn) {
            	post_fn(img);
            }
        })
        .fail(function() {
            alert("Failed to load data");
        });
};

$('body').ready(webtex.init);
