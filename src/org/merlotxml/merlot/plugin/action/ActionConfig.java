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

package org.merlotxml.merlot.plugin.action;

import java.util.*;
import javax.swing.*;

import org.merlotxml.util.xml.XPathUtil;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

/**
 * Merlot Pluggable Action Configuration
 * 
 * @author Tim McCune
 * @version $Id: ActionConfig.java,v 1.4 2001/01/30 04:50:42 camk Exp $
 */
public class ActionConfig {
	
	//Constants
	protected static final String XPATH_TEXT = "/text()";
	protected static final String XPATH_MENU = "menu" + XPATH_TEXT;
	protected static final String XPATH_CLASS = "class" + XPATH_TEXT;
	protected static final String XPATH_CONFIG = "config";
	protected static final String XPATH_ICON = "icon" + XPATH_TEXT;
	protected static final String XPATH_TOOL_TIP = "tooltip" + XPATH_TEXT;
  
	// Attributes
	protected ClassLoader classLoader;
	protected JMenuItem menu;

	// Associations
	protected ActionPluginConfig parent;
	protected Action myAction;

	// Operations
	
	public ActionConfig(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	public Action getAction() {
		return myAction;
	}
	
  public JMenuItem getMenuItem() {
    return menu;
  }
	public void parse(Node node) throws SAXException, IllegalAccessException,
		InstantiationException, ClassNotFoundException
	{
		Node configNode;
		String s;
		
		menu = new JMenuItem();
		if ( (s = XPathUtil.getValue(node, XPATH_MENU)) != null) {
			menu.setText(s);
		}
		if ( (s = XPathUtil.getValue(node, XPATH_CLASS)) != null) {
			myAction = (Action) classLoader.loadClass(s).newInstance();
			if ( (configNode = XPathUtil.selectSingleNode(node, XPATH_CONFIG)) != null) {
				myAction.init(configNode);
			}
		}
		if ( (s = XPathUtil.getValue(node, XPATH_ICON)) != null) {
			//Finish this later
		}
		if ( (s = XPathUtil.getValue(node, XPATH_TOOL_TIP)) != null) {
			menu.setToolTipText(s);
		}
	}
	
	public String toString() {
		StringBuffer rtn = new StringBuffer();
		rtn.append("menuItem: " + menu + "\n");
		rtn.append("myAction: " + myAction);
		return rtn.toString();
	}
}
