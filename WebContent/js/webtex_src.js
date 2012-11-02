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
   <script type="text/javascript" src=".../js/jquery.js"></script>
   <script type="text/javascript" src=".../js/webtex.js"></script>
   ...
   <p>
   <img alt="tex:x^2+y^2=1" />
   <img alt="tex:\sin^2\theta + \cos^2\theta = 1" />
   </p>
   ...
 */

webtex = {
	MAX_D : 10,
	
	//Function to transform the whole document.  Add SRC to each IMG with
	//ALT text starting with "tex:".  However, skip if element already
	//has a SRC.  Note that 'src=""' may be a non-empty src, because the
	//browser will prefix the base URL.
	init : function () {
	    $('img[alt^="tex\\:"]:not([src])')
	    	.addClass('tex')
	    	.each(function(index, img) {
	    		var params = {tex : img.alt.substring(4)};
	    		// Support for adaptation of size to surrounding text.
//	    		params.D = webtex.contextSize(img);
				img.src = webtex.url + $.param(params);
			    // Fetch to get depth.
		        webtex.httpRequest(img);
		    });
	    $('#webtex.error').hide();
	},

//	contextSize : function(img) {
//		var fs = $(img).css('font-size'), s;
//	
//	    if (fs.match(/pt$/ig)) {
//	    	s = Math.round(parseInt(fs)/12);
//	    } else {
//	    	s = Math.round(parseInt(fs)/11);
//	    }
//	
//	    s = Math.max(s, webtex.MAX_D);
//	
//	    console.log('Debug: font size: %s | D=%s', fs, s);
//	    return s;
//	},

	httpRequest : function(img, post_fn) {
		$.ajax(img.src)
	        .done(function(d, s, xhr) {
	        	img.math = {
	        			log : decodeURIComponent(xhr.getResponseHeader("X-MathImage-log")),
	        			depth : xhr.getResponseHeader("X-MathImage-depth")
	        	};
	 	    	$(img).addClass('dp' + img.math.depth.replace('-', '_'));
			    if (post_fn) {
			    	post_fn(img);
	            }
	        })
	        .fail(function(d, s, xhr) {
	            console.log("Failed to load data: %s: %s", s, img.src);
	        });
	}
};

//Set the path to the service script relative.
webtex.url = $('script[src]').last().attr('src').replace(/js\/webtex(_src)?\.js$/g, 'WebTex?');

$(document).ready(webtex.init);
