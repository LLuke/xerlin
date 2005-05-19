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


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.awt.*;
import java.io.*;

import java.net.*;
import java.util.*;
import javax.swing.*;

import javax.swing.plaf.*;


import org.merlotxml.util.*;


/**
 * 
 * Settings access for the app
 * 
 * @author Kelly A. Campbell
 *
 * @version $Id: XMLEditorSettings.java,v 1.26 2002/09/23 23:28:35 justin Exp $
 *
 */
public class XMLEditorSettings implements MerlotConstants
{

	public final static int LARGE_ICON = 32;
	public final static int SMALL_ICON = 16;
	
	
    protected final static String APP_ICON_SMALL = "app.icon.s";
    protected final static String APP_ICON_LARGE = "app.icon.l";
    
	protected final static String BKG_COLOR    = "background.color";
	protected final static String BKG_PICTURE  = "background.picture";
	protected final static String FRAME_TITLE  = "frame.title";
	protected final static String INSET        = "frame.inset";
	
	protected final static String TOP_DISPLACE = "top.displace";
	protected final static String BOT_DISPLACE = "bot.displace";
	
	protected final static String DOM_LIAISON  = "xml.dom.liaison";
	
	protected final static String FILTER_NODES = "merlot.filternodes";
	
	/**
	 * Various debugging stuff like opening a certain file quickly
	 * instead of making the user walk through the file chooser
	 */
	protected final static String DEBUG_MODE   = "merlot.debug";
	
	protected final static String SUPPRESS_ADD = "merlot.filteradds";

    protected final static String WRITE_ATTS = "merlot.write.default-atts";
	
	protected final static String EDITOR_LIST = "merlot.editor.classes";

	protected final static String ICON_DIR_PROP  = "merlot.icon.dir";
	protected final static String ICON_PROP_FILE = "merlot.icon.props";
	protected final static String MERLOT_ICON_PREFIX = "micon.";
	
	
	protected final static String DEFAULT_LF    = "merlot.default.lookandfeel";
	protected final static String DEFAULT_LIB   = "merlot.default.library";
	protected final static String DEFAULT_EDITOR= "merlot.default.editorclass";
	
	protected final static String SPLASH_SCREEN = "xerlin.splashscreen";
	
	protected final static String RESOURCE_PACKAGE = "merlot.resource.package";
	
	protected final static String COUNTRY  = "country";
	protected final static String LANGUAGE = "language";
	protected final static String VARIANT  = "variant";
	
	// this is different from the icon dir for now... icons & plugins will move
	// to resource bundles eventually
	protected final static String MERLOT_GRAPHICS_DIR = "merlot.graphics.dir";
	protected final static String ICON_LOADER      = "merlot.iconloader.class";
	protected final static String APPICON_LOADER      = "merlot.appiconloader.class";
	
	
	protected final static String MERLOT_RESOURCE_PATH = "merlot.resource.path";
	protected final static String UNDO_LIMIT = "undo.limit";

    protected final static String MERLOT_PLUGIN_PATH = "merlot.plugin.path";
	
	private final static String SYS_PROP_PLUGIN_URLS = "org.merlotxml.merlot.pluginURLs";

    private final static String INDENT_OUTPUT = "xerlin.indent.output";
    
    
    
    
    public static String FILESEP = System.getProperty("file.separator");
    public static String USER_DIR = System.getProperty("user.home");
    public static String WORKING_DIR = ".merlot";

	public static File USER_MERLOT_DIR = new File(USER_DIR
												  + FILESEP
												  + WORKING_DIR);

	protected String _propsFile = "merlot.properties";
 

	protected Properties _props; //properties for user home dir	
    protected Properties _defaultProps;// properties for merlot dir
	protected String[]   _editorList = null;
	protected String[]	 _openFiles;
	protected Hashtable  _icons;
    private boolean _showSplash;
	

	
	protected static XMLEditorSettings _settings = null;
	
    protected MerlotSplashScreen _splash = null;
    

	public XMLEditorSettings(String[] args, boolean showSplash) {
        _settings = this;
        _showSplash = showSplash;
		Properties defaults = getDefaults();
		_openFiles = parseArgs(args);
        _defaultProps  = loadDefPropsFile(defaults); //default preference
        _props = loadPropsFile(_defaultProps);       //user preference
        reparseDefines(args);
        MerlotDebug.init(_props);

	    if (_showSplash)	
		    startSplashScreen();
		initIcons();
    }

