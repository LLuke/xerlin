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
http://www.merlotxml.org.
*/


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.net.*;
import java.util.*;
import javax.swing.*;

import java.awt.event.*;
import java.lang.reflect.*;

import org.merlotxml.util.StringUtil;

/**
 * Resource loader for Merlot. This accesses resource bundles in a 
 * generalized way
 */

public class MerlotResource 
{
    protected static HashMap _bundles = new HashMap();
    
    
    public static String FILESEP = System.getProperty("file.separator");
    
    protected static String _classPrepend = "org.merlotxml.merlot.resource";
    
    protected final static String MERLOT_GRAPHICS_DIR = "merlot.graphics.dir";
    
    
    private static Hashtable _keycodes;

    /**
     * Returns a string from a resource bundle.
     * @param bname the application name
     * @param key the resource key
     */
    
    public static String getString(String bname, String key)
	throws MissingResourceException
    {

	return getStringImpl(bname,key,Locale.getDefault());
	
    }
    
    /**
     * Returns a string from a resource bundle.
     * @param bname the application name
     * @param key the resource key
     */
    
    public static String getString(String bname, String key, Locale lc)
	throws MissingResourceException
    {
	return getStringImpl(bname,key,lc);
	
    }
    
    /**
     * Gets an image file and loads it
     */
    public static ImageIcon getImage(String bname, String key) 
    {
	return getImageImpl(bname,key,Locale.getDefault());
				
    }
	
    public static ImageIcon getImage(String bname, String key, Locale lc) 
    {
	return getImageImpl(bname,key,lc);
    }

    public static KeyStroke getKeyStroke(String bname, String key)
    {	
	
	String keycode = getString(bname,key);
	return getKeyStrokeImpl(keycode);
    }
    
    public static KeyStroke getKeyStroke(String bname, String key, Locale lc)
    {

	String keycode = getString(bname,key,lc);
	return getKeyStrokeImpl(keycode);
    }
	

	
	
    private static String getStringImpl(String bname, String key, Locale lc) 
	throws MissingResourceException
    {
	ResourceBundle bun = loadBundle(bname,lc);
	if (bun == null) {
	    throw new MissingResourceException("Bundle not found",bname,key);
	}
	String s = bun.getString(key);
	StringUtil.KeyFinder finder = new MyKeyFinder(bname);
		
	String ret = StringUtil.lookupKeysInString(s,finder);
	return ret;
		
    }
	
	
    private static ResourceBundle loadBundle(String name, Locale locale) 
    
    {
	String bundlekey = name +"."+locale.toString();
		
	ResourceBundle rb = (ResourceBundle)_bundles.get(bundlekey);
	if (rb == null && !_bundles.containsKey(name)) {
	    rb = ResourceBundle.getBundle(_classPrepend+"."+name, locale);
	    _bundles.put(bundlekey,rb);
	}
	return rb;
		
		
    }
    
    protected static ImageIcon getImageImpl(String bname, String key, Locale locale)
    {
	
	
	ResourceBundle bun = loadBundle(bname,locale);
	if (bun == null) {
	    throw new MissingResourceException("Bundle not found",bname,key);
	}
	String filename = bun.getString(key);
	return loadImage(bname,filename);
		
		
    }
	

    protected static ImageIcon loadImage(String bname, String filename) 
    {
	return loadImage(bname, filename, false) ;
    }
	
    protected static ImageIcon loadImage(String bname, String filename, boolean recursing) 
	throws MissingResourceException
    {
		
	ImageIcon i = null;

	if (filename != null) {
	    try {
		try {
		    // hack to make images load from a jar
		    // file via getResource when they're within
		    // a subdir. Basically, the class that loads 
		    // them via getResource must be in the same dir.
		    Class imageloader = MerlotResource.class;
										
		    String imgldrclass = getString(bname, "iconloader");
		    if (imgldrclass != null) {
			try {
			    Class tmploader  = Class.forName(imgldrclass);
			    imageloader = tmploader;
			}
			catch (ClassNotFoundException ex){
			}
		    }

					
		    URL u = imageloader.getResource(filename);
		    if (u != null) {
			i = new ImageIcon(u);
		    }
		    else {
			u = new URL(filename);
						
			i = new ImageIcon(u);
		    }
		}
		catch (java.net.MalformedURLException mf) {
		}
				
		if (i == null && !recursing) {
		    // try prepending the graphics dir
		    String gdir = getString(bname,MERLOT_GRAPHICS_DIR);
		    return loadImage(bname,gdir+FILESEP+filename,true);
		}
				
	    }
	    catch (Exception e) {
		MerlotDebug.exception(e);
	    }
	}
	return i;
		
    
    }
	
