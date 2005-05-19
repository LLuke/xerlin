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

import javax.swing.Icon;

import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * Library item node... only thing special about this is it get's the icons
 * from the first child
 */

public class MerlotLibraryItem extends MerlotDOMElement 
{
	//	private MerlotDOMNode _contents = null;
	
	public MerlotLibraryItem(Element data, XMLFile doc)
	{
		super(data, doc);
		//	MerlotDebug.msg("new MerlotLibraryItem");
		/*
		MerlotDOMNode[] nodes = getChildNodes();
		if (nodes != null && nodes.length > 0) { // fixed BUG01105, BUG01106
			_contents = nodes[0];
		}
		*/
		
	}
	
	public Icon getSmallIcon() 
	{
		//	MerlotDebug.msg("Contents = "+_contents);
		MerlotDOMNode nd = getFirstChild();
		if (nd != null) {
			return nd.getSmallIcon();
		}
		/*
		if (_contents != null) {
			return _contents.getSmallIcon();
		}
		*/
		else {
			return super.getSmallIcon();
		}
		

				
	}
	
	/**
	 * This gets a fragment with the libitem pruned out
	 */

	public MerlotDOMFragment getFragment() 
	{
		MerlotDOMDocument d = getMerlotDOMDocument();

		if (d != null) {
			MerlotDOMFragment frag = d.createDocumentFragment();
			MerlotDOMNode[] children = getChildNodes();
			
			MerlotDOMNode[] clonedset = new MerlotDOMNode[children.length]; // nodes we cloned from
			MerlotDOMNode node;
			for (int i=0; i < children.length; i++) {
				node = children[i];
				clonedset[i] = node;
				frag.appendChild((MerlotDOMNode)node.clone());
				
			}
			
			frag.setClonedFrom(clonedset);
			
			return frag;
			
		}
		return null;
		
	}
	
	/**
	 * prunes out library items from a fragment
	 */
	public static void pruneLibraryItems(MerlotDOMFragment frag) 
	{
		boolean remove;
		MerlotDOMNode[] children = frag.getChildNodes();
		for (int i=0; i < children.length; i++) {
			if (children[i] instanceof MerlotLibraryItem) {
				frag.removeChild(children[i]);
				MerlotDOMNode[] subchildren = children[i].getChildNodes();
				for (int j=0; j<subchildren.length; j++) {
					remove = false;
					if (subchildren[j].getRealNode() instanceof Text) {
						if (subchildren[j].getRealNode().getNodeValue() != null) {
							if (subchildren[j].getRealNode().getNodeValue().trim().length() == 0) {
								remove = true;
							}
						}
					}
					if (!remove) {
						frag.appendChild(subchildren[j]);
					}
				}
			}
		}	
	}
	
	
}
