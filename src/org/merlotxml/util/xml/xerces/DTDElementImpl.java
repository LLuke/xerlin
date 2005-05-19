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

package org.merlotxml.util.xml.xerces;

import java.util.Enumeration;
import java.util.Hashtable;

import org.merlotxml.util.xml.DTDAttribute;
import org.merlotxml.util.xml.DTDContentSpec;
import org.merlotxml.util.xml.DTDElement;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarSimpleType;



/**
 *
 * @author Evert Hoff
 */

public class DTDElementImpl implements DTDElement, Comparable
{
	private DTDDocumentImpl _doc = null;
	
	private String _name = null;
	
	private Hashtable _attrs = null;
    private Hashtable _attributeSimpleTypes = new Hashtable();
    private GrammarComplexType _complexType;
	
	// Not set yet.
	private int _type = -2;
	
	public DTDElementImpl( DTDDocumentImpl doc, String name) 
	{
		_doc = doc;
		_name = name;
	}
    
    public DTDElementImpl(DTDDocumentImpl doc, GrammarComplexType complexType) {
        _complexType = complexType;
        _doc = doc;
    }
	
	public DTDDocumentImpl getDTDDocumentImpl()
	{
		return _doc;
	}
	
	public String getName() 
	{
		return _complexType.getName();
	}
	
	/*
	public int getNodeType()
	{
		if ( _type == -2 )
		{
			GrammarAccess grammar = _doc.getGrammarAccess();
			_type = grammar.getNodeType( _name );
		}
		return _type;
	}
	*/
	
	public Enumeration getAttributes() 
	{
        if (_attrs == null) {
            _attrs = new Hashtable();
            GrammarSimpleType[] attributeSimpleTypes = 
             _complexType.getAttributes();
            for (int i = 0; i < attributeSimpleTypes.length; i++) {
                GrammarSimpleType attributeSimpleType = attributeSimpleTypes[i];
                String name = attributeSimpleType.getName();
                _attrs.put(name, new DTDAttributeImpl((GrammarSimpleType)attributeSimpleType));
                _attributeSimpleTypes.put(name, attributeSimpleType);
            }
        }
        return _attrs.elements();
	}
	
	public DTDAttribute getAttribute(String name) 
	{
		if ( _attrs == null )
			getAttributes();
		return (DTDAttribute)_attrs.get( name );
	}

	public DTDContentSpec getContentSpec() 
	{
		// not implemented yet
		return null;
	}
	
    public int compareTo(Object o) 
    {
		if (o instanceof DTDElementImpl) 
		{
	   		return (getName().compareTo(((DTDElementImpl)o).getName()));
		}
		else 
		{
	    	throw new ClassCastException();
		}
    }
    
}

	
