Welcome to the Xerlin XML editor version @version@. 

Xerlin is an opensource XML modeling application 
written for the Java 2 platform (JDK1.2.2 or JDK1.3). 
Xerlin is being released by SpeedLegal Holdings, Inc.
as an opensource project. It is being written by a team
of engineers interested in seeing a nice user interface
for working with XML files.

This software is distributed under an Apache-style license.
Please read the LICENSE.txt file included with this software.

QUICK START
===========

You must install Sun's Java 2 Standard Edition to use Xerlin.
This is available from http://java.sun.com/j2se/  Xerlin
requires at least JDK 1.2.2 (or the JRE equivalent. JDK is 
the developers version, JRE is just the runtime environment)
JDK1.3 should also work.

You can start Xerlin on Windows by double-clicking on the 
@XERLINJAR@ file or on UNIX by running 
"java -jar @XERLINJAR@" This will startup the main 
editor window. There are also shell and bat files provided
for starting up Xerlin.

To start a new file, click on the New File icon, or choose 
"New file" from the File menu. Xerlin will prompt you to
select a DTD. If you do not have any dtd plugins installed,
you will not see any available the first time. Click the
"Browse" button to find a DTD file. When you select a DTD 
installed on your filesystem, Xerlin will ask for a public
identifier. You can click "None" if you don't want a
PUBLICID added to your XML file. NOTE: Xerlin
doesn't handle all DTD files yet. 

Xerlin then asks for a root element name. You can type one in, or
select one from the list.

When you open an existing XML file which Xerlin cannot find 
the DTD for, it will prompt you to find the DTD. 
You can also browse for the DTD at this point. NOTE: Xerlin
caches information about DTD files, so the next time you 
open a file with the same SYSTEM identifier in the DOCTYPE
it will find the DTD you specified earlier. This cache is 
erased when Xerlin exits however.

For more detailed information, refer to the UsersGuide included in the
docs directory, or for the most up-to-date version, visit the documentation
section of the website.

MORE ABOUT XERLIN
=================

This software is in development, so it might
not have all the features you want. We welcome contributions of 
ideas for new features, and even implementations or patches for
those features. You can submit bug reports and requests for new 
features on our bug reporting web page available from the Xerlin
homepage at http://www.xerlin.org/


MAILING LISTS
-------------
If you are interested in contributing to the project, or just want 
to keep up on the latest developments with Xerlin, please join our 
mailing list. 

There are 3 lists, one each for announcements, users, and developers. 
Archives are available for each list. 

See http://www.xerlin.org/mailinglist.shtml for information

WEB SITE
--------
The web site has lots of useful info and the latest versions.
Go to:

http://www.xerlin.org/



CONTRIBUTIONS
=============

We're happy to receive contributions and include them in the Xerlin
project.

For those who want to contribute, first you should join the
mailing list to keep up with source changes and discussions.
You should know or be willing to learn the following technologies:
Java, Swing, and XML.

To submit patches make sure you're patching against the latest
CVS source or a versioned source release. We prefer patches in
unix "diff -u" format.

OTHER NOTES
===========

Xerlin includes software provided by IBM, Apache, and other
third party sources. These are distributed under the licenses included. 
SpeedLegal and these third parties do not provide any 
warranties regarding this software. See the LICENSE file for more legal
information.

