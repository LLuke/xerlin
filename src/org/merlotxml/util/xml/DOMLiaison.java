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


package org.merlotxml.util.xml;

import java.io.*;
import java.util.Properties;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * DOM Liaison
 * 
 * This interface makes up for deficiencies in the DOM API.
 * It allows you to plug in different XML libraries by creating
 * implementations of this interface.
 * 
 * @author Tim McCune
 * @version $Id: DOMLiaison.java,v 1.5 2000/08/21 21:44:48 timm Exp $
 */
public interface DOMLiaison {

	/**
	 * Create a Document
	 * @return An empty Document
	 */
	public Document createDocument();
	
	/**
	 * Print a Document
	 * 
	 * @param doc The Document to print
	 * @param output Writer to send the output to
	 * @param resultns Result name space for the output.  Used for things
	 *		like HTML hacks.
	 * @param format If true, output will be nicely tab-formatted.
	 *		If false, there shouldn't be any line breaks or tabs between
	 *		elements in the output.  Sometimes setting this to false
	 *		is necessary to get your HTML to work right.
	 * @exception org.merlotxml.util.xml.DOMLiaisonImplException
	 *		Wrapper exception that is thrown if the implementing class
	 *		throws any kind of exception.
	 */
	public void print(Document doc, Writer output, String resultns, boolean format)
	throws DOMLiaisonImplException;
	
	/**
	 * Parse a stream of XML into a Document
	 * 
	 * @param xmlReader XML stream reader
	 * @return The Document that was parsed
	 * @exception org.merlotxml.util.xml.DOMLiaisonImplException
	 *		Wrapper exception that is thrown if the implementing class
	 *		throws any kind of exception.
	 * @deprecated Use parseXMLStream(Reader)
	 */
	public Document parseXMLStream(InputStream is)
	throws DOMLiaisonImplException;
	
	public Document parseXMLStream(Reader in)
	throws DOMLiaisonImplException;
	

    public void setProperties(Properties props);
    
    public void addEntityResolver(EntityResolver er);
    


}
