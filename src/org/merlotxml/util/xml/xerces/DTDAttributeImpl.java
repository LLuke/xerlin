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

import java.io.*;
import java.util.*;

import  org.merlotxml.util.xml.*;

import org.apache.xerces.validators.common.XMLAttributeDecl;

/**
 *  DTDAttribute
 * 
 *
 * @author Evert Hoff
 * @version 
 */

public class DTDAttributeImpl implements DTDAttribute, DTDConstants
{
	private DTDElementImpl _element = null;
	private String _name = null;
	private int _type = -2; // Not yet set.
	private int _defaultType = -2;
	private XMLAttributeDecl _decl = null;
	
	public DTDAttributeImpl( DTDElementImpl element, String name ) 
	{
		_element = element;
		_name = name;
	}
	
	public String getName() 
	{
		return _name;
	}
	
	public XMLAttributeDecl getAttributeDecl()
	{
		if ( _decl == null )
		{
			GrammarAccess grammar
		 	 = _element.getDTDDocumentImpl().getGrammarAccess();
			_decl = grammar.getAttributeDecl( _element.getName(), _name );
		}
		return _decl;
	}
	
	public int getType() 
	{
		if ( _type != -2 )
			return _type;
		XMLAttributeDecl decl = getAttributeDecl();
		int declType = decl.type;
		
		switch ( declType ) 
		{
			// REVISIT: Not sure whether this one is correct
			case XMLAttributeDecl.TYPE_ENUMERATION:
				_type = TOKEN_GROUP;
				break;
			case XMLAttributeDecl.TYPE_CDATA:
				_type = CDATA;
				break;
			// REVISIT: Have nothing for NMTOKENS
			//case XMLAttributeDecl.:
			//	return NMTOKENS;
			case XMLAttributeDecl.TYPE_NMTOKEN:
                _type = decl.list?DTDConstants.NMTOKENS:DTDConstants.NMTOKEN;
				break;
			case XMLAttributeDecl.TYPE_ID:
				_type = ID;
				break;
			case XMLAttributeDecl.TYPE_IDREF:
				_type = IDREF;
				break;
			case XMLAttributeDecl.TYPE_ENTITY:
			case XMLAttributeDecl.TYPE_NOTATION:
			// REVISIT: Do something with schema types
			// Schema types
			case XMLAttributeDecl.TYPE_SIMPLE:
			case XMLAttributeDecl.TYPE_ANY_ANY:
			case XMLAttributeDecl.TYPE_ANY_OTHER:
			// Looks like this one disappeared from Xerces.
			//case XMLAttributeDecl.TYPE_ANY_LOCAL:
			case XMLAttributeDecl.TYPE_ANY_LIST:
			default:
				_type = NONE;
		}
		//System.out.println( "Xerces type: " + declType + " for " + _name );
		//System.out.println( "Returning type: " + _type + " for " + _name );
		return _type;
	}

	public int getDefaultType() 
	{
		if ( _defaultType != -2 )
			return _defaultType;
		XMLAttributeDecl decl = getAttributeDecl();
		int declType = decl.defaultType;
		switch ( declType ) 
		{
			case XMLAttributeDecl.DEFAULT_TYPE_IMPLIED:
				_defaultType = IMPLIED;
				break;
			case XMLAttributeDecl.DEFAULT_TYPE_REQUIRED:
				_defaultType = REQUIRED;
				break;
			case XMLAttributeDecl.DEFAULT_TYPE_DEFAULT:
				_defaultType = NONE;
				break;
			// REVISIT
			case XMLAttributeDecl.DEFAULT_TYPE_FIXED:
                _defaultType = FIXED;
                break;
			case XMLAttributeDecl.DEFAULT_TYPE_PROHIBITED:
			case XMLAttributeDecl.DEFAULT_TYPE_REQUIRED_AND_FIXED:
			default:
				_defaultType = NONE;
		}
		//System.out.println( "Returning default type: " + _defaultType + " for " + _name );
		return _defaultType;
	}

	private boolean tokensLoaded = false;
	//private Enumeration _tokens = null;
	private List _literals = null;
	
	public Enumeration getTokens() 
	{
		if ( tokensLoaded )
			return Collections.enumeration( _literals );
		GrammarAccess grammar
		 = _element.getDTDDocumentImpl().getGrammarAccess();
		String[] literals = grammar.getEnumeration( _element.getName(), _name );
		if ( literals == null )
		{
			_literals = new Vector();
			return Collections.enumeration( _literals );
		}
		_literals = Arrays.asList( literals );
		tokensLoaded = true;
		return Collections.enumeration( _literals );
		/*
		Vector v;
		
		int t = getType();
		switch (t) {
		case TOKEN_GROUP:
			return _attDef.elements();
		case CDATA:
			return null;
		case NMTOKEN:
			v = new Vector();
			v.addElement(_attDef.elementAt(0));
			return v.elements();
		case NMTOKENS:
			return _attDef.elements();
		default:
			return null;
		}
		*/
	}
	
	public String getDefaultValue() 
	{
		XMLAttributeDecl decl = getAttributeDecl();
		return decl.defaultValue;
	}
}
