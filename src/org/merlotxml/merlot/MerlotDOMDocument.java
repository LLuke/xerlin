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

// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.XPathUtil;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * DOM document container for Merlot. Contains a DOM node, handles getting an
 * icon for a particular node, getting the editor for a node, etc. Also
 * implements transferable so that the node can be drag and dropped, or 
 * cut and pasted.
 */

public class MerlotDOMDocument extends MerlotDOMNode {

	/**
	 * The IDManager for this document
	 */
	protected IDManager _idManager = null;

	/** Node descriptors */
	protected HashMap _nodeDescription;

	public MerlotDOMDocument(Document data, XMLFile doc) {
		super(data, doc);
		_nodeDescription = new HashMap();
	}

	public Document getDocument() {
		return (Document) getRealNode();
	}

	public MerlotDOMFragment createDocumentFragment() {
		Document d = getDocument();
		DocumentFragment df = d.createDocumentFragment();
		return new MerlotDOMFragment(df, getXMLFile());
	}

	public IDManager getIdManager() {
		if (_idManager == null) {
			_idManager = new IDManager(this);
		}
		return _idManager;
	}

	public MerlotDOMNode getMerlotDOMNode(Node node) {
		MerlotDOMNode mNode = XMLFile.getInstanciatedNode(node);
		if (mNode == null)
			mNode = findMerlotDOMNode(node);
		return mNode;
	}

	/**
	 * Finds the corresponding MerlotDOMNode for a Node.
	 */
	public MerlotDOMNode findMerlotDOMNode(Node node) {
		long start = System.currentTimeMillis();
		Object[] properties = new Object[1];
		properties[0] = node;
		SearchCriteria c = new SearchCriteria(properties) {
			public boolean match(MerlotDOMNode mNode) {
				boolean match = false;
				Node testNode = mNode.getRealNode();
				if (testNode == (Node) this.properties[0])
					match = true;
				return match;
			}
		};
		MerlotDOMNode ret = findFirstDescendant(this, c);
		long end = System.currentTimeMillis();
		System.out.println("GETTING MERLOTDOMNODE FOR NODE: " + (end - start));
		return ret;
	}

	public MerlotDOMNode findNodeById(String id) {
		//long start = System.currentTimeMillis();
		MerlotDOMNode ret = null;
		Node node = XPathUtil.getNodeById(getRealNode(), id);
		if (node == null) {
			ret = findNodeByIdSlow(id);
		} else {
			ret = getMerlotDOMNode(node);
		}
		//long end = System.currentTimeMillis();
		//System.out.println( "FINDING NODE BY ID: " + (end-start) );
		return ret;
	}

	/**
	 * Finds the first node with the specified ID. If the document is not validated,
	 * duplicate IDs might exist and this might return the wrong value.
	 */
	public MerlotDOMNode findNodeByIdSlow(String id) {
		Object[] properties = new String[1];
		properties[0] = id;
		SearchCriteria c = new SearchCriteria(properties) {
			public boolean match(MerlotDOMNode node) {
				boolean match = false;
				Vector v = node.getAttributesOfType(DTDConstants.ID);
				Iterator it = v.iterator();
				while (it.hasNext()) {
					Node attribute = (Node) it.next();
					if (attribute.getNodeValue().equals(this.properties[0])) {
						match = true;
						break;
					}
				}
				return match;
			}
		};
		MerlotDOMNode ret = findFirstDescendant(this, c);
		return ret;
	}

	public class SearchCriteria {
		Object[] properties;
		public SearchCriteria(Object[] properties) {
			this.properties = properties;
		}
		public boolean match(MerlotDOMNode node) {
			return true;
		}
	}

	/**
	 * Visits nodes in the tree from the top down, but stops as soon as a node is
	 * found that matches the criteria. Extend <code>SearchCriteria</code> in order to
	 * do different searches.
	 */
	public MerlotDOMNode findFirstDescendant(
		MerlotDOMNode startNode,
		SearchCriteria c) {
		MerlotDOMNode match = null;
		if (startNode.getChildNodes() != null) {
			MerlotDOMNode[] mlist = startNode.getChildNodes();
			int childcount = mlist.length;
			for (int i = 0; i < childcount; i++) {
				if (c.match(mlist[i])) {
					match = mlist[i];
					break;
				} else {
					match = findFirstDescendant(mlist[i], c);
					if (match != null)
						break;
				}
			}
		}
		return match;
	}

	/**
	 * Visits every node in the tree from the top down, and retrieves all nodes matching
	 * the criteria. Extend <code>SearchCriteria</code> in order to do different searches.
	 */
	public Vector findAllDescendants(
		MerlotDOMNode startNode,
		SearchCriteria c) {
		Vector matches = new Vector();
		if (startNode.getChildNodes() != null) {
			MerlotDOMNode[] mlist = startNode.getChildNodes();
			int childcount = mlist.length;
			for (int i = 0; i < childcount; i++) {
				if (c.match(mlist[i])) {
					MerlotDOMNode match = mlist[i];
					matches.add(match);
				}
				Vector childMatches = findAllDescendants(mlist[i], c);
				matches.addAll(childMatches);
			}
		}
		return matches;
	}
}
