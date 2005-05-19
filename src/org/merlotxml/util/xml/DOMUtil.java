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
Merlot XML Editor (http://www.channelpoint.com/merlot/)."
 
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
(http://www.channelpoint.com/merlot/)."

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
http://www.channelpoint.com/merlot.
*/


// Copyright 1998, ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.util.xml;

import java.util.*;

import org.w3c.dom.*;

/**
 * DOM Utilities
 * 
 * @author Tim McCune
 * @version $Id: DOMUtil.java,v 1.4 2000/06/24 17:58:55 camk Exp $
 */

public class DOMUtil {

	
	/**
	 * Import a single node into a document.  The node may have come from an
	 * external document.
	 * 
	 * @param parent Node at which to insert the imported node
	 * @param child Node to append to insertionPoint
	 * 
	 */
	public static Node importNode(Node parent, Node child) 
	{
		return importNode(parent,child,true);
	}
	

	/**
	 * Import a single node into a document.  The node may have come from an
	 * external document.
	 * 
	 * @param parent    Node at which to insert the imported node
	 * @param child     Node to append to insertionPoint
	 * @param doappend  if true, this will go ahead and append the 
	 * node, otherwise, no action is done with the newly created node.
	 */
	public static Node importNode(Node parent, Node child, boolean doappend) {

		Attr attr;
		int i;
		NamedNodeMap attributes;
		Node copy = null;
		NodeList children = null;
		String tagName = null;
		Document doc = parent.getOwnerDocument();
		
		try {
			children = child.getChildNodes();
		}
		catch (DOMException e) {}
		
		switch (child.getNodeType()) {
		case Node.CDATA_SECTION_NODE:
			copy = doc.createCDATASection(((CDATASection) child).getData());
			break;
		case Node.COMMENT_NODE:
			copy = doc.createComment(((Comment) child).getData());
			break;
		case Node.DOCUMENT_FRAGMENT_NODE:
			copy = doc.createDocumentFragment();
			break;
		case Node.ELEMENT_NODE:
			tagName = ((Element) child).getTagName();
			copy = doc.createElement(tagName);
			if ( (attributes = child.getAttributes()) != null) {
				for (i = 0; i < attributes.getLength(); i++) {
					attr = (Attr) attributes.item(i);
					((Element) copy).setAttribute(attr.getName(), attr.getValue());
				}
			}
			break;
		case Node.ENTITY_REFERENCE_NODE:
			copy = doc.createEntityReference(((EntityReference) child).getNodeName());
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			copy = doc.createProcessingInstruction(((ProcessingInstruction) child).getTarget(),
														((ProcessingInstruction) child).getData());
			break;
		case Node.TEXT_NODE:
			copy = doc.createTextNode(((Text) child).getData());
			break;
		default:
			return null;
			
		}
		
		if (children != null) {
			for (i = 0; i < children.getLength(); i++) {
				if (children.item(i) != null) {	//Retard check for xml4j
					importNode(copy, children.item(i));
				}
			}
		}
		if (doappend){
			parent.appendChild(copy);
		}
		

		return copy;
	
	}
	
	

	
}
