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

package org.merlotxml.merlot.plugin.dtd;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.merlot.plugin.PluginConfig;
import org.merlotxml.merlot.plugin.PluginConfigException;
import org.merlotxml.merlot.plugin.PluginManager;
import org.merlotxml.util.IconUtil;
import org.merlotxml.util.xml.XPathUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Merlot DTD Plugin Configuration
 * 
 * @author Tim McCune
 */

public class DTDPluginConfig extends PluginConfig {
	
	//Constants
	public static final int ICON_SIZE_LARGE = 0;
	public static final int ICON_SIZE_SMALL = 1;
	
	protected static final String XPATH_DEFAULT_EDITOR = XPATH_PLUGIN + "/defaultEditor";
	protected static final String XPATH_DTD = XPATH_PLUGIN + "/dtd";
	protected static final String XPATH_EDITOR = XPATH_PLUGIN + "/editor";
	protected static final String XPATH_ICON = XPATH_PLUGIN + "/icon";
	protected static final String XPATH_TREE = XPATH_PLUGIN + "/tree-table";
	
	//Associations
	protected DefaultEditorConfig _defaultEditorConfig;
	protected List _dtdConfigs;
	protected List _editorConfigs;
	protected List _iconConfigs;
	protected TreeTableConfig _treeTableConfig;
	protected DisplayTextConfig _displayTextConfig;
	protected Map iconMap = new HashMap();
	
	//Operations

	public DTDPluginConfig(PluginManager manager, 
						   ClassLoader loader, 
						   File source) {
		super(manager,loader,source);
	}

	
	/**
	 * @exception PluginConfigException Thrown if there was a problem
	 *		loading a DTD or an Editor object
	 */
	public void parse(Node node) throws PluginConfigException
	{
		try {
			super.parse(node);
			MerlotDebug.msg("Loading DTDPlugin " + this.name);	
			DTDConfig newDTDConfig;
			EditorConfig newEditorConfig;
			IconConfig newIconConfig;
			int i;
			Node defaultNode;
			NodeList configList;
			
			if ( (defaultNode = XPathUtil.selectSingleNode(node, "//defaultEditor")) != null) {
				_defaultEditorConfig = new DefaultEditorConfig(classLoader);
                _defaultEditorConfig.parse(defaultNode);
			}
			
			configList = XPathUtil.selectNodes(node, XPATH_DTD);
			for (i = 0; i < configList.getLength(); i++) {
				if (_dtdConfigs == null) {
					_dtdConfigs = new ArrayList();
				}
				
				newDTDConfig = new DTDConfig();
				newDTDConfig.setParent(this);
				try {
					newDTDConfig.parse(configList.item(i));
					_dtdConfigs.add(newDTDConfig);
				}
				catch (FileNotFoundException fnf) {
					MerlotDebug.msg("DTD File not found while loading dtd config: "+fnf.getMessage());
				}
				catch (Exception ex) {
					MerlotDebug.exception(ex);
				}
				
			}
			
			configList = XPathUtil.selectNodes(node, XPATH_EDITOR);
			for (i = 0; i < configList.getLength(); i++) {
				if (_editorConfigs == null) {
					_editorConfigs = new ArrayList();
				}
				try {
					newEditorConfig = new EditorConfig(classLoader);
					newEditorConfig.parse(configList.item(i));
					_editorConfigs.add(newEditorConfig);
				}
				catch (ClassNotFoundException cnf) {
					MerlotDebug.msg("Class not found: "+cnf.getMessage());
				}
			}
			
			configList = XPathUtil.selectNodes(node, XPATH_ICON);
			for (i = 0; i < configList.getLength(); i++) {
				if (_iconConfigs == null) {
					_iconConfigs = new ArrayList();
				}
				newIconConfig = new IconConfig();
				newIconConfig.setParent(this);
				try {
					newIconConfig.parse(configList.item(i));
					_iconConfigs.add(newIconConfig);
				}
				catch (Exception ex) {
					MerlotDebug.msg("Exception while parsing icon config: "+ex.getMessage());
				}
				
			}
			
			Node treeTableNode = XPathUtil.selectSingleNode(node, "//tree-table" );
			if ( treeTableNode != null )
			{
				_treeTableConfig = new TreeTableConfig();
				try {
					_treeTableConfig.parse( treeTableNode );
				}
				catch (Exception ex) {
					MerlotDebug.msg("Exception while parsing tree table config: "+ex.getMessage());
				}
			}
			else
				MerlotDebug.msg( "Plugin does not contain a tree-table node.");

			Node displayTextNode = XPathUtil.selectSingleNode(node, "//display-text" );
			if ( displayTextNode != null )
			{
				_displayTextConfig = new DisplayTextConfig();
				try {
					_displayTextConfig.parse( node );
				}
				catch (Exception ex) {
					MerlotDebug.msg("Exception while parsing display text config: "+ex );
				}
			}
			else
				MerlotDebug.msg( "Plugin does not contain a display-text node.");

		}
		catch (Exception e) {
			MerlotDebug.msg( "PluginConfigException: " + e );
			throw new PluginConfigException(e);
		}
	}
	