    public XMLEditorSettings(String[] args) {
        this(args, true);
    }
	
	public Properties getDefaults() 
	{
		Properties defaults = new Properties();
        defaults.put(APP_ICON_SMALL, "xerlin16icon.gif");
        defaults.put(APP_ICON_LARGE, "xerlin32icon.gif");
        
		defaults.put(BKG_PICTURE, "xerlin.gif");
		defaults.put(BKG_COLOR,   "0xffffff");
		defaults.put(FRAME_TITLE, "Xerlin"); 
		defaults.put(INSET,       "75");
		defaults.put(TOP_DISPLACE,"0");	  // extra displacement frm top of screen
		defaults.put(BOT_DISPLACE, "75"); // extra displacement frm bot of screen
	
		defaults.put(DOM_LIAISON,   "org.merlotxml.util.xml.xml4j.DOMLiaison");
		
		defaults.put(FILTER_NODES, "true");
		defaults.put(DEBUG_MODE,   "false");
		defaults.put(SUPPRESS_ADD, "true");
        defaults.put(WRITE_ATTS, "false");
        defaults.put(INDENT_OUTPUT, "true");
	
		defaults.put(DEFAULT_LF,"default");
        //	defaults.put(DEFAULT_LIB,"lib/library.xmllib");
		defaults.put(SPLASH_SCREEN, "xerlinsplash.gif");
		defaults.put(RESOURCE_PACKAGE, "org.merlotxml.merlot.resource");
	
		defaults.put(MERLOT_GRAPHICS_DIR,"appicons");
		defaults.put(ICON_LOADER,"org.merlotxml.merlot.icons.ImageLoader");
		defaults.put(APPICON_LOADER, "org.merlotxml.merlot.appicons.ImageLoader");
		defaults.put(DEFAULT_EDITOR, "org.merlotxml.merlot.GenericDOMEditor");
		defaults.put(MERLOT_RESOURCE_PATH, "org.merlotxml.merlot.resource");
        defaults.put(MERLOT_PLUGIN_PATH, "plugins");
        
		defaults.put(UNDO_LIMIT, "10");
		
		
        return defaults;
		
	}

	/**
         * This allows a property to be set/modified
         * Useful within plugin architecture so that some kind
	 * of memory can be achieved - i.e. last directory accessed,
	 * username/password last used etc. 
         */

	public void setProperty(String propertyName, String propertyValue)
        {
        	_props.put(propertyName, propertyValue);
        }



    protected void reparseDefines(String[] args) 
    {
        	Iterator iter;
		java.util.List openList = new ArrayList();
		String a;
		String[] rtn;
		
		for (int i = 0; i < args.length; i++) {
			a = args[i];
			MerlotDebug.msg("args["+i+"] = "+a);
			
            if (a.equals("-d")) {
				// set a resource property
				if (i+1 < args.length) {
					String s = args[++i];
					String key, val;
					int x = s.indexOf('=');
					if (x >= 0) {
						key = s.substring(0,x).trim();
						val = s.substring(x+1).trim();
						
						MerlotDebug.msg("setting '"+key+"' to '"+val+"'");
						_props.put(key,val);
                        
						if (key.startsWith("merlot.debug")) {
							MerlotDebug.reloadSettings();
						}
                    }
				}
                else {
					printUsage("-d requires a parameter of the form key=value");
				}
			
			}
			
		}
		
			
        
    }
    