    protected static KeyStroke getKeyStrokeImpl(String keycode)
    {
	//	String keycode = ResourceCatalog.getString(rr);
	int modifiers = 0;
	char c = '\0';
		
	if (keycode != null) {
	    // now try to parse the keycode
	    StringTokenizer tok = new StringTokenizer(keycode,"-");
	    while (tok.hasMoreTokens()) {
		String t = tok.nextToken();
		if (t.equalsIgnoreCase("cmd")) {
		    // platform default command key
		    modifiers |= getCommandKeyMask();
		}
		else if (t.equalsIgnoreCase("shift")) {
		    modifiers |= java.awt.Event.SHIFT_MASK;
		}
		else if (t.equalsIgnoreCase("ctrl")) {
		    modifiers |= java.awt.Event.CTRL_MASK;
		}
		else if (t.equalsIgnoreCase("meta")) {
		    modifiers |= java.awt.Event.META_MASK;
		}
		else if (t.equalsIgnoreCase("alt")) {
		    modifiers |= java.awt.Event.ALT_MASK;
		}
		else if (t.length() == 1) {
		    c = t.toUpperCase().charAt(0);
		}
		else if (t.startsWith("VK_")) {
				
		    c = (char)getKeyCodeNamed(t);
					
		    // XXX get a keycode field via 
		    // reflection
		}
				
	    }
	    if ((int)c > 0) {
		KeyStroke ks = KeyStroke.getKeyStroke((int)c,modifiers);
		return ks;
	    }
	}
	return null;
		
    }
	
    /**
     * Returns the platform's preferred command key.
     * This is CTRL on unix and windows, and META on Mac
     */
    protected static int getCommandKeyMask() 
    {
	int os = XMLEditorSettings.getOSType();
	switch (os) {
	case XMLEditorSettings.MACOS:
	    return java.awt.Event.META_MASK;
	default:
	    return java.awt.Event.CTRL_MASK;
	}
		
    }
    

    protected static int getKeyCodeNamed(String n) 
    {
	if (_keycodes == null) {
	    loadKeyCodes();
	}	
	Object o = _keycodes.get(n);
	if (o instanceof Integer) {
	    return ((Integer)o).intValue();
	}
	return -1;
				
    }
	

    protected static void loadKeyCodes() 
    {
	_keycodes = new Hashtable();
	KeyEvent evt = new KeyEvent(new java.awt.Label(""),0,0L,0,0);
		
	Field[] f = KeyEvent.class.getDeclaredFields();
	for (int i=0;i<f.length;i++) {
	    String name = f[i].getName();
	    if (name.startsWith("VK_")) {
		try {
		    int val = f[i].getInt(evt);
		    _keycodes.put(name,new Integer(val));
		    //		MerlotDebug.msg("keycode: "+name+" = "+val);
					
		}
		catch (Exception ex) {
		}
				
	    }
			
			
	}
		
		
    }
	
    protected static class MyKeyFinder implements StringUtil.KeyFinder 
    {
	String _bundle;
		
	public MyKeyFinder(String bundle) 
	{
	    _bundle = bundle;
	}
		
	public String lookupString(String key) 
	{
	    String ret = null;
	    try {
		ret = MerlotResource.getString(_bundle,key);
	    }
	    catch (Exception ex) {
	    }
	    if (ret == null) {
		ret = XMLEditorSettings.getSharedInstance().getProperty(key);
	    }
	    return ret;
	}
		
		
    }
	
}
