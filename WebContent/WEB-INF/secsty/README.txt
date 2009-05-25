# Copyright: (c) 2007 The Open University, Milton Keynes, UK.
# License: GPL version 2 or (at your option) any later version.
# Author: Jonathan Fine <jfine@pytex.org>, <J.Fine@open.ac.uk>
# $Source$
# $Revision: 131 $ $Date: 2008-04-18 11:42:48 +0200 (Fri, 18 Apr 2008) $

Secure style files

Secure style files are style files that can be safely used with a TeX
daemon.  We are aware of four security problems that can affect style
files - corruption, poisoning, killing and denial of service.
Examples of each (for plain TeX) follow.

Corruption: \let\alpha\beta will cause wrong behaviour.

Poisoning: \csname\the\inputlineno\endcsname will, when repeatedly
executed, consume the hash space, causing TeX to exit.  Poisoning is
the cumulative consumption of resources over several calls to the TeX
daemon.

Killing: \end will cause the TeX daemon to come to an end.  Similarly,
enough '{'s in a row will exceed TeX's grouping levels capacity.
Killing is sudden, whereas poisoning is gradual.

Denial of service: \def\x{\x} \x will put TeX into an endless loop.

Of these problems, corruption is probably the worst.  The others can
prevent answers being given, but corruption is the giving of wrong
answers.  If you are aware of any other classes of problems, please
let me know.

To install secplain.fmt, first run 
    $ initex secplain.sty
(and if 'initex' is not found then try this)
    $ tex --ini secplain.sty
which will generate ouput something like
    This is TeX, Version 3.14159 (Web2C 7.4.5) (INITEX)
    (./secplain.sty (./secmove.sty) (./seccode.sty)
    \_maxdimen=\_dimen10
    \_hideskip=\_skip10
    [...]
    \font\_cmti10=cmti10
    11970 words of font info for 40 preloaded fonts
    14 hyphenation exceptions
    Hyphenation trie of length 6075 has 181 ops out of 35111
      181 for language 0
    No pages of output.
    Transcript written on secplain.log.

You will then need to place it where tex can find it.  On Debian it is
    /var/lib/texmf/web2c/
and so the command
    # mv secplain.fmt /var/lib/texmf/web2c/
will move the file.  You will also have to update the file-name
database tex uses
    # mktexlsr

If you're not sure where to put secplain.fmt, try running 
   locate plain.fmt tex.fmt latex.fmt
(your system may not have all of these files) and put it in the same
folder.

To test the installation of secplain
    $ tex '&secplain' '\_end'
    This is TeX, Version 3.14159 (Web2C 7.4.5)
    No pages of output.
    Transcript written on texput.log.

Well done.  That should take care of the installation of secplain.
