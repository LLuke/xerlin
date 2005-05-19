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

import java.beans.PropertyVetoException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.merlotxml.util.xml.DTDElement;
import org.merlotxml.util.xml.GrammarComplexType;


/**
 * This is a null editor that suppresses certain types of elements from being
 * edited graphically.
 */

public class NullEditor implements MerlotDOMEditor
{
	private static final String NULL_TYPES_PROP = "merlot.editor.null.types";
	
	private String[] _types;
	


	public NullEditor () 
	{
		// read the set of elements from the properties file
		String nulltypes = XMLEditorSettings.getSharedInstance().getProperty(NULL_TYPES_PROP);
		Vector v = new Vector();
		
		if (nulltypes != null) {
		   
			StringTokenizer tok = new StringTokenizer(nulltypes,", ");
			while (tok.hasMoreTokens()) {
				String s = tok.nextToken();
				v.addElement(s);
			}
		}
		_types = new String[v.size()];
		for (int i=0;i<_types.length;i++) {
			_types[i] = (String)v.elementAt(i);
		}
		
		
	}
	
	public void grabFocus(JPanel p)
	{
	}
	
	
	public  JMenuItem[] getMenuItems(MerlotDOMNode node)
	{
		return null;
	}
	
	

	/**
	 * returns a panel for editing this type of component.
	 */
	public JPanel getEditPanel(MerlotDOMNode node)
	{
		throw new RuntimeException("Can't edit "+node);
		
	}
	
	
	/**
	 * called by the editor when the user has chosen to save their
	 * changes in a panel. 
	 * @param p the panel that was retreived with getEditPanel(node);
	 *
	 */
	public void savePanel(JPanel p)
		throws PropertyVetoException 
	{
	}
	
	

	/**
	 * Returns the element types that this editor handles
	 */
	public String[] getEditableTypes()
	{
		return _types;
	}
	
	
	/**
	 * returns true if this editor also edits it's children. <P>
	 * If this returns true, then the editsChild(childnode) is called
	 * for each child to see if this editor wants to edit that 
	 * particular child
	 * XXX currently not used on the editor level

	 */
	public boolean editsChildren()
	{
		return false;
	}
	
	
	/**
	 * Returns true if the component editor wants a particular node hidden
	 * from the user. If the editor wants to filter 
	 * what the user sees in their display, it should look at the
	 * given node, otherwise it should return false. This is usefull
	 * particularly if the editor handles its children. It can hide
	 * the children nodes from the user's view.
	 */
	public boolean suppressNode(MerlotDOMNode node)
	{
		return true;
	}
	
	
	/**
	 * allows the plugin to hide certain items on the add-> menu. For
	 * example, the plugin for the accessibility permissions might not
	 * want the user to be able to directly add an "access" element, so
	 * it can request that that be suppressed.
	 */
	public boolean suppressAddType(DTDElement el)
	{
		return true;
	}
	
	public boolean suppressAddType(GrammarComplexType el)
	{
		return true;
	}
	


	
}
