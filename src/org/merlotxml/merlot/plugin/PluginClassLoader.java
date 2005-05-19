/*
====================================================================
Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
====================================================================

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions 
are met:

1. Redistribution of source code must retain the above copyright 
notice, this list of conditions and the following disclaimer. 

2. Redistribution in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the 
documentation and/or other materials provided with the distribution.

3. All advertising materials mentioning features or use of this 
software must display the following acknowledgment:  "This product 
includes software developed by ChannelPoint, Inc. for use in the 
Merlot XML Editor (http://www.merlotxml.org/)."
 
4. Any names trademarked by ChannelPoint, Inc. must not be used to 
endorse or promote products derived from this software without prior
written permission. For written permission, please contact
legal@channelpoint.com.

5.  Products derived from this software may not be called "Merlot"
nor may "Merlot" appear in their names without prior written
permission of ChannelPoint, Inc.

6. Redistribution of any form whatsoever must retain the following
acknowledgment:  "This product includes software developed by 
ChannelPoint, Inc. for use in the Merlot XML Editor 
(http://www.merlotxml.org/)."

THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO 
EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ====================================================================

For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.  
For information on the Merlot project, please see 
http://www.merlotxml.org/.
*/

package org.merlotxml.merlot.plugin;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.merlotxml.merlot.XMLEditorSettings;

/**
 *
 * This classloader loads plugin classes and resources from merlot plugin jar files or
 * plugin development directories.
 * <P>
 * When loading a class or a resource from a jar file, it first looks in the jar directly
 * (as the normal class loader does), then it checks the classes directory, and lastly, it
 * checks for embedded jar files located in a lib directory within the plugin jar file. 
 * <P>
 * To load classes and resource from embedded jar files, the PluginClassLoader must write out 
 * jars in the lib directory to a temporary location to be used by ZipFile.
 *
 *
 * @author Kelly Campbell
 * @author Tim McCune
 * @version $Id: PluginClassLoader.java,v 1.25 2001/10/24 19:13:07 camk Exp $
 */

public class PluginClassLoader extends ClassLoader 
{
    private static final String DIR_CLASSES = "classes";
    private static final String DIR_LIB = "lib";
    
    private static char FILESEP = '/';
    private static String FILESEPSTR = "/";
    private static final String CLASSEXT = ".class";
    

    /** A list of zip files that were embedded in the main plugin zip file */
    private List _embeddedZipFiles = null;

    /** ZipFile objects for embedded zip files. We keep these separate because we need to know
     * the URL for each zip file in order to provide URL's from the find* methods. */
    private Map _cachedZipFiles = new HashMap();

    /** This class loader originated from a source directory */
    private File _sourcedir;

    /** This class loader originated from a zip/jar file */
    private ZipFile _sourcezip;

    /** Original source of the zip file sourcezip points to */
    private File _sourcefile;
	
    /** 
	 * Other classloaders to consult first (like the parent classloader, but for dependencies
	 * among plugins 
	 */
	private List _dependClassloaders;
	

    /**
     * Creates a new PluginClassLoader from a the specified directory or jar file
     *
     * @param source the plugin jar file or directory
     * @see #setSource(File)
     */
	public PluginClassLoader(File source) {
		super(PluginClassLoader.class.getClassLoader());
		setSource(source);
    }

    /**
     * Sets this loader's source to the given directory or jar file.
     *
     * @param source a directory containing the plugin.xml, dtd, and classes for a plugin, or a 
     * jar file containing the same contents.
     *
     * @exception IllegalArgumentException if the source is not a directory or readable Jar file
     */
    public void setSource(File source) 
		throws IllegalArgumentException
    {
		if (source.isDirectory()) {
			_sourcedir = source;
		}
		else {
			try {
				_sourcezip = new ZipFile(source);
				_sourcefile = source;
				_sourcedir = unpackZipFile(_sourcefile);
			}
			catch (ZipException e) {
				throw new IllegalArgumentException("Plugin '"+source+"' is not a directory or a Jar file: "+e.getMessage());
			}
			catch (IOException e) {
				throw new IllegalArgumentException("IOException while trying to use plugin Jar file '"+source+"': "+e.getMessage());
			}
		}
	
    }

