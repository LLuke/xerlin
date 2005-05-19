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
import org.w3c.dom.*;
import org.merlotxml.util.xml.*;

/**
 * 
 *
 * @author Evert Hoff
 * @version 
 */

public class DTDDocumentImpl implements DTDDocument
{
	private Hashtable _elements = null;
	private boolean   _initialized = false;

    private String _pluginId;	
	private String _publicId;
	private String _systemId;
	
	private GrammarAccess _grammar;


    public DTDDocumentImpl(String pluginId,  String publicId, String systemId )
    {
        _pluginId = pluginId;
        _publicId = publicId;
        _systemId = systemId;
        _grammar = new GrammarAccess( pluginId, systemId );
        System.out.println( "Created Xerces DTDDocumentImpl: "
        + getExternalID() );
    }
	
	public DTDDocumentImpl( String publicId, String systemId ) 
	{
		_publicId = publicId;
		_systemId = systemId;
		_grammar = new GrammarAccess( systemId );
		System.out.println( "Created Xerces DTDDocumentImpl: " 
		+ getExternalID() );
	}
	
	public GrammarAccess getGrammarAccess()
	{
		return _grammar;
	}
	
	public String getName() 
	{
		// REVISIT
		String tempName = "No_name_yet_" + System.currentTimeMillis();
		return tempName;
	}
	
	public Enumeration getElements() 
	{
		if (!_initialized && _elements == null && _grammar != null) 
		{
			String[] elements = _grammar.getElements();
            if (elements==null)
                return null;

			_initialized = true;
			_elements = new Hashtable();
			for ( int i = 0; i < elements.length; i++ )
			{
				String elementName = elements[i];
				DTDElement el = new DTDElementImpl( this, elementName );
				_elements.put( elementName, el );
			}
		}
		if (_elements != null) {
			return _elements.elements();
		}
		else {
			return null;
		}
	}
	
	public DTDElement fetchElement(String name) 
	{
		if (_elements != null) {
			DTDElement el = (DTDElement)_elements.get(name);
			return el;
		}
		return null;
	}

	/*
	public int getNodeType( String nodeName )
	{
		DTDElement element = fetchElement( nodeName );
		int ret = -2;
		if ( element != null )
			ret = element.getNodeType();
		return ret;
	}
	*/
	
	public Enumeration getInsertableElements(Element el, int index) 
	{
		String elementName = el.getNodeName();
		String[] currentChildElements = getChildNodeNamesWithoutText( el );
		// Recalculate the index as if the #text nodes never existed.
		index = getIndexWithoutTextNodes( el, index );
		//System.out.println( "Getting insertable elements for " + elementName 
		//+ " at position " + index );
		String[] results = _grammar.whatCanGoHere( elementName,
		currentChildElements, index );
		List list = new Vector();
		for ( int i = 0; i < results.length; i++ )
		{
			String result = results[i];
			if ( result.equals( "#text" ) )
				result = DTDConstants.PCDATA_KEY;
			//System.out.println( " Can insert: " + result );
			DTDElement element = (DTDElement)_elements.get( result );
			if ( element == null )
				element = new DTDElementImpl( this, result );
			list.add( element );
		}
		Enumeration ret = Collections.enumeration( list );
		return ret;
	}

	public Enumeration getInsertableElements( Element el ) 
	{
		String elementName = el.getNodeName();
		String[] currentChildElements = getChildNodeNames( el );
		// Use a list temporarily to maintain the sequence
		List list = new Vector();
		for ( int i = 0; i <= currentChildElements.length; i++ )
		{
			Enumeration insertables = getInsertableElements( el, i );
			while ( insertables.hasMoreElements() )
				list.add( insertables.nextElement() );
		}
		// Remove duplicates while maintaining sequence
		Comparator c = new ListComparator( list );
		TreeSet noDuplicates = new TreeSet( c );
		noDuplicates.addAll( list );
		Enumeration ret = Collections.enumeration( noDuplicates );
		return ret;
	}
	
	public int getInsertPosition( Element parent, String childElementName )
	{
		String parentName = parent.getNodeName();
		//System.out.println( "Getting insert position for parent " + parentName
		//+ " and child " + childElementName );
		String[] currentChildElements = getChildNodeNames( parent );
		int insertPosition = -1;
		// All the available insert positions
		outer: for ( int i = 0; i <= currentChildElements.length; i++ )
		{
			if ( i < currentChildElements.length )
			{
				String currentChild = currentChildElements[i];
				//System.out.println( " At current child " + currentChild + " i="
				//+ i );
			}
			Enumeration e = getInsertableElements( parent, i );
			while ( e.hasMoreElements() )
			{
				DTDElement el = (DTDElement)e.nextElement();
				String name = el.getName();
				//System.out.println( "  Checking insertable " + name );
				if ( name.equals( childElementName ) )
				{
					insertPosition = i;
					//System.out.println( "  Returning position " + insertPosition );
				}
			}
		}
		// REVISIT: Throw an exception
		if ( insertPosition == -1 )
			insertPosition = currentChildElements.length;
		//System.out.println( "  Returning position " + insertPosition );
		return insertPosition;
	}
	
