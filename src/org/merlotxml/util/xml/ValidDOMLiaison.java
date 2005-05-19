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

import java.io.InputStream;
import java.io.Writer;

/**
 * This interface provides means to access the DTD definitions as well as the
 * DOM document
 * <P>
 * 
 * 
 *
 * @author Kelly A. Campbell
 */

public interface ValidDOMLiaison extends DOMLiaison
{
	
	/**
	 * Creates a new document that should maintain validity. NEW... not really
	 * used or implemented yet
	 */
	public ValidDocument createValidDocument();
	
	


	/**
	 * This parses an XML stream using a validating parser 
	 * and maintains references to the
	 * DTDDocuments used in it. It returns a ValidatedDocument which contains
	 * a org.w3c.dom.Document and the DTDDocuments it uses.
	 * <P>
	 * Uses the default EntityResolver for resolving the DTD documents
	 * <P>
	 * @param is InputStream to parse
	 * @param fileLocation optional URL for the file used to find relative DTD's
	 *
	 * @return com.channelpoint.commerce.util.xml.ValidatedDocument
	 * @exception DOMLiaisonImplException wrapper for exceptions thrown
	 * by the validating parser.
	 */

	public ValidDocument parseValidXMLStream(InputStream is, String fileLocation)
		throws DOMLiaisonImplException;
	
	
	
	/**
	 * Print that takes a valid document so it can print out the DTD
	 * specification properly.
	 */
	public void print(ValidDocument doc, Writer output, String resultns, boolean pretty)
		throws DOMLiaisonImplException;
	

	
}
