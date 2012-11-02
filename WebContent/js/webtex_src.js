// Javascript that uses WebTex to add images to your web page.

// Copyright: (c) 2009-2012 KTH, Royal Institute of Technology, Stockholm, Sweden
// Author: Fredrik Jönsson <fjo@kth.se>
//
// Based on:
// Copyright: (c) 2007 The Open University, Milton Keynes, UK
// Author: Jonathan Fine <jfine@pytex.org>, <J.Fine@open.ac.uk>

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
	
	// Add src to each img class webtex and alt text starting with "tex:".
	init : function () {
	    $('img.webtex[alt^="tex\\:"]')
	    	.each(function(index, img) {
			    // This fetches in reality twice. Browser will load the image
	    		// when src is set, but we have to fetch again to get the headers.
	    		// The fetch should be cached by the browser and not cost anything.
	    		var params = {tex : img.alt.substring(4)};
//	    		params.D = webtex.size(img);
				img.src = webtex.url + $.param(params);
		        webtex.httpRequest(img);
		    });
	},

	// Try to set a size depending on font-size in surrounding.
//	size : function(img) {
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
	        	img.webtex = {
	        			log : decodeURIComponent(xhr.getResponseHeader("X-MathImage-log")),
	        			depth : xhr.getResponseHeader("X-MathImage-depth"),
	        			tex : decodeURIComponent(xhr.getResponseHeader("X-MathImage-tex"))
	        	};
	 	    	$(img).addClass('dp' + img.webtex.depth.replace('-', '_'));
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