	/**
	 * @return An array of file names to open, or an empty array
	 *		if none were supplied.
	 */
	protected String[] parseArgs(String[] args) 
	{
		Iterator iter;
		java.util.List openList = new ArrayList();
		String a;
		String[] rtn;
		
		for (int i = 0; i < args.length; i++) {
			a = args[i];
			MerlotDebug.msg("args["+i+"] = "+a);
			
			if (a.equals("-f")) {
				// different props file
				if (i+1 < args.length) {
					_propsFile = args[++i];
					MerlotDebug.msg("Props file = "+_propsFile);
					
				}
				else {
					printUsage("-f requires a parameter");
				}
			}
			else if (a.equals("-d")) {
                // we reparse these later on
			   
				if (i+1 < args.length) {
					String s = args[++i];
                }
            }

			else if (a.equals("-o")) {
                //change System.out and System.err to point to a file
			   
				if (i+1 < args.length) {
					String s = args[++i];
					try {
						FileOutputStream outStream = new FileOutputStream(s,true);
						PrintStream out = new PrintStream(outStream);
						System.out.println("Redirecting output and errors to '"+s+"'");
						System.setOut(out);
						System.setErr(out);
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
					
                }
            }
			else if (a.equals("-h") || a.equals("--help")) {
				printUsage("");
			}
			else if (a.startsWith("-")) {
				printUsage("Unknown argument: "+a);
			}
			else {
				openList.add(args[i]);
			}
		}
		
		rtn = new String[openList.size()];
		iter = openList.iterator();
		for (int i = 0; i < openList.size(); i++) {
			rtn[i] = (String) iter.next();
		}
		return rtn;			
				
	}
	
	public String[] getOpenFiles() {
		return _openFiles;
	}
	
	protected void printUsage(String msg) 
	{
		System.err.println(msg);
		System.err.println("Usage: merlot [options] <files>");
		System.err.println(" Options: ");
		System.err.println("          [-f  properties file] What merlot.properties to startup with.");
		System.err.println("          [-d key=value]        Define a property. Example: -d merlot.debug=true");
		System.err.println("          [-h or --help]        This message.");
		
		System.exit(-1);
	}
	

	protected Properties loadDefPropsFile(Properties defaults) 
	{
		Properties myprops = new Properties(defaults);
     	URL u = null;
		
		try {
			u = new URL(_propsFile);
		}
		catch (Exception ex) {
			u = this.getClass().getResource(_propsFile);
		}
		
		if (u != null) {
			MerlotDebug.msg("Loading properties from "+u.toString());
			try {			
				InputStream is = u.openStream();
				if (is != null) {
					
					myprops.load(is);
				}
				
			}
			catch (Exception ex) {
				MerlotDebug.exception(ex);
			}
			
		}
		else {
			MerlotDebug.msg("Properties file not found. Using internal defaults.");
		}
		
		return myprops;
		
	}
    
    protected Properties loadPropsFile(Properties defaults) 
	{   
        Properties myprops = new Properties(defaults);
        
        File  f = null; 
        InputStream is = null; 
        try {
            f = new File(USER_MERLOT_DIR, _propsFile);
            is = new FileInputStream(f);
            myprops.load(is);
             
            is.close();
            
        }
		catch(Exception ex) {
			MerlotDebug.msg("Properties file not found. Using internal defaults.");
            
		}
		return myprops;
		
	}

	public String getProperty(String s)  
	{
		return loadKeys(_props.getProperty(s));
	}
	
    public Properties getProperties() 
    {
        return _props;
    }
    public Properties getDefaultProperties() 
    {
        return _defaultProps;
    }

	public static XMLEditorSettings getSharedInstance()
	{
		return _settings;
	}
	

	/**
	 * Gets the background picture specified in the properties if it exists
	 *
	 * @return ImageIcon of the background pic or null if not found
	 */
	
	public ImageIcon getBackgroundImage () 
	{
		return (loadImageFromProp(BKG_PICTURE));
		
		
	}
  
    public ImageIcon getAppIconSmall() 
    {
        return (loadImageFromProp(APP_ICON_SMALL));
        
    }
    public ImageIcon getAppIconLarge()
    {
         return (loadImageFromProp(APP_ICON_LARGE));
    }
    

	public ImageIcon loadImage(String filename, String loaderclassname) 
	{
		return loadImage(filename, loaderclassname, false) ;
	}
	
	protected ImageIcon loadImage(String filename, 
								  String imgldrclass, 
								  boolean recursing) 
	{
		
		ImageIcon i = null;
		//	String val = getProperty(prop_name);
		if (filename != null) {
			try {
				try {
					// total hack to make images load from a jar
					// file via getResource when they're within
					// a subdir. Basically, the class that loads 
					// them via getResource must be in the same dir.
					Class imageloader = this.getClass();
					
					
					//	String imgldrclass = getProperty(IMAGE_LOADER);
					if (imgldrclass != null) {
						try {
							Class tmploader  = Class.forName(imgldrclass);
							imageloader = tmploader;
						}
						catch (ClassNotFoundException ex){
						}
					}
											
					//	MerlotDebug.msg("val = " + val);
					URL u = imageloader.getResource(filename);
					if (u != null) {
						i = new ImageIcon(u);
					}
					else {
						u = new URL(filename);
					
						i = new ImageIcon(u);
					}

				}
				catch (MalformedURLException mf) {
				}
				// hack hack hack cough hack
				if (i == null && !recursing) {
					// try prepending the graphics dir
					String appiconloader = getProperty(APPICON_LOADER);
					return loadImage(filename,appiconloader,true);
					
					/*	String gdir = getProperty(MERLOT_GRAPHICS_DIR);
					return loadImage(gdir+FILESEP+filename,true);
					*/
				}
			}
			catch (Exception e) {
				MerlotDebug.exception(e);
			}
		}
		//	MerlotDebug.msg("i = " + i);
		
		return i;
		
    
  }

	protected ImageIcon loadImageFromProp(String propname) 
	{
		String val = getProperty(propname);
		if (val != null) {
			return loadImage(val,getProperty(ICON_LOADER));
		}
		return null;
		
	}
	

	/**
	 * Gets the property named 'background.color'
	 *
	 * @return the color property or a default color of Black
	 *
	 */

	public Color getBackgroundColor()
	{
		
		Color bkgColor = null;
		
		String val = getProperty(BKG_COLOR);
		if (val != null) {
			bkgColor = Color.decode(val);
		}
		
		if (bkgColor == null) {
			bkgColor = Color.white;
		}
		
		return bkgColor;
		
	}
	
    public int getFrameInset() 
	{
		Integer i = Integer.valueOf(getProperty(INSET));
		return i.intValue();
	}
	
	public String getFrameTitle() 
	{
		return getProperty(FRAME_TITLE);
	}
	
	public int getTopDisplacement() 
	{
		Integer i = Integer.valueOf(getProperty(TOP_DISPLACE));
		return i.intValue();
	}
	
	public int getBottomDisplacement() 
	{
		Integer i = Integer.valueOf(getProperty(BOT_DISPLACE));
		return i.intValue();
	}

	public String getDOMLiaisonClassname() 
	{
		return getProperty(DOM_LIAISON);
	}
	
	private boolean stringToBoolean (String s) 
	{
		if (s != null) {
			if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes")) {
				return true;
			}
		}
		return false;
		
	}
	
