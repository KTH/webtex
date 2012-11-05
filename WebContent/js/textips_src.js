/*
  Copyright: (C) 2009-2012 KTH, Kungliga tekniska hogskolan, http://www.kth.se/
  Author: Fredrik Jonsson <fjo@kth.se>
  Copyright: (C) 2007 The Open University, Milton Keynes, UK
  Author: Jonathan Fine <jfine@pytex.org>, <J.Fine@open.ac.uk>


  Javascript used to drive WebTex demo page.

  This file is part of WebTex.

  WebTex is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  WebTex is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with WebTex.  If not, see <http://www.gnu.org/licenses/>.
*/  

webtex.textips = {
	submit : function() {
		var params = {tex : $('#tex_src').val()},
			img = document.createElement('img');
		$(img).addClass('webtex').attr('src', webtex.url + $.param(params));
		webtex.httpRequest(img, {async : false});
		$('#output').html(img);
		$('#log').html(img.webtex.log);
		return false;
	},

	init : function () {
	    $('#button').click(webtex.textips.submit);
	    $('#form').submit(webtex.textips.submit);
	    $('div.textips img').click(function() {
	    	$('#tex_src').val(this.webtex.tex);
	    	$('#button').click();
	    });
	    $('#not_initialized').hide();
	}
};

$(document).ready(webtex.textips.init);
