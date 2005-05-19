// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
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
http://www.merlotxml.org/
*/



// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.util.xml;

import java.io.*;
import java.util.*;

/**
 *  provides constant values
 * 
 *
 * @author Kelly A. Campbell
 * @version $Id: DTDConstants.java,v 1.6 2001/08/30 04:13:47 justin Exp $
 */

public interface  DTDConstants
{

	// these numbers are all pretty much arbitrary 

	// Attribute list stuff
	public static final int NONE     = -1;
	public static final int IMPLIED  = 0;
	public static final int REQUIRED = 1;
    public static final int FIXED    = 2;
	
	public static final int ANY      = 10;
	public static final int EMPTY    = 11;
	

	public static final int TOKEN_GROUP = 12;
	public static final int NMTOKEN     = 13;
	public static final int NMTOKENS    = 14;
    public static final int ID              = 15;
    public static final int IDREF       = 16;
    

	public static final int CDATA    = 20;
	public static final int PCDATA   = 21;
    public static final int COMMENT  = 22;
    public static final int PROCESSING_INSTRUCTION = 23;
	
	public static final int GROUP    = 30;
	
	// SpecNode stuff

	public static final int CONTENT_LEAF   = '-';
	
	public static final int CONTENT_GROUP  = '(';
	public static final int CONTENT_OR     = '|';
	public static final int CONTENT_CONCAT = ',';
	
	public static final int CONTENT_ONEMAX = '?';
	public static final int CONTENT_SINGLE = '=';
	public static final int CONTENT_STAR   = '*';
	public static final int CONTENT_PLUS   = '+';
	
	
	public static final String PCDATA_KEY = "#text";
    public static final String COMMENT_KEY = "#comment";
    public static final String PROCESSING_INSTRUCTION_KEY = "#processing_instruction";
}