	public boolean isFilteringNodes() 
	{
		return stringToBoolean(getProperty(FILTER_NODES));
	}
	
	public boolean isDebugOn() 
	{
		return debugModeOn();
		
	}
	

	public boolean debugModeOn() 
	{
		return stringToBoolean(getProperty(DEBUG_MODE));
	}

	public boolean getSuppressAddMenuItems() 
	{
		return stringToBoolean(getProperty(SUPPRESS_ADD));
	}

    public String getEditors() 
    {
        return getProperty(EDITOR_LIST);
    }

	public String getDefaultEditor() 
	{
		return getProperty(DEFAULT_EDITOR);
	}
	

	/**
	 * Initializes our icon cache in it's own thread
	 */
	protected void initIcons() 
	{
		// need to make this load icons and plugins from resource bundles
	    
	    String msg = MerlotResource.getString(UI,"splash.loadingIcons.msg");
        if (_showSplash)
	        showSplashStatus(msg);
	    
		Properties iconProps = null;
		_icons = new Hashtable();
		
		// first see if we have anyplace to get the icons from
		String iconfile = getProperty(ICON_PROP_FILE);
		if (iconfile != null) {
			if (iconfile.equals("this")) {
				iconProps = _props;
			}
			else {
				try {
					iconProps = new Properties();
					URL u = this.getClass().getResource(iconfile);
					if (u == null) {
						u = new URL(iconfile);
					}
					
					if (u != null) {
						InputStream is = u.openStream();
						iconProps.load(is);
					}

						
				} 
				catch (Exception ex) {
					MerlotDebug.exception(ex);
				}
				
			}
			// check for a icon location
			String prepend = iconProps.getProperty(ICON_DIR_PROP);
			//	String filesep = System.getProperty("file.separator");
			Enumeration e = iconProps.keys();
			while (e.hasMoreElements()) {
				String s = (String)e.nextElement();
				if (s.startsWith(MERLOT_ICON_PREFIX)) {
					String filename = iconProps.getProperty(s);
					if (prepend != null) {
						filename = prepend + FILESEP + filename;
					}
					addIcon(s,filename);
				}
			}
		}
		
	}
	