   /**
    * Generates an URL class of the resource in a jar file or directory. 
    * Example:<P>
    *
    * <PRE>
    *   &lt;path&gt;.&lt;to&gt;.&lt;some&gt;.&lt;class&gt;.&lt;NameOfClass&gt;.class.getResource(&lt;resourceName&gt;);
    * </PRE>
    *
    * will pass as a parameter 'name' this string value:
    *
    * <PRE>
    * &lt;path&gt;/&lt;to&gt;/&lt;some&gt;/&lt;class&gt;/&lt;resourceName&gt;
    * </PRE>
    *
    * This value is used as an end of the URL. The begining depends on the
    * source of the classes (if we use a JAR file or a directory).
    *
    * @param name Name of the resource as is passed from the standard
    * getResource method. It is a full class name with all '.' replaced by '/'.
    *
    * @return URL Resource URL class or null if no _sourcezip or _sourcedir
    * is specified.
    */
    public URL findResource(String name)
    {
		URL result = null;
	
		// Do we have _sourcezip?  (We are using JAR file)
		//System.out.println(" PluginClassLoader.findResource("+name+")");
		Object dirzip;
		/*
		if (_sourcezip != null) {
			dirzip = _sourcezip;
		}
		else {
		*/
			dirzip = _sourcedir;
			//}
			//	System.out.println("findResource("+name+") dirzip = "+dirzip);
			
	
		if (result == null) result = findResourceInRoot(dirzip, name);
		if (result == null) result = findResourceInClasses(dirzip, name);
		if (result == null) result = findResourceInLib(dirzip, name);

		if (result == null) {
			// not found? check dependencies
			result = findResourceInDependencies(name);
		}
    
		if (result == null) {
			result = super.findResource(name);
		}

	
		//	System.out.println(" findResource result = "+result);
		return result;
	
    }
    
    /**
     * Produces a jar URL of the form jar:file:///path/to/jarfile.jar!/resourcename.
     */
    private URL createJarFileURL(File jarfile, String resourcename) 
    {
		URL result = null;
		try {
			result = new URL("jar:"+jarfile.toURL()+"!/"+resourcename);
		}
		catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		return result;
    }

    /**
     * Reads all the bytes from the specified stream, and returns a byte array containg them
     */
    private byte[] readAllBytes(InputStream is) 
		throws IOException
    {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
	
		int i = 0;
		int off = 0;

	
		i = bis.read();
	
		while (i >= 0) {
			baos.write(i);
			i = bis.read();
		}
		baos.close();
		return baos.toByteArray();
    }
    

