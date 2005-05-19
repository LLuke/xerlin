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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.w3c.dom.Element;


/**
 * 
 * This is a special encapsulation of a DOM Node because we need to override the
 * toString() method to return the proper representation for the JTreeTable.
 *
 * 
 * @author Kelly A. Campbell
 *
 */

public class MerlotNodeRenderer extends DefaultTreeCellRenderer
{
	protected String _dispAttr = null; 
	

	/*
	public MerlotNodeRenderer (TreeModel model) 
	{
		super(model);
	}
	*/

	public MerlotNodeRenderer () 
	{
		super();
	}

	/**
	 * Displays an attribute if available instead of the node name
	 */
	public MerlotNodeRenderer (String attr) 
	{
		super();
		_dispAttr = attr;
	}
	

    /**
	 *
	 *
	 */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf, int row,
						  boolean hasFocus) 
	{
	
		Object myval = value;
		if (_dispAttr != null && value instanceof MerlotDOMElement) {
			myval = ((MerlotDOMElement)value).getAttribute(_dispAttr);
			if (myval == null || myval.equals("")) {
				myval = value;
			}
		}
		
		if (value instanceof MerlotDOMNode && myval == value) {
			myval = ((MerlotDOMNode)value).getNodeName();
		}
		super.getTreeCellRendererComponent (tree, myval,
											sel,expanded,
											leaf,row,hasFocus);
		
		
	
		if (value instanceof MerlotDOMNode) {
			MerlotDOMNode nd = (MerlotDOMNode)value;

			setIcon(nd.getSmallIcon());
            /*
			MerlotDOMNode parent = nd.getParentNode();

			if (parent!=null && !parent.locationIsValid(true)) {
				setForeground(Color.lightGray);
			} else if (!nd.locationIsValid(false))	{
				MerlotDOMNode previousSibling = nd.getPreviousSibling();
				// hack to prevent parasiting text node appearing when a previous node was removed
				while (previousSibling != null 
					&& (previousSibling instanceof MerlotDOMText 
						|| previousSibling.isSpecialNode())
					)
					previousSibling = previousSibling.getPreviousSibling();
				if (previousSibling != null && !previousSibling.locationIsValid(false)) {
					setForeground(Color.lightGray);
				} else {
					setForeground(Color.red);
				}
			} else if (!nd.isValid()) {
				setForeground(Color.orange.darker());
			} 
            */
            //MerlotDebug.msg("Rendering node: " + nd + " (" + nd.hashCode() + ")");
            if (nd instanceof MerlotDOMElement) {
                MerlotDOMElement element = (MerlotDOMElement)nd;
                //MerlotDebug.msg(" getIsLocationValid: " + element.getIsLocationValid());
                //MerlotDebug.msg(" getIsContentValid: " + element.getIsContentValid());
                //MerlotDebug.msg(" getIsEachChildValid: " + element.getIsEachChildValid());
                Element elem = (Element)nd.getRealNode();
                //DTDDocument dtdd = nd.getXMLFile().getValidDocument().
                //                                        getDTDForElement(elem);
                if (!element.getHasBeenValidated())
                    setForeground(Color.gray);
                else if (!element.getIsLocationValid())
                    setForeground(Color.magenta);
                //else if (!element.getIsContentValid()
                //        || (dtdd!=null && !dtdd.elementIsValid(elem, false)))
                else if (!element.getIsContentValid() ||
                          !element.getIsLocationValid() ||
                          !element.getIsComplete())
                    setForeground(Color.red);
                else if (!element.getIsEachChildValid())
                    setForeground(Color.orange.darker());
            }
		}
		return this;
    }

	
}



