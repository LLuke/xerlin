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

import org.merlotxml.util.xml.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import org.w3c.dom.*;
import org.apache.xerces.dom.DocumentImpl;

/**
 * DOM element container for Merlot. Contains a DOM node, handles getting an
 * icon for a particular node, getting the editor for a node, etc. Also
 * implements transferable so that the node can be drag and dropped, or 
 * cut and pasted.
 */

public class MerlotDOMElement extends MerlotDOMNode
{
	


	public MerlotDOMElement(Element data, XMLFile doc) 
	{
		super(data,doc);
	}
	
	public boolean isElement() 
	{
		return true;
	}
	

	/**
	 * Returns the DTD name of the element (which we consider it's type)
	 * in the context of merlot
	 */
	public String getElementName() 
	{
		return _theNode.getNodeName();
				
	}
	
	public String getAttribute(String s) 
	{
		return ((Element)_theNode).getAttribute(s);
	}
	
	/**
	 * Sets the attributes according the given hashtable. 
	 * (probably should make getAttributes consistent with this at some point
	 */
	public void setAttributes(HashMap h) 
	{
		Iterator i = h.keySet().iterator();
		
		while (i.hasNext()) {
			String key = (String)i.next();
			String val;
			Object o = h.get(key);
			if (o instanceof String) {
				val = (String)o;
			}
			else if (o != null) {
				val = o.toString();
			}
			else {
				val = null;
			}
			setAttribute(key,val);
		}
	}
	
	/**
	 * sets the attributes one at a time
	 */
	public void setAttribute(String name, String value) 
	{
        XMLEditorSettings xes = XMLEditorSettings.getSharedInstance();
        boolean writeDefaults =
                    xes.getProperty("merlot.write.default-atts").equals("true");
		boolean hasChanged = false;
		Element el = (Element)_theNode;
		if (value != null) {
			String oldValue = el.getAttribute( name );
            DTDAttribute dtdAttr = getDTDAttribute( name );
            int type = dtdAttr.getType();
			if ( !value.equals( oldValue ) 
                || (writeDefaults && type==DTDConstants.TOKEN_GROUP))
			{
				el.setAttribute(name,value);
				if ( type == DTDConstants.ID )
				{
					DocumentImpl docImpl = (DocumentImpl)getDocument();
					// This updates Xerces' internal map of IDs versus Elements and
					// allows the new id to be found with
					// org.w3c.dom.Document.getElementById( String id )
					// Xerces doesn't automatically update this map when an
					// attribute is set.
					docImpl.putIdentifier( name, el );
				}
				hasChanged = true;
			} else if(dtdAttr.getDefaultType()==DTDConstants.FIXED) {
                el.setAttribute(name,dtdAttr.getDefaultValue());
            }
		}
		else {
			if ( el.getAttributeNode( name ) != null )
			{
				el.removeAttribute(name);
				hasChanged = true;
			}
		}
		
		if ( hasChanged )
			fireNodeChanged();
	}
	/*
	public void setAttributes(HashMap h) 
	{
		Iterator i = h.keySet().iterator();
		
		while (i.hasNext()) {
			String key = (String)i.next();
			String val;
			Object o = h.get(key);
			if (o instanceof String) {
				val = (String)o;
			}
			else if (o != null) {
				val = o.toString();
			}
			else {
				val = null;
			}
			if (val != null) {
				//((Element)_theNode).setAttribute(key,val);
				setAttribute( key, val );
			}
			else {
				((Element)_theNode).removeAttribute(key);
			}
			
		}
		fireNodeChanged();
		
	}
	*/
	/**
	 * sets the attributes one at a time
	 */
	/*
	public void setAttribute(String name, String value) 
	{
		if (value != null) {
			Element el = (Element)_theNode;
			el.setAttribute(name,value);
			DTDAttribute dtdAttr = getDTDAttribute( name );
			int type = dtdAttr.getType();
			if ( type == DTDConstants.ID )
			{
				DocumentImpl docImpl = (DocumentImpl)getDocument();
				// This updates Xerces' internal map of IDs versus Elements and
				// allows the new id to be found with
				// org.w3c.dom.Document.getElementById( String id )
				// Xerces doesn't automatically update this map when an
				// attribute is set.
				docImpl.putIdentifier( name, el );
			}
		}
		else {
			((Element)_theNode).removeAttribute(name);
		}
		
		fireNodeChanged();
	}
	*/
	
	private boolean _valid = true;
	private boolean _hasBeenValidated = false;
	
	public boolean isValid ()
	{
		// Each element used to be validated each time the mouse moves over a
		// node in the tree, now each one is validated once and thereafter only
		// revalidated when a change occurs.
		boolean valid;
		if ( _hasBeenValidated )
			valid = _valid;
		else
		{
			valid = _file.elementIsValid((Element)_theNode, true);
			_valid = valid;
			_hasBeenValidated = true;
		}
		return valid;
	}
	
	public void validate()
	{
		_valid = _file.elementIsValid((Element)_theNode, true);
		_hasBeenValidated = true;
	}
	
	public DTDElement getDTDElement()
	{
		DTDDocument dtd = getDTDDocument();
		return dtd.fetchElement( this.getNodeName() );
	}
	
	public DTDAttribute getDTDAttribute( String name )
	{
		return getDTDElement().getAttribute( name );
	}
}
