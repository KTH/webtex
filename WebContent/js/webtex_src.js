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
webtex.imgSrc = $('script[src]').last().attr('src').replace('js/webtex_src.js', 'WebTex?');

//Function to transform the whole document.  Add SRC to each IMG with
//ALT text starting with "tex:".  However, skip if element already
//has a SRC.  Note that 'src=""' may be a non-empty src, because the
//browser will prefix the base URL.
webtex.init = function () {
	// All images with alt starting with 'tex:' and no src attribute.
    $('img[alt^="tex\\:"]:not([src])')
    	.addClass('tex')
    	.each(function(index, img) {
    		var tex_src = img.alt.substring(4);
			if (webtex.allowResize) {
				img.src = webtex.imgSrc 
					+ 'D=' + webtex.contextSize(img)
					+ '&tex=' + encodeURIComponent(tex_src);
			} else {
				img.src = webtex.imgSrc 
					+ 'tex=' + encodeURIComponent(tex_src);
			}
		    // Fetch to get depth.
	        webtex.httpRequest(img);
	    });
    $('#webtex.error').hide();
};

webtex.contextSize = function(img) {
	var fs = $(img).css('font-size'), s;

    if (fs.substr(fs.indexOf('p')) == 'pt') {
    	s = Math.round(fs.substr(0, pos)/12);
    } else {
    	s = Math.round(fs.substr(0, pos)/11);
    }

    s = Math.max(s, webtex.MAX_D);

    console.log('Debug: font size: %s | D=%s', fs, s);
    return s;
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
            console.log("Failed to load data: %s", img.src);
        });
};

$('body').ready(webtex.init);