	/**
	 * loads an imageicon and adds it to the icons hashtable
	 */
	protected void addIcon(String key, String filename) 
	{
		// strip off the prefix
		String eltype  = null;
		String size    = null;
		ImageIcon icon = null;
		Class me = this.getClass();
		URL u = null;
		String iconloader = getProperty(ICON_LOADER);
		
		try {
			String iconkey = key.substring(MERLOT_ICON_PREFIX.length());
			//	MerlotDebug.msg("iconkey = "+iconkey + " filename = " + filename);
			// first word should be the dtd element type, 
			// second should be (small | large)
			int dot = iconkey.indexOf('.');
			if (dot > 0) {
				icon = loadImage(filename,iconloader);
				if (icon != null) {
					_icons.put(iconkey,icon);
				}
				
			}
		}
		catch (Exception ex) {
			MerlotDebug.exception(ex);
		}
		
				
	}
	

	/**
	 * This returns an image icon for the given name and size (SMALL, LARGE).
	 * If the icon isn't found, null is returned
	 */
	public ImageIcon getIcon(String name, int size) 
	{
		String iconsize;
		switch (size) {
		case SMALL_ICON:
			iconsize = "s";
			break;
		case LARGE_ICON:
			iconsize = "l";
			break;
		default:
			iconsize = "s";
			break;
		}
		
		String iconkey = name + "." + iconsize;
		//	MerlotDebug.msg("Looking for icon: "+iconkey);
		
		Object o = _icons.get(iconkey);
		if (o instanceof ImageIcon) {
			//	MerlotDebug.msg("found icon: "+o);
			
			return (ImageIcon)o;
		}
		return null;
				
	}
	
	
	public String getLookAndFeel () 
	{
		return getProperty(DEFAULT_LF);
	}

	public String getDefaultLibrary() 
	{
		return getProperty(DEFAULT_LIB);
	}

	public ImageIcon getSplashScreenImage()
	{
		ImageIcon icon = loadImageFromProp(SPLASH_SCREEN);
		return icon;
		
		
	}
	
	
    public void startSplashScreen() 
    {
		// load the image for the splash screen
		ImageIcon icon = getSplashScreenImage();
		
		if (icon != null) {
            MerlotDebug.msg("Showing splash screen");
			
			_splash = new MerlotSplashScreen(icon);
			// that's it.. now set the status
		}
		
    }
    
    public void showSplashStatus(String s) 
    {
		if (_splash != null) {
			_splash.showStatus(s);
		}
		
    }
    public void closeSplash() 
    {
        MerlotDebug.msg("Closing splash screen");
		if (_splash != null) {
			_splash.close();
			_splash = null;
			// make it gc'able
		}
		
    }
    
    public String getResourcePackage() 
	{
		return getProperty(RESOURCE_PACKAGE);
   
	}
	
	public Locale getLocale() 
	{
		String country  = getProperty(COUNTRY);
		String language = getProperty(LANGUAGE);
		String other    = getProperty(VARIANT);
		if (country != null && language != null) {
			Locale l;
			if (other != null) {
				l = new Locale(language,country,other);
			}
			else {
				l = new Locale(language,country);
			}
			return l;
		}
		return Locale.getDefault();
				
	}
	