	public int getIndexWithoutTextNodes( Element el, int index )
	{
		int countWithoutText = 0;
		if ( index < 0 )
			index = 0;
		NodeList children = el.getChildNodes();
		for ( int i = 0; i <= index; i++ )
		{
			if ( i >= index )
			{
				//System.out.println( "Returning countWithoutText=" 
				//+ countWithoutText );
				return countWithoutText;
			}
			if ( i < children.getLength() )
			{
				Node child = (Node)children.item( i );
				String childName = child.getNodeName();
				//System.out.println( "getIndexWithoutTextNodes: childName: " 
				//+ childName );
                if ( childName != null 
                    && !childName.equals("#text") 
                    && !childName.equals("#comment") 
                    && !(child instanceof ProcessingInstruction))
				{
					countWithoutText++;
				}
			}
		}
		//System.out.println( "Returning countWithoutText=0" );
		return 0;
	}
	
	public String[] getChildNodeNames( Element el )
	{
		Vector v = new Vector();
		NodeList children = el.getChildNodes();
		for ( int i = 0; i < children.getLength(); i++ )
		{
			Node child = (Node)children.item( i );
			String childName = child.getNodeName();
			if ( childName != null )
			{
				v.add( childName );
				//System.out.println( "Adding child node: " + childName );
			}
		}
		String[] childNames = new String[0];
		childNames = (String[])v.toArray( childNames );
		return childNames;
	}
	
	public String[] getChildNodeNamesWithoutText( Element el )
	{
		Vector v = new Vector();
		NodeList children = el.getChildNodes();
		for ( int i = 0; i < children.getLength(); i++ )
		{
			Node child = (Node)children.item( i );
			String childName = child.getNodeName();
			// REVISIT: Not sure if it is right to exclude #text like this, but
			// it seems to work
			if ( childName != null 
                && !childName.equals( "#text" ) 
                && !childName.equals("#comment")
				&& !(child instanceof ProcessingInstruction))
			{
				v.add( childName );
				//System.out.println( "Adding child node: " + childName );
			}
		}
		String[] childNames = new String[0];
		childNames = (String[])v.toArray( childNames );
		return childNames;
	}
	
    public boolean elementIsValid (Element el, boolean checkChildren)
    {
		boolean ret = false;
		String elementName = el.getNodeName();
		String[] childNodeNames = getChildNodeNamesWithoutText( el );
		ret = _grammar.validateContent( elementName, childNodeNames );
		if ( ret == false )
			System.out.println( "Element " + elementName + " is not valid." );
		if ( ret == true && checkChildren )
		{
			NodeList list = el.getChildNodes();
			for ( int i = 0; i < list.getLength(); i++ )
			{
				Node child = list.item( i );
				if ( child instanceof Element )
				{
					Element childElement = (Element)child;
					ret = elementIsValid( childElement, true );
					if ( ret == false )
						break;
				}
			}
		}
		return ret;
	}
	
	/**
	 * Returns the external identifier or null if there is none.
	 * <P>
	 * The string should include PUBLIC and SYSTEM identifiers if they
	 * are available.
	 */
	public String getExternalID() 
	{
		StringBuffer sb = new StringBuffer();
		boolean added_public = false;
		
		if (_publicId != null && !_publicId.equals("")) {
			sb.append("PUBLIC \""+_publicId+"\"");
			added_public = true;
		}
		if (_systemId == null) {
		    _systemId = "";
		}
		
		if (!added_public) {
		    sb.append("SYSTEM");
		}
		sb.append(" \""+_systemId+"\"");
	
		if (sb.length() > 0) {
			return sb.toString();
		}
		
		/*
		ExternalID id = _doc.getExternalID();
		if (id != null) {
			return id.toString();
		}
		*/
		return null;
					
	}
	
	public class ListComparator implements Comparator
	{
		public List list;
		
		public ListComparator( List list )
		{
			this.list = list;
		}
		
		public int compare( Object o1, Object o2 )
		throws ClassCastException
		{
			int rank1 = list.indexOf( o1 );
			int rank2 = list.indexOf( o2 );
			if ( rank1 == -1 || rank2 == -1 )
				return 0;
			int ret = rank1 - rank2;
			return ret;
		}
		
		public boolean equals( Object obj )
		{
			boolean ret = false;
			if ( obj instanceof ListComparator )
			{
				ListComparator lc = (ListComparator)obj;
				if ( lc.list == this.list )
					ret = true;
			}
			return ret;
		}
	}
}


