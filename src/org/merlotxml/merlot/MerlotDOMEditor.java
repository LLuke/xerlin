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
http://www.merlotxml.org
*/


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.beans.PropertyVetoException;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.merlotxml.util.xml.DTDElement;
import org.merlotxml.util.xml.GrammarComplexType;


/**
 * This is an interface which every Node editor panel must implement. Node editors
 * provide a way to change information contained in a DOM node within an xml file.
 * This includes special menu items to add to the right-click popup menu and an editing
 * panel to change attributes and sub-nodes. DOMEditors can also specify what types of
 * nodes should not show up in the tree view (this functionality may move into the
 * plugin classes instead of here... it doesn't really belong here.)
 * 
 * @author Kelly A. Campbell
 * @see org.merlotxml.merlot.GenericDOMEditor
 * @see org.merlotxml.merlot.GenericDOMEditPanel
 *
 */

public interface MerlotDOMEditor 
{

	/**
	 * Returns a set of menu items for any special actions for 
	 * this particular editor
	 * that it wants in the popup menu. Standard stuff like cut, copy, paste
	 * is taken care of by other objects. If nothing needs added,
	 * can return null.
	 * 
	 * @param node the node for which to get the menu items
	 */
	public  JMenuItem[] getMenuItems(MerlotDOMNode node);
	

	/**
	 * returns a panel for editing this type of component.
	 */
	public JPanel getEditPanel(MerlotDOMNode node);
	
	/**
	 * called by the editor when the user has chosen to save their
	 * changes in a panel. 
	 * @param p the panel that was retreived with getEditPanel(node);
	 *
	 */
	public void savePanel(JPanel p)
		throws PropertyVetoException;
	
	/**
	 * Tells the edit panel it's gaining focus, so it can put the cursor in the first
	 * field. XXX this should probably be handled by event listening instead
	 */
	public void grabFocus(JPanel p);
	
	
	/**
	 * Returns true if the component editor wants a particular node hidden
	 * from the user. If the editor wants to filter 
	 * what the user sees in their display, it should look at the
	 * given node, otherwise it should return false. This is usefull
	 * particularly if the editor handles its children. It can hide
	 * the children nodes from the user's view.
	 */
	public boolean suppressNode(MerlotDOMNode node);
	
	/**
	 * allows the plugin to hide certain items on the add-> menu. For
	 * example, the plugin for the accessibility permissions might not
	 * want the user to be able to directly add an "access" element, so
	 * it can request that that be suppressed.
     * @deprecated
	 */
	public boolean suppressAddType(DTDElement el);
	
    public boolean suppressAddType(GrammarComplexType el);
	  
}
