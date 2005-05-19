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
http://www.merlotxml.org.
*/


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.util.xml;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * Container for a validated Document and it's DTDDocuments
 * 
 * 
 *
 * @author Kelly A. Campbell
 * @version $Id: ValidDocument.java,v 1.9 2001/08/14 15:47:27 camk Exp $
 */

public class ValidDocument 
{
	private Document  _doc;
	private Hashtable _dtds;
	private DTDDocument _maindtd = null;
    private String _encoding = null;
    

    private DTDCacheEntry _cachedDTD = null;
    
    /** location of the file used to find relative DTD's */
    private String _fileLocation = null;
    
	
	/**
	 * Hashtable of all the defined elements in the different DTD's
	 */
	private Hashtable _elements;
	
	/**
	 * Reverse hashtable for looking up what document type a element 
	 * came from
	 */
	private Hashtable _element2DTD;
	
    /** Stack for keeping track of multiple DTD's used by a document */
    private Stack _dtdStack;
    
	
	public ValidDocument () 
	{
		_doc = null;
		_dtds = new Hashtable();
		_dtdStack = new Stack();
		
	}
	

	public ValidDocument(Document doc) 
	{
		_doc = doc;
		_dtds = new Hashtable();
	}
	
	public Document getDocument()
	{
		return _doc;
	}
	
    public String getEncoding() 
    {
	return _encoding;
    }
    
    public void setEncoding(String enc) 
    {
	_encoding = enc;
	//	System.out.println("Set ValidDocument encoding to "+enc);
	

    }

	public void setDocument(Document doc) 
	{
		_doc = doc;
	}
	
    public void addDTD(DTDCacheEntry cachedDTD, String doctype) 
    {
		if (_maindtd == null) {
			_maindtd = cachedDTD.getParsedDTD();
			_cachedDTD = cachedDTD;
			
		}
	
		_dtds.put(doctype,cachedDTD.getParsedDTD());
		_dtdStack.push(cachedDTD);
		
	
    }
    
    /**
     * @deprecated
     */
    /*
	public void addDTDDocument(DTDDocument dtd, String key) 
	{
	    //	System.out.println("Adding DTD: "+dtd+" name: "+dtd.getName() + " key: "+key);
		// assume that the first dtd we're given is our main dtd
		if (_maindtd == null) {
			_maindtd = dtd;
		}
		
		_dtds.put(key,dtd);
		_dtdStack.push(dtd);
		
	}
    */
    public Stack getDTDStackCopy() 
    {
	return (Stack) _dtdStack.clone();
    }
    
	
	public DTDDocument getDTDDocument(String name) 
	{
		return (DTDDocument)_dtds.get(name);
	}
	
	public DTDDocument getMainDTDDocument() 
	{
		return _maindtd;
	}
    public DTDCacheEntry getDTDCacheEntry() 
    {
	return _cachedDTD;
    }
    
    public void setFileLocation (String fileLocation) 
    {
	_fileLocation = fileLocation;
    }
    public String getFileLocation () 
    {
	return _fileLocation;
    }
    

	public Enumeration getDTDAttributes(String elementName) 
	{
		lazyInitElements();
		//	System.out.println("getDTDAttributes("+elementName+")");
		
		DTDElement el = (DTDElement)_elements.get(elementName);
		if (el != null) {
		    //	System.out.println("el != null");
			
			return el.getAttributes();
		}
		return null;
		
		
	}
	

	public DTDDocument getDTDForElement( String nodeName )
	{
		lazyInitElements();
		Object o = _element2DTD.get( nodeName );
		if (o != null && o instanceof DTDDocument) {
			return (DTDDocument)o;
		}
		return null;
	}
	
	public DTDDocument getDTDForElement(Element el) 
	{
		lazyInitElements();
		Object o = _element2DTD.get(el.getNodeName());
		if (o != null && o instanceof DTDDocument) {
			return (DTDDocument)o;
		}
		return null;
	}
	

	protected void lazyInitElements() 
	{
		if (_elements == null) {
			_elements = new Hashtable();
			_element2DTD = new Hashtable();
			
			Enumeration e = _dtds.elements();
			while (e.hasMoreElements()) {
			    //	System.out.println("e.hasMoreElements()");
				
				DTDDocument doc = (DTDDocument)e.nextElement();
				Enumeration els = doc.getElements();
				if (els != null) {
				//	System.out.println("document has elements: "+els);
					
					while (els.hasMoreElements()) {
						DTDElement el = (DTDElement)els.nextElement();
						_elements.put(el.getName(),el);
						//		System.out.println("element("+el.getName()+")");
						_element2DTD.put(el.getName(),doc);
							
					}
				}
			}
		}
	}
	
}


