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

import java.io.*;
import java.util.*;

import org.merlotxml.merlot.plugin.*;
import org.merlotxml.util.xml.XPathUtil;

import org.w3c.dom.*;

/**
 * Merlot Action Plugin Configuration		<p>
 * An action plugin can contain multiple actions.
 * Similarly, an action plugin config can contain
 * multiple action configs.
 * 
 * @author Tim McCune
 * @version	$Id: ActionPluginConfig.java,v 1.4 2001/09/27 16:13:34 camk Exp $
 */

public class ActionPluginConfig extends PluginConfig {
	
	//Constants
	protected static final String XPATH_ACTION = XPATH_PLUGIN + "/action";
	
	//Associations
	protected List actionConfigs;
	
	//Operations
	
	public ActionPluginConfig(PluginManager manager, 
							  ClassLoader loader, 
							  File source) {
		super(manager,loader,source);
		actionConfigs = new ArrayList();
	}
	
	public List getActionConfigs() {
		return actionConfigs;
	}
	
	/**
	 * @exception PluginConfigException Thrown if there was a problem
	 *		loading an action object
	 */
	public void parse(Node node) throws PluginConfigException
	{
		try {
			super.parse(node);
		
			NodeList actionConfigList = XPathUtil.selectNodes(node, XPATH_ACTION);
			for (int i = 0; i < actionConfigList.getLength(); i++) {
				actionConfigs.add(actionConfigList.item(i));
			}
		}
		catch (Exception e) {
			throw new PluginConfigException(e);
		}
	}
	
	protected void init()
		throws PluginConfigException
	{
		try {
			ArrayList newList = new ArrayList();
			Iterator it = actionConfigs.iterator();
			while (it.hasNext()) {
				Node n = (Node)it.next();
				ActionConfig newActionConfig = new ActionConfig(classLoader);
				newActionConfig.parse(n);
				newList.add(newActionConfig);
			}
			actionConfigs = newList;
		}
		catch (Exception e) {
			throw new PluginConfigException(e);
		}
	}
	

	public String toString() {
		StringBuffer rtn = new StringBuffer(super.toString());
		rtn.append("\nactionConfigs: " + actionConfigs + "\n");
		return rtn.toString();
	}
	
}
