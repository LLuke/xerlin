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

import java.util.*;

import org.merlotxml.util.xml.*;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

/**
 * Merlot Pluggable Icon Configuration
 * 
 * @author Tim McCune
 * @version $Id: IconConfig.java,v 1.1 2000/05/25 06:54:33 timm Exp $
 */
class IconConfig {
	
	//Constants
	protected static final String XPATH_TEXT = "/text()";
	protected static final String XPATH_ELEMENT = "element" + XPATH_TEXT;
	protected static final String XPATH_SMALL_SOURCE = "@smallSource";
	protected static final String XPATH_SOURCE = "@source";
	
	// Attributes
	protected List elements;
	protected String smallSource;
	protected String source;
	
	// Associations
	protected DTDPluginConfig _parent = null;

	// Operations
	
	/**
	 * @exception SAXException Thrown if the XML is malformed
	 */
	public void parse(Node node) throws SAXException
	{
		String s;
		
		elements = XPathUtil.getValueList(node, XPATH_ELEMENT);
		smallSource = XPathUtil.getValue(node, XPATH_SMALL_SOURCE);
		source = XPathUtil.getValue(node, XPATH_SOURCE);		  
	}
	
	void setParent(DTDPluginConfig parent) {
		_parent = parent;
	}
	
	public boolean contains(String elementName) {
		return elements.contains(elementName);
	}
	
	public String getSmallSource() {
		return smallSource;
	}
	
	public String getSource() {
		return source;
	}
	
	public String toString() {
		StringBuffer rtn = new StringBuffer();
		rtn.append("elements: " + elements + "\n");
		rtn.append("source: " + source + "\n");
		rtn.append("smallSource: " + smallSource + "\n");
		return rtn.toString();
	}
}
