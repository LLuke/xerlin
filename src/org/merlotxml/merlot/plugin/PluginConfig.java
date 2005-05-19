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

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;

import org.merlotxml.merlot.*;
import org.merlotxml.util.xml.*;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

/**
 * Merlot Plugin Configuration
 * 
 * @author Tim McCune
 * @version	$Id: PluginConfig.java,v 1.6 2001/09/27 16:13:33 camk Exp $
 */

public abstract class PluginConfig {
	
	//Constants
	protected static final String XPATH_TEXT = "/text()";
	protected static final String XPATH_PLUGIN = "/*";
	protected static final String XPATH_LONG_NAME = XPATH_PLUGIN + "/longName" + XPATH_TEXT;
	protected static final String XPATH_NAME = XPATH_PLUGIN + "/name" + XPATH_TEXT;
	protected static final String XPATH_VERSION = XPATH_PLUGIN + "/version" + XPATH_TEXT;
	protected static final String XPATH_AUTHOR = XPATH_PLUGIN + "/author" + XPATH_TEXT;
	protected static final String XPATH_URL = XPATH_PLUGIN + "/url" + XPATH_TEXT;
	protected static final String XPATH_DEPENDENCIES = XPATH_PLUGIN + "/dependency" + XPATH_TEXT;
	
	// Attributes
	protected File source;
	protected String longName;
	protected String name;
	protected String version;
	protected String author;
	protected URL url;
	private Node _node;

	// Associations
	/** the classloader which this plugin uses to find its classes */
	protected ClassLoader classLoader;

	/** The plugin manager instance */
	private PluginManager _pluginManager;

	/** List of other PluginConfigs this plugin requires */
	private List _dependencies;

	// Operations
	
	protected PluginConfig(PluginManager manager, ClassLoader loader, File source) {
		this.classLoader = loader;
		this.source = source;
		_pluginManager = manager;
		
	}
	

	protected void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	/**
	 * Parse the default elements common to all plugins.
	 *
	 * @exception MalformedURLException Thrown if a URL value was supplied that
	 *		is not a valid URL
	 * @exception SAXException Thrown if the configuration XML is incorrect
	 * @exception PluginConfigException Not thrown here, but declared in case
	 *		a subclass needs to throw it
	 */
	protected void parse(Node node) 
		throws MalformedURLException, SAXException,
			   PluginConfigException
	{

		String s;
		_node = node;
		name = XPathUtil.getValue(node, XPATH_NAME);
		longName = XPathUtil.getValue(node, XPATH_LONG_NAME);
		version = XPathUtil.getValue(node, XPATH_VERSION);
		author = XPathUtil.getValue(node, XPATH_AUTHOR);
		if ( (s = XPathUtil.getValue(node, XPATH_URL)) != null) {
			url = new URL(s);
		}

		// check for dependencies on other plugins
		_dependencies = XPathUtil.getValueList(node, XPATH_DEPENDENCIES);
				
		String msg = MerlotResource.getString(MerlotConstants.UI,"splash.loadingPlugins.msg");
	    XMLEditorSettings.getSharedInstance().showSplashStatus(msg+" "+name);
		
	}
	
	protected void resolveDependencies() 
		throws PluginConfigException
	{
		if (_dependencies != null) {
			ArrayList tmpDependencies = new ArrayList();
			Iterator it = _dependencies.iterator();
			while (it.hasNext()) {
				String depName = (String)it.next();
				PluginConfig config = _pluginManager.getPlugin(depName);
				if (config == null) {
					System.out.println(name+" depends on "+depName+" but the dependency couldn't be found.");		
					_dependencies = null;
					throw new PluginConfigException("Plugin '"+name+"' depends on '"+depName+"' but it was not found.");
				}
				else {
					tmpDependencies.add(config);
				  
					((PluginClassLoader)classLoader).addClassLoader(config.classLoader);
					
					MerlotDebug.msg(name+" depends on "+depName);
				}
			}
			_dependencies = tmpDependencies;
		}
	}
	
	protected void init() throws PluginConfigException
	{
	}
	
	
	protected void setSource(File source) {
		this.source = source;
	}
	
	public File getSource() {
		return source;
	}
	
	public AbstractAction getAboutAction() {
		return new AboutAction();		
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		StringBuffer rtn = new StringBuffer();
		rtn.append("name: " + name + "\n");
		rtn.append("long name: " + longName + "\n");
		rtn.append("version: " + version + "\n");
		rtn.append("author: " + author + "\n");
		rtn.append("url: " + url + "\n");
		rtn.append("myPluginManager: " + _pluginManager + "\n");
		return rtn.toString();
	}
	
	private class AboutAction extends AbstractAction {
		
		private static final int ABOUT_COLS = 50;
		
		public AboutAction() {
			putValue(MerlotConstants.ACTION_NAME, name);
		}
		
		public void actionPerformed(ActionEvent event) {
		
			JPanel panel;
			Object[] formatArgs = {name};
			String titleFormat = MerlotResource.getString(MerlotConstants.UI, "plugin.about.title");
			String title = MessageFormat.format(titleFormat, formatArgs);
			StringBuffer about = new StringBuffer();
			
			if (longName != null) {
				about.append(longName + " ");
			}
			else if (name != null) {
				about.append(name + " ");
			}
			if (version != null) {
				about.append(version + "\n");
			}
			if (author != null) {
				about.append(author + "\n");
			}
			if (url != null) {
				about.append(url + "\n");
			}
			
			panel = MerlotUtils.createMultiLineLabel(about.toString(), ABOUT_COLS);
			MerlotOptionPane.showInternalConfirmDialog(XMLEditorFrame.getSharedInstance().getDesktopPane(),
													   panel,
													   title,
													   JOptionPane.DEFAULT_OPTION,
													   JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	public Node getNode() 
	{
		return _node;
	}
}
