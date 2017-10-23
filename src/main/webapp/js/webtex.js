/*
  Copyright: (C) 2009-2012 KTH, Kungliga tekniska hogskolan, http://www.kth.se/
  Author: Fredrik Jonsson <fjo@kth.se>
  Copyright: (C) 2007 The Open University, Milton Keynes, UK
  Author: Jonathan Fine <jfine@pytex.org>, <J.Fine@open.ac.uk>


  Javascript that uses WebTex to add images to your web page.

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
    init : function() {
        $('img.webtex[alt^="tex\\:"]').each(function(index, img) {
            // This fetches in reality twice. Browser will load the image
            // when src is set, but we have to fetch again to get the headers.
            // The fetch should be cached by the browser and not cost anything.
            var params = {
                tex : img.alt.substring(4)
            };
            // params.D = webtex.size(img);
            img.src = webtex.url + $.param(params);
            webtex.httpRequest(img);
        });
    },

    // Try to set a size depending on font-size in surrounding.
// size : function(img) {
//      var fs = $(img).css('font-size'), s;

//      if (fs.match(/pt$/ig)) {
//          s = Math.round(parseInt(fs)/12);
//      } else {
//          s = Math.round(parseInt(fs)/11);
//      }

//      s = Math.max(s, webtex.MAX_D);

//      console.log('Debug: font size: %s | D=%s', fs, s);
//      return s;
// },

    httpRequest : function(img, settings) {
        $.ajax(img.src, settings)
            .done(
                function(d, s, xhr) {
                    img.webtex = {
                        log : decodeURIComponent(xhr.getResponseHeader("X-MathImage-log")),
                        tex : decodeURIComponent(xhr.getResponseHeader("X-MathImage-tex")),
                        depth : xhr.getResponseHeader("X-MathImage-depth"),
                        width : xhr.getResponseHeader("X-MathImage-width"),
                        height : xhr.getResponseHeader("X-MathImage-height")
                    };
                    $(img).addClass('dp' + img.webtex.depth.replace('-', '_'))
                        .attr('width', img.webtex.width)
                        .attr('height', img.webtex.height);
            })
            .fail(function(d, s, xhr) {
                console.log("Failed to load data: %s: %s", s, img.src);
            });
    }
};

// Set the path to the service script relative.
webtex.url = $('script[src]').last().attr('src').replace(/js\/webtex(-min)?\.js$/g, 'WebTex?');

$(document).ready(webtex.init);