	public DefaultEditorConfig getDefaultEditorConfig() {
		return _defaultEditorConfig;
	}
	
	public Class getEditorClassFor(String elementName) {
		
		EditorConfig nextConfig;
		Iterator iter;
		
		if (_editorConfigs != null) {
			iter = _editorConfigs.iterator();
			while (iter.hasNext()) {
				nextConfig = (EditorConfig) iter.next();
				if (nextConfig.contains(elementName)) {
					return nextConfig.getEditorClass();
				}
			}
		}
		if (_defaultEditorConfig != null) {
			return _defaultEditorConfig.getEditorClass();
		}
		else {
			return null;
		}
	}
	
	public Icon getIconFor(String elementName, int size) {
		
		IconConfig nextIconConfig;
		IconMapKey key = new IconMapKey(elementName, size);
		ImageIcon rtn;

		Iterator iter;
		String imageName;
		
		if (iconMap.containsKey(key)) {
			return (Icon) iconMap.get(key);
		}
		
		if (_iconConfigs != null) {
			iter = _iconConfigs.iterator();
			
			while (iter.hasNext()) {
				nextIconConfig = (IconConfig) iter.next();
				if (nextIconConfig.contains(elementName)) {
					switch (size) {
					case ICON_SIZE_SMALL:
						imageName = nextIconConfig.getSmallSource();
						break;
					case ICON_SIZE_LARGE:
						imageName = nextIconConfig.getSource();
						break;
					default:
						return null;
					}
					
					Icon result = IconUtil.getIcon(imageName, classLoader);
					if (result != null) {
					    iconMap.put(key,result);
					}
					return result;
					
				}
			}
		}
		
		return null;
		
	}
	
	public TreeTableConfig getTreeTableConfig()
	{
		return _treeTableConfig;
	}
	
	public DisplayTextConfig getDisplayTextConfig()
	{
		return _displayTextConfig;
	}
	
	public String toString() {
		StringBuffer rtn = new StringBuffer(super.toString());
		rtn.append("\ndtdConfigs: " + _dtdConfigs + "\n");
		rtn.append("editorConfigs: " + _editorConfigs + "\n");
		rtn.append("defaultEditorConfig: " + _defaultEditorConfig + "\n");
		rtn.append("iconConfigs: " + _iconConfigs + "\n");
		return rtn.toString();
	}
	
	private class IconMapKey {
		public String elementName;
		public int size;
		public IconMapKey(String elementName, int size) {
			this.elementName = elementName;
			this.size = size;
		}

	    public boolean equals(Object o) 
	    {
		if (!(o instanceof IconMapKey)) {
		    return false;
		}
		IconMapKey other = (IconMapKey)o;
		if (!other.elementName.equals(elementName)) {
		    return false;
		}
		if (other.size != size) {
		    return false;
		}
		return true;
	    }
	    
	    public int hashCode() 
	    {
		int result = elementName.hashCode() * size;
		return result;
	    }
	    
	    
	}
	
}