    /**
     * Gets a class implementation from the plugin source. Looks in the jar/dir directly first,
     * then checks for a match by prepending the classes directory. If that doesn't work,
     * it checks embedded zip files contained in the lib directory.
     *
     */
	private byte[] getClassImpl(String className) {
		
		//	 System.out.println("        >>>>>> Fetching the implementation of "+className);
		try {
			String fixedname = className.replace('.',FILESEP) + CLASSEXT;
			InputStream is = getResourceAsStream(fixedname);
	    		
			if (is != null) {
				return readAllBytes(is);
			}
			else {
				return null;
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}

    }
    
    protected Class findClass(String name) throws ClassNotFoundException {

		//	System.out.println("        >>>>>> Find class : "+name);
		
		// Try to load it from our repository
		byte  classData[] = getClassImpl(name);
		if (classData == null) {
			throw new ClassNotFoundException(name);
		}
		
		// Define it (parse the class file)
		Class result = defineClass(name, classData, 0, classData.length);
		if (result == null) {
			throw new ClassFormatError();
		}
		
		//	System.out.println("        >>>>>> Returning newly loaded class: "+result);
		return result;
    
    }


    /**
     * This finds a resource and returns a stream of it from a zip file
     */
    protected URL findResourceInZip(File file, String name) 
    {
		URL result = null;
		ZipFile zip = null;
		
		try {
			zip = (ZipFile)_cachedZipFiles.get(file.getAbsolutePath());
			if (zip == null) {
				zip = new ZipFile(file);
			}
			//System.out.println(">>>> PluginClassLoader looking for "+name+" in zip "+ zip.getName());
			ZipEntry sourceEntry;
			sourceEntry = zip.getEntry(name);
			if (sourceEntry != null) {
				result = createJarFileURL(file,name);
				
			}
			else {
				// 		System.out.println("   sourceEntry is null");
			}
			
		}
		catch (IOException e) {
			// ignore
		}
		catch (java.security.AccessControlException ex) {
			//	System.out.println(">>>> PluginClassLoader looking for "+name+" in zip "+ file.getAbsolutePath());
			ex.printStackTrace();
			//	java.security.Permission permission = ex.getPermission();
			//	System.out.println("Requested permission: "+permission);
			
		}
		catch (Exception e) {
			//	e.printStackTrace();
		}
		        
		if (result != null) {
			// 	    System.out.println(">>>> PluginClassLoader found "+name+" in zip "+zip.getName()+" result = "+result);
		}
		
		return result;
    }
    

    /**
     * Writes out embedded zips to temp files
     */
    synchronized protected void copyEmbeddedZipFiles(ZipFile zip) 
    {
		if (_embeddedZipFiles == null) { // assumes one PluginClassLoader per plugin zip file
			_embeddedZipFiles = new ArrayList();
			Enumeration enum = zip.entries();
			while (enum.hasMoreElements()) {
				ZipEntry je = (ZipEntry) enum.nextElement();
				if (je.getName().startsWith(DIR_LIB)) {
					ZipFile childZip;
					int nameStartsAt = je.getName().lastIndexOf('/');
					if (nameStartsAt < 0) {
						nameStartsAt = je.getName().lastIndexOf('\\');
					}
					String filename = je.getName().substring(nameStartsAt+1);
					File tmpFile = null;
					try {
						tmpFile = File.createTempFile("merlot-"+filename,null);
						tmpFile.deleteOnExit();
						BufferedInputStream bis = new BufferedInputStream(zip.getInputStream(je));
						FileOutputStream outstream = new FileOutputStream(tmpFile);
						BufferedOutputStream bos = new BufferedOutputStream(outstream);
						int i;
						while ( (i = bis.read()) > -1) {
							bos.write(i);
						}
						bos.flush();
						bos.close();
						outstream.close();
						bis.close();
						//	System.out.println(">>>> PluginClassLoader wrote temp zip file: "+tmpFile);
			
						ZipFile zf = new ZipFile(tmpFile);
						_embeddedZipFiles.add(tmpFile);
						_cachedZipFiles.put(tmpFile.getAbsolutePath(), zf);
			
					}
					catch (IOException ex) {
						if (tmpFile != null ) {
							tmpFile.delete();
						}
			
					}
		    
				}
			}
		}
    }
    
	/**
	 * Unpacks a plugin file where the plugin is located
	 * @return the directory it unpacked into or null
	 */
	protected File unpackZipFile(File f) 
		throws IOException
	{
		String zipfilename = f.getName();
		if (zipfilename != null) {
			zipfilename = zipfilename.substring(0,zipfilename.lastIndexOf('.'));
		}
		//	System.out.println("zipfilename = "+zipfilename);
		String zipdirname = f.getCanonicalPath();
		int nameStartsAt = zipdirname.lastIndexOf('/');
		if (nameStartsAt < 0) {
			nameStartsAt = zipdirname.lastIndexOf('\\');
		}
		zipdirname = zipdirname.substring(0,nameStartsAt);
		//System.out.println("zipdirname = "+zipdirname);
		
		File unzipDir = new File(zipdirname+"/"+zipfilename);
		//System.out.println("unzipDir = "+unzipDir);
		
		unzipDir.mkdir();
		
		boolean unzip = false;
		// check timestamp
		File timestampFile = new File(unzipDir,"expandedTimestamp");
		if (timestampFile.exists()) {
			if (f.lastModified() > timestampFile.lastModified()) {
				unzip = true;
				// delete the old stuff
				// deleting dirs is expensive cause we have to do it recursively
				// so leave it for now and hope it doesn't become a problem
			}
		}
		else {
			unzip = true;
		}
		if (unzip) {
			ZipFile zip = new ZipFile(f);
			Enumeration enum = zip.entries();
			while (enum.hasMoreElements()) {
				ZipEntry je = (ZipEntry) enum.nextElement();
				ZipFile childZip;
				//	System.out.println("entry: "+je);
				if (je.isDirectory()) {
					File tmpFile = new File(unzipDir, je.getName());
					tmpFile.mkdirs();
				}
				else {
					
					File tmpFile = new File(unzipDir,je.getName());
					try {
						
						BufferedInputStream bis = new BufferedInputStream(zip.getInputStream(je));
						FileOutputStream outstream = new FileOutputStream(tmpFile);
						BufferedOutputStream bos = new BufferedOutputStream(outstream);
						int i;
						while ( (i = bis.read()) > -1) {
							bos.write(i);
						}
						bos.flush();
						bos.close();
						outstream.close();
						bis.close();
						//	System.out.println(">>>> PluginClassLoader wrote temp zip file: "+tmpFile);
						
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				
		    }
			timestampFile.delete();
			timestampFile.createNewFile();
			
		}
		return unzipDir;
		
	}
	


    /**
     * Finds resources in lib/zip files
     */
    protected URL findResourceInLib(Object dirzip, String name) 
    {
		URL result = null;
		//	System.out.println(">>>> PluginClassLoader looking for "+name+" in lib");
	
		if (dirzip instanceof ZipFile) {
			copyEmbeddedZipFiles((ZipFile)dirzip);
			Iterator it = _embeddedZipFiles.iterator();
			while (it.hasNext()) {
				File zf = (File)it.next();
				result = findResourceInZip(zf,name);
				if (result != null) {
					break;
				}
			}
		}
		else if (dirzip instanceof File) {
			String[] libJars = new File((File)dirzip, DIR_LIB).list();
			// ZipFile childZip;
			File childZip;
			if (libJars != null) {
				for (int i = 0; i < libJars.length && result == null; i++) {
					childZip = new File(new String(_sourcedir + FILESEPSTR + DIR_LIB + FILESEPSTR + libJars[i]));
					result = findResourceInZip(childZip,name);
				}	
			}
		}
		if (result != null) {
			//	System.out.println(">>>> PluginClassLoader found "+name+" in lib "+dirzip);
		}
		return result;
    }
    
    /**
     * Finds resources in classes directory
     */
    protected URL findResourceInClasses(Object dirzip, String name) 
    {
		URL result = null;
		//	System.out.println(">>>> PluginClassLoader looking for "+name+" in classes");
	
		if (dirzip instanceof ZipFile) {
			result = findResourceInZip(_sourcefile, DIR_CLASSES + FILESEP + name);
		}
		else if (dirzip instanceof File) {
			File sourceFile = new File((File)dirzip, DIR_CLASSES + FILESEP + name);
			if (sourceFile.exists()) {
				try {
					result = sourceFile.toURL();
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	
		if (result != null) {
			//	    System.out.println(">>>> PluginClassLoader found "+name+" in classes "+dirzip);
		}
		return result;
    }

    /**
     * Finds resources in jar or directory root
     */
    protected URL findResourceInRoot(Object dirzip, String name) 
    {
	URL result = null;
	//	System.out.println(">>>> PluginClassLoader looking for "+name+" in root");
	
	if (dirzip instanceof ZipFile) {
	    result = findResourceInZip(_sourcefile,  name);
	}
	else if (dirzip instanceof File) {
	    File sourceFile = new File((File)dirzip, FILESEP + name);
	    if (sourceFile.exists()) {
		try {
		    result = sourceFile.toURL();
		}
		catch (MalformedURLException e) {
		    e.printStackTrace();
		}
	    }
	}
	
	if (result != null) {
		//	    System.out.println(">>>> PluginClassLoader found "+name+" in root "+dirzip);
	}
	return result;
    }

	void addClassLoader(ClassLoader loader) 
	{
		//		System.out.println(">>>> PluginClassLoader adding dep classloader: "+loader);
		
		if (_dependClassloaders == null) {
			_dependClassloaders = new ArrayList();
		}
		_dependClassloaders.add(loader);
	}
	
	protected URL findResourceInDependencies(String name) 
	{
		URL result = null;
		//	System.out.println(">>>> PluginClassLoader looking for "+name+" in dependencies: "+_dependClassloaders);
		
		if (_dependClassloaders != null) {
			Iterator it = _dependClassloaders.iterator();
			while (result == null && it.hasNext()) {
				PluginClassLoader loader = (PluginClassLoader)it.next();
				result = loader.findResource(name);
			}
		}
		//	if (result != null) System.out.println(">>>> PluginClassLoader found "+name+" in dependency classloader");
		
		return result;
	}
	
	protected String findLibrary(String libname) 
	{
		// can't delegate to the parent from here since findLibrary is protected
		// look in the lib/os/platform dir
		String osName, osArch, osLibName;
		switch (XMLEditorSettings.getOSType()) {
		case XMLEditorSettings.WINDOWS:
			osName="win32";
			break;
		case XMLEditorSettings.SOLARIS:
			osName="solaris";
			break;
		case XMLEditorSettings.LINUX:
			osName="linux";
			break;
		case XMLEditorSettings.MACOS:
			osName="macos";
			break;
		default:
			return null;
		}
		osArch = System.getProperty("os.arch");
		if (osArch.equals("i386")) {
			osArch = "x86";
		}
		osLibName = System.mapLibraryName(libname);
		String filename = _sourcedir+"/lib/"+osName+"/"+osArch+"/"+osLibName;
		
		//	System.out.println("looking for "+filename);
		File libFile = new File(filename);
		if (libFile.exists()) {
			//		System.out.println("  >>> found "+libFile.getAbsolutePath());
			return libFile.getAbsolutePath();
		}
		return findLibraryInDependencies(libname);
		
	}
  
	protected String findLibraryInDependencies(String libname) 
	{
		
		String result = null;
		//	System.out.println(">>>> PluginClassLoader looking for library "+libname+" in dependencies: "+_dependClassloaders);
		
		if (_dependClassloaders != null) {
			Iterator it = _dependClassloaders.iterator();
			while (result == null && it.hasNext()) {
				PluginClassLoader loader = (PluginClassLoader)it.next();
				result = loader.findLibrary(libname);
			}
		}
		//	if (result != null) System.out.println(">>>> PluginClassLoader found "+libname+" in dependency classloader");
		
		return result;	
	}
	
	File getPluginDir() 
	{
		return _sourcedir;
	}
	
}
    
