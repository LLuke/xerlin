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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.merlotxml.merlot.MerlotConstants;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.merlot.MerlotResource;
import org.merlotxml.merlot.XMLEditor;
import org.merlotxml.merlot.XMLEditorSettings;
import org.merlotxml.merlot.plugin.action.ActionPluginConfig;
import org.merlotxml.merlot.plugin.dtd.DTDPluginConfig;
import org.merlotxml.merlot.plugin.nodeAction.NodeActionPluginConfig;
import org.merlotxml.util.FileUtil;
import org.merlotxml.util.xml.DOMLiaisonImplException;
import org.merlotxml.util.xml.ValidDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Merlot Plugin Manager		<p>
 * 
 * Responsible for locating and loading all Merlot plugins,
 * and keeping track of them.<P>
 * 
 * Plugin initialization is a two-stage process. The first stage is loading the plugin 
 * configuration. This retrieves information from the plugin.xml file. The second
 * stage is initialization which is when the plugin's resources are initialized and 
 * classes can be loaded. These two stages allow for plugins to name dependencies on
 * other plugins, and their classloaders are linked together. However since plugins
 * are read in filesystem order, the dependencies can't be resolved until all configs
 * have been read.<p>
 *
 * Stage 1 is implemented by the {@link #loadPlugins} method. Stage 2 is implemented by
 * the {@link #initPlugins} method.<P>
 *
 * NOTE: Circular plugin dependencies are not checked for, and will cause a fatal
 * error in classloading if they exist.<P>
 * 
 * @author Tim McCune
 * @author Kelly Campbell
 */

public class PluginManager
{
	//Constants
	protected static final String ERR_PLUGIN_NOT_RECOGNIZED = "Unrecognized plugin format";
	protected static final String ERR_DUP_PLUGIN = "Plugin named {0} already loaded";
	public static final String PLUGIN_CONFIG_FILE = "plugin.xml";
	private static final String TMP_PLUGIN_PREFIX = "merlotPlugin";
	private static final String TMP_PLUGIN_SUFFIX = ".jar";
	
	//Attributes
	private static final Object mutex = new Object();
	protected static PluginManager instance;
    private String currentFilePath;	

	/** Map of plugin configs keyed by the name */
	private Map _plugins;
	

	//Methods
	
	public static PluginManager getInstance() {
		if (instance != null) {
			return instance;
		}
		else {
			synchronized (mutex) {
				if (instance == null) {
					instance = new PluginManager();
				}
				return instance;
			}
		}	
	}
	
	protected PluginManager() {
		_plugins = new HashMap();
	}
	
	public List getPlugins() {
		return new ArrayList(_plugins.values());
	}

	public PluginConfig getPlugin(String name) 
	{
		return (PluginConfig)_plugins.get(name);
		
	}
	

    public String getCurrentFilePath() {
        return currentFilePath;
    }
	
	/**
	 * @exception PluginManagerException Thrown if a plugin's config file is in
	 *		an unrecognized format
	 * @exception IOException Thrown if a plugin couldn't be read
	 * @exception InstantiationException Thrown if there was a problem creating
	 *		an XML parser
	 * @exception IllegalAccessException Thrown if there was a problem creating
	 *		an XML parser
	 * @exception ClassNotFoundException Thrown if there was a problem creating
	 *		an XML parser
	 * @exception DOMLiaisonImplException Thrown if there was a problem creating
	 *		an XML parser
	 * @exception MalformedURLException Thrown if a plugin provided a malformed
	 *		URL in its config file
	 * @exception SAXException Thrown if the plugin config file contains malformed XML
	 * @exception PluginConfigException Thrown if there was a plugin-specific error
	 */
	public void loadPlugins()
	throws PluginManagerException, IOException, InstantiationException,
		   IllegalAccessException, ClassNotFoundException, DOMLiaisonImplException,
		   MalformedURLException, SAXException, PluginConfigException
	{
	    String msg = MerlotResource.getString(MerlotConstants.UI,"splash.loadingPlugins.msg");
	    XMLEditorSettings.getSharedInstance().showSplashStatus(msg);

		// get the path list from the settings
		List path = XMLEditorSettings.getSharedInstance().getPluginPath();
		if (path != null) {
			Iterator it = path.iterator();
			while (it.hasNext()) {
			    try {
					File f = (File) it.next();
					searchForPlugins(f);
			    }
			    catch (Exception ex) {
					MerlotDebug.exception(ex);
			    }
			}
		}
		
		// Add the URL list from the settings
		Iterator iter = XMLEditorSettings.getSharedInstance().getPluginURLs().iterator();
		while (iter.hasNext()) {
		    URL  u = (URL) iter.next();
		    XMLEditorSettings.getSharedInstance().showSplashStatus(msg+" "+u);
			File f = downloadURL(u);
			if (f != null) {
				initPlugin(f);
			}
			
		}
		resolveDependencies();
		initPlugins();
		
		
	}
	
	protected void searchForPlugins(File dir)
	throws PluginManagerException, IOException, InstantiationException,
		   IllegalAccessException, ClassNotFoundException, DOMLiaisonImplException,
		   MalformedURLException, SAXException, PluginConfigException
	{
		// two phase loading - load jars first and dirs second so that jars that are newer than 
		// the dir they unpacked into are loaded
		File[] files = dir.listFiles();
		
		if (files != null) {
			
			//For every jar file
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					initPlugin(files[i]);
				}
			}
			//For every directory file
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					initPlugin(files[i]);
				}
			}
			
		}
	}
	
	private void initPlugin(File pluginFile)
	throws PluginManagerException, IOException, InstantiationException,
		   IllegalAccessException, ClassNotFoundException, DOMLiaisonImplException,
		   MalformedURLException, SAXException, PluginConfigException
	{			
		PluginClassLoader cl;
		File configFile;
		int j;
		PluginConfig pc = null;
		String classpath;
		String s;
		ZipEntry configEntry;
		ZipFile nextZipFile;
			
		cl = new PluginClassLoader(pluginFile);
		pluginFile = cl.getPluginDir();
		
		//See if there is a plugin.xml file in the root
		//	System.out.println("plugin file = "+pluginFile);
		
		if (pluginFile.isDirectory()) {
			configFile = new File(pluginFile, PLUGIN_CONFIG_FILE);
			
			//If there is,
			if (configFile.exists() && configFile.canRead() && configFile.isFile()) {
				try {
							
					//Create a new plugin config
                    currentFilePath = pluginFile.getPath();
					pc = createPluginConfig(new FileInputStream(configFile), pluginFile, cl);
				}
				catch (Exception e) {
				    MerlotDebug.exception(e);
				}
			}
			//If not, just ignore this directory.
		}
		else {
			try {
				nextZipFile = new ZipFile(pluginFile);
			}
			catch (ZipException e) {
				//This is not a zip file.  No problem, just skip it.
				return;
			}
			
			//Make sure we're not duplicating a directory plugin
			s = pluginFile.getAbsolutePath();
			if ( (j = s.indexOf(".jar")) == -1) {
				j = s.indexOf(".zip");
			}
			if (j > -1) {
				s = s.substring(0, j) + File.separator + PLUGIN_CONFIG_FILE;
				if (new File(s).exists()) {
					return;
				}
			}
					
			//If there is, create a new plugin config
			if ( (configEntry = nextZipFile.getEntry(PLUGIN_CONFIG_FILE)) != null) {
			    try {
                    currentFilePath = nextZipFile.getName(); 
					pc = createPluginConfig(nextZipFile.getInputStream(configEntry), pluginFile, cl);
				}
				catch (Exception e) {
					MerlotDebug.exception(e);
			    }
			}
			nextZipFile.close();
		}

        // Accept the first form of the plugin if there are multiple definitions.
        if (pc != null && !  _plugins.containsKey(pc.getName()))  {
            //and add it to our list
            //System.out.println("Added plugin: "+pc.getName());
            _plugins.put(pc.getName(),pc);
        } else {
            System.out.println("Duplicate plugin definition [" + pc.getName() +"] found. Accepting first instance encountered.");
        }
	}
	
	protected PluginConfig createPluginConfig(InputStream input,
													 File source, 
													 ClassLoader cl)
		throws PluginManagerException, 
			   InstantiationException,
			   IllegalAccessException,
			   ClassNotFoundException, 
			   DOMLiaisonImplException, 
			   MalformedURLException,
			   SAXException, 
			   PluginConfigException
	{
		Document doc = null;
		try {
			ValidDocument vDoc = XMLEditor.getSharedInstance().getDOMLiaison().parseValidXMLStream(input, source.toString());
			doc = vDoc.getDocument();
			Node firstElement;
			PluginConfig rtn;
			String nodeName;
		
			if ( (firstElement = (Element)doc.getDocumentElement()) != null) {
				nodeName = firstElement.getNodeName();
			}
			else {
				nodeName = "";
			}
			//System.out.println("trying to load plugin: "+source);
			
			if (nodeName.equals("action-plugin")) {
				rtn = new ActionPluginConfig(this,cl,source);
			}
			else if (nodeName.equals("dtd-plugin")) {
				rtn = new DTDPluginConfig(this,cl,source);
			}
			else if (nodeName.equals("node-action-plugin")) {
				rtn = new NodeActionPluginConfig(this,cl,source);
			}
			else {
				throw new PluginManagerException(ERR_PLUGIN_NOT_RECOGNIZED + ": \"" + nodeName + "\"");
			}
			rtn.parse(doc);
			return rtn;		
		}
		catch (Exception ex) {
			System.out.println( "Exception loading plugin document: " + ex );
			throw new PluginConfigException(ex);
		}

	}

	
	private File downloadContent(URLConnection connection, File cacheFile) 
	{
		try {
			// download to a temp file
			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
			File outFile = File.createTempFile(TMP_PLUGIN_PREFIX, TMP_PLUGIN_SUFFIX);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
			int i;
			while ( (i = in.read()) != -1) {
				out.write(i);
			}
			out.flush();
			out.close();
			in.close();

			FileUtil.copyFile(outFile, cacheFile);
			outFile.delete();
			//outFile.deleteOnExit();
			
			return cacheFile;
			
		}
		catch (IOException ex) {
			MerlotDebug.exception(ex);
		}
		return null;
		
	}
	
	private File downloadURL(URL url) 
	{
		MerlotDebug.msg("Downloading URL: "+url);
		
		String filename = url.getFile();
		if (filename.indexOf('/') >= 0) {
			filename = filename.substring(filename.lastIndexOf('/')+1);
		}
		File userPluginsDir = new File(XMLEditorSettings.USER_MERLOT_DIR,"plugins");
		
		File cache = new File(userPluginsDir,filename);

		try {
			// check cache for local copy of this url
			
			if (!userPluginsDir.exists()) {
				userPluginsDir.mkdirs();
			}
			
			URLConnection connection = url.openConnection();
			
			if (cache.exists() && cache.canRead()) {
				// now check the timestamp on the file vs. the server
				connection.connect();
				long remoteTimestamp = connection.getLastModified();
				//	System.out.println("remoteTimestamp = "+connection.getLastModified()+" local timestamp = "+cache.lastModified());
				
				if (remoteTimestamp == 0 || remoteTimestamp > cache.lastModified()) {
					cache = downloadContent(connection,cache);
				}
				else {
					MerlotDebug.msg("Using cached version for URL: "+url);
				}
				
			}
			else {
				cache = downloadContent(connection,cache);
			}
			

		}
		catch (IOException ex) {
			MerlotDebug.exception(ex);
		}
		if (cache != null && cache.exists()) {
			return cache;
		}
		else {
			return null;
		}
	}
	

	/** 
	 * Go through all the plugins and tell each to resolve its dependencies
	 */
	private void resolveDependencies() 
	{
		Iterator it = _plugins.values().iterator();
		while (it.hasNext()) {
			try {
				PluginConfig config = (PluginConfig)it.next();
				config.resolveDependencies();
			}
			catch (PluginConfigException ex) {
				MerlotDebug.exception(ex);
			}
		}
	}

	/** 
	 * Go through all the plugins and tell each to resolve its dependencies
	 */
	private void initPlugins() 
	{
		ArrayList badPlugins = new ArrayList();
		
		Iterator it = _plugins.values().iterator();
		PluginConfig config = null;
		
		while (it.hasNext()) {
			try {
				config = (PluginConfig)it.next();
				config.init();
			}
			catch (PluginConfigException ex) {
				System.err.println("Plugin "+config.getName()+" could not be loaded due to an error");
				badPlugins.add(config);
				ex.printStackTrace();			}
			catch (Throwable t) {
				System.err.println("Plugin "+config.getName()+" could not be loaded due to an error");
				badPlugins.add(config);
				t.printStackTrace();
				
			}
		}
		it = badPlugins.iterator();
		while (it.hasNext()) {
			config = (PluginConfig)it.next();
			_plugins.remove(config.getName());
		}
		
	}	


	/**
	 * Tester
	 */
	public static void main(String[] args) {
		
		try {
			PluginManager pm = new PluginManager();
			pm.searchForPlugins(new File(args[0]));
			System.out.println(pm.getPlugins());
			//PluginConfig config = createPluginConfig(new FileInputStream(args[0]));
			//System.out.print(config.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
