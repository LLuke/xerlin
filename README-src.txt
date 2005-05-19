This is information about buidling and running Xerlin from 
the source code.

To build Xerlin, you can use Ant with the build.xml file. Download the ant
distribution from http://jakarta.apache.org.

If you downloaded the source distribution from the web site, you will
also need the jar files from the lib directory of the binary distribution
to build. These jars are also included in the CVS repository. They are not 
included with the source distribution to avoid duplication for people who
download both the binary and source distributions. 

The jars in the lib directory are:
StrutLayout1.2.1.jar
xalan-1.0.jar
xerces-1.0.3.jar


Xerlin is primarily developed on RedHat Linux with the Blackdown
or Sun JDK and WindowsNT/2000 with Sun's JDK1.2.2. 
If you run into build problems on other platforms,
help us fix it by giving us a detailed report of the problems. 

Look at the setclasspath.csh file for an example of what you will
need to have in your classpath to build Xerlin. To actually build,
type  "ant" from the src directory. 

To run Xerlin, you can use Xerlin.bat or Xerlin.sh or use the
commandline "java org.merlotxml.merlot.XMLEditor"

The source is organized into a couple of packages:

org.merlotxml.
              awt:         The PercentLayout LayoutManager

	      merlot:	   The main xerlin source
		    plugin: Plugin support classes

	      util:	   Utility classes used by xerlin, 
			   including the xml parser adapters

The "lib" directory contains some third-party sources required
by Xerlin, along with some patches for those 
third-party components. The license files for these thirdparty
sources are located in the doc directory.

Xerlin currently uses Apache's Xerces as it's parser. 

One of the main layout managers Xerlin uses is a patched version
of Matthew Phillips' StrutLayout class. The unpatched version is
available from

http://www.ozemail.com.au/~mpp/strutlayout/doc/overview.html

The patched version is included as StrutLayout-1.2.1.


We also use as one of our components, Sun's JTreeTable available
from The Swing Connection:

http://java.sun.com/products/jfc/tsc/articles/treetable2/

CVS
====

Instructions for accessing the source via anonymous cvs are
available from the website. There is also a browsable version
using CVSWeb online.

SUBMITTING PATCHES
==================

We prefer patches in a unix "diff -u" unified diff format. Submit 
patches either to the mailing list or to the authors directly 
at xerlin@xerlin.org