    public int getUndoLimit() 
    {
	String s = getProperty(UNDO_LIMIT);
	try {
	    int ul = Integer.parseInt(s);
	    return ul;
	}
	catch (Exception ex) {
	}
	return 10;
		
    }
   
    
	protected  String loadKeys(String str) 
		throws MissingResourceException, UnsupportedOperationException
	{

		// this is where all those years of c/c++ pay off in java
		boolean foundkey = false;
		if (str != null) {
			StringBuffer sb = new StringBuffer(str);
			//	MerlotDebug.msg("loading keys for '"+str+"'");
									  
			int len = sb.length();
			// now go through the string looking for "{%"
			StringBuffer newsb = new StringBuffer(sb.capacity());
			int i, j = 0, k = 0;
			for (i=0;i<len;i++) {
				char c = sb.charAt(i);
				if ((c == '{') && (i+2 < len) && (sb.charAt(i+1) == '%')) {
					// we got a potential key
					int endkey = -1;
					StringBuffer key = new StringBuffer();
					for  (j=i+2; j+1 < len && endkey < 0; j++) {
						if (sb.charAt(j) == '%' && sb.charAt(j+1) == '}') {
							endkey = j-1;
						}
						else {
							key.append(sb.charAt(j));
						}
					}
					if (endkey > 0) {
					
			
						try {
						
							// try the local property file first
							String s = getProperty(key.toString());
							if (s == null) {
                                // XXX
							}
							newsb.append(s);
							i = endkey+2;
							foundkey = true;
						}
						catch (MissingResourceException ex) {
						}
					}
				}
				if (!foundkey) {
					newsb.append(c);
					k++;
				}
				foundkey = false;
				
				
			}
			return newsb.toString();
		}
		return null;
		
		
	}

	public boolean useJFileChooser() 
	{
		
		int ostype = getOSType();
		switch (ostype) {
		default:
		case SOLARIS:
		case LINUX:
			return true;
		case WINDOWS:
		case MACOS:
			return false;
		}
	
	}

	// os types
	public static final int WINDOWS = 1;
	public static final int SOLARIS = 2;
	public static final int LINUX   = 3;
	public static final int MACOS   = 4;


	public static int getOSType() 
	{
		String s = System.getProperty("os.name").toLowerCase();
		if (s.indexOf("windows") >= 0) {
			return WINDOWS;
		}
		if (s.indexOf("sunos") >= 0) {
			return SOLARIS;
		}
		
		if (s.indexOf("linux") >= 0) {
			return LINUX;
		}
		if ((s.indexOf("mac") >= 0) || (s.indexOf("mac os") >= 0)) {
			return MACOS;
		}
		MerlotDebug.msg("[debug] OSType = "+s+" Returning WINDOWS");
		
		return WINDOWS;
		
	}
    
    public void saveProperties(Properties props){
        //create merlot dir if it does not exist
        File f = new File (USER_DIR
                           + FILESEP
                           + WORKING_DIR);

        if (!f.exists() && !f.isDirectory())
            f.mkdir();
        
        //create property file if it does not exist
        f = new File(USER_DIR
                     + FILESEP
                     + WORKING_DIR
                     + FILESEP
                     + _propsFile);
        try {
             
            if (!f.exists())
                f.createNewFile();
        
        } catch (Exception ex) {
            MerlotDebug.msg("Error, unable to save property file");
            return;
        }

        OutputStream out = null;
        try {
		    out = new FileOutputStream(f);
            props.store(out,"merlot.properties") ;
      	}
		catch (Exception ex) {
            MerlotDebug.msg("Error, unable to save property file");
        }finally{  
       		if (out != null) {
                try { 
                    out.close(); 
                } catch (java.io.IOException e) {
                }
                out = null;
            }
		}
    } 
	
    /**
     * returns the plugin path property all parsed up into separate directories
     *
     * @return List containing valid File objects which are directories containing 
     * plugins
     */
    public java.util.List getPluginPath() 
    {
        String p = getProperty(MERLOT_PLUGIN_PATH);
        if (p != null) {
            StringTokenizer tok = new StringTokenizer(p,":;");
            ArrayList list = new ArrayList();
            while (tok.hasMoreTokens()) {
                String s =tok.nextToken();
                File f = new File(s);
                if (f.exists() && f.canRead() && f.isDirectory()) {
                    list.add(f);
                }
            }
            return list;
        }
        return null;
    }
	
	/**
	 * @return A list of URLs that identify plugins that should be made available
	 */
	public java.util.List getPluginURLs() throws MalformedURLException {
		
		java.util.List rtn = new ArrayList();
		String s = System.getProperty(SYS_PROP_PLUGIN_URLS);
		
		if (s != null) {
			StringTokenizer tok = new StringTokenizer(s, " ");
			while (tok.hasMoreTokens()) {
				rtn.add(new URL(tok.nextToken()));
			}
		}
		return rtn;
	}
     
}
