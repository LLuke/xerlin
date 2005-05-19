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

package org.merlotxml.util.xml;

import java.util.Enumeration;

import org.w3c.dom.Element;

/**
 * This interface provides means to access the DTD definitions in a manner
 * that can be independent of whatever third party underlying structures
 * are used for the implementation.
 * <P>
 * 
 * 
 *
 * @author Kelly A. Campbell
 * @deprecated Use GrammarDocument.
 */

public interface DTDDocument
{

	/**
	 * Returns the name of the DTD 
	 */
	public String getName();
	
	//public int getNodeType( String nodeName );
	
	/**
	 * Returns the list of declared elements from the document.
	 * @return Enumeration consisting of DTDElement objects or null
     * @deprecated Use GrammarDocument.
	 */
	public Enumeration getElements();
	
	/**
     * @deprecated Use GrammarDocument.
	 */
	public DTDElement fetchElement( String name );
	
	/**
	 * Returns a list of the possible elements that can be inserted or appended
	 * on this element.
	 *
	 * @param el A DOM element
	 * @return vector containing DTDElement objects
	 */
    //	public Enumeration    getAppendableElements(Element el);
	
	/**
	 * Returns the list of the possible elements that can be inserted
	 * as a child in an element.
	 * 
	 * @param el a DOM element (future parent element)
	 * @param index index where the returned elements should be insertable
	 * 
	 * @return Enumeration of the insertable DTDElements objects
     * @deprecated Use GrammarComplexType
	 */
	public Enumeration getInsertableElements(Element el, int index);
		
	/**
     * @deprecated Use GrammarComplexType
	 */
	public Enumeration getInsertableElements( Element el );
	
	/**
     * @deprecated Use GrammarComplexType
	 */
	public int getInsertPosition( Element parent, String childElementName );
	
	/**
	 * Returns whether or not an element (with or without its children)
	 * is valid according to its DTD definition.
     * @deprecated Use GrammarComplexType
	 */
	public boolean elementIsValid (Element el, boolean checkChildren);
	
	/**
	 * Returns the SYSTEM identifier for a dtd
	 */
	public String getExternalID();

    /**
     * Allows setting of the GrammarDocument
     */
    public void setGrammarDocument(GrammarDocument gdoc);
}
