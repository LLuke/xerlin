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


// Copyright 1999 Channelpoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.util.Properties;

import org.w3c.dom.DOMException;


/**
 * debug stuff
 *
 *<P>
 * @author Kelly A. Campbell
 *
 */

public class MerlotDebug 
{

    protected static boolean DEBUG_ON = true;
    protected static boolean DEBUG_EX = false;
    static 
    {
	//	reloadSettings();
    }

    public static void init(Properties p) 
    {
	String debug = p.getProperty("merlot.debug");
	if (debug != null) {
	    DEBUG_ON = debug.equals("true");
	}
	debug = p.getProperty("merlot.debug.ex");
	if (debug != null) {
	    DEBUG_EX = debug.equals("true");
	}
	
    }
    
    public static void reloadSettings() 
    {
	
	try {
	    DEBUG_ON = XMLEditorSettings.getSharedInstance().getProperty("merlot.debug").equals("true");
	}
	catch (Exception ex) {
	}
	try {
	    DEBUG_EX = XMLEditorSettings.getSharedInstance().getProperty("merlot.debug.ex").equals("true");
	}
	catch (Exception ex) {
	}
    
		
    }
	

    public static void msg (String s) 
    {
	if (DEBUG_ON) {
	    System.out.println(s);
	}
    }
	
    public static void exception (Throwable t) 
    {
	if (DEBUG_ON || DEBUG_EX) {
	    String errmsg = "";
	    if (t instanceof DOMException) {
		int code = ((DOMException)t).code;
		switch (code) {
		case DOMException.HIERARCHY_REQUEST_ERR:
		    errmsg = "Hierarchy request error";
		    break;
		case DOMException.NO_MODIFICATION_ALLOWED_ERR:
		    errmsg = "No modification allowed";
		    break;
		case DOMException.WRONG_DOCUMENT_ERR:
		    errmsg = "Wrong document";
		    break;
		case DOMException.INDEX_SIZE_ERR:
		    errmsg = "Index size error";
		    break;
		case DOMException.DOMSTRING_SIZE_ERR:
		    errmsg = "Wstring size error";
		    break;
		    /*	case DOMException.INVALID_NAME_ERR:
			errmsg = "Invalid name error";
			break;
		    */
		case DOMException.NO_DATA_ALLOWED_ERR:
		    errmsg = "No data allowed error";
		    break;
		case DOMException.NOT_FOUND_ERR:
		    errmsg = "Not found error";
		    break;
		case DOMException.NOT_SUPPORTED_ERR:
		    errmsg = "Not supported error";
		    break;
		case DOMException.INUSE_ATTRIBUTE_ERR:
		    errmsg = "In us attribute";
		    break;
		default:
		    errmsg = "Unknown error type: "+code;
		    break;
				    
		}
				
	    }
			

	    System.err.println("Exception: " +errmsg+": "+ t.getMessage());
	    t.printStackTrace();
			
	}
			
    }
		
		
	
}

