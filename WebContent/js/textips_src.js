// Javascript used to drive WebTex demo page.

// Copyright: (c) 2009-2012 KTH, Royal Institute of Technology, Stockholm, Sweden
// Author: Fredrik Jönsson <fjo@kth.se>
//
// Based on:
// Copyright: (c) 2007 The Open University, Milton Keynes, UK
// Author: Jonathan Fine <jfine@pytex.org>, <J.Fine@open.ac.uk>

webtex.textips = {
	submit : function() {
		var params = {tex : $('#tex_src').val()},
			img = document.createElement('img');
		$(img).addClass('tex').attr('src', webtex.url + $.param(params));
		webtex.httpRequest(img, function() {
			$('#log').html(img.webtex.log);
		});
		$('#output').html(img);
		return false;
	},

	init : function () {
	    $('#button').click(webtex.textips.submit);
	    $('#form').submit(webtex.textips.submit);
	    $('div.textips img').click(function() {
	    	$('#tex_src').val(this.webtex.tex);
	    	$('#button').click();
	    });
	    $('#webtex.textips.error').hide();
	}
};

$(document).ready(webtex.textips.init);
