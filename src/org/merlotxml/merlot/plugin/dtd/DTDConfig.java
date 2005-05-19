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

import java.io.*;
import java.util.zip.*;

import org.merlotxml.util.xml.*;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

/**
 * Merlot Pluggable DTD Configuration
 * 
 * @author Tim McCune
 * @version $Id: DTDConfig.java,v 1.4 2001/08/14 15:47:26 camk Exp $
 */
class DTDConfig {
	
	//Constants
	protected static final String XPATH_TEXT = "/text()";
	protected static final String XPATH_FILE = "file" + XPATH_TEXT;
	protected static final String XPATH_DOCTYPE = "doctype" + XPATH_TEXT;
	protected static final String XPATH_PUBLIC_ID = "publicID" + XPATH_TEXT;
	protected static final String XPATH_SYSTEM_ID = "systemID" + XPATH_TEXT;
	
	// Attributes
	protected String _doctype;
	protected String _publicID;
	protected String _systemID;

	// Associations
	protected DTDPluginConfig _parent = null;

	// Operations
	
	/**
	 * @exception SAXException Thrown if the XML is malformed
	 * @exception IOException Thrown if there is a problem reading in the DTD
	 * @exception FileNotFoundException Thrown if the specified DTD is not found
	 */
	public void parse(Node node) throws SAXException, IOException, FileNotFoundException
	{
		File pluginSource = _parent.getSource();
		InputStream dtdSource = null;
		PluginDTDCacheEntry cacheEntry;
		String s;
		ZipEntry dtdEntry;
		ZipFile sourceZip;
		
		if (( (s = XPathUtil.getValue(node, XPATH_FILE)) != null) &&
			(pluginSource != null)) {
			if (pluginSource.isDirectory()) {
				dtdSource = new FileInputStream(new File(pluginSource, s));
			}
			else {
				try {
					sourceZip = new ZipFile(pluginSource);
					dtdEntry = sourceZip.getEntry(s);
					dtdSource = sourceZip.getInputStream(dtdEntry);
				}
				catch (ZipException e) {
					//Source is not a zip file or a directory.
					//This should never happen.
				}
			}
		}
		
		_doctype = XPathUtil.getValue(node, XPATH_DOCTYPE);
		_publicID = XPathUtil.getValue(node, XPATH_PUBLIC_ID);
		_systemID = XPathUtil.getValue(node, XPATH_SYSTEM_ID);
		
		if (dtdSource != null) {
			cacheEntry = new PluginDTDCacheEntry(_publicID, _systemID, this, _parent);
			cacheEntry.setDoctype(_doctype);
            cacheEntry.setFilePath(s);
			DTDCache.getSharedInstance().loadDTDIntoCache(dtdSource, cacheEntry);
		}														  
	}
	
	void setParent(DTDPluginConfig parent) {
		_parent = parent;
	}
	
	public String toString() {
		StringBuffer rtn = new StringBuffer();
		rtn.append("doctype: " + _doctype + "\n");
		rtn.append("publicID: " + _publicID + "\n");
		rtn.append("systemID: " + _systemID + "\n");
		return rtn.toString();
	}
}
